package io.seda.inventory.data

import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

@Table(name = "verify_codes")
data class Identifier(
    var identifier: String,
    var metadata: String = ""
)

interface IdentifierRepository: CoroutineCrudRepository<Identifier, String> {
    suspend fun findByIdentifier(identifier: String): Identifier?;
    suspend fun findByMetadata(metadata: String): Identifier?;
}
