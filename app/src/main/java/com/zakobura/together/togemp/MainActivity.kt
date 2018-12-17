package com.zakobura.together.togemp

import android.Manifest
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.zakobura.together.togemp.logic.Card
import com.zakobura.together.togemp.logic.ChildLogic
import com.zakobura.together.togemp.logic.ConnectionMessage
import com.zakobura.together.togemp.logic.ConnectionMessage.ReceiverAction
import com.zakobura.together.togemp.logic.ConnectionMessage.createStrMsg
import com.zakobura.together.togemp.logic.ParentLogic
import com.zakobura.together.togemp.util.*
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnShowRationale
import permissions.dispatcher.PermissionRequest
import permissions.dispatcher.RuntimePermissions


@RuntimePermissions
class MainActivity : AppCompatActivity(), SensorEventListener {

    lateinit var sensorManager: SensorManager

    var isHide: Boolean = false

    private fun setCardsList() {
        if (childLogic.sortCardList.size != 0) {
            val cardAdapter = if (!isHide) {
                CardAdapter(childLogic.sortCardList, this@MainActivity)
            } else {
                val backCards = mutableListOf<Card>()
                var count = 0
                while (count < childLogic.sortCardList.size) {
                    backCards.add(Card("back", 0))
                    count++
                }
                CardAdapter(backCards, this)
            }
            cardRecyclerView.adapter = cardAdapter
        } else {
            if (isParent) {
                parentLogic.changeToNextTurn()
                sendPayload(this@MainActivity, parentLogic.recievePlayer.id, ConnectionMessage.createStrYourTurnMsg())
            } else {
                sendPayload(this, childLogic.parentId, ConnectionMessage.createStrDiscardFinishMsg())
            }
            goFinishView()
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ORIENTATION) {
            if (cardsView.visibility == VISIBLE) {
                val shouldBeHide = event.values[SensorManager.DATA_Z] < 45
                if (isHide != shouldBeHide) {
                    isHide = shouldBeHide
                    setCardsList()
                }
            }
        }
    }

    private var isParent = false

    private var connectedDeviceCount = 1

    private var isGameStarted = false

    private val parentLogic = ParentLogic()

    private val childLogic = ChildLogic()

    private var isSender = false

    private val cardMaxNumber = 4

    /**
     * String: endpointId
     * Boolean: endpointIdの端末と通信の許可が取れているかどうか
     */
    val endpointIds = mutableListOf<String>()

