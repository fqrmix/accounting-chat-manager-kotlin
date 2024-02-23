package org.example.excel.utils

import kotlinx.coroutines.runBlocking
import org.example.storage.exposed.models.Schedule
import org.example.storage.exposed.models.User
import org.example.storage.exposed.repository.impl.UserRepositoryImpl
import org.example.storage.exposed.utils.UserGroup
import java.time.LocalDateTime

interface ScheduleBuilderFactory {
    fun createUser(name: String, lunchTime: String, groupName: UserGroup, telegramId: Long? = null): User
    fun createTimeObject(timeValue: String): TimeObject
    fun createSchedule(startDateTime: LocalDateTime, endDateTime: LocalDateTime, user: User): Schedule
}

class DefaultScheduleBuilderFactory : ScheduleBuilderFactory {
    override fun createUser(name: String, lunchTime: String, groupName: UserGroup, telegramId: Long?): User {
        var user: User?
        runBlocking {
            with(UserRepositoryImpl.getInstance()){
                user = create(
                    User(
                        name = name,
                        lunchTime = lunchTime.replace(".", ":"),
                        groupName = groupName,
                        telegramId = telegramId
                    )
                )
            }
        }
        return user!!
    }

    override fun createTimeObject(timeValue: String): TimeObject {
        return TimeObject(timeValue)
    }

    override fun createSchedule(startDateTime: LocalDateTime, endDateTime: LocalDateTime, user: User): Schedule {
        return Schedule(startDateTime = startDateTime, endDateTime = endDateTime, user = user)
    }
}