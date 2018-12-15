package com.example.hashimotoakira.togemp

import android.Manifest
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
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

@RuntimePermissions
class MainActivity : AppCompatActivity() {

    private var isParent = false

    private var participantNumber = 0

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
                if (connectedDeviceCount == participantNumber) {
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
                if (it == "start") { // 親がコネクション完了通知で、子が子同士通信するためにrequestする流れ
                    logD("onPayloadReceived $endpointId")
                    // getCard()
                } else {
                    showToast(this@MainActivity, it)
                    val message = ConnectionMessage.parseStrMsg(it)

                    childLogic.createHands(message.cardList)

                    cardRecyclerView.let {
                        it.setHasFixedSize(true)
                        it.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
                        val cardAdapter = CardAdapter(message.cardList, this@MainActivity)
                        cardAdapter.setClickListener(object : CardViewHolder.ItemClickListener {
                            override fun onItemClick(view: View, position: Int) {

                            }
                        })
                        it.adapter = cardAdapter
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
            AlertDialog.Builder(this)
                    .setTitle("何人参加しますか")
                    .setItems(arrayOf("2", "3", "4", "5")) { _: DialogInterface, i: Int ->
                        participantNumber = i + 2
                        startDiscoveryWithPermissionCheck(endpointDiscoveryCallback)
                    }
                    .create().show()
            goConnectingView()
        }
        enterRoom.setOnClickListener {
            startAdvertisingWithPermissionCheck(connectionLifecycleCallback)
            goConnectingView()
        }
        nextButton.setOnClickListener {
            goShufflingView()
        }
        shuffleButton.setOnClickListener {
            parentLogic.createHands()
            goDealView()
        }
        dealButton.setOnClickListener {
            var playerCount = 1
            logD("dealButton  start")
            while (playerCount < parentLogic.playerInfoCount) {
                parentLogic.getPlayerInitialHands(playerCount).let {pair ->
                    logD("dealButton  ${pair.second[0].suit}    ${pair.second[0].number}")
                    if (pair.first == ParentLogic.PARENT_ID) {
                        cardRecyclerView.let {
                            it.setHasFixedSize(true)
                            it.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
                            val cardAdapter = CardAdapter(pair.second, this@MainActivity)
                            cardAdapter.setClickListener(object : CardViewHolder.ItemClickListener {
                                override fun onItemClick(view: View, position: Int) {

                                }
                            })
                            it.adapter = cardAdapter
                            goHandView()
                        }
                    } else {
//                        sendPayload(this@MainActivity,
//                                pair.first,
//                                createStrMsg(ReceiverAction.GetCard, pair.second))
                    }
                }
                playerCount++
            }
        }
//        cardRecyclerView.let {
//            it.setHasFixedSize(true)
//            it.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
//            val cardAdapter = CardAdapter(cards, this@MainActivity)
//            cardAdapter.setClickListener(object : CardViewHolder.ItemClickListener {
//                override fun onItemClick(view: View, position: Int) {
//
//                }
//            })
//            it.adapter = cardAdapter
//        }
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

}
