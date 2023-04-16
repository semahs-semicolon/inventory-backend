package io.seda.inventory.services

import io.seda.inventory.data.Product
import io.seda.inventory.data.ProductRepository
import io.seda.inventory.exceptions.NotFoundException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Service

@Service
class ProductService {
    @Autowired lateinit var productRepository: ProductRepository;
    @Autowired lateinit var databaseClient: DatabaseClient // well FTS and stuff lol.

    data class SimpleProduct(val id: String, val name: String, val description: String?);
    fun Product.toSimpleProduct(): SimpleProduct {
        return SimpleProduct(id?.toULong().toString(), name, description);
    }

    suspend fun getProducts(page: Int, count: Int, search: String): Flow<SimpleProduct> {
        val pageRequest: PageRequest = PageRequest.of(page, count)
        return productRepository.findAllProducts(search, pageRequest)
            .map { it.toSimpleProduct() }
    }

    suspend fun createProduct(name: String, description: String): SimpleProduct {
        var product = Product(name = name, description = description);
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
}