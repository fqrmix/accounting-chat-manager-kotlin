package org.example.storage.exposed.repository.impl

import org.example.storage.exposed.models.Schedule
import org.example.storage.exposed.models.User
import org.example.storage.exposed.repository.ScheduleRepository

open class ScheduleRepositoryImpl : ScheduleRepository {
    override fun insert(schedule: Schedule): Schedule {
        TODO("Not yet implemented")
    }

    override fun update(schedule: Schedule): Schedule {
        TODO("Not yet implemented")
    }

    override fun getAll(limit: Int): List<Schedule> {
        TODO("Not yet implemented")
    }

    override fun getByUser(user: User): Schedule {
        TODO("Not yet implemented")
    }

    override fun delete(id: Long): Schedule {
        TODO("Not yet implemented")
    }
}