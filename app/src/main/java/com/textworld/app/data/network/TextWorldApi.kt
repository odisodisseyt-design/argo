package com.textworld.app.data.network

import com.textworld.app.core.Outcome
import com.textworld.app.data.model.AllocateRequest
import com.textworld.app.data.model.Allocation
import com.textworld.app.data.model.CampaignState
import com.textworld.app.data.model.Friend
import com.textworld.app.data.model.HealthResponse
import com.textworld.app.data.model.Invite
import com.textworld.app.data.model.PublicUser
import com.textworld.app.data.model.TurnResponse
import com.textworld.app.data.model.TwJson
import com.textworld.app.data.model.parsePayload
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/** Every call returns an Outcome; transport errors never escape as crashes. */
class TextWorldApi(private val tokenProvider: () -> String?) {

    private suspend fun <T> get(path: String, decode: (String) -> T): Outcome<T> =
        withContext(Dispatchers.IO) {
            val client = ApiClient.create(tokenProvider())
            try {
                val response = client.get(ApiClient.url(path))
                val text = response.bodyAsText()
                if (response.status.value !in 200..299) {
                    Outcome.Failure(IllegalStateException("HTTP ${response.status.value}"), serverMessage(text))
                } else {
                    Outcome.Success(decode(text))
                }
            } catch (t: Throwable) {
                Outcome.Failure(t, "Мир не ответил. Попробуй ещё раз.")
            } finally {
                client.close()
            }
        }

    private suspend fun <T> post(path: String, bodyJson: String, decode: (String) -> T): Outcome<T> =
        withContext(Dispatchers.IO) {
            val client = ApiClient.create(tokenProvider())
            try {
                val response = client.post(ApiClient.url(path)) {
                    tokenProvider()?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                    contentType(ContentType.Application.Json)
                    setBody(bodyJson)
                }
                val text = response.bodyAsText()
                if (response.status.value !in 200..299) {
                    Outcome.Failure(IllegalStateException("HTTP ${response.status.value}"), serverMessage(text))
                } else {
                    Outcome.Success(decode(text))
                }
            } catch (t: Throwable) {
                Outcome.Failure(t, "Мир не ответил. Попробуй ещё раз.")
            } finally {
                client.close()
            }
        }

    private fun serverMessage(text: String): String = runCatching {
        parsePayload<ApiError>(text).message
    }.getOrNull()?.takeIf { it.isNotBlank() } ?: "Мир не ответил. Попробуй ещё раз."

    suspend fun health(): Outcome<HealthResponse> = get("/api/health") { parsePayload(it) }

    suspend fun login(username: String, password: String): Outcome<AuthResponse> =
        post("/api/auth/login", jsonBody("username" to username, "password" to password)) { parsePayload(it) }

    suspend fun register(username: String, password: String): Outcome<AuthResponse> =
        post("/api/auth/register", jsonBody("username" to username, "password" to password)) { parsePayload(it) }

    suspend fun me(): Outcome<PublicUser> = get("/api/me") { parsePayload(it) }

    suspend fun campaign(campaignId: String): Outcome<CampaignState> =
        get("/api/campaign/$campaignId") { parsePayload(it) }

    suspend fun newCampaign(packId: String, name: String, invitees: List<Int>): Outcome<CampaignIdResponse> =
        post(
            "/api/campaign/new",
            jsonBody("pack_id" to packId, "name" to name, "invitees" to invitees),
        ) { parsePayload(it) }

    suspend fun allocate(campaignId: String, allocations: List<Pair<String, Int>>): Outcome<CharacterResponse> {
        val request = AllocateRequest(
            campaignId,
            allocations.map { (id, delta) -> Allocation(id, delta) },
        )
        return post("/api/allocate", TwJson.encodeToString(request)) { parsePayload(it) }
    }

    suspend fun friends(): Outcome<List<Friend>> = get("/api/friends") { parsePayload<FriendsResponse>(it).friends }
    suspend fun onlineUsers(): Outcome<List<PublicUser>> = get("/api/users/online") { parsePayload<UsersResponse>(it).users }
    suspend fun invites(): Outcome<List<Invite>> = get("/api/invites") { parsePayload<InvitesResponse>(it).invites }
    suspend fun myCampaigns(): Outcome<List<CampaignState>> =
        get("/api/campaigns/mine") { parsePayload<CampaignsResponse>(it).campaigns }

    suspend fun friendRequest(username: String): Outcome<OkResponse> =
        post("/api/friends/request", jsonBody("username" to username)) { parsePayload(it) }

    suspend fun friendAccept(id: Int): Outcome<OkResponse> =
        post("/api/friends/accept", jsonBody("id" to id)) { parsePayload(it) }

    suspend fun invite(campaignId: String, userId: Int): Outcome<OkResponse> =
        post("/api/invites", jsonBody("campaign_id" to campaignId, "user_id" to userId)) { parsePayload(it) }

    suspend fun acceptInvite(inviteId: Int): Outcome<InviteAcceptResponse> =
        post("/api/invites/$inviteId/accept", jsonBody()) { parsePayload(it) }

    suspend fun acceptDetail(detailId: Int, accept: Boolean): Outcome<OkResponse> =
        post("/api/detail/$detailId/accept", jsonBody("accept" to accept)) { parsePayload(it) }

    suspend fun postSettings(payload: Map<String, Any?>): Outcome<OkResponse> =
        post("/api/settings", jsonBody(*payload.toList().toTypedArray())) { parsePayload(it) }

    suspend fun shop(action: String, body: Map<String, Any?>): Outcome<TurnResponse> =
        post("/api/shop/$action", jsonBody(*body.toList().toTypedArray())) { parsePayload(it) }

