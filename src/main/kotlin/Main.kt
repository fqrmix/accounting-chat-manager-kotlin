package org.example

import org.example.bot.*
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