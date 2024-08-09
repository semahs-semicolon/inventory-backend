package io.seda.inventory.controller

import io.seda.inventory.data.NoticeWithAuthor
import io.seda.inventory.services.AICategorizationService
import io.seda.inventory.services.CategoryService
import io.seda.inventory.services.NoticeService
import io.seda.inventory.services.ProductService
import kotlinx.coroutines.flow.toList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/notices")
class NoticeController {
    @Autowired lateinit var noticeService: NoticeService;

    data class CreateNoticeRequest(val title: String, val content: String);
    @PostMapping("")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    suspend fun createCategory(@RequestBody request: CreateNoticeRequest): NoticeWithAuthor {
        return noticeService.createArticle(
            request.title,
            request.content
        )
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    suspend fun deleteCategory(@PathVariable("id") id: String) {
        return noticeService.deleteArticle(id)
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    suspend fun updateCategory(@PathVariable("id") id: String, @RequestBody request: CreateNoticeRequest): NoticeWithAuthor {
        return noticeService.editArticle(id, request.title, request.content)
    }


    @GetMapping("")
    suspend fun getCategoryUnder(): List<NoticeWithAuthor> {
        return noticeService.listArticle().toList();
    }

    @GetMapping("/{id}")
    suspend fun getCategory(@PathVariable("id") id: String): NoticeWithAuthor {
        return noticeService.getArticle(id);
    }

}
