package io.seda.inventory.services

import io.seda.inventory.auth.UserPrincipal
import io.seda.inventory.data.*
import io.seda.inventory.exceptions.NotFoundException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContext
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import kotlin.coroutines.coroutineContext

@Service
class NoticeService {

    @Autowired
    lateinit var noticeRepository: NoticeRepository;
    @Autowired
    lateinit var userRepository: UserRepository;

    fun Notice.toNoticeWithAuthor() :NoticeWithAuthor {
        return NoticeWithAuthor(id, title, author, "", content, date)
    }

    @PreAuthorize("hasAuthority('CREATE_NOTICE')")
    suspend fun createArticle(title: String, content: String): NoticeWithAuthor {
        val ctx = coroutineContext[ReactorContext.Key]?.context?.get<Mono<SecurityContext>>(SecurityContext::class.java)?.awaitSingle() ?: throw IllegalStateException("Not authenticated?");
        val user = ctx.authentication.principal as UserPrincipal;

        var notice = Notice(
            title = title,
            content =  content,
            author = user.id
        );

        notice = noticeRepository.save(notice);


        var entityUser = userRepository.findById(user.id) ?: throw IllegalStateException("You're not found");

        return notice.toNoticeWithAuthor().also { it.authorName = entityUser.nickname }
    }

    suspend fun editArticle(id: String, title: String, content: String): NoticeWithAuthor {
        var notice = noticeRepository.findById(id.toLong()) ?: throw NotFoundException("Notice not found")
        val ctx = coroutineContext[ReactorContext.Key]?.context?.get<Mono<SecurityContext>>(SecurityContext::class.java)?.awaitSingle() ?: throw IllegalStateException("Not authenticated?");
        val user = ctx.authentication.principal as UserPrincipal;

        if (notice.author != user.id) throw ResponseStatusException(HttpStatus.FORBIDDEN);

        notice.title = title
        notice.content = content
        notice = noticeRepository.save(notice)

        var entityUser = userRepository.findById(notice.author) ?: throw IllegalStateException("You're not found");

        return notice.toNoticeWithAuthor().also { it.authorName = entityUser.nickname }
    }

    suspend fun getArticle(id: String): NoticeWithAuthor {
        return noticeRepository.getArticleWithAuthor(id.toLong())
    }

    suspend fun deleteArticle(id: String) {
        var notice = noticeRepository.findById(id.toLong()) ?: throw NotFoundException("Notice not found")
        val ctx = coroutineContext[ReactorContext.Key]?.context?.get<Mono<SecurityContext>>(SecurityContext::class.java)?.awaitSingle() ?: throw IllegalStateException("Not authenticated?");
        val user = ctx.authentication.principal as UserPrincipal;

        if (notice.author != user.id && ctx.authentication.authorities.any { it.authority.equals("DELETE_NOTICE") }) throw ResponseStatusException(HttpStatus.FORBIDDEN);


        notice.deleted = true;
        notice = noticeRepository.save(notice)
    }

    suspend fun listArticle(): Flow<NoticeWithAuthor> {
        return noticeRepository.getArticlesWithAuthor().map { it.content = it.content.slice(0 until Math.min(100, it.content.length)); it }; // only 100 letters.
    }
}