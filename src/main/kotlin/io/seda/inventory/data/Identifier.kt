package io.seda.inventory.data

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

@Table(name = "identifier")
data class Identifier(
    @Id
    var identifier: String,
    var metadata: String = ""
)

interface IdentifierRepository: CoroutineCrudRepository<Identifier, String> {
    suspend fun findByIdentifier(identifier: String): Identifier?;
    suspend fun findByMetadata(metadata: String): Identifier?;
}
