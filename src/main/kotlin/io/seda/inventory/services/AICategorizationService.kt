package io.seda.inventory.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.seda.inventory.data.Category
import io.seda.inventory.data.CategoryRepository
import io.seda.inventory.data.Product
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient


@Service
class AICategorizationService {

    @Value("\${modelurl}")
    lateinit var url: String

    var webClientBuilder: WebClient.Builder = WebClient.builder();

    val objectMapper: ObjectMapper = ObjectMapper();

    @Autowired
    lateinit var categoryRepository: CategoryRepository;

    suspend fun categorizeItAll(findAll: Flow<Product>): Flow<Product> {
        val builtList = buildCategoryList(categoryRepository.findAll().toList())
        return findAll.map {
            return@map flow {
                val result = askModel(builtList.keys, it) ?: ""
                val category = builtList[result]
                it.categoryId = category?.id;

                emit(it)
            }
        }.flattenMerge(10);
    }



    data class CategorySub(val name: String, val id: Long, val child: MutableList<CategorySub>);
    fun buildCategoryList(category: List<Category>): Map<String, CategorySub> {
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

    data class AIResult(val content: String);
    suspend fun askModel(category: Collection<String>, product: Product): String? {
        val prompt = """
            주어진 카테고리 목록에서 입력된 물품에 가장 잘 어울리는 카테고리를 골라 이유와 함께 출력하십시오 
            만약 적절한 카테고리가 없다면 "/기타"를 출력하십시오.
            
            카테고리 목록: [${category.map { "\"${it}\"" }.joinToString(", ")}]
            
            입력: 비커
            출력: {"reason": "비커는 액체류를 담을 수 있는 용기로, 비커는 비커입니다", "category": "비커 < 실험기구"}
            
            입력: ${product.name}
            출력: 
        """.trimIndent()

        val result = webClientBuilder.build().post().uri(url+"/completion")
            .bodyValue(mapOf(
                "prompt" to prompt,
                "n_predict" to 128,
                "json_schema" to mapOf(
//                            "type" to "string",
//                            "enum" to category,
                    "type" to "object",
                    "properties" to mapOf(
                        "category" to mapOf(
                            "type" to "string",
                            "enum" to category
                        ),
                        "reason" to mapOf(
                            "type" to "string"
                        )
                    ),
                    "required" to listOf("category", "reason")
                ),
                "cache_prompt" to true
            ))
            .retrieve()
            .bodyToMono(AICategorizationService.AIResult::class.java)
            .awaitSingle();
//        return Vector.of(*embedding.embedding);
        println(prompt)
        println(result.content.trim() + " for " + product.name)
//        return result.content.trim().trim('\"')
        return objectMapper.readTree(result.content)["category"].asText()
    }

}