package org.example.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.document
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.logging.LogLevel
import kotlinx.coroutines.runBlocking
import org.example.bot.MessageScheduler.createScheduledTask
import org.example.excel.ExcelDataProcessor
import org.example.excel.parser.ScheduleParser
import org.example.excel.utils.DefaultScheduleBuilderFactory
import org.example.excel.utils.ScheduleFile
import org.example.storage.exposed.models.Schedule
import org.example.storage.exposed.repository.impl.ScheduleRepositoryImpl
import org.example.storage.exposed.utils.DatabaseSingleton.suspendedTransaction
import org.example.storage.exposed.utils.UserGroup
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

    if (scheduleList.isNotEmpty()) {
        scheduleList.forEach {
            try {
                createScheduledTask(
                    RunnableTask {
                        this.sendMessage(
                            chatId = ChatId.fromId(-1002023309104),
                            text = "[${it.user.name}](tg://user?id=${it.user.telegramId}) ушел(-ла) на обед \uD83C\uDF7D",
                            parseMode = ParseMode.MARKDOWN
                        )
                    },
                    executionTime = LocalDateTime.of(
                        LocalDate.now(),
                        LocalTime.parse(it.user.lunchTime)
                    )
                )
            } catch (e: Exception) {
                println("Failed to create createLunchTasks: $e")
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
                    createScheduledTask(
                        RunnableTask {
                            this@createChattersTasks.sendMessage(
                                chatId = ChatId.fromId(-1002023309104),
                                text = "[${it.user.name}](tg://user?id=${it.user.telegramId}), заходи, пожалуйста, в чаты " +
                                        "\uD83D\uDCAC",
                                parseMode = ParseMode.MARKDOWN
                            )
                        },
                        executionTime = it.startDateTime
                    )
                } catch (e: Exception) {
                    println("Failed to create chattersTasks: $e")
                }

                try {
                    createScheduledTask(
                        RunnableTask {
                            this@createChattersTasks.sendMessage(
                                chatId = ChatId.fromId(-1002023309104),
                                text = "[${it.user.name}](tg://user?id=${it.user.telegramId}), выходи, пожалуйста, из чатов " +
                                        "\uD83C\uDFC3\uD83C\uDFC3\u200D♂\uFE0F\uD83C\uDFC3\u200D♂\uFE0F",
                                parseMode = ParseMode.MARKDOWN
                            )
                        },
                        executionTime = it.endDateTime
                    )
                } catch (e: Exception) {
                    println("Failed to create chattersTasks: $e")
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

    if (scheduleList.isNotEmpty()) {
        try {
            createScheduledTask(
                RunnableTask {
                    this.sendMessage(
                        chatId = ChatId.fromId(-1002023309104),
                        text = "Доброе утро \uD83C\uDF05\n\n" +
                                "Сегодня в чатах:\n" + scheduleList.joinToString (separator = "\n") {
                            "`${it.startDateTime.toLocalTime()} - ${it.endDateTime.toLocalTime()}` | " +
                                    "[${it.user.name}](tg://user?id=${it.user.telegramId})"
                        },
                        parseMode = ParseMode.MARKDOWN
                    ) },
                executionTime = executionDateTime
            )
        } catch (e: Exception) {
            println("Failed to create currentDayScheduleTasks: $e")
        }

    }
}


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
                        message.document?.let {
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
                            val schedulesToDelete = mutableListOf<Schedule>()

                            with(ScheduleRepositoryImpl.getInstance()) {
                                val databaseSchedules = runBlocking { findAll() }

                                databaseSchedules.forEach { databaseSchedule ->
                                    scheduleList.forEach { currentSchedule ->
                                        if (currentSchedule.startDateTime.toLocalDate() == databaseSchedule.startDateTime.toLocalDate()
                                            && currentSchedule.endDateTime.toLocalDate() == databaseSchedule.endDateTime.toLocalDate()
                                        ) {
                                            schedulesToDelete.add(databaseSchedule)
                                        }
                                    }
                                }

                                val databaseActions = mutableListOf<() -> Unit>()

                                if(schedulesToDelete.isNotEmpty()) {
                                    databaseActions += { runBlocking { batchDelete(schedulesToDelete.distinct()) } }
                                }

                                if (scheduleList.isNotEmpty()) {
                                    databaseActions += { runBlocking { batchCreate(scheduleList) } }
                                }

                                if (databaseActions.isNotEmpty()) {
                                    suspendedTransaction {
                                        databaseActions.forEach{ it() }
                                    }
                                }
                            }

                            bot.sendMessage(ChatId.fromId(message.chat.id),"График успешно загружен!")

                        } catch (e: Exception) {
                            bot.sendMessage(
                                ChatId.fromId(message.chat.id),
                                "При обработке графика произошла ошибка!\n\n${e.message}"
                            )
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
                        update.consume()
                    }

                }

                command("today") {
                    val scheduleList : List<Schedule>

                    with(ScheduleRepositoryImpl.getInstance()) {
                        runBlocking {
                            scheduleList = findAllByDate(LocalDateTime.now())
                        }
                    }

                    val mappedScheduleList = scheduleList.splitByGroup()

                    var messageText = "Сегодня в чатах:\n\n"

                    mappedScheduleList.forEach { (userGroup, scheduleList) ->
                        messageText += userGroup.toString() + "\n"
                        messageText += scheduleList.joinToString (separator = "\n") {
                            "`${it.startDateTime.toLocalTime()} - ${it.endDateTime.toLocalTime()}` | " +
                                    "[${it.user.name}](tg://user?id=${it.user.telegramId})"
                        }
                        messageText += "\n"
                    }

                    bot.sendMessage(
                        chatId = ChatId.fromId(message.chat.id),
                        text = messageText,
                        parseMode = ParseMode.MARKDOWN
                    )
                }

                command("init") {
                    try {
                        bot.sendMessage(
                            chatId = ChatId.fromId(message.chat.id),
                            text = message.javaClass.toString()
                        )

                        bot.sendMessage(
                            chatId = ChatId.fromId(message.chat.id),
                            text = message.chat.type.javaClass.toString()
                        )

                        bot.sendMessage(
                            chatId = ChatId.fromId(message.chat.id),
                            text = message.chat.id.toString()
                        )

                        bot.sendMessage(
                            chatId = ChatId.fromId(message.chat.id),
                            text = "Init was successfully done!"
                        )
                    } catch (e: Exception) {
                        println(e)
                    }

                }
            }
        }
    }

}