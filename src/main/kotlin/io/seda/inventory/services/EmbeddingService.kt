package io.seda.inventory.services

import io.r2dbc.postgresql.codec.Vector
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient


@Service
class EmbeddingService {

    @Value("\${embeddingurl}")
    lateinit var url: String

    var webClientBuilder: WebClient.Builder = WebClient.builder();


    data class Embedding(val embedding: FloatArray)

    suspend fun generateEmbedding(part: FilePart): Vector {
        val embedding = webClientBuilder.build().post().uri(url+"/process")
            .body(BodyInserters.fromMultipartData("", part))
            .retrieve()
            .bodyToMono(Embedding::class.java)
            .awaitSingle();

        return Vector.of(*embedding.embedding);
    }

    data class EmbeddingRequest(val imageid: String);
    suspend fun generateEmbedding(imageid: String): FloatArray {
        val embedding = webClientBuilder.build().post().uri(url+"/processs3")
            .bodyValue(EmbeddingRequest(imageid))
            .retrieve()
            .bodyToMono(Embedding::class.java)
            .awaitSingle();
//        return Vector.of(*embedding.embedding);
        return embedding.embedding;
    }

}