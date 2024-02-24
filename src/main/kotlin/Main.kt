package org.example

import io.github.oshai.kotlinlogging.KotlinLogging
import org.example.bot.AccountingChatBot
import org.example.bot.utils.createLunchTasks
import org.example.bot.utils.*
import org.example.storage.exposed.utils.DatabaseSingleton

class Main {
    companion object {
        val logger = KotlinLogging.logger {}
    }
    fun run() {
        try {
            val bot = AccountingChatBot().build()

            MessageScheduler.init()
            DatabaseSingleton.init()

            with(bot) {
                startPolling()
                createCurrentDayScheduleTasks()
                createChattersTasks()
                createLunchTasks()
            }

        } catch (e: Exception) {
            logger.atError {
                message = "There was a exception while bot running"
                cause = e
            }
        }

    }
}

fun main() {
    try {
        Main().run()
    } catch (e: Exception) {
        Main.logger.atError {
            message = "There was a exception while bot running"
            cause = e
        }
    }

}