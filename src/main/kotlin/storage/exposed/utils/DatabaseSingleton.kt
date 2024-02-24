package org.example.storage.exposed.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.expandArgs
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction


object DatabaseSingleton {
    private lateinit var database: Database
    private val logger = KotlinLogging.logger {}

    fun init() {
        try {
            logger.info { "Trying to connect to Postgres database" }
            database = Database.connect(
                url = System.getenv("POSTGRES_URL"),
                driver = "org.postgresql.Driver",
                user = System.getenv("POSTGRES_USER"),
                password = System.getenv("POSTGRES_PASSWORD"),
            )
            logger.info { "Connection to Postgres database established. DBInfo: $database" }
        } catch (e: Exception) {
            logger.atWarn {
                message = "Connection to Postgres database failed"
                cause = e
            }
        }

    }


    suspend fun <T> suspendedTransaction(block: suspend () -> T): T {
        if (!::database.isInitialized) {
            throw RuntimeException("DatabaseSingleton object is not initialized. " +
                    "Call init() function once before database interaction")
        }

        return newSuspendedTransaction(Dispatchers.IO) {
            addLogger(DatabaseLogger)
            block()
        }
    }

}

object DatabaseLogger : SqlLogger {

    private val log = KotlinLogging.logger {}
    override fun log(context: StatementContext, transaction: Transaction) {
        log.info { context.expandArgs(transaction) }
    }
}
