package org.example

import org.example.bot.AccountingChatBot
import org.example.bot.utils.createLunchTasks
import org.example.bot.utils.*
import org.example.storage.exposed.utils.DatabaseSingleton

class Main {
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