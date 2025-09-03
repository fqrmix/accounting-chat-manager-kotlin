package org.example.excel

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.example.excel.parser.ScheduleParser
import org.example.excel.utils.ScheduleBuilderFactory
import org.example.excel.utils.ScheduleFile
import org.example.excel.utils.TimeObject
import org.example.storage.exposed.models.Schedule
import org.example.storage.exposed.models.User
import org.example.storage.exposed.repository.impl.UserRepositoryImpl
import org.example.storage.exposed.utils.getUserGroupByName
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.DataRow
import org.jetbrains.kotlinx.dataframe.api.dropNulls
import org.jetbrains.kotlinx.dataframe.api.select
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.NoSuchElementException

class ExcelDataProcessor private constructor(
    private val excelParser: ScheduleParser,
    private val scheduleBuilderFactory: ScheduleBuilderFactory,
    private val scheduleFile: ScheduleFile
) {

    private val scheduleList: MutableList<Schedule> = mutableListOf()
    private val logger = KotlinLogging.logger {}

    init {
        try {
            this.processExcel()
        } catch (e: Exception) {
            // Обработка ошибок при инициализации
            throw RuntimeException("Failed to initialize ExcelDataProcessor: ${e.message}")
        }
    }

    fun getScheduleList(): MutableList<Schedule> {
        if (scheduleList.isNotEmpty()) {
            return scheduleList
        } else {
            throw RuntimeException("You cannot get the schedule list without processing the Excel file. " +
                    "Run the processExcel() function first.")
        }
    }

    private fun processExcel() {
        try {
            val parsedData = excelParser.parse(scheduleFile)
            build(parsedData)
        } catch (e: Exception) {
            // Обработка ошибок при обработке Excel файла
            throw RuntimeException("Failed to process Excel file: ${e.message}")
        }
    }

    private fun build(dataFrame: AnyFrame) {
        for (date in dataFrame.columnNames()) {
            if (date.toDoubleOrNull() != null) {
                processDateColumn(dataFrame, date)
            }
        }
    }

    private fun processDateColumn(dataFrame: AnyFrame, date: String) {
        for (item in dataFrame.select { "Name" and "Lunch" and "Group" and date }.dropNulls()) {
            if (item[date].toString().isNotEmpty()) {
                try {
                    val user = buildUser(item)
                    val currentItemTimeList = buildTimeList(item[date].toString())
                    buildUserScheduleList(user, date.toDouble(), currentItemTimeList)
                } catch (e: Exception) {
                    // Обработка ошибок при построении пользовательского расписания
                    throw RuntimeException("Failed to build user schedule: ${e.message}")
                }
            }
        }
    }

    private fun buildUser(item: DataRow<Any?>): User {
        var user : User?
        try {
            runBlocking {
                with(UserRepositoryImpl.getInstance()){
                    try {
                        user = findByName(item["Name"].toString())
                        logger.info { "User $user already exist in database" }
                    } catch (e: NoSuchElementException) {
                        user = scheduleBuilderFactory.createUser(
                            item["Name"].toString().trim(),
                            item["Lunch"].toString().trim(),
                            requireNotNull(
                                getUserGroupByName(item["Group"].toString().trim())
                            )
                        )
                        logger.info { "User $user was successfully created!" }
                    }
                }
            }
            return user!!
        } catch (e: Exception) {
            // Обработка ошибок при создании объекта User
            throw RuntimeException("Failed to create User: ${e.stackTraceToString()}")
        }
    }

    private fun buildTimeList(dateValue: String): List<TimeObject> {
        return try {
            val trimmedDateValue = dateValue.trim()
            val delimiter = trimmedDateValue.getDelimeter()
            if (delimiter != null && dateValue.contains(delimiter) && dateValue.length > 12) {
                dateValue.split(delimiter).map { scheduleBuilderFactory.createTimeObject(it.trim()) }
            } else {
                listOf(scheduleBuilderFactory.createTimeObject(dateValue.trim()))
            }
        } catch (e: Exception) {
            // Обработка ошибок при создании списка TimeObject
            throw RuntimeException("Failed to build time list: ${e.message}")
        }
    }

    private fun String.getDelimeter(): String? {
        return when {
            this.contains("\n") -> "\n"
            this.contains(" ") -> " "
            else -> null
        }
    }

    private fun buildSchedule(user: User, date: Double, timeObject: TimeObject): Schedule {
        return try {
            scheduleBuilderFactory.createSchedule(
                LocalDateTime.of(convertExcelValueToDate(date), timeObject.getStartTime()),
                LocalDateTime.of(convertExcelValueToDate(date), timeObject.getEndTime()),
                user
            )
        } catch (e: Exception) {
            // Обработка ошибок при создании объекта Schedule
            throw RuntimeException("Failed to create Schedule: ${e.message}")
        }
    }

    private fun buildUserScheduleList(user: User, date: Double, timeList: List<TimeObject>) {
        timeList.forEach {
            try {
                scheduleList.add(buildSchedule(user, date, it))
            } catch (e: Exception) {
                // Обработка ошибок при добавлении расписания пользователя в список
                throw RuntimeException("Failed to add schedule to the list: ${e.message}")
            }
        }
    }

    private fun convertExcelValueToDate(excelDate: Double): LocalDate {
        return try {
            LocalDate.parse("1900-01-01").plusDays(excelDate.toLong() - 2)
        } catch (e: Exception) {
            // Обработка ошибок при преобразовании Excel даты в LocalDate
            throw RuntimeException("Failed to convert Excel value to date: ${e.message}")
        }
    }


    // Builder class
    class ExcelDataProcessorBuilder(
        private var excelParser: ScheduleParser? = null,
        private var scheduleBuilderFactory: ScheduleBuilderFactory? = null,
        private var scheduleFile: ScheduleFile? = null
    ) {
        fun setExcelParser(excelParser: ScheduleParser): ExcelDataProcessorBuilder {
            this.excelParser = excelParser
            return this
        }

        fun setScheduleBuilderFactory(scheduleBuilderFactory: ScheduleBuilderFactory): ExcelDataProcessorBuilder {
            this.scheduleBuilderFactory = scheduleBuilderFactory
            return this
        }

        fun setScheduleFile(scheduleFile: ScheduleFile): ExcelDataProcessorBuilder {
            this.scheduleFile = scheduleFile
            return this
        }

        fun build(): ExcelDataProcessor {
            requireNotNull(excelParser) { "ExcelParser must be set" }
            requireNotNull(scheduleBuilderFactory) { "ScheduleBuilderFactory must be set" }
            requireNotNull(scheduleFile) { "ScheduleFile must be set" }
            return ExcelDataProcessor(excelParser!!, scheduleBuilderFactory!!, scheduleFile!!)
        }
    }
}
