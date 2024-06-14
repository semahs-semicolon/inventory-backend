package io.seda.inventory.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.seda.inventory.data.Category
import io.seda.inventory.data.CategoryRepository
import io.seda.inventory.data.Product
import kotlinx.coroutines.delay
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
        val list = findAll.toList();
        val size = 25
//        for (i in 0 ..list.size / size) {
//            val slice = list.slice(i * size .. Math.min(i * size + size - 1, list.size - 1))
//
//            val result = deeplTranslate(objectMapper.writeValueAsString(slice.map { a -> a.name }))
//            val parsed = objectMapper.readTree(result)
//
//            for (j in 0 .. size -1) {
//                println(slice[j].name + " / "+parsed[j].asText())
//                slice[j].name = parsed[j]?.asText() ?: "???"
//            }
//            delay(1000)
//        }

        return list.asFlow().map {
            return@map flow {
                val result = askModel(builtList.keys, it) ?: ""
                val category = builtList[result]
                it.categoryId = category?.id;

                emit(it)
            }
        }.flattenMerge(10);
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
        val prompt = """
            주어진 카테고리 목록에서 입력된 물품에 가장 잘 어울리는 카테고리를 골라 출력하십시오 
            만약 적절한 카테고리가 없다면 "기타"를 출력하십시오.
            
            카테고리 목록: [${category.map { "\"${it}\"" }.joinToString(", ")}]
            
            ### 입력:
            ${product.name}
            
            ### 출력:
            
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
                        )
                    ),
                    "required" to listOf("category")
                ),
                "cache_prompt" to true
            ))
            .retrieve()
            .bodyToMono(AIResult::class.java)
            .awaitSingle();
//        return Vector.of(*embedding.embedding);
        println(prompt)
        println(result.content.trim() + " for " + product.name)
//        return result.content.trim().trim('\"')
        return objectMapper.readTree(result.content)["category"].asText()
    }

}