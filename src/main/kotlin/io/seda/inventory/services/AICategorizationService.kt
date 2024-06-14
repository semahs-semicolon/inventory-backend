package io.seda.inventory.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.seda.inventory.data.Category
import io.seda.inventory.data.CategoryRepository
import io.seda.inventory.data.Product
import io.seda.inventory.data.ProductRepository
import io.seda.inventory.exceptions.NotFoundException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.lang.IllegalStateException


@Service
class AICategorizationService {

    @Value("\${modelurl}")
    lateinit var url: String

    var webClientBuilder: WebClient.Builder = WebClient.builder();

    val objectMapper: ObjectMapper = ObjectMapper();

    @Autowired
    lateinit var categoryRepository: CategoryRepository;
    @Autowired
    lateinit var productRepository: ProductRepository;

    suspend fun categorizeItAll() {
        if (status.running) throw IllegalStateException("Can not run two categorization at same time")
        status.running = true;

        val builtList = buildCategoryList(categoryRepository.findAll().toList())
        val list = productRepository.findAll().toList();

        status.total = list.size
        status.completed = 0

        job = with(CoroutineScope(Dispatchers.IO)) {
            launch {
                list.asFlow().map {
                    return@map flow {
                        while(currentCoroutineContext().isActive) {
                            try {
                                val result = askModel(builtList.keys, it) ?: ""
                                val category = builtList[result]
                                it.categoryId = category?.id
                                it.categoryAccepted = false
                                emit(it)
                                status.completed++
                                break;
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }.flattenMerge(10).collect {productRepository.save(it)}
                status.running = false;
            }
        }
    }

    suspend fun cancelItAll() {
        if (job == null) throw IllegalStateException("Job not running")
        job?.cancel()
        status.running = false;
    }



    data class CategorySub(val name: String, val id: Long, val child: MutableList<CategorySub>);
    suspend fun buildCategoryList(category: List<Category>): Map<String, CategorySub> {
        val map = hashMapOf<Long, CategorySub>()
        val noParentsIds = arrayListOf<Long>()
        for (category1 in category) {
            map[category1.categoryId ?: -1] = CategorySub(category1.name, category1.categoryId ?: -1, arrayListOf())
            if (category1.parentCategoryId == null)
                noParentsIds.add(category1.categoryId ?: -1)
        }
        for (category1 in category) {
            map[category1.categoryId]?.let {  map[category1.parentCategoryId ?: -1]?.child?.add(it) }
        }

        val list = hashMapOf<String, CategorySub>()
        for (noParentsId in noParentsIds) {
            map[noParentsId]?.let {doDfs(map, it, list, "", true) }
        }
//        for (category1 in category) {
//            list.add(category1.name)
//        }
//        val keyArr = list.keys.toList()
//        val result = deeplTranslate(objectMapper.writeValueAsString(keyArr))
//        val parsed = objectMapper.readTree(result)
//
//        val list2 = hashMapOf<String, CategorySub>()
//        for (i in 0 .. keyArr.size - 1) {
//            list2[parsed[i].asText()] = list[keyArr[i]]!!
//        }

        return list
    }
    fun doDfs(map: Map<Long, CategorySub>, root: CategorySub, list: MutableMap<String, CategorySub>, current: String, isRoot: Boolean) {
        var name: String;
        if (!isRoot)
            name = root.name +" < " + current;
        else
            name = root.name;

        list[name] = root;

        for (categorySub in root.child) {
            doDfs(map, categorySub, list, name, false)
        }
    }

    data class DEEPLXResult(val data: String);
    suspend fun deeplTranslate(name: String): String {
        println(name)
        while(true) {
            try {
                val result = webClientBuilder.build().post().uri("http://localhost:1188/translate")
                    .bodyValue(
                        mapOf(
                            "text" to name,
                            "source_lang" to "KO",
                            "target_lang" to "EN"
                        )
                    )
                    .retrieve()
                    .bodyToMono(AICategorizationService.DEEPLXResult::class.java)
                    .awaitSingle();
                println(result.data)
                return result.data;
            } catch (e: Exception) {
                println(e)
            }
            delay(2000)
        }
    }

    data class AIResult(val content: String);
    suspend fun askModel(category: Collection<String>, product: Product): String? {
//        val suitability = Map
        val suitabliltyMap = category.asFlow().map {
            flow {
                val prompt = """
                    주어진 물품이 해당 카테고리에 적합한지 여부를 T/F로 출력하시오.

                    ### 입력:
                    물품: ${product.name}
                    카테고리: ${it}

                    ### 출력:

                """.trimIndent()
                val result = webClientBuilder.build().post().uri(url+"/completion")
                    .bodyValue(mapOf(
                        "prompt" to prompt,
                        "n_predict" to 3,
                        "json_schema" to mapOf(
                            "enum" to listOf("T", "F")
                        ),
                        "cache_prompt" to true
                    ))
                    .retrieve()
                    .bodyToMono(AIResult::class.java)
                    .awaitSingle();
                println(it + " for "+product.name +" is "+result)
                emit(it to (result.content.trim() == "\"T\""))
            }
        }.flattenMerge(12).toList().toMap().toMutableMap()
        println(product.name + " / "+ suitabliltyMap)

        suitabliltyMap["기타"] = true
        val nextCategories = suitabliltyMap.filterValues { it }.map { it.key }
//        val nextCategories = category;

        val prompt = """
            주어진 카테고리 목록에서 입력된 물품에 가장 잘 어울리는 카테고리를 골라 출력하십시오 
            만약 적절한 카테고리가 없다면 "기타"를 출력하십시오.
            
            카테고리 목록: [${nextCategories.map { "\"${it}\"" }.joinToString(", ")}]
            
            ### 입력:
            ${product.name}
            
            ### 출력:
            
        """.trimIndent()

        val result = webClientBuilder.build().post().uri(url+"/completion")
            .bodyValue(mapOf(
                "prompt" to prompt,
                "n_predict" to 64,
                "temperature" to 0.3,
                "json_schema" to mapOf(
//                            "type" to "string",
//                            "enum" to category,
                    "type" to "object",
                    "properties" to mapOf(
                        "category" to mapOf(
                            "type" to "string",
                            "enum" to nextCategories
                        )
                    ),
                    "required" to listOf("category")
                ),
                "cache_prompt" to true
            ))
            .retrieve()
            .bodyToMono(AIResult::class.java)
            .awaitSingle();
        println(prompt)
        println(result.content.trim() + " for " + product.name)
        return objectMapper.readTree(result.content)["category"].asText()
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

    suspend fun categorize(product: String) {
        val product = productRepository.findById(product.toULong().toLong())
        if (product == null) throw NotFoundException("Product with id $product not found")
        val builtList = buildCategoryList(categoryRepository.findAll().toList())
        while(currentCoroutineContext().isActive) {
            try {
                val result = askModel(builtList.keys, product) ?: ""
                val category = builtList[result]
                product.categoryId = category?.id
                product.categoryAccepted = false
                productRepository.save(product)
                break;
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}