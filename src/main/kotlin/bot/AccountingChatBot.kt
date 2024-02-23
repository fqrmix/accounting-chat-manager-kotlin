package org.example.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.document
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.logging.LogLevel
import kotlinx.coroutines.runBlocking
import org.example.bot.utils.ChatterMessage
import org.example.excel.ExcelDataProcessor
import org.example.excel.parser.ScheduleParser
import org.example.excel.utils.DefaultScheduleBuilderFactory
import org.example.excel.utils.ScheduleFile
import org.example.storage.exposed.models.Schedule
import org.example.storage.exposed.repository.impl.ScheduleRepositoryImpl
import org.example.storage.exposed.utils.DatabaseSingleton.suspendedTransaction
import java.time.LocalDateTime

class AccountingChatBot {

    private var userStates: MutableMap<ChatId, State> = mutableMapOf()

    companion object {
        private val TOKEN = System.getenv("TELEGRAM_TOKEN")
        private const val TIMEOUT_TIME = 30
        const val GROUP_CHAT_ID = -1002023309104
    }

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

                    val chatterMessage: ChatterMessage = ChatterMessage.Builder()
                        .setType(ChatterMessage.Type.TODAY_MESSAGE)
                        .setScheduleList(scheduleList)
                        .build()


                    bot.sendMessage(
                        chatId = ChatId.fromId(message.chat.id),
                        text = chatterMessage.getText(),
                        parseMode = chatterMessage.getParseMode()
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