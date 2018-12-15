package com.example.hashimotoakira.togemp

import android.Manifest
import android.content.DialogInterface
import android.media.Image
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hashimotoakira.togemp.logic.ChildLogic
import com.example.hashimotoakira.togemp.logic.ConnectionMessage
import com.example.hashimotoakira.togemp.logic.ConnectionMessage.*
import com.example.hashimotoakira.togemp.logic.ParentLogic
import com.example.hashimotoakira.togemp.util.*
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.android.synthetic.main.activity_main.*
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnShowRationale
import permissions.dispatcher.PermissionRequest
import permissions.dispatcher.RuntimePermissions
import org.greenrobot.eventbus.Subscribe
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import com.example.hashimotoakira.togemp.util.MessageEvent
import org.greenrobot.eventbus.EventBus


@RuntimePermissions
class MainActivity : AppCompatActivity() {

    private var isParent = false

//    private var participantNumber = 0

    private var connectedDeviceCount = 1

    private val parentLogic = ParentLogic()

    private val childLogic = ChildLogic()

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
                childLogic.setParentId(endpointId)
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
                    endpointIds.forEach { toEndpointId ->
                        sendPayload(this@MainActivity,
                                toEndpointId,
                                "start")
                    }
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
                showToast(this@MainActivity, it)
                logD("onPayloadReceived  $it")
                if (it == "start") { // 親がコネクション完了通知で、子が子同士通信するためにrequestする流れ
                    logD("onPayloadReceived $endpointId")
                    // getCard()
                } else {
                    if (isParent && parentLogic.recievePlayer.id != ParentLogic.PARENT_ID) {
                        sendPayload(this@MainActivity, parentLogic.recievePlayer.id, it)
                        parentLogic.changeToNextTurn()
                        return@also
                    }

                    val message = ConnectionMessage.parseStrMsg(it)

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
                    }
                }
            }
        }
    }

    //ここからスタート
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        makeRoom.setOnClickListener {
            isParent = true
            parentLogic.addPlayer(ParentLogic.PARENT_ID)
            parentLogic.setPlayerPositionById(ParentLogic.PARENT_ID)
//            AlertDialog.Builder(this)
//                    .setTitle("何人参加しますか")
//                    .setItems(arrayOf("2", "3", "4", "5")) { _: DialogInterface, i: Int ->
//                        participantNumber = i + 2
//                        startDiscoveryWithPermissionCheck(endpointDiscoveryCallback)
//                    }
//                    .create().show()
            startDiscoveryWithPermissionCheck(endpointDiscoveryCallback)
            goConnectingView()
        }
        enterRoom.setOnClickListener {
            startAdvertisingWithPermissionCheck(connectionLifecycleCallback)
            goConnectingView()
        }
        nextButton.setOnClickListener {
            goShufflingView()
            val target = GlideDrawableImageViewTarget(shuffleButton)
            Glide.with(this).load(R.raw.anim01_prompt_shuffle).into(target)
        }
        shuffleButton.setOnClickListener {
            parentLogic.createHands()

            val shuffleButton = findViewById<View>(R.id.shuffleButton) as ImageView
            val shuffilingText = findViewById<View>(R.id.shuffilingText) as TextView
            val shufflingView = findViewById<View>(R.id.shufflingView) as ImageView
            shuffleButton.visibility = View.GONE
            shuffilingText.visibility = View.GONE
            shufflingView.visibility = View.VISIBLE

            val target = GlideDrawableImageViewTarget(shufflingView)
            Glide.with(this).load(R.raw.anim02_shuffle).into(target)
            // 4.333秒たったら元のViewに戻す
            val runnable = Runnable {
                goDealView()
            }
            Handler().postDelayed(runnable, 4333)
        }
        dealButton.setOnClickListener {
            var playerCount = 1
            logD("dealButton  start")
            while (playerCount < parentLogic.playerInfoCount + 1) {
                parentLogic.getPlayerInitialHands(playerCount).let {pair ->
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
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    // This method will be called when a MessageEvent is posted
    @Subscribe
    fun onMessageEvent(event: MessageEvent) {
        logD("onMessageEvent  ${event.position}")
        val card = childLogic.sendCard(event.position + 1)
        setCardsList()
        val msg = ConnectionMessage.createStrMsg(ReceiverAction.GetCard, card)
        if (isParent) {
            sendPayload(this, parentLogic.recievePlayer.id, msg)
            parentLogic.changeToNextTurn()
        } else {
            sendPayload(this, childLogic.getParentId(), msg)
        }
    }

    private fun setCardsList() {
        val cardAdapter = CardAdapter(childLogic.sortCardList, this@MainActivity)
        cardRecyclerView.adapter = cardAdapter
    }

}
