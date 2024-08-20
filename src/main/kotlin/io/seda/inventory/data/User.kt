package io.seda.inventory.data

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

@Table(name = "users")
data class User(
    @Id
    var id: Long? = null,
    val username: String,
    var password: String,
    var nickname: String,
    var authority: List<String> = listOf(),
    var identifier: String?,
)

interface UserRepository: CoroutineCrudRepository<User, Long> {
    suspend fun findByUsername(username: String): User?;
    suspend fun findByIdentifier(identifier: String): User?;
}
