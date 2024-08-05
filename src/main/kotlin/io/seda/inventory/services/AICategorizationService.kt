package io.seda.inventory.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.seda.inventory.configuration.WebClientAwsSigner
import io.seda.inventory.data.Category
import io.seda.inventory.data.CategoryRepository
import io.seda.inventory.data.Product
import io.seda.inventory.data.ProductRepository
import io.seda.inventory.exceptions.NotFoundException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import reactor.kotlin.core.publisher.toMono
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.signer.Aws4Signer
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.lambda.LambdaAsyncClient
import software.amazon.awssdk.services.lambda.model.InvocationType
import software.amazon.awssdk.services.lambda.model.InvokeRequest
import java.lang.UnsupportedOperationException


@Service
class AICategorizationService {

    var webClientBuilder: WebClient.Builder = WebClient.builder();

    val objectMapper: ObjectMapper = ObjectMapper();

    @Autowired
    lateinit var categoryRepository: CategoryRepository;
    @Autowired
    lateinit var productRepository: ProductRepository;


    suspend fun categorizeItAll() {
        if (true) throw UnsupportedOperationException("Not supported, too expensive.")
        if (status.running) throw IllegalStateException("Can not run two categorization at same time")
        status.running = true;

//        val builtList = buildCategoryList(categoryRepository.findAll().toList())
//        val list = productRepository.findAll().toList();
//
//        status.total = list.size
//        status.completed = 0
//
//        job = with(CoroutineScope(Dispatchers.IO)) {
//            launch {
//                list.asFlow().map {
//                    return@map flow {
//                        while(currentCoroutineContext().isActive) {
//                            try {
//                                val result = askModel(builtList.keys, it) ?: ""
//                                val category = builtList[result]
//                                it.categoryId = category?.id
//                                it.categoryAccepted = false
//                                emit(it)
//                                status.completed++
//                                break;
//                            } catch (e: Exception) {
//                                e.printStackTrace()
//                            }
//                        }
//                    }
//                }.flattenMerge(1).collect {productRepository.save(it)}
//                status.running = false;
//            }
//        }
    }

    suspend fun cancelItAll() {
        if (job == null) throw IllegalStateException("Job not running")
        job?.cancel()
        status.running = false;
    }



    data class Status(
        var completed: Int,
        var total: Int,
        var running: Boolean
    )
    var job: Job? = null;
    var status: Status = Status(0,0, false);
    fun getStatusInfo(): Status {
        return status;
    }

    data class AICategorizationRequest(val type: String = "single", val productId: String);

    @Value("\${categorization_lambda_arn}")
    lateinit var embedding_endpoint: String

    @Autowired
    lateinit var lambdaAsyncClient: LambdaAsyncClient;

    suspend fun categorizeAsync(product: String) {
        val body = objectMapper.writeValueAsString(AICategorizationRequest(productId = product, type = "single"));
        lambdaAsyncClient.invoke(InvokeRequest.builder()
            .functionName(embedding_endpoint)
            .invocationType(InvocationType.EVENT)
            .payload(SdkBytes.fromUtf8String(body)).build()).toMono().awaitSingleOrNull();
    }

}