package io.seda.inventory.services

import io.seda.inventory.data.Category
import io.seda.inventory.data.CategoryRepository
import io.seda.inventory.exceptions.NotFoundException
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class CategoryService {

    @Autowired
    lateinit var categoryRepository: CategoryRepository;
    data class SimpleCategory(val id: String, val name: String, val description: String?, val primaryImage: String?, val parentCategoryId: String?);

    data class CategoryWithChild(val id: String, val name: String, val description: String, val primaryImage: String?, val parentCategoryId: String?,
                                 var products: List<ProductService.SimpleProduct>);

    fun Category.toSimpleCategory(): SimpleCategory {
        return SimpleCategory(id?.toULong().toString(), name, description, primaryImage, parentCategoryId?.toULong()?.toString())
    }
    fun Category.toCategoryWithChild(): CategoryWithChild {
        return CategoryWithChild(id?.toULong().toString(), name, description, primaryImage, parentCategoryId?.toULong()?.toString(), mutableListOf())
    }


    suspend fun createCategory(name: String, description: String, primaryImage: String?, parentCategoryId: String?): SimpleCategory {
        var category = Category(null, name, description, primaryImage, parentCategoryId?.toULong()?.toLong())
        category = categoryRepository.save(category)
        return category.toSimpleCategory()
    }

    data class UpdatedMetadata(
        var name: String?,
        var description: String?,
        var primaryImage: String?
    )

    suspend fun updateMeta(id: String, updatedMetadata: UpdatedMetadata): SimpleCategory {
        var category = categoryRepository.findById(id.toULong().toLong()) ?: throw NotFoundException("Category with id $id not found")
        updatedMetadata.name?.let {
            category.name = it
        }
        updatedMetadata.description?.let {
            category.description = it
        }
        category.primaryImage = updatedMetadata.primaryImage
        category = categoryRepository.save(category);
        return category.toSimpleCategory()
    }

    suspend fun moveCategory(id: String, parentId: String?): SimpleCategory {
        var category = categoryRepository.findById(id.toULong().toLong()) ?: throw NotFoundException("Category with id $id not found")
        category.parentCategoryId = parentId?.toULong()?.toLong();
        category = categoryRepository.save(category);
        return category.toSimpleCategory();
    }

    suspend fun deleteCategory(id: String) {
        return categoryRepository.deleteById(id.toULong().toLong());
    }
    suspend fun searchCategory(keyword: String): List<SimpleCategory> {
        return categoryRepository.search(keyword)
            .map { it.toSimpleCategory() }
            .toList();
    }

    suspend fun getCategoryUnder(parentId: String?): List<SimpleCategory> {
        return categoryRepository.findAllByParentCategoryId(parentId?.toULong()?.toLong())
            .map { it.toSimpleCategory() }
            .toList();
    }

    @Autowired lateinit var productService: ProductService;
    suspend fun getCategory(id: String): CategoryWithChild {
        val category = categoryRepository.findById(id.toULong().toLong()) ?: throw NotFoundException("Category with id $id not found")
        return category.toCategoryWithChild().apply {
            products = productService.getProductsByCategoryId(id)
        };
    }


}