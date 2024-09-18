package io.seda.inventory.data

import io.r2dbc.postgresql.codec.Json
import kotlinx.coroutines.flow.Flow
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.LocalDate

@Table(name = "reserved_date")
data class ReservedDate(
    @Id
    var id: Long? = null,
    var date: String,
    var available: List<Json>
)

interface ReservedDateRepository: CoroutineCrudRepository<ReservedDate, Long> {
    fun findByDate(date: String): ReservedDate?
}
