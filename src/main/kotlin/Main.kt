package org.example

import org.example.bot.AccountingChatBot
import org.example.bot.MessageScheduler

class Main {
    fun run() {
        val bot = AccountingChatBot().build()
        MessageScheduler.init()
        bot.startPolling()



    }
}

fun main() {
    Main().run()
}