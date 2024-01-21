package org.example

import org.example.bot.AccountingChatBot
import org.example.bot.MessageScheduler
import org.example.bot.createLunchTasks
import org.example.bot.createChattersTasks
import org.example.bot.createCurrentDayScheduleTasks
import org.example.storage.exposed.utils.DatabaseSingleton

class Main {
    fun run() {
        try {
            val bot = AccountingChatBot().build()

            MessageScheduler.init()
            DatabaseSingleton.init()

            with(bot) {
                createCurrentDayScheduleTasks()
                createChattersTasks()
                createLunchTasks()
                startPolling()
            }

        } catch (e: Exception) {
            println(e)
        }

    }
}

fun main() {
    try {
        Main().run()
    } catch (e: Exception) {
        println(e)
    }

}