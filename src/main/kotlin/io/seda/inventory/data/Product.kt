package io.seda.inventory.data

import kotlinx.coroutines.flow.Flow
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

@Table(name = "products")
data class Product(
    @Id var id: Long? = null,
    var name: String,
    var description: String,
    var primaryImage: String?,
    var images: MutableList<String> = mutableListOf(),
    var categoryId: Long? = null,
    var tags: MutableList<String> = mutableListOf()
)

interface ProductRepository: CoroutineCrudRepository<Product, Long> {
    @Query("SELECT * FROM products WHERE name LIKE :search OFFSET :offset LIMIT :limit")
    fun findAllProducts(search: String, offset: Long, limit: Int): Flow<Product>

    fun findAllProductsByCategoryId(categoryId: Long?): Flow<Product>
}