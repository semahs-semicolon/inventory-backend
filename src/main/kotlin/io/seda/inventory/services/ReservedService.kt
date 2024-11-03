package io.seda.inventory.services

import io.r2dbc.postgresql.codec.Json
import io.seda.inventory.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.LocalDate

@Service
class ReservedService {
    @Autowired lateinit var reservedRoomRepository: ReservedRoomRepository
    @Autowired lateinit var reservedTimesetRepository: ReservedTimesetRepository
    @Autowired lateinit var reservedDateRepository: ReservedDateRepository
    @Autowired lateinit var reservedScheduleRepository: ReservedScheduleRepository

    suspend fun createSchedule(reqStudent: String, studentSum: Int, reqRoom: Long, timeset: List<Long>, reqDate: Long, purpose: String) {
        val schedule = ReservedSchedule(
            reqStudent = reqStudent,
            studentSum = studentSum,
            approved = false,
            reqTime = Instant.now().epochSecond,
            reqRoom = reqRoom,
            timeset = timeset,
            reviewer = null,
            pending = true,
            reqDate = reqDate,
            purpose = purpose
        )
        reservedScheduleRepository.save(schedule)
    }
    suspend fun revokeSchedule(id: Long) {
        reservedScheduleRepository.deleteById(id)
    }
    suspend fun updateSchedule(id: Long, reqStudent: String?, studentSum: Int?, timeset: List<Long>?, reqRoom: Long?, purpose: String?) {
        val schedule = reservedScheduleRepository.findById(id) ?: return
        schedule.reqStudent = reqStudent ?: schedule.reqStudent
        schedule.studentSum = studentSum ?: schedule.studentSum
        schedule.timeset = timeset ?: schedule.timeset
        schedule.reqRoom = reqRoom ?: schedule.reqRoom
        schedule.purpose = purpose ?: schedule.purpose
        reservedScheduleRepository.save(schedule)
    }
    suspend fun approveSchedule(id: Long, reviewer: String) {
        val schedule = reservedScheduleRepository.findById(id) ?: return
        schedule.approved = true
        schedule.pending = false
        schedule.reviewer = reviewer
        reservedScheduleRepository.save(schedule)
    }
    suspend fun rejectSchedule(id: Long, reviewer: String) {
        val schedule = reservedScheduleRepository.findById(id) ?: return
        schedule.approved = false
        schedule.pending = false
        schedule.reviewer = reviewer
        reservedScheduleRepository.save(schedule)
    }
    suspend fun createRoom(displayName: String, maxStudent: Int) {
        val room = ReservedRoom(
            displayName = displayName,
            maxStudent = maxStudent
        )
        reservedRoomRepository.save(room)
    }
    suspend fun deleteRoom(id: Long) {
        reservedRoomRepository.deleteById(id)
    }
    suspend fun updateRoom(id: Long, displayName: String?, maxStudent: Int?) {
        val room = reservedRoomRepository.findById(id) ?: return
        room.displayName = displayName ?: room.displayName
        room.maxStudent = maxStudent ?: room.maxStudent
        reservedRoomRepository.save(room)
    }
    suspend fun createTimeset(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int, displayName: String) {
        val timeset = ReservedTimeset(
            startHour = startHour,
            startMinute = startMinute,
            endHour = endHour,
            endMinute = endMinute,
            displayName = displayName
        )
        reservedTimesetRepository.save(timeset)
    }
    suspend fun deleteTimeset(id: Long) {
        reservedTimesetRepository.deleteById(id)
    }
    suspend fun updateTimeset(id: Long, startHour: Int?, startMinute: Int?, endHour: Int?, endMinute: Int?) {
        val timeset = reservedTimesetRepository.findById(id) ?: return
        timeset.startHour = startHour ?: timeset.startHour
        timeset.startMinute = startMinute ?: timeset.startMinute
        timeset.endHour = endHour ?: timeset.endHour
        timeset.endMinute = endMinute ?: timeset.endMinute
        reservedTimesetRepository.save(timeset)
    }
    suspend fun createDate(date: LocalDate, available: String?) {
        val reservedDate = ReservedDate(
            date = date,
            available = Json.of(available ?: "[]")
        )
        reservedDateRepository.save(reservedDate)
    }
    suspend fun deleteDate(id: Long) {
        reservedDateRepository.deleteById(id)
    }
    suspend fun updateDate(id: Long, date: LocalDate) {
        val reservedDate = reservedDateRepository.findById(id) ?: return
        if(reservedDateRepository.findOneByDate(date) != null) {
            throw Exception("Date already reserved")
        }
        reservedDate.date = date
        reservedDateRepository.save(reservedDate)
    }
    suspend fun updateDateAvailable(id: Long, available: String) {
        val reservedDate = reservedDateRepository.findById(id) ?: return
        reservedDate.available = Json.of(available)
        reservedDateRepository.save(reservedDate)
    }
    suspend fun getScheduleByQuery(
        reqStudent: String?,
        studentSum: Int?,
        pending: Boolean?,
        approved: Boolean?,
        reviewer: String?,
        reqTime: Long?,
        reqRoom: Long?,
        reqDate: Long?,
        purpose: String?
    ): List<ReservedSchedule> = reservedScheduleRepository.findReservedSchedules(reqStudent, studentSum, pending, approved, reviewer, reqTime, reqRoom, reqDate, purpose).toList()
    suspend fun getRoomByDisplayName(displayName: String): ReservedRoom? {
        return reservedRoomRepository.findAllByDisplayName(displayName)
    }
    suspend fun getDateByDate(date: LocalDate): Mono<ReservedDate> {
        return reservedDateRepository.findOneByDate(date)
    }
    suspend fun getDateBetween(start: LocalDate, end: LocalDate): Flow<ReservedDate> {
        return reservedDateRepository.findAllByDateBetween(start, end)
    }
    suspend fun getRoomById(id: Long): ReservedRoom? {
        return reservedRoomRepository.findById(id)
    }
    suspend fun getTimesetById(id: Long): ReservedTimeset? {
        return reservedTimesetRepository.findById(id)
    }
    suspend fun getDateById(id: Long): ReservedDate? {
        return reservedDateRepository.findById(id)
    }
    suspend fun getScheduleById(id: Long): ReservedSchedule? {
        return reservedScheduleRepository.findById(id)
    }
    suspend fun getAllRoom(): List<ReservedRoom> {
        return reservedRoomRepository.findAll().toList()
    }
    suspend fun getAllTimeset(): List<ReservedTimeset> {
        return reservedTimesetRepository.findAll().toList()
    }
    suspend fun getAllDate(): Flow<ReservedDate> {
        return reservedDateRepository.findAll()
    }
    suspend fun getAllSchedule(): List<ReservedSchedule> {
        return reservedScheduleRepository.findAll().toList()
    }
}
