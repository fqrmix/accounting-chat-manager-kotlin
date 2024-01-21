package org.example.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.logging.LogLevel
import kotlinx.coroutines.runBlocking
import org.example.excel.ExcelDataProcessor
import org.example.excel.parser.ScheduleParser
import org.example.excel.utils.DefaultScheduleBuilderFactory
import org.example.excel.utils.ScheduleFile
import org.example.storage.exposed.models.Schedule
import org.example.storage.exposed.repository.impl.ScheduleRepositoryImpl
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

private val TOKEN = System.getenv("TELEGRAM_TOKEN")
private const val TIMEOUT_TIME = 30

fun Bot.createLunchTasks() {
    val scheduleList : List<Schedule>

    with(ScheduleRepositoryImpl.getInstance()) {
        runBlocking {
            scheduleList = findAllByDate(LocalDateTime.now())
        }
    }

    scheduleList.forEach {
        MessageScheduler.createScheduledTask(
            RunnableTask {
                this.sendMessage(
                    chatId = ChatId.fromId(966243980),
                    text = "${it.user.name} ушел на обед."
                )
            },
            executionTime = LocalDateTime.of(
                LocalDate.now(),
                LocalTime.parse(it.user.lunchTime)
            )
        )
    }
}

fun Bot.createChattersTasks() {
    val scheduleList : List<Schedule>

    with(ScheduleRepositoryImpl.getInstance()) {
        runBlocking {
            scheduleList = findAllByDate(LocalDateTime.now().plusDays(1).withYear(2023))
        }
    }
    scheduleList.forEach {
        with(MessageScheduler) {
            createScheduledTask(
                RunnableTask { this@createChattersTasks.sendMessage(
                    chatId = ChatId.fromId(966243980),
                    text = "Привет ${it.user.name}! Заходи в чаты"
                ) },
                executionTime = it.startDateTime
            )

            createScheduledTask(
                RunnableTask { this@createChattersTasks.sendMessage(
                    chatId = ChatId.fromId(966243980),
                    text = "Привет ${it.user.name}! Выходи из чатов"
                ) },
                executionTime = it.endDateTime
            )
        }
    }
}

fun Bot.createCurrentDayScheduleTasks() {

    val currentDateTime = LocalDateTime.now()
    val executionDateTime = currentDateTime
        .withHour(16)
        .withMinute(53)

    val scheduleList : List<Schedule>

    with(ScheduleRepositoryImpl.getInstance()) {
        runBlocking {
            scheduleList = findAllByDate(LocalDateTime.now())
        }
    }
    MessageScheduler.createScheduledTask(
        RunnableTask { this.sendMessage(
            chatId = ChatId.fromId(966243980),
            text = "Сегодня в чатах: + $scheduleList"
        ) },
        executionTime = executionDateTime
    )
}



class AccountingChatBot {

    private var userStates: MutableMap<ChatId, State> = mutableMapOf()

    enum class State {
        LOAD_PENDING
    }

    fun build(): Bot {
        return bot {
            token = TOKEN
            timeout = TIMEOUT_TIME
            logLevel = LogLevel.Network.Body
            dispatch {
                command("load") {
                    bot.sendMessage(ChatId.fromId(message.chat.id),"Пришли файл")
                    userStates[ChatId.fromId(message.chat.id)] = State.LOAD_PENDING
                    update.consume()
                }

                text {
                    if (userStates[ChatId.fromId(message.chat.id)] == State.LOAD_PENDING) {
                        bot.sendMessage(ChatId.fromId(message.chat.id),"Я жду файл!")
                    }
                }

                document {
                    if (userStates[ChatId.fromId(message.chat.id)] == State.LOAD_PENDING) {
                        message.document?.let { it ->
                        try {
                            val excelParser = ScheduleParser()
                            val scheduleBuilderFactory = DefaultScheduleBuilderFactory()
                            val scheduleFile = ScheduleFile(bot.downloadFileBytes(it.fileId)!!)

                            val excelDataProcessor = ExcelDataProcessor.ExcelDataProcessorBuilder()
                                .setExcelParser(excelParser)
                                .setScheduleBuilderFactory(scheduleBuilderFactory)
                                .setScheduleFile(scheduleFile)
                                .build()

                            val scheduleList = excelDataProcessor.getScheduleList()
                            ScheduleRepositoryImpl
                                .getInstance()
                                .batchCreate(scheduleList)

                            bot.sendMessage(ChatId.fromId(message.chat.id),"График успешно загружен!")

                        } catch (e: Exception) {
                            bot.sendMessage(ChatId.fromId(message.chat.id),"При обработке графика произошла ошибка!\n\n $e")
                            println(e)
                        }
                            userStates.remove(ChatId.fromId(message.chat.id))
                        }
                    }
                }

                command("cancel") {
                    if (userStates[ChatId.fromId(message.chat.id)] == State.LOAD_PENDING) {
                        bot.sendMessage(ChatId.fromId(message.chat.id),"Действие отменено")
                        userStates.remove(ChatId.fromId(message.chat.id))
                    }

                }

                command("today") {
                    with(ScheduleRepositoryImpl.getInstance()) {
                        findAllByDate(LocalDateTime.now()).forEach {
                            bot.sendMessage(
                                chatId = ChatId.fromId(message.chat.id),
                                text = "${it.user}. " +
                                        "Время: ${it.startDateTime.toLocalTime()}-${it.endDateTime.toLocalTime()}"
                            )
                        }
                    }
                }
            }
        }
    }

}