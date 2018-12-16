package com.example.hashimotoakira.togemp

import android.Manifest
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import com.example.hashimotoakira.togemp.logic.Card
import com.example.hashimotoakira.togemp.logic.ChildLogic
import com.example.hashimotoakira.togemp.logic.ConnectionMessage
import com.example.hashimotoakira.togemp.logic.ConnectionMessage.ReceiverAction
import com.example.hashimotoakira.togemp.logic.ConnectionMessage.createStrMsg
import com.example.hashimotoakira.togemp.logic.ParentLogic
import com.example.hashimotoakira.togemp.util.*
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
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

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ORIENTATION) {
            if (cardsView.visibility == View.VISIBLE && isSender) {
                if (event.values[SensorManager.DATA_Z] > 75) {
                    if (isHide) {
                        val cardAdapter = CardAdapter(childLogic.sortCardList, this@MainActivity)
                        cardRecyclerView.adapter = cardAdapter
                        isHide = false
                    }
                } else {
                    if (!isHide) {
                        val backCards = mutableListOf<Card>()
                        var count = 0
                        while (count < childLogic.sortCardList.size) {
                            backCards.add(Card("back", 0))
                            count++
                        }
                        val cardAdapter = CardAdapter(backCards, this@MainActivity)
                        cardRecyclerView.adapter = cardAdapter
                        isHide = true
                    }
                }
            }
        }
    }

    private var isParent = false

//    private var participantNumber = 0

    private var connectedDeviceCount = 1

    private val parentLogic = ParentLogic()

    private val childLogic = ChildLogic()

    private var isSender = false

    /**
     * String: endpointId
     * Boolean: endpointIdの端末と通信の許可が取れているかどうか
     */
    val endpointIds = mutableListOf<String>()

    // 検出の応答が返って来た際に呼ばれるコールバック
    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String,
                                     discoveredEndpointInfo: DiscoveredEndpointInfo) {
            // 端末を検出した
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
                    nextButton.isEnabled = true
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
                        goHandViewByChild()
                        cardRecyclerView.let {
                            it.setHasFixedSize(true)
                            it.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
                            val cardAdapter = CardAdapter(childLogic.sortCardList, this@MainActivity)
                            it.adapter = cardAdapter
                        }
                    } else if (message.receiverAction == ReceiverAction.GetCard) {
                        childLogic.receiveCard(message.cardList)
                        setCardsList()
                        if (isParent) {
                            parentLogic.changeToNextTurn()
                        }
                        turnEndButton.isEnabled = true
                    } else if (message.receiverAction == ReceiverAction.YourTurn) {
                        turnEndButton.isEnabled = true
                    } else if (message.receiverAction == ReceiverAction.DiscardFinish) {
                        parentLogic.changeToNextTurn()
                        if (parentLogic.recievePlayer.id != ParentLogic.PARENT_ID) {
                            sendPayload(this@MainActivity, parentLogic.recievePlayer.id, ConnectionMessage.createStrYourTurnMsg())
                        } else {
                            turnEndButton.isEnabled = true
                        }
                        sendPayload(this@MainActivity,endpointId, ConnectionMessage.createStrRankMsg(parentLogic.finishPlaying(endpointId)))
//                        if (parentLogic.notFinishPlayerIdList.size == 1) {
//                            if (parentLogic.notFinishPlayerIdList[0] != ParentLogic.PARENT_ID) {
//                                sendPayload(this@MainActivity, parentLogic.notFinishPlayerIdList[0], ConnectionMessage.createStrYourLastMsg(parentLogic.playerInfoList.size))
//                            } else {
//                                EventBus.getDefault().post(MessageEvent(message.rank))
//                            }
//                        }
                    } else if (message.receiverAction == ReceiverAction.DrawFinish) {
                        sendPayload(this@MainActivity,endpointId, ConnectionMessage.createStrRankMsg(parentLogic.finishPlaying(endpointId)))
//                        if (parentLogic.notFinishPlayerIdList.size == 1) {
//                            if (parentLogic.notFinishPlayerIdList[0] != ParentLogic.PARENT_ID) {
//                                sendPayload(this@MainActivity, parentLogic.notFinishPlayerIdList[0], ConnectionMessage.createStrYourLastMsg(parentLogic.playerInfoList.size))
//                            } else {
//                                EventBus.getDefault().post(MessageEvent(message.rank))
//                            }
//                        }
                    }
//                    else if (message.receiverAction == ReceiverAction.Rank) {
//                        EventBus.getDefault().post(MessageEvent(message.rank))
//                    }
                }
            }
        }
    }

    //ここからスタート
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        makeRoom.setOnClickListener {
            isParent = true
            turnEndButton.isEnabled = true
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
            parentLogic.createHands(1)

            shuffleButton.visibility = View.GONE
            shufflingText.visibility = View.GONE
            shufflingView.visibility = View.VISIBLE

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

            dealButton.visibility = View.GONE
            dealingText.visibility = View.GONE
            dealingView.visibility = View.VISIBLE

            val target = GlideDrawableImageViewTarget(dealingView)
            Glide.with(this).load(R.raw.anim04_deal).into(target)

            // 2秒たったら次のViewへ
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
                                val cardAdapter = CardAdapter(pair.second, this@MainActivity)
                                it.adapter = cardAdapter
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
            Handler().postDelayed(runnable, 2000)
        }
        turnEndButton.setOnClickListener {
            isSender = true
            turnEndButton.isEnabled = false
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
        settingView.visibility = View.GONE
        connectingView.visibility = View.VISIBLE
        if (isParent) {
            nextButton.visibility = View.VISIBLE
            playerCount.visibility = View.VISIBLE
        }
    }

    private fun goShufflingView() {
        connectingView.visibility = View.GONE
        shuffleView.visibility = View.VISIBLE
    }

    private fun goDealView() {
        shuffleView.visibility = View.GONE
        dealView.visibility = View.VISIBLE
    }

    private fun goHandView() {
        dealView.visibility = View.GONE
        cardsView.visibility = View.VISIBLE
    }

    private fun goHandViewByChild() {
        connectingView.visibility = View.GONE
        cardsView.visibility = View.VISIBLE
    }

    private fun goFinishView() {
        cardsView.visibility = View.GONE
        finishedView.visibility = View.VISIBLE
//        if (rank == 1) {
//            rankText.text = rank.toString()
//        } else if (rank == parentLogic.playerInfoList.size) {
//            rankText.text = "ビリ"
//        } else {
//            rankText.text = rank.toString()
//        }
    }


    // ここから下は基本触らない

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
                    logD("startDiscovery Failed")
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
            if (childLogic.sortCardList.size != 0) {
                val cardAdapter = CardAdapter(childLogic.sortCardList, this@MainActivity)
                cardRecyclerView.adapter = cardAdapter
            } else {
                if (isParent) {
                    parentLogic.finishPlaying(ParentLogic.PARENT_ID)
                } else {
                    sendPayload(this, childLogic.parentId, ConnectionMessage.createStrDrawFinishMsg())
                }
                goFinishView()
            }
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

    private fun setCardsList() {
        if (childLogic.sortCardList.size != 0) {
            val cardAdapter = CardAdapter(childLogic.sortCardList, this@MainActivity)
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

}
