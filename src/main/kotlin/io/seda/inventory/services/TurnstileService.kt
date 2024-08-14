package io.seda.inventory.services
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono


@Service
class TurnstileService {
    @Value("\${TURNSTILE_SECRET}") lateinit var secretKey: String;
    suspend fun verify(turnstileToken: String): Mono<Boolean> {
        val client = WebClient.create("https://challenges.cloudflare.com")
        val formData: MultiValueMap<String, String> = LinkedMultiValueMap()
        formData.add("secret", secretKey)
        formData.add("response", turnstileToken)
        val result: Mono<String> = client.post()
            .uri("/turnstile/v0/siteverify")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(formData)
            .exchange()
            .flatMap { response ->
                if (response.statusCode() == HttpStatus.OK) {
                    response.bodyToMono(String::class.java)
                } else {
                    Mono.error(IllegalStateException("Unexpected status code during verifying turnstile: ${response.statusCode()}"))
                }
            }

        return result
            .map { response -> parseJsonSuccess(response) }
            .flatMap { success ->
                if (success) {
                    Mono.just(true)
                } else {
                    Mono.error(Exception("Turnstile verification failed"))
                }
            }
    }
    private fun parseJsonSuccess(json: String): Boolean {
        val objectMapper = ObjectMapper()
        val jsonNode: JsonNode = objectMapper.readTree(json)
        return jsonNode.get("success").asBoolean()
    }

}
