package io.seda.inventory.data

import io.r2dbc.postgresql.codec.Json
import kotlinx.coroutines.flow.Flow
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import reactor.core.publisher.Mono
import java.time.LocalDate

@Table(name = "reserved_date")
data class ReservedDate(
    @Id
    var id: Long? = null,
    var date: LocalDate,
    var available: Json
)

interface ReservedDateRepository: CoroutineCrudRepository<ReservedDate, Long> {
    fun findFirstByDate(date: LocalDate): Mono<ReservedDate>
    @Query("SELECT * FROM reserved_date WHERE date BETWEEN :start AND :end")
    fun findAllByDateBetween(start: LocalDate, end: LocalDate): Flow<ReservedDate>
}
