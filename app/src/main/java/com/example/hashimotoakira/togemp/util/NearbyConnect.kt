package com.example.hashimotoakira.togemp.util

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*


/**
 * Nearby Connectionで必要なメソッドをまとめたファイル
 */

private val currentStrategy: Strategy = Strategy.P2P_CLUSTER
val advertisingOptions: AdvertisingOptions = AdvertisingOptions.Builder().setStrategy(currentStrategy).build()
val discoveryOptions: DiscoveryOptions = DiscoveryOptions.Builder().setStrategy(currentStrategy).build()

/**
 * 検出した端末に接続要求をだす
 * ③　親が使用する
 *
 * @param endpointId
 * @param connectionLifecycleCallback 接続準備の通知が来た際の処理
 */
fun requestConnection(activityContext: Context,
                      endpointName: String,
                      endpointId: String,
                      connectionLifecycleCallback: ConnectionLifecycleCallback) {
    Nearby.getConnectionsClient(activityContext).requestConnection(
            endpointName,
            endpointId,
            connectionLifecycleCallback
    )
            .addOnSuccessListener {
                logD("requestConnection endPointId = $endpointId Succeeded")
            }
            .addOnFailureListener {
                logD("requestConnection endPointId = $endpointId Failed $it ")
            }
}

/**
 * ④　子が呼んだ後に親が呼ぶ
 *
 * @param endpointId        接続を許可する接続先の端末 ID
 * @param payloadCallback   接続後に送信されたテータを受け取る
 */
fun acceptConnections(activityContext: Context,
                      endpointId: String,
                      payloadCallback: PayloadCallback) {
    Nearby.getConnectionsClient(activityContext).acceptConnection(
            endpointId,
            payloadCallback)
            .addOnSuccessListener {
                logD("acceptConnections endPointId = $endpointId Succeeded")
            }
            .addOnFailureListener {
                logD("acceptConnections endPointId = $endpointId Failed $it")
            }
}

fun rejectConnections(activityContext: Context,
                      endpointId: String) {
    Nearby.getConnectionsClient(activityContext).rejectConnection(endpointId)
            .addOnSuccessListener {
                logD("rejectConnections endPointId = $endpointId Succeeded")
            }
            .addOnFailureListener {
                logD("rejectConnections endPointId = $endpointId Failed $it")
            }
}

fun sendPayload(activityContext: Context,
                toEndpointId: String,
                message: String) {
    logD("rejectConnections endPointId = $toEndpointId Succeeded")

    Nearby.getConnectionsClient(activityContext).sendPayload(
            toEndpointId,
            Payload.fromBytes(message.toByteArray())
    )
}