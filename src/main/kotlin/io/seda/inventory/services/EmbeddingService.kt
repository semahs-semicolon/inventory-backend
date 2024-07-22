package io.seda.inventory.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.r2dbc.postgresql.codec.Vector
import io.seda.inventory.configuration.WebClientAwsSigner
import kotlinx.coroutines.future.await
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.kotlin.core.publisher.toMono
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.signer.Aws4Signer
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region

@Service
class EmbeddingService {

    @Value("\${embedding_lambda_url}")
    lateinit var embedding_endpoint: String

    @Autowired
    lateinit var webClientBuilder: WebClient.Builder;

    @Autowired
    lateinit var objectMapper: ObjectMapper;

    @Autowired
    lateinit var awsCredentialsProvider: AwsCredentialsProvider;

    data class Embedding(val embedding: FloatArray)


    data class EmbeddingRequest(val imageid: String);
    suspend fun generateEmbedding(imageid: String): FloatArray {
        val body = objectMapper.writeValueAsString(EmbeddingRequest(imageid));

        val embedding =
            webClientBuilder
                .filter(
                    ExchangeFilterFunction.ofRequestProcessor(
                        WebClientAwsSigner(body, Aws4Signer.create(), awsCredentialsProvider, "es", Region.US_EAST_1),
                    )
                ).build()
                .post()
            .uri(embedding_endpoint+"/processs3")
            .bodyValue(EmbeddingRequest(imageid))
            .retrieve()
            .bodyToMono(Embedding::class.java)
            .awaitSingle();
        return embedding.embedding;
    }

}