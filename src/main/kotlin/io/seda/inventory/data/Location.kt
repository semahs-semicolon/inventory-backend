package io.seda.inventory.data

import io.r2dbc.postgresql.codec.Json
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

@Table("locations2")
data class Location(
    @Id var id: Long? = null, var name: String, var parentId: Long?, var : Json) // This will work for now

interface LocationRepository: CoroutineCrudRepository<Location, Long> {
    suspend fun existsByParentId(parentID: Long): Boolean;
}