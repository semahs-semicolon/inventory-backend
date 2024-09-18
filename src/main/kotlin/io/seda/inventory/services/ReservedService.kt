package io.seda.inventory.services

import io.r2dbc.postgresql.codec.Json
import io.seda.inventory.data.*
import kotlinx.coroutines.flow.toList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class ReservedService {
    @Autowired lateinit var reservedRoomRepository: ReservedRoomRepository
    @Autowired lateinit var reservedTimesetRepository: ReservedTimesetRepository
    @Autowired lateinit var reservedDateRepository: ReservedDateRepository
    @Autowired lateinit var reservedScheduleRepository: ReservedScheduleRepository

    suspend fun createSchedule(reqStudent: String, studentSum: Int, reqRoom: Long, timeset: List<Long>) {
        val schedule = ReservedSchedule(
            reqStudent = reqStudent,
            studentSum = studentSum,
            approved = false,
            reqTime = Instant.now().epochSecond,
            reqRoom = reqRoom,
            timeset = timeset,
            reviewer = null,
            pending = true
        )
        reservedScheduleRepository.save(schedule)
    }
    suspend fun revokeSchedule(id: Long) {
        reservedScheduleRepository.deleteById(id)
    }
    suspend fun updateSchedule(id: Long, reqStudent: String?, studentSum: Int?, reqRoom: Long?, timeset: List<Long>?) {
        val schedule = reservedScheduleRepository.findById(id) ?: return
        schedule.reqStudent = reqStudent ?: schedule.reqStudent
        schedule.studentSum = studentSum ?: schedule.studentSum
        schedule.reqRoom = reqRoom ?: schedule.reqRoom
        schedule.timeset = timeset ?: schedule.timeset
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
    suspend fun createDate(date: Long, available: List<Json>) {
        val reservedDate = ReservedDate(
            date = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            available = available
        )
        reservedDateRepository.save(reservedDate)
    }
    suspend fun deleteDate(id: Long) {
        reservedDateRepository.deleteById(id)
    }
    suspend fun updateDate(id: Long, date: Long) {
        val reservedDate = reservedDateRepository.findById(id) ?: return
        if(reservedDateRepository.findByDate(Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) != null) {
            throw Exception("Date already reserved")
        }
        reservedDate.date = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        reservedDateRepository.save(reservedDate)
    }
    suspend fun updateDateAvailable(id: Long, available: List<Json>) {
        val reservedDate = reservedDateRepository.findById(id) ?: return
        reservedDate.available = available
        reservedDateRepository.save(reservedDate)
    }
    suspend fun getScheduleByQuery(reqStudent: String?, studentSum: Int?, pending: Boolean?, approved: Boolean?, reviewer: String?, reqTime: Long?, reqRoom: Long?, timeset: List<Long>?): List<ReservedSchedule> {
        return reservedScheduleRepository.findAllByReqStudentAndStudentSumAndPendingAndApprovedAndReviewerAndReqTimeAndReqRoomAndTimeset(reqStudent, studentSum, pending, approved, reviewer, reqTime, reqRoom, timeset)?.toList() ?: listOf()
    }
    suspend fun getRoomByDisplayName(displayName: String): ReservedRoom? {
        return reservedRoomRepository.findAllByDisplayName(displayName)
    }
    suspend fun getTimesetByDisplayName(displayName: String): ReservedTimeset? {
        return reservedTimesetRepository.findAllByDisplayName(displayName)
    }
    suspend fun getDateByDate(date: String): ReservedDate? {
        return reservedDateRepository.findByDate(date)
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
    suspend fun getAllDate(): List<ReservedDate> {
        return reservedDateRepository.findAll().toList()
    }
    suspend fun getAllSchedule(): List<ReservedSchedule> {
        return reservedScheduleRepository.findAll().toList()
    }
}
