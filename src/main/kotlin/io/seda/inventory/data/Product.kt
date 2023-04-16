package io.seda.inventory.data

import kotlinx.coroutines.flow.Flow
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

@Table(name = "products")
data class Product(@Id var id: Long? = null, var name: String, var description: String)

interface ProductRepository: CoroutineCrudRepository<Product, Long> {
    @Query("SELECT * FROM products WHERE name LIKE %:search%")
    fun findAllProducts(search: String, pageable: Pageable): Flow<Product>
}