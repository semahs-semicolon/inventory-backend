package io.seda.inventory.controller

import io.seda.inventory.services.ItemService
import io.seda.inventory.services.ProductService
import kotlinx.coroutines.flow.Flow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.codec.multipart.Part
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/products")
class ProductController {
    @Autowired
    lateinit var productService: ProductService;
    @Autowired
    lateinit var itemService: ItemService;

    @GetMapping("")
    suspend fun search(@RequestParam("search") search: String, @RequestParam("size") size: Int, @RequestParam("page") page: Int): Flow<ProductService.SimpleProduct> {
        val result = productService.getProducts(page, size, search);
        return result;
    }

    @GetMapping("/orphans")
    suspend fun search(@RequestParam("size") size: Int, @RequestParam("page") page: Int): Flow<ProductService.SimpleProduct> {
        val result = productService.getOrphanProducts(page, size);
        return result;
    }

    data class ImageSearchRequest(val embedding: FloatArray, val size: Int, val page: Int)
    @PostMapping("imageSearch")
    suspend fun imageSearch(@RequestBody imageSearchRequest: ImageSearchRequest): Flow<ProductService.SimpleProduct> {
        val result = productService.getProducts(imageSearchRequest.page, imageSearchRequest.size, imageSearchRequest.embedding)
        return result;
    }

    data class ProductCreationRequest(val name: String?, val imageId: String?, val description: String?, val categoryId: String?);

    @PostMapping("")
    suspend fun create(@RequestBody request: ProductCreationRequest): ProductService.SimpleProduct {
        requireNotNull(request.name) {"Name can not be null"}
        requireNotNull(request.description) {"Description can not be null"}
        return productService.createProduct(request.name, request.description, request.imageId, request.categoryId);
    }

    @DeleteMapping("/{id}")
    suspend fun delete(@PathVariable("id") id: String) {
        return productService.deleteProduct(id)
    }

    @PatchMapping("/{id}")
    suspend fun update(@PathVariable("id") id: String, @RequestBody request: ProductCreationRequest): ProductService.SimpleProduct {
        return productService.updateProduct(id, request.name, request.description, request.imageId, request.categoryId);
    }

    @GetMapping("/{id}")
    suspend fun get(@PathVariable("id") id:String): ProductService.SimpleProduct {
        return productService.getSimpleProduct(id);
    }

    @GetMapping("/{id}/items")
    suspend fun getItems(@PathVariable("id") id:String): List<ItemService.InjectableItem> {
        return itemService.findItemsByProduct(id);
    }
}