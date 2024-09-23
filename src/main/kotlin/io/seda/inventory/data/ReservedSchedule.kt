package io.seda.inventory.data

import kotlinx.coroutines.flow.Flow
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.Optional

@Table(name = "reserved_schedule")
data class ReservedSchedule(
    @Id
    var id: Long? = null,
    var reqStudent: String,
    var studentSum: Int,
    var pending: Boolean,
    var approved: Boolean,
    var reviewer: String?,
    var reqTime: Long,
    var reqRoom: Long,
    var timeset: List<Long>,
    var reqDate: Long,
)

interface ReservedScheduleRepository: CoroutineCrudRepository<ReservedSchedule, Long> {
    @Query("SELECT * FROM reserved_schedule WHERE (:reqStudent IS NULL OR req_student = :reqStudent) " +
            "AND (:studentSum IS NULL OR student_sum = :studentSum) " +
            "AND (:pending IS NULL OR pending = :pending) " +
            "AND (:approved IS NULL OR approved = :approved) " +
            "AND (:reviewer IS NULL OR reviewer = :reviewer) " +
            "AND (:reqTime IS NULL OR req_time = :reqTime) " +
            "AND (:reqRoom IS NULL OR req_room = :reqRoom) " +
            "AND (:reqDate IS NULL OR req_date = :reqDate)")
    fun findReservedSchedules(
        reqStudent: String?,
        studentSum: Int?,
        pending: Boolean?,
        approved: Boolean?,
        reviewer: String?,
        reqTime: Long?,
        reqRoom: Long?,
        reqDate: Long?
    ): Flow<ReservedSchedule>
    @Query("SELECT * FROM reserved_schedule WHERE req_time BETWEEN :start AND :end")
    fun findAllByReqTimeBetween(start: Long, end: Long): Flow<ReservedSchedule>
}
