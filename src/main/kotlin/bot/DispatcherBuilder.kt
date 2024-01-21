package org.example.bot

import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.entities.ChatId

class DispatcherBuilder {
    private data class Command(
        val commandName: String,
        val handleCommand: suspend CommandHandlerEnvironment.() -> Unit,
    )

    private lateinit var commandList : List<Command>

    fun Dispatcher.setUpCommands() {
        command("start") {
            val chatId = ChatId.fromId(message.chat.id)
            bot.sendMessage(
                chatId = chatId,
                text = "TestBot"
            )
        }

        command("command1") {
            // TODO
        }

        command("command2") {
            // TODO
        }

        command("command3") {
            // TODO
        }

        command("command4") {
            // TODO
        }
    }

    private fun Dispatcher.setUpCallbacks() {
        callbackQuery(callbackData = "callbackData1") {
            // TODO
        }

        callbackQuery(callbackData = "callbackData2") {
            // TODO
        }

        callbackQuery(callbackData = "callbackData3") {
            // TODO
        }

        callbackQuery(callbackData = "callbackData4") {
            // TODO
        }
    }

    fun build() {

    }

}