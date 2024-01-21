package org.example.storage.exposed.repository

import org.example.storage.exposed.models.Schedule
import java.time.LocalDateTime

interface ScheduleRepository: CrudRepository<Schedule> {

    /**
     * Find schedule of specific user by his object
     */
//    suspend fun getByUser(user: User): Schedule

    suspend fun findAllByDate(date: LocalDateTime): List<Schedule>
}