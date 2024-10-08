package io.seda.inventory.data

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

@Table(name = "identifier")
data class Identifier(
    @Id
    var id: Long? = null,
    var identifierCode: String,
    var metadata: String? = ""
)

interface IdentifierRepository: CoroutineCrudRepository<Identifier, String> {
    suspend fun findByIdentifierCode(identifierCode: String): Identifier?;
    suspend fun findByMetadata(metadata: String): Identifier?;
}
