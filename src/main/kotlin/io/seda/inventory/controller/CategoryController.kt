package io.seda.inventory.controller

import io.seda.inventory.services.CategoryService
import io.seda.inventory.services.ProductService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/categories")
class CategoryController {
    @Autowired lateinit var categoryService: CategoryService;
    @Autowired lateinit var productService: ProductService;

    data class CreateCategoryRequest(val name: String, val description: String, val primaryImage: String?, val parentCategoryId: String?);
    @PostMapping("")
    suspend fun createCategory(@RequestBody request: CreateCategoryRequest): CategoryService.SimpleCategory {
        return categoryService.createCategory(
            request.name,
            request.description,
            request.primaryImage,
            request.parentCategoryId
        )
    }

    @DeleteMapping("/{id}")
    suspend fun deleteCategory(@PathVariable("id") id: String) {
        return categoryService.deleteCategory(id)
    }

    @PatchMapping("/{id}")
    suspend fun updateCategory(@PathVariable("id") id: String, @RequestBody request: CategoryService.UpdatedMetadata): CategoryService.SimpleCategory {
        return categoryService.updateMeta(id, request)
    }

    data class CategoryMoveRequest(val parentCategoryId: String?);
    suspend fun moveCategory(@PathVariable("id") id: String, @RequestBody request: CategoryMoveRequest): CategoryService.SimpleCategory {
        return categoryService.moveCategory(id, request.parentCategoryId);
    }

    @GetMapping("")
    suspend fun getCategoryUnder(): List<CategoryService.SimpleCategory> {
        return categoryService.getCategoryUnder(null);
    }
    @GetMapping("/{id}/categories")
    suspend fun getCategoryUnder(@PathVariable("id") id: String?): List<CategoryService.SimpleCategory> {
        return categoryService.getCategoryUnder(id);
    }
    data class CategorySearchRequest(val keyword: String);
    @GetMapping("/search")
    suspend fun search(@RequestBody request: CategorySearchRequest): List<CategoryService.SimpleCategory> {
        return categoryService.searchCategory(request.keyword)
    }

    @GetMapping("/{id}")
    suspend fun getCategory(@PathVariable("id") id: String): CategoryService.CategoryWithChild {
        return categoryService.getCategory(id);
    }

    @GetMapping("/{id}/products")
    suspend fun getProducts(@PathVariable("id") id: String): List<ProductService.SimpleProduct> {
        return productService.getProductsByCategoryId(id);
    }


}