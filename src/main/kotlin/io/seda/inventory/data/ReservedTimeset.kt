package io.seda.inventory.data

import kotlinx.coroutines.flow.Flow
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

@Table(name = "reserved_time")
data class ReservedTimeset(
    @Id
    var id: Long? = null,
    var startHour: Int,
    var startMinute: Int,
    var endHour: Int,
    var endMinute: Int,
    var displayName: String
)

interface ReservedTimesetRepository: CoroutineCrudRepository<ReservedTimeset, Long> {
    fun findAllByDisplayName(displayName: String): ReservedTimeset?
}
