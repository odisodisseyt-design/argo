package com.textworld.app.data.network

import com.textworld.app.core.Constants
import com.textworld.app.data.model.TwJson
import com.textworld.app.data.model.WireEvent
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.min

sealed interface RealtimeEvent {
    data class Server(val name: String, val payload: String?) : RealtimeEvent
    data object Disconnected : RealtimeEvent
    data object Fallback : RealtimeEvent
}

/** WebSocket channel with exponential backoff and a long-poll fallback. */
class RealtimeClient(private val tokenProvider: () -> String?) {

    private fun wsUrl(campaignId: String): String {
        val base = ApiClient.baseUrl.replace("https://", "wss://").replace("http://", "wss://")
        return "$base/ws/campaign/$campaignId?token=${tokenProvider().orEmpty()}"
    }

    fun events(campaignId: String, afterStep: () -> Int): Flow<RealtimeEvent> = flow {
        val client = ApiClient.create(tokenProvider())
        var delayMs = Constants.WS_RECONNECT_BASE_MS
        var failures = 0
        try {
            while (true) {
                try {
                    client.webSocket(urlString = wsUrl(campaignId)) {
                        failures = 0
                        delayMs = Constants.WS_RECONNECT_BASE_MS
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val raw = frame.readText()
                                val wire = runCatching { TwJson.decodeFromString<WireEvent>(raw) }.getOrNull()
                                if (wire != null) {
                                    emit(RealtimeEvent.Server(wire.event, wire.payload?.toString()))
                                }
                            }
                        }
                    }
                } catch (t: Throwable) {
                    failures++
                    emit(RealtimeEvent.Disconnected)
                    if (failures >= 3) emit(RealtimeEvent.Fallback)
                    delay(delayMs)
                    delayMs = min(delayMs * 2, Constants.WS_RECONNECT_MAX_MS)
                }
            }
        } finally {
            client.close()
        }
    }
}
