package org.example.web.dto

import org.example.storage.exposed.models.Schedule
import org.example.storage.exposed.models.User
import org.example.storage.exposed.utils.getUserGroupByName
import java.time.LocalDateTime

fun User.toDto() = UserDto(
    id = id,
    name = name,
    lunchTime = lunchTime,
    groupName = groupName.name,
    telegramId = telegramId
)

/**
 * Returns null when [UserDto.groupName] does not match a known [org.example.storage.exposed.utils.UserGroup].
 */
fun UserDto.toModel(idOverride: Long? = id): User? {
    val group = getUserGroupByName(groupName) ?: return null
    return User(
        id = idOverride,
        name = name,
        lunchTime = lunchTime,
        groupName = group,
        telegramId = telegramId
    )
}

fun Schedule.toDto() = ScheduleDto(
    id = id,
    startDateTime = startDateTime.toString(),
    endDateTime = endDateTime.toString(),
    userId = requireNotNull(user.id),
    userName = user.name
)

fun ScheduleDto.toModel(user: User, idOverride: Long? = id) = Schedule(
    id = idOverride,
    startDateTime = LocalDateTime.parse(startDateTime),
    endDateTime = LocalDateTime.parse(endDateTime),
    user = user
)
