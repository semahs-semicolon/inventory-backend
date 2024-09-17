package io.seda.inventory.data

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

@Table(name = "reserved_room")
data class ReservedRoom(
    @Id
    var id: Long? = null,
    var displayName: String,
    var maxStudent: Int,
)

interface ReservedRoomRepository: CoroutineCrudRepository<ReservedRoom, Long> {
    fun findAllByDisplayName(displayName: String): ReservedRoom?
}
