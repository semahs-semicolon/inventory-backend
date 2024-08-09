package io.seda.inventory.services
import org.springframework.beans.factory.annotation.Value
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.springframework.stereotype.Service

@Serializable
private data class TurnstileBody(val secret: String, val response: String)
@Serializable
data class TurnstileResponse @OptIn(ExperimentalSerializationApi::class) constructor(val success: Boolean, @JsonNames("error-codes") val errorCodes: List<String>)

@Service
class TurnstileService {
    @Value("\${TURNSTILE_SECRET}") lateinit var secretKey: String;
    @Value("\${TURNSTILE_ENDPOINT}") lateinit var endpoint: String;
    suspend fun verify(turnstileToken: String): Boolean {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }
        val res = client.post(endpoint) {
            contentType(ContentType.Application.Json)
            setBody(TurnstileBody(secretKey, turnstileToken))
        }
        if(res.status == HttpStatusCode.OK) {
            val body: TurnstileResponse = res.body();
            if(body.success) {
                return true;
            } else {
                throw Exception("verify error: ${body.errorCodes}");
            }
        } else {
            throw Exception("fetch error: ${res.status}")
        }
    }
}
