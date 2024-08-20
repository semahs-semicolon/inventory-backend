package io.seda.inventory.data

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.lang.Nullable

@Table(name = "identifier")
data class Identifier(
    @Id
    var id: Long? = null,
    var code: String,
    @Nullable
    var metadata: String = ""
)

interface IdentifierRepository: CoroutineCrudRepository<Identifier, String> {
    suspend fun findByCode(code: String): Identifier?;
    suspend fun findByMetadata(metadata: String): Identifier?;
}
