package org.example.bot.utils

import org.example.storage.exposed.models.Schedule
import org.example.storage.exposed.utils.UserGroup

fun List<Schedule>.splitByGroup(): MutableMap<UserGroup, List<Schedule>> {
    val splittedScheduleList = mutableMapOf<UserGroup, List<Schedule>>()

    UserGroup.entries.forEach { userGroup ->

        val currentList = this.filter {
            it.user.groupName == userGroup
        }

        if (currentList.isNotEmpty()) {
            splittedScheduleList[userGroup] = currentList
        }

    }

    return splittedScheduleList
}