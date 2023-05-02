package io.seda.inventory.data

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

@Table("locations")
data class Location(
    @Id var id: Long? = null, var x: Int, var y: Int, var width: Int, var height: Int, var name: String, var parentId: Long?, var backgroundId: String?) // This will work for now

interface LocationRepository: CoroutineCrudRepository<Location, Long> {
    suspend fun existsByParentId(parentID: Long): Boolean;
}