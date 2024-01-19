package org.example.storage.exposed.repository

import org.example.storage.exposed.models.Schedule
import org.example.storage.exposed.models.User

interface ScheduleRepository: CrudRepository<Schedule> {

    /**
     * Find schedule of specific user by his object
     */
    fun getByUser(user: User): Schedule
}