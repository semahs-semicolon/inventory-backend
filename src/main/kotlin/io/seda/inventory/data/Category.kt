package io.seda.inventory.data

import kotlinx.coroutines.flow.Flow
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository


@Table(name = "categories")
data class Category(
    @Id var id: Long? = null,
    var name: String,
    var description: String,
    var primaryImage: String?,
    var parentCategoryId: Long? = null
)

interface CategoryRepository: CoroutineCrudRepository<Category, Long> {
    fun findAllByParentCategoryId(parentCategoryId: Long?): Flow<Category>

    @Query("SELECT * FROM categories WHERE name LIKE :search")
    fun search(keyword: String): Flow<Category>
}