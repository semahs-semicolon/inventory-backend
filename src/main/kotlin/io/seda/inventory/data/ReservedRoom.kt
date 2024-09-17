package io.seda.inventory.data

import kotlinx.coroutines.flow.Flow
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.repository.Query
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
    @Query("SELECT * FROM reserved_room")
    fun findAllByAnything(): Flow<ReservedRoom>
    fun findAllByDisplayName(displayName: String): ReservedRoom?
}
