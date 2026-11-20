package com.textworld.app.data.network

import com.textworld.app.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.http.HttpHeaders
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import java.util.concurrent.TimeUnit

/** Single HTTP/WebSocket client factory. Never logs tokens or world data. */
object ApiClient {
    @Volatile
    var baseUrl: String = BuildConfig.DEFAULT_SERVER_URL

    fun updateBaseUrl(url: String) {
        baseUrl = url.trim().trimEnd('/')
    }

    fun url(path: String): String = "$baseUrl$path"

    fun create(token: String? = null): HttpClient = HttpClient(OkHttp) {
        if (!token.isNullOrBlank()) {
            defaultRequest { header(HttpHeaders.Authorization, "Bearer $token") }
        }
        engine {
            config {
                connectTimeout(15, TimeUnit.SECONDS)
                readTimeout(30, TimeUnit.SECONDS)
                writeTimeout(30, TimeUnit.SECONDS)
                retryOnConnectionFailure(true)
            }
        }
        install(WebSockets) {
            pingIntervalMillis = 15_000
        }
        install(Logging) {
            level = LogLevel.NONE
        }
        // Token is attached per request by the repository, never logged.
        expectSuccess = false
    }
}
