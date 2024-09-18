package io.seda.inventory.controller

import io.r2dbc.postgresql.codec.Json
import io.seda.inventory.data.ReservedDate
import io.seda.inventory.data.ReservedSchedule
import io.seda.inventory.services.ReservedService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.time.LocalDate

@RestController
@RequestMapping("/reserved")
class ReservedController {
    @Autowired
    lateinit var reservedService: ReservedService

    @GetMapping("/room")
    suspend fun getRoomList() = reservedService.getAllRoom()

    @GetMapping("/room/{id}")
    suspend fun getRoomDetail(@PathVariable("id") id: Long) = reservedService.getRoomById(id)

    @PutMapping("/room")
    suspend fun createRoom(displayName: String, maxStudent: Int) = reservedService.createRoom(displayName, maxStudent)

    @DeleteMapping("/room/{id}")
    suspend fun deleteRoom(@PathVariable("id") id: Long) = reservedService.deleteRoom(id)

    data class RoomModifyRequest(val displayName: String, val maxStudent: Int)
    @PutMapping("/room/{id}")
    suspend fun modifyRoom(@PathVariable("id") id: Long, @RequestBody request: RoomModifyRequest) = reservedService.updateRoom(id, request.displayName, request.maxStudent)

    @GetMapping("/schedule")
    suspend fun getScheduleList(
        @RequestParam(value = "reqStudent", required = false) reqStudent: String?,
        @RequestParam(value = "studentSum", required = false) studentSum: Int?,
        @RequestParam(value = "pending", required = false) pending: Boolean?,
        @RequestParam(value = "approved", required = false) approved: Boolean?,
        @RequestParam(value = "reviewer", required = false) reviewer: String?,
        @RequestParam(value = "reqTime", required = false) reqTime: Long?,
        @RequestParam(value = "reqRoom", required = false) reqRoom: Long?
    ): List<ReservedSchedule> {
        return reservedService.getScheduleByQuery(reqStudent, studentSum, pending, approved, reviewer, reqTime, reqRoom)
    }

    @GetMapping("/schedule/{id}")
    suspend fun getScheduleDetail(@PathVariable("id") id: Long) = reservedService.getScheduleById(id)

    data class ScheduleRequest(val reqStudent: String, val studentSum: Int, val reqRoom: Long, val timeset: List<Long>)
    @PutMapping("/schedule")
    suspend fun createSchedule(@RequestBody request: ScheduleRequest) = reservedService.createSchedule(request.reqStudent, request.studentSum, request.reqRoom, request.timeset)

    @DeleteMapping("/schedule/{id}")
    suspend fun revokeSchedule(@PathVariable("id") id: Long) = reservedService.revokeSchedule(id)

    data class ReviewerRequest(val reviewer: String)
    @PostMapping("/schedule/{id}/approve")
    suspend fun approveSchedule(@PathVariable("id") id: Long, @RequestBody request: ReviewerRequest) = reservedService.approveSchedule(id, request.reviewer)

    @PostMapping("/schedule/{id}/reject")
    suspend fun rejectSchedule(@PathVariable("id") id: Long,  @RequestBody request: ReviewerRequest) = reservedService.rejectSchedule(id, request.reviewer)

    data class ScheduleModifyRequest(val reqStudent: String?, val studentSum: Int?, val reqRoom: Long?, val timeset: List<Long>?)
    @PutMapping("/schedule/{id}")
    suspend fun modifySchedule(@PathVariable("id") id: Long, @RequestBody request: ScheduleModifyRequest) = reservedService.updateSchedule(id, request.reqStudent, request.studentSum, request.reqRoom, request.timeset)

    @GetMapping("/timeset")
    suspend fun getTimesetList() = reservedService.getAllTimeset()

    @GetMapping("/timeset/{id}")
    suspend fun getTimesetDetail(@PathVariable("id") id: Long) = reservedService.getTimesetById(id)

    //startHour, startMinute, endHour, endMinute
    data class TimesetRequest(val startHour: Int, val startMinute: Int, val endHour: Int, val endMinute: Int, val displayName: String)
    @PutMapping("/timeset")
    suspend fun createTimeset(@RequestBody request: TimesetRequest) = reservedService.createTimeset(request.startHour, request.startMinute, request.endHour, request.endMinute, request.displayName)

    @DeleteMapping("/timeset/{id}")
    suspend fun deleteTimeset(@PathVariable("id") id: Long) = reservedService.deleteTimeset(id)

    data class TimesetModifyRequest(val startHour: Int?, val startMinute: Int?, val endHour: Int?, val endMinute: Int?)
    @PutMapping("/timeset/{id}")
    suspend fun modifyTimeset(@PathVariable("id") id: Long, @RequestBody request: TimesetModifyRequest) = reservedService.updateTimeset(id, request.startHour, request.startMinute, request.endHour, request.endMinute)

    @GetMapping("/date")
    suspend fun getReservedDateList() = reservedService.getAllDate()

    //dateTimestamp: yyyy-MM-dd
    @GetMapping("/date/date/{dateTimestamp}")
    suspend fun getReservedDateByDate(@PathVariable("dateTimestamp") dateTimestamp: String): Mono<ReservedDate> {
        return reservedService.getDateByDate(LocalDate.parse(dateTimestamp))
    }
    @GetMapping("/date/between/{start}/{end}")
    suspend fun getReservedDateBetween(@PathVariable("start") start: String, @PathVariable("end") end: String) = reservedService.getDateBetween(LocalDate.parse(start), LocalDate.parse(end))
    @GetMapping("/date/{id}")
    suspend fun getReservedDateDetail(@PathVariable("id") id: Long) = reservedService.getDateById(id)

    data class ReservedDateRequest(val date: String, val available: Json?)
    @PutMapping("/date")
    suspend fun createReservedDate(@RequestBody request: ReservedDateRequest) = reservedService.createDate(LocalDate.parse(request.date), request.available)

    data class ReservedDateModifyAvailableRequest(val available: Json)
    @PutMapping("/date/{id}")
    suspend fun modifyReservedDateAvailable(@PathVariable("id") id: Long, @RequestBody request: ReservedDateModifyAvailableRequest) = reservedService.updateDateAvailable(id, request.available)

    data class ReservedDateModifyRequest(val date: String)
    @PostMapping("/date/{id}")
    suspend fun modifyReservedDate(@PathVariable("id") id: Long, @RequestBody request: ReservedDateModifyRequest) = run {
        reservedService.updateDate(id, LocalDate.parse(request.date))
    }

    @DeleteMapping("/date/{id}")
    suspend fun deleteReservedDate(@PathVariable("id") id: Long) = reservedService.deleteDate(id)


}
