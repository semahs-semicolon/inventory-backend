package io.seda.inventory.data

import kotlinx.coroutines.flow.Flow
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.Instant


@Table(name = "notice")
data class Notice(
    @Id var id: Long? = null,
    var title: String,
    var author: Long,
    var content: String,
    var date: Instant = Instant.now(),
    var deleted: Boolean = false
)

data class NoticeWithAuthor(
    @Id var id: Long?,
    var title: String,
    var author: Long,
    var authorName: String,
    var content: String,
    var date: Instant
)

interface NoticeRepository: CoroutineCrudRepository<Notice, Long> {
    @Query("SELECT n.*, u.nickname as author_name FROM notice n LEFT JOIN users u ON u.id = n.author WHERE deleted = FALSE AND n.id = :id")
    suspend fun getArticleWithAuthor(id: Long): NoticeWithAuthor;

    @Query("SELECT n.*, u.nickname as author_name FROM notice n LEFT JOIN users u ON u.id = n.author WHERE deleted = FALSE ORDER BY date DESC")
    fun getArticlesWithAuthor(): Flow<NoticeWithAuthor>;
}