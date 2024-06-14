package io.seda.inventory.services

import io.seda.inventory.data.Product
import io.seda.inventory.data.ProductRepository
import io.seda.inventory.exceptions.NotFoundException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.data.domain.PageRequest
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Service

@Service
class ProductService {
    @Autowired lateinit var productRepository: ProductRepository;
    @Autowired lateinit var databaseClient: DatabaseClient // well FTS and stuff lol.

    @Autowired lateinit var embeddingService: EmbeddingService;
    @Autowired lateinit var aiCategorizationService: AICategorizationService;

    data class SimpleProduct(val id: String, val name: String, val description: String?, val primaryImage: String?, val imageEmbedding: FloatArray?, val categoryId: Long?, val categoryAccepted: Boolean?);
    fun Product.toSimpleProduct(): SimpleProduct {
        return SimpleProduct(id?.toULong().toString(), name, description, primaryImage, imageEmbedding, categoryId, categoryAccepted);
    }

    suspend fun getProducts(page: Int, count: Int, search: String): Flow<SimpleProduct> {
        val pageRequest: PageRequest = PageRequest.of(page, count)
        return productRepository.findAllProducts("%$search%", pageRequest.offset, pageRequest.pageSize)
            .map { it.toSimpleProduct() }
    }
    suspend fun getProducts(page: Int, count: Int, search: FloatArray): Flow<SimpleProduct> {
//        val pageRequest: PageRequest = PageRequest.of(page, count)
        return productRepository.findAllProductsByEmbedding(search, count)
            .map { it.apply { it.imageEmbedding = null }.toSimpleProduct() }
    }

    suspend fun getOrphanProducts(page: Int, count: Int): Flow<SimpleProduct> {
//        val pageRequest: PageRequest = PageRequest.of(page, count)
        val pageRequest: PageRequest = PageRequest.of(page, count)
        return productRepository.findAllOrphanProducts(pageRequest.offset, pageRequest.pageSize)
            .map { it.apply { it.imageEmbedding = null }.toSimpleProduct() }
    }

    suspend fun updateProduct(id: String, name: String?, description: String?, imageId: String?, categoryId: String?): SimpleProduct {
        var loc = productRepository.findById(id.toULong().toLong()) ?: throw NotFoundException("product with id $id not found")
        var imageEmbedding = if (imageId == null)  null else embeddingService.generateEmbedding(imageId)

        name?.let {
            loc.name = it
        };
        description?.let {
            loc.description = it;
        }
        loc.images = loc.images.filter { !it.equals(loc.primaryImage) }.toMutableList()
        if (imageId != null) {
            loc.images.add(imageId)
            loc.imageEmbedding = imageEmbedding
        }
        loc.primaryImage = imageId;
        loc.categoryId = categoryId?.toULong()?.toLong();
        loc = productRepository.save(loc);
        return loc.toSimpleProduct();
    }

    suspend fun createProduct(name: String, description: String, imageId: String?, categoryId: String?): SimpleProduct {
        var imageEmbedding = if (imageId == null) null else embeddingService.generateEmbedding(imageId)

        var product = Product(name = name, description = description, primaryImage = imageId, images = arrayOf(imageId).toList().filterNotNull().toMutableList(), categoryId = categoryId?.toULong()?.toLong()
            , imageEmbedding =  imageEmbedding);
        product = productRepository.save(product);
        return product.toSimpleProduct();
    }

    suspend fun deleteProduct(id: String) {
        var loc = productRepository.findById(id.toULong().toLong()) ?: throw NotFoundException("product with id $id not found")
        return productRepository.deleteById(id.toULong().toLong())
    }

    suspend fun getSimpleProduct(productId: String): SimpleProduct {
        return productRepository.findById(productId.toULong().toLong())?.toSimpleProduct() ?: throw NotFoundException("product with id $productId not found");
    }

    suspend fun exists(productId: Long): Boolean {
        return productRepository.existsById(productId);
    }

    suspend fun getProductsByCategoryId(categoryId: String?): List<SimpleProduct> {
        return productRepository.findAllProductsByCategoryId(categoryId?.toULong()?.toLong())
            .map {it.toSimpleProduct()}
            .toList()
    }
    suspend fun getProductsByNoConfirm(page: Int, count: Int): List<SimpleProduct> {
        val pageRequest: PageRequest = PageRequest.of(page, count)
        return productRepository.findAllCategoryNotAccepted(pageRequest.offset, pageRequest.pageSize)
            .map {it.toSimpleProduct()}
            .toList()
    }



    @EventListener(ApplicationReadyEvent::class)
    fun doSomethingAfterStartup() {
        println("um")
        runBlocking {
            productRepository.findAll()
                .filter { a -> a.imageEmbedding === null && a.primaryImage != null }
                .map {
                    println(it)
                    it.imageEmbedding = embeddingService.generateEmbedding(it.primaryImage ?: "")
                    it
                }
                .map { productRepository.save(it) }.toList()
        }

//        runBlocking {
//            aiCategorizationService.categorizeItAll(
//                productRepository.findAll()
//            ).map { productRepository.save(it) }.toList()
//        }
    }

    suspend fun updateCategoryId(productId: String, categoryId: String?): ProductService.SimpleProduct {
        val product = productRepository.findById(productId.toULong().toLong());
        if (product == null) throw NotFoundException("Product with id $productId not found");
        product.categoryAccepted = true;
        product.categoryId = categoryId?.toULong()?.toLong();
        productRepository.save(product);
        return product.toSimpleProduct();
    }

}