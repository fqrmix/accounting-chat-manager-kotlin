package org.example.storage.exposed.repository

import org.example.storage.exposed.models.Schedule
import java.time.LocalDateTime

interface ScheduleRepository: CrudRepository<Schedule> {

    suspend fun findAllByDate(date: LocalDateTime): List<Schedule>

    suspend fun batchDelete(items: List<Schedule>)

    suspend fun batchCreate(items: List<Schedule>)
}