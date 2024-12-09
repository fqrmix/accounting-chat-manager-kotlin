package org.example.bot.utils

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import kotlinx.coroutines.runBlocking
import org.example.bot.AccountingChatBot
import org.example.bot.AccountingChatBot.Companion.GROUP_CHAT_ID
import org.example.storage.exposed.models.Schedule
import org.example.storage.exposed.repository.impl.ScheduleRepositoryImpl
import org.example.storage.exposed.utils.UserGroup
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

fun Bot.createLunchTasks() {
    val scheduleList : List<Schedule>

    with(ScheduleRepositoryImpl.getInstance()) {
        runBlocking {
            scheduleList = findAllByDate(LocalDateTime.now()).distinctBy { it.user }
        }
    }

    if (scheduleList.isNotEmpty()) {
        scheduleList.forEach {
            if (it.user.groupName == UserGroup.SUPPORT) {
                try {
                    val lunchMessage = ChatterMessage.Builder()
                        .setType(ChatterMessage.Type.OUT_FOR_LUNCH_MESSAGE)
                        .setUser(it.user)
                        .build()

                    MessageScheduler.createScheduledTask(
                        RunnableTask {
                            logSuccessOrError({
                                this.sendMessage(
                                    chatId = ChatId.fromId(GROUP_CHAT_ID),
                                    text = lunchMessage.getText(),
                                    parseMode = lunchMessage.getParseMode()
                                )
                            })
                        },
                        executionTime = LocalDateTime.of(
                            LocalDate.now(),
                            LocalTime.parse(it.user.lunchTime)
                        )
                    )
                } catch (e: Exception) {
                    AccountingChatBot.logger.atWarn {
                        message = "Failed to create createLunchTasks"
                        cause = e
                    }
                }
            }
        }
    }
}

fun Bot.createChattersTasks() {
    val scheduleList : List<Schedule>

    with(ScheduleRepositoryImpl.getInstance()) {
        runBlocking {
            scheduleList = findAllByDate(LocalDateTime.now())
        }
    }

    if (scheduleList.isNotEmpty()) {
        scheduleList.forEach {
            with(MessageScheduler) {
                try {
                    val chatterMessage = ChatterMessage.Builder()
                        .setType(ChatterMessage.Type.JOIN_TO_CHAT_MESSAGE)
                        .setUser(it.user)
                        .build()

                    createScheduledTask(
                        RunnableTask {
                            logSuccessOrError({
                                this@createChattersTasks.sendMessage(
                                    chatId = ChatId.fromId(GROUP_CHAT_ID),
                                    text = chatterMessage.getText(),
                                    parseMode = chatterMessage.getParseMode()
                                )
                            })
                        },
                        executionTime = it.startDateTime
                    )
                } catch (e: Exception) {
                    AccountingChatBot.logger.atWarn {
                        message = "Failed to create chattersTasks"
                        cause = e
                    }
                }

                try {
                    val chatterMessage = ChatterMessage.Builder()
                        .setType(ChatterMessage.Type.OUT_OF_CHAT_MESSAGE)
                        .setUser(it.user)
                        .build()

                    createScheduledTask(
                        RunnableTask {
                            logSuccessOrError({
                                this@createChattersTasks.sendMessage(
                                    chatId = ChatId.fromId(GROUP_CHAT_ID),
                                    text = chatterMessage.getText(),
                                    parseMode = chatterMessage.getParseMode()
                                )
                            })
                        },
                        executionTime = it.endDateTime
                    )
                } catch (e: Exception) {
                    AccountingChatBot.logger.atWarn {
                        message = "Failed to create chattersTasks"
                        cause = e
                    }
                }
            }
        }
    }

}

fun Bot.createCurrentDayScheduleTasks() {

    val executionDateTime = LocalDateTime.now()
        .withHour(8)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)

    val scheduleList : List<Schedule>

    with(ScheduleRepositoryImpl.getInstance()) {
        runBlocking {
            scheduleList = findAllByDate(LocalDateTime.now())
        }
    }

    val chatterMessage: ChatterMessage = ChatterMessage.Builder()
        .setType(ChatterMessage.Type.MORNING_MESSAGE)
        .setScheduleList(scheduleList)
        .build()

    if (scheduleList.isNotEmpty()) {
        try {
            MessageScheduler.createScheduledTask(
                RunnableTask {
                    logSuccessOrError({
                        this.sendMessage(
                            chatId = ChatId.fromId(GROUP_CHAT_ID),
                            text = chatterMessage.getText(),
                            parseMode = chatterMessage.getParseMode()
                        )
                    })
                },
                executionTime = executionDateTime
            )
        } catch (e: Exception) {
            AccountingChatBot.logger.atWarn {
                message = "Failed to create currentDayScheduleTasks"
                cause = e
            }
        }

    }
}