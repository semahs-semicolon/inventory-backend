package io.seda.inventory.data

import io.r2dbc.postgresql.codec.Vector
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
    var tags: MutableList<String> = mutableListOf(),
    var imageEmbedding: FloatArray? = null,
    var categoryAccepted: Boolean = false
)

data class EnrichedProduct(
    @Id var id: Long? = null,
    var name: String,
    var description: String,
    var primaryImage: String?,
    var images: MutableList<String> = mutableListOf(),
    var categoryId: Long? = null,
    var tags: MutableList<String> = mutableListOf(),
    var imageEmbedding: FloatArray? = null,
    var categoryAccepted: Boolean = false,
    @Transient var highlight_name: String? = null,
    @Transient var highlight_desc: String? = null,
    @Transient var rank: Float? = null
)

interface ProductRepository: CoroutineCrudRepository<Product, Long> {
    @Query("SELECT *, ts_rank_cd(textsearch, tsq) AS rank, ts_headline('public.korean', description, tsq, 'HighlightAll=true,StartSel=\$\$ST1\$\$,StopSel=\$\$ST2\$\$') as highlight_desc, ts_headline('public.korean', \"name\", tsq, 'HighlightAll=true,StartSel=\$\$ST1\$\$,StopSel=\$\$ST2\$\$') as highlight_name FROM products, websearch_to_tsquery('public.korean', :search) tsq WHERE tsq @@ textsearch ORDER BY rank DESC OFFSET :offset LIMIT :limit")
    fun findAllProducts(search: String, offset: Long, limit: Int): Flow<EnrichedProduct>

    @Query("SELECT * FROM products ORDER BY id DESC OFFSET :offset LIMIT :limit")
    fun findAllProducts(offset: Long, limit: Int): Flow<Product>


    @Query("SELECT * FROM products ORDER BY image_embedding <=> :search LIMIT :limit")
    fun findAllProductsByEmbedding(search: FloatArray, limit: Int): Flow<Product>
    @Query("SELECT * FROM products p LEFT JOIN items i ON p.id = i.product_id WHERE i.id is NULL OFFSET :offset LIMIT :limit")
    fun findAllOrphanProducts(offset: Long, limit: Int): Flow<Product>

    @Query("SELECT * FROM products p WHERE p.category_accepted is FALSE OFFSET :offset LIMIT :limit")
    fun findAllCategoryNotAccepted(offset: Long, limit: Int): Flow<Product>

    fun findAllProductsByCategoryId(categoryId: Long?): Flow<Product>
}