package io.seda.inventory.controller

import io.seda.inventory.services.ItemService
import io.seda.inventory.services.ProductService
import kotlinx.coroutines.flow.Flow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/products")
class ProductController {
    @Autowired
    lateinit var productService: ProductService;
    @Autowired
    lateinit var itemService: ItemService;

    @GetMapping("/")
    suspend fun search(@RequestParam("search") search: String, @RequestParam("size") size: Int, @RequestParam("page") page: Int): Flow<ProductService.SimpleProduct> {
        val result = productService.getProducts(page, size, search);
        return result;
    }

    data class ProductCreationRequest(val name: String, val description: String);

    @PostMapping("/")
    suspend fun create(request: ProductCreationRequest): ProductService.SimpleProduct {
        return productService.createProduct(request.name, request.description);
    }

    @DeleteMapping("/{id}")
    suspend fun delete(@PathVariable("id") id: String) {
        return productService.deleteProduct(id)
    }

    @PatchMapping("/{id}")
    suspend fun update(@PathVariable("id") id: String, request: ProductCreationRequest): ProductService.SimpleProduct {
        return productService.updateProduct(id, request.name, request.description);
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