    suspend fun branch(action: String, body: Map<String, Any?>): Outcome<TurnResponse> =
        post("/api/branches/$action", jsonBody(*body.toList().toTypedArray())) { parsePayload(it) }

    /**
     * POST /api/turn -> SSE (meta / delta / final). Falls back to a single JSON
     * envelope when the server answers without an SSE stream.
     */
    fun submitTurn(campaignId: String, text: String): Flow<TurnEvent> = flow {
        val client = ApiClient.create(tokenProvider())
        try {
            val body = jsonBody("campaign_id" to campaignId, "text" to text)
            client.preparePost(ApiClient.url("/api/turn")) {
                tokenProvider()?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                contentType(ContentType.Application.Json)
                setBody(body)
            }.execute { response ->
                val contentType = response.headers[HttpHeaders.ContentType].orEmpty()
                if (contentType.contains("text/event-stream")) {
                    val channel = response.bodyAsChannel()
                    var eventName = "message"
                    val data = StringBuilder()
                    while (!channel.isClosedForRead) {
                        val line = channel.readUTF8Line() ?: break
                        when {
                            line.isEmpty() -> {
                                if (data.isNotBlank()) emit(parseSseEvent(eventName, data.toString()))
                                data.clear(); eventName = "message"
                            }
                            line.startsWith("event:") -> eventName = line.removePrefix("event:").trim()
                            line.startsWith("data:") -> data.append(line.removePrefix("data:").trim())
                        }
                    }
                    if (data.isNotBlank()) emit(parseSseEvent(eventName, data.toString()))
                } else {
                    val raw = response.bodyAsText()
                    val parsed = runCatching { parsePayload<TurnResponse>(raw) }.getOrNull()
                    if (parsed == null) {
                        emit(TurnEvent.Error(serverMessage(raw)))
                    } else if (parsed.error != null) {
                        emit(TurnEvent.Error(parsed.message ?: "Мир не ответил. Попробуй ещё раз."))
                    } else {
                        emit(TurnEvent.Final(parsed))
                    }
                }
            }
        } catch (t: Throwable) {
            emit(TurnEvent.Error("Мир не ответил. Попробуй ещё раз."))
        } finally {
            client.close()
        }
    }

    private fun parseSseEvent(name: String, data: String): TurnEvent = when (name) {
        "meta" -> runCatching {
            val obj = parsePayload<Map<String, JsonElement>>(data)
            val step = (obj["step_no"] as? JsonPrimitive)?.content?.toIntOrNull() ?: 0
            TurnEvent.Meta(step)
        }.getOrDefault(TurnEvent.Meta(0))
        "delta" -> runCatching {
            val obj = parsePayload<Map<String, JsonElement>>(data)
            val text = (obj["text"] as? JsonPrimitive)?.content.orEmpty()
            TurnEvent.Delta(text)
        }.getOrDefault(TurnEvent.Delta(""))
        "final" -> runCatching { TurnEvent.Final(parsePayload(data)) }
            .getOrDefault(TurnEvent.Error("Мир не ответил. Попробуй ещё раз."))
        "error" -> TurnEvent.Error(serverMessage(data))
        else -> TurnEvent.Raw(name, data)
    }

    companion object {
        private fun jsonBody(vararg pairs: Pair<String, Any?>): String = buildJsonObject {
            pairs.forEach { (key, value) -> put(key, value.toJsonElement()) }
        }.toString()

        private fun Any?.toJsonElement(): JsonElement = when (this) {
            null -> JsonNull
            is JsonElement -> this
            is String -> JsonPrimitive(this)
            is Boolean -> JsonPrimitive(this)
            is Number -> JsonPrimitive(this)
            is Char -> JsonPrimitive(this.toString())
            is Enum<*> -> JsonPrimitive(this.name)
            is Iterable<*> -> JsonArray(this.map { it.toJsonElement() })
            is Array<*> -> JsonArray(this.map { it.toJsonElement() })
            is Map<*, *> -> buildJsonObject {
                this@toJsonElement.forEach { (key, value) -> put(key.toString(), value.toJsonElement()) }
            }
            else -> JsonPrimitive(this.toString())
        }
    }
}

sealed interface TurnEvent {
    data class Meta(val stepNo: Int) : TurnEvent
    data class Delta(val text: String) : TurnEvent
    data class Final(val response: TurnResponse) : TurnEvent
    data class Error(val message: String) : TurnEvent
    data class Raw(val name: String, val data: String) : TurnEvent
}

@kotlinx.serialization.Serializable data class ApiError(val message: String = "")
@kotlinx.serialization.Serializable data class AuthResponse(
    val token: String = "", val user: PublicUser = PublicUser(), val error: String? = null, val message: String? = null,
)
@kotlinx.serialization.Serializable data class CampaignIdResponse(val campaign_id: String = "", val error: String? = null, val message: String? = null)
@kotlinx.serialization.Serializable data class CharacterResponse(val character: com.textworld.app.data.model.Character = com.textworld.app.data.model.Character())
@kotlinx.serialization.Serializable data class FriendsResponse(val friends: List<Friend> = emptyList())
@kotlinx.serialization.Serializable data class UsersResponse(val users: List<PublicUser> = emptyList())
@kotlinx.serialization.Serializable data class InvitesResponse(val invites: List<Invite> = emptyList())
@kotlinx.serialization.Serializable data class CampaignsResponse(val campaigns: List<CampaignState> = emptyList())
@kotlinx.serialization.Serializable data class OkResponse(val ok: Boolean = false, val error: String? = null, val message: String? = null)
@kotlinx.serialization.Serializable data class InviteAcceptResponse(val ok: Boolean = false, val campaign_id: String = "", val error: String? = null)
