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
    fun findAllByReqStudentAndStudentSumAndPendingAndApprovedAndReviewerAndReqTimeAndReqRoomAndReqDate(
        reqStudent: Optional<String>,
        studentSum: Optional<Int>,
        pending: Optional<Boolean>,
        approved: Optional<Boolean>,
        reviewer: Optional<String>,
        reqTime: Optional<Long>,
        reqRoom: Optional<Long>,
        reqDate: Optional<Long>
    ): Flow<ReservedSchedule>
    @Query("SELECT * FROM reserved_schedule WHERE req_time BETWEEN :start AND :end")
    fun findAllByReqTimeBetween(start: Long, end: Long): Flow<ReservedSchedule>
}
