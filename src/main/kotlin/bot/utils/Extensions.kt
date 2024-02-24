package org.example.bot.utils

import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.types.TelegramBotResult
import org.example.bot.AccountingChatBot.Companion.logger
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

fun <T> logSuccessOrError(vararg block: () -> TelegramBotResult<T>): TelegramBotResult<T> {
    lateinit var result: TelegramBotResult<T>

    block.forEach {
        result = it()

        result.fold(
            {
                it as Message
                logger.info { "Message was successfully send into chatID ${it.chat.id}. Message body: ${it.text.toString()}" }
            },
            {
                logger.error { "There was a error while sending a message. Reason: $it" }
            }
        )
    }

    return result
}