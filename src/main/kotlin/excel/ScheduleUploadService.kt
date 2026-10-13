package org.example.excel

import org.example.excel.parser.ScheduleParser
import org.example.excel.utils.DefaultScheduleBuilderFactory
import org.example.excel.utils.ScheduleFile
import org.example.storage.exposed.models.Schedule
import org.example.storage.exposed.repository.impl.ScheduleRepositoryImpl
import org.example.storage.exposed.utils.DatabaseSingleton.suspendedTransaction

data class ScheduleUploadResult(val createdCount: Int, val deletedCount: Int)

/**
 * Parses an uploaded schedule Excel file and replaces any existing schedule
 * entries that overlap by date with the newly parsed ones.
 *
 * Shared between the Telegram bot's `/load` flow and the admin HTTP API so
 * both entry points apply the exact same merge rules.
 */
object ScheduleUploadService {

    suspend fun replaceSchedulesFromExcel(fileBytes: ByteArray): ScheduleUploadResult {
        val scheduleList = ExcelDataProcessor.ExcelDataProcessorBuilder()
            .setExcelParser(ScheduleParser())
            .setScheduleBuilderFactory(DefaultScheduleBuilderFactory())
            .setScheduleFile(ScheduleFile(fileBytes))
            .build()
            .getScheduleList()

        val schedulesToDelete = mutableListOf<Schedule>()

        with(ScheduleRepositoryImpl.getInstance()) {
            val databaseSchedules = findAll()

            databaseSchedules.forEach { databaseSchedule ->
                scheduleList.forEach { currentSchedule ->
                    if (currentSchedule.startDateTime.toLocalDate() == databaseSchedule.startDateTime.toLocalDate()
                        && currentSchedule.endDateTime.toLocalDate() == databaseSchedule.endDateTime.toLocalDate()
                    ) {
                        schedulesToDelete.add(databaseSchedule)
                    }
                }
            }

            val distinctToDelete = schedulesToDelete.distinct()
            val databaseActions = mutableListOf<suspend () -> Unit>()

            if (distinctToDelete.isNotEmpty()) {
                databaseActions += { batchDelete(distinctToDelete) }
            }
            if (scheduleList.isNotEmpty()) {
                databaseActions += { batchCreate(scheduleList) }
            }

            if (databaseActions.isNotEmpty()) {
                suspendedTransaction {
                    databaseActions.forEach { it() }
                }
            }

            return ScheduleUploadResult(
                createdCount = scheduleList.size,
                deletedCount = distinctToDelete.size
            )
        }
    }
}
