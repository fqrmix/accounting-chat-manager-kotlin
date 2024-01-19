package org.example.storage.exposed.utils

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction


object DatabaseSingleton {
    private lateinit var database: Database
    fun init() {
        database = Database.connect(
            "url",
            driver = "org.postgresql.Driver",
            user = "user",
            password = "password",
        )
    }


    suspend fun <T> suspendedTransaction(block: suspend () -> T): T {
        if (!::database.isInitialized) {
            throw RuntimeException("DatabaseSingleton object is not initialized. " +
                    "Call init() function once before database interaction")
        }

        return newSuspendedTransaction(Dispatchers.IO) {
            addLogger(StdOutSqlLogger)
            block()
        }
    }

}
