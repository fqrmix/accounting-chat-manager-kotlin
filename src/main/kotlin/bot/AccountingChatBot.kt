package org.example.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch

import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.logging.LogLevel
import java.time.LocalDateTime

private const val TOKEN = "TOKEN"
private const val TIMEOUT_TIME = 30

class AccountingChatBot {

    //...
    private var _chatId: ChatId.Id? = null
    private val chatId by lazy { requireNotNull(_chatId) }

    fun build(): Bot {
        return bot {
            token = TOKEN
            timeout = TIMEOUT_TIME
            logLevel = LogLevel.Network.Body
            dispatch {
                text {
                    MessageScheduler.createScheduledTask(
                        MyRunnableTask { bot.sendMessage(ChatId.fromId(message.chat.id), text = "$text + 1") },
                        LocalDateTime.now().plusSeconds(10)
                    )

                    MessageScheduler.createScheduledTask(
                        MyRunnableTask { bot.sendMessage(ChatId.fromId(message.chat.id), text = "$text + 2") },
                        LocalDateTime.now().plusSeconds(20)
                    )

                    MessageScheduler.createScheduledTask(
                        MyRunnableTask { bot.sendMessage(ChatId.fromId(message.chat.id), text = "$text + 3") },
                        LocalDateTime.now().plusSeconds(30)
                    )

                }
            }
        }
    }



}