    // 検出の応答が返って来た際に呼ばれるコールバック
    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String,
                                     discoveredEndpointInfo: DiscoveredEndpointInfo) {
            if (!isGameStarted) {// 端末を検出した
                endpointIds.add(endpointId)
                if (isParent) {
                    parentLogic.addPlayer(endpointId)
                    parentLogic.setPlayerPositionById(endpointId)
                }
                logD("onEndpointFound  endpointID = $endpointId")
                requestConnection(
                        this@MainActivity,
                        packageName,
                        endpointId,
                        connectionLifecycleCallback)
            }
        }

        override fun onEndpointLost(endpointId: String) {
            // 検出済みの端末を見失った
            // TODO endpointIdsで対応したものを消去する?
            logD("onEndpointLost  endpointID = $endpointId")
        }
    }

    // 接続準備が出来た際に呼ばれるコールバック
    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        // requestConnectionの後に呼ばれる
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            logD("connectionLifecycleCallback onConnectionInitiated endpointId = $endpointId ")
            if (!isParent) {
                childLogic.parentId = endpointId
            }
            acceptConnections(this@MainActivity, endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            logD("connectionLifecycleCallback onConnectionResult: $connectedDeviceCount ${result.status.statusCode}")
            if (isParent) {
                connectedDeviceCount++
                playerCount.text = connectedDeviceCount.toString()
                if (connectedDeviceCount > 1) {
                    logD("connectionLifecycleCallback onConnectionResult sendPayload")
                    nextButton.run {
                        isEnabled = true
                        alpha = 1F
                    }
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            logD("connectionLifecycleCallback onDisconnected endpointId = $endpointId")
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadTransferUpdate(endpointId: String, payload: PayloadTransferUpdate) {
            logD(endpointId)
        }

        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            String(payload.asBytes()!!).also {
                //                showToast(this@MainActivity, it)
                logD("onPayloadReceived  $it")
                if (it == "start") { // 親がコネクション完了通知で、子が子同士通信するためにrequestする流れ
                    logD("onPayloadReceived $endpointId")

                } else {

                    val message = ConnectionMessage.parseStrMsg(it)

                    if (isParent && parentLogic.recievePlayer.id != ParentLogic.PARENT_ID && message.receiverAction == ReceiverAction.GetCard) {
                        logD("getCurrentReceivePlayer = ${parentLogic.recievePlayer.id}")
                        sendPayload(this@MainActivity, parentLogic.recievePlayer.id, it)
                        parentLogic.changeToNextTurn()
                        logD("getCurrentReceivePlayer = ${parentLogic.recievePlayer.id} after change")
                        return@also
                    }

                    if (message.receiverAction == ReceiverAction.DealCard) {
                        childLogic.createHands(message.cardList)
                        goHandViewForChild()
                        cardRecyclerView.let {
                            it.setHasFixedSize(true)
                            it.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
                            setCardsList()
                        }
                    } else if (message.receiverAction == ReceiverAction.GetCard) {
                        childLogic.receiveCard(message.cardList)
                        setCardsList()
                        if (isParent) {
                            parentLogic.changeToNextTurn()
                        }
                        enableEndButton()
                    } else if (message.receiverAction == ReceiverAction.YourTurn) {
                        enableEndButton()
                    } else if (message.receiverAction == ReceiverAction.DiscardFinish) {
                        parentLogic.changeToNextTurn()
                        if (parentLogic.recievePlayer.id != ParentLogic.PARENT_ID) {
                            sendPayload(this@MainActivity, parentLogic.recievePlayer.id, ConnectionMessage.createStrYourTurnMsg())
                        } else {
                            enableEndButton()
                        }
                        sendPayload(this@MainActivity, endpointId, ConnectionMessage.createStrRankMsg(parentLogic.finishPlaying(endpointId)))
                    } else if (message.receiverAction == ReceiverAction.DrawFinish) {
                        sendPayload(this@MainActivity, endpointId, ConnectionMessage.createStrRankMsg(parentLogic.finishPlaying(endpointId)))
                    }
                }
            }
        }
    }

    //ここからスタート
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        selectBabaButton.setOnClickListener {
            selectBabaButton.visibility = GONE
            makeRoom.visibility = VISIBLE
            enterRoom.visibility = VISIBLE
        }
        makeRoom.setOnClickListener {
            isParent = true
            enableEndButton()
            parentLogic.addPlayer(ParentLogic.PARENT_ID)
            parentLogic.setPlayerPositionById(ParentLogic.PARENT_ID)
            startDiscoveryWithPermissionCheck(endpointDiscoveryCallback)
            goConnectingView()
        }
        enterRoom.setOnClickListener {
            startAdvertisingWithPermissionCheck(connectionLifecycleCallback)
            connectingTextView.text = "待機中"
            goConnectingView()
        }
        nextButton.setOnClickListener {
            goShufflingView()
            val target = GlideDrawableImageViewTarget(shuffleButton)
            Glide.with(this).load(R.raw.anim01_prompt_shuffle).into(target)
        }
        shuffleButton.setOnClickListener {
            parentLogic.createHands(cardMaxNumber)

            shuffleButton.visibility = GONE
            shufflingText.visibility = GONE
            shufflingView.visibility = VISIBLE

            val target = GlideDrawableImageViewTarget(shufflingView)
            Glide.with(this).load(R.raw.anim02_shuffle).into(target)

            // 4.333秒たったら次のViewへ
            val runnable = Runnable {
                goDealView()
                val target = GlideDrawableImageViewTarget(dealButton)
                Glide.with(this).load(R.raw.anim03_prompt_deal).into(target)
            }
            Handler().postDelayed(runnable, 4333)
        }
        dealButton.setOnClickListener {

            dealButton.visibility = GONE
            dealingText.visibility = GONE
            dealingView.visibility = VISIBLE

            val target = GlideDrawableImageViewTarget(dealingView)
            Glide.with(this).load(R.raw.anim04_deal).into(target)

            // 2.2秒たったら次のViewへ
            val runnable = Runnable {
                var playerCount = 1
                logD("dealButton  start")
                while (playerCount < parentLogic.playerInfoCount + 1) {
                    parentLogic.getPlayerInitialHands(playerCount).let { pair ->
                        logD("dealButton  ${pair.second[0].suit}    ${pair.second[0].number}")
                        if (pair.first == ParentLogic.PARENT_ID) {
                            childLogic.createHands(pair.second)
                            cardRecyclerView.let {
                                it.setHasFixedSize(true)
                                it.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
                                setCardsList()
                                goHandView()
                            }
                        } else {
                            sendPayload(this@MainActivity,
                                    pair.first,
                                    createStrMsg(ReceiverAction.DealCard, pair.second))
                        }
                    }
                    playerCount++
                }
            }
            Handler().postDelayed(runnable, 2200)
        }
        turnEndButton.setOnClickListener {
            isSender = true
            disableEndButton()
            val backCards = mutableListOf<Card>()
            var count = 0
            while (count < childLogic.sortCardList.size) {
                backCards.add(Card("back", 0))
                count++
            }
            val cardAdapter = CardAdapter(backCards, this@MainActivity)
            cardRecyclerView.adapter = cardAdapter
        }
    }

    private fun goConnectingView() {
        settingView.visibility = GONE
        connectingView.visibility = VISIBLE
        if (isParent) {
            nextButton.visibility = VISIBLE
            playerCount.visibility = VISIBLE
        }
    }

    private fun goShufflingView() {
        connectingView.visibility = GONE
        shuffleView.visibility = VISIBLE
    }

    private fun goDealView() {
        shuffleView.visibility = GONE
        dealView.visibility = VISIBLE
    }

    private fun goHandView() {
        dealView.visibility = GONE
        cardsView.visibility = VISIBLE
        showToast(this, "あなたが最初に引かれる番です。手札を捨て終えたらターン終了を押してください。")
    }

    private fun goHandViewForChild() {
        connectingView.visibility = GONE
        cardsView.visibility = VISIBLE
        showToast(this, "手札を捨てて下さい。また、プレイ中、カードを引いた後、引かれる準備ができたらターンエンドを押して下さい。")
    }

    private fun goFinishView() {
        cardsView.visibility = GONE
        finishedView.visibility = VISIBLE
//        if (rank == 1) {
//            rankText.text = rank.toString()
//        } else if (rank == parentLogic.playerInfoList.size) {
//            rankText.text = "ビリ"
//        } else {
//            rankText.text = rank.toString()
//        }
    }

    private fun enableEndButton() {
        turnEndButton.run {
            isEnabled = true
            alpha = 1.0F
        }
    }

    private fun disableEndButton() {
        turnEndButton.run {
            isEnabled = false
            alpha = 0.3F
        }
    }

    // RuntimePermission用
    // PermissionDispatcherの関係でActivityに入れてある

    @OnShowRationale(Manifest.permission.BLUETOOTH)
    fun showRationaleForBluetooth(request: PermissionRequest) {
    }

    @OnShowRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
    fun showRationaleForAccess(request: PermissionRequest) {
    }

    // ここもNearbyConnectクラスに入れておきたい

    /**
     * 検出可能な状態にする
     *
     * ①　子が呼ぶ
     *
     * @param connectionLifecycleCallback 接続準備の通知が来た際の処理
     */

    @NeedsPermission(Manifest.permission.BLUETOOTH)
    fun startAdvertising(connectionLifecycleCallback: ConnectionLifecycleCallback) {
        Nearby.getConnectionsClient(this).startAdvertising(
                "Device A",
                packageName,
                connectionLifecycleCallback,
                advertisingOptions)
                .addOnSuccessListener {
                    logD("startAdvertising Succeeded")
                }
                .addOnFailureListener {
                    logD("startAdvertising Failed $it")
                }
    }


    /**
     * 検出可能な端末を検出する
     *
     * ②　親が呼ぶ
     *
     * @param endpointDiscoveryCallback 検出結果の通知が来た際の処理
     */
    @NeedsPermission(Manifest.permission.ACCESS_COARSE_LOCATION) // TODO 初回のハンドリングしないと。許可した後何もできなくなる
    fun startDiscovery(endpointDiscoveryCallback: EndpointDiscoveryCallback) {
        Nearby.getConnectionsClient(this).startDiscovery(
                packageName,
                endpointDiscoveryCallback,
                discoveryOptions
        )
                .addOnSuccessListener {
                    logD("startDiscovery Succeeded")
                }
                .addOnFailureListener {
                    Handler().postDelayed({
                        startDiscovery(endpointDiscoveryCallback)
                    }, 2000)
                }
    }


    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        val sensors = sensorManager.getSensorList(Sensor.TYPE_ORIENTATION)
        if (sensors.size > 0) {
            val sensor = sensors[0]
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        sensorManager.unregisterListener(this)
        super.onStop()
    }

    var firstPosition: Int? = null

    // This method will be called when a MessageEvent is posted
    @Subscribe
    fun onMessageEvent(event: MessageEvent) {
        logD("whydontmove onMessageEvent")
        logD("onMessageEvent  ${event.position}")
        if (isSender) {
            isSender = false
            val card = childLogic.sendCard(event.position + 1)
            val msg = ConnectionMessage.createStrMsg(ReceiverAction.GetCard, card)
            if (isParent) {
                sendPayload(this, parentLogic.recievePlayer.id, msg)
                parentLogic.changeToNextTurn()
            } else {
                sendPayload(this, childLogic.parentId, msg)
            }
            setCardsList()
            firstPosition = null
        } else {
            if (firstPosition == null) {
                firstPosition = event.position
            } else {
                if (childLogic.discard(firstPosition!! + 1, event.position + 1)) {
                    setCardsList()
                    showToast(this, "すてました")
                } else {
                    showToast(this, "そのカードはそろってないですねー")
                }
                firstPosition = null
            }
        }
    }

    // This method will be called when a MessageEvent is posted
//    @Subscribe
//    fun onRankEvent(event: RankEvent) {
////        goFinishView(event.rank)
//    }

}
