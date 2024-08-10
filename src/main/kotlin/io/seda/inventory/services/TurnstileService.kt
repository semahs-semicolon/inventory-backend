package io.seda.inventory.services
import org.springframework.beans.factory.annotation.Value
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.springframework.stereotype.Service

@Serializable
data class TurnstileResponse @OptIn(ExperimentalSerializationApi::class) constructor(val success: Boolean, @JsonNames("error-codes") val errorCodes: List<String>, @JsonNames("challenge_ts") val timestamp: String, val hostname: String, val action: String, val cdata: String)

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
        val res: HttpResponse = client.submitForm(
            url = endpoint,
            formParameters = parameters {
                append("response", turnstileToken)
                append("secret", secretKey)
            }
        )
        if(res.status.value in 200..299) {
            val body: TurnstileResponse = res.body();
            println(body.toString())
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
