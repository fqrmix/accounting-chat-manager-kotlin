package org.example

import io.github.oshai.kotlinlogging.KotlinLogging
import org.example.bot.AccountingChatBot
import org.example.bot.utils.createLunchTasks
import org.example.bot.utils.*
import org.example.storage.exposed.utils.DatabaseSingleton
import org.example.web.WebServer

class Main {
    companion object {
        val logger = KotlinLogging.logger {}
    }
    fun run() {
        DatabaseSingleton.init()
        MessageScheduler.init()

        try {
            val bot = AccountingChatBot().build()
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

        // Runs regardless of bot startup outcome, so the admin API stays
        // reachable even if the Telegram bot fails to start (e.g. bad token).
        WebServer.start(wait = true)
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