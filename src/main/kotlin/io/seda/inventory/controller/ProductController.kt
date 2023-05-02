package io.seda.inventory.controller

import io.seda.inventory.services.ItemService
import io.seda.inventory.services.ProductService
import kotlinx.coroutines.flow.Flow
import org.springframework.beans.factory.annotation.Autowired
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

    data class ProductCreationRequest(val name: String, val imageId: String?, val description: String);

    @PostMapping("")
    suspend fun create(@RequestBody request: ProductCreationRequest): ProductService.SimpleProduct {
        return productService.createProduct(request.name, request.description, request.imageId);
    }

    @DeleteMapping("/{id}")
    suspend fun delete(@PathVariable("id") id: String) {
        return productService.deleteProduct(id)
    }

    @PatchMapping("/{id}")
    suspend fun update(@PathVariable("id") id: String, @RequestBody request: ProductCreationRequest): ProductService.SimpleProduct {
        return productService.updateProduct(id, request.name, request.description, request.imageId);
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