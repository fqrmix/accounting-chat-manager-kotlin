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

    private val logger = KotlinLogging.logger {}
    private val scheduleList: MutableList<Schedule> = mutableListOf()

    init {
        try {
            processExcel()
        } catch (e: Exception) {
            logger.error(e) {
                "Ошибка инициализации ExcelDataProcessor. file=$scheduleFile"
            }
            throw RuntimeException("Failed to initialize ExcelDataProcessor", e)
        }
    }

    fun getScheduleList(): MutableList<Schedule> {
        if (scheduleList.isEmpty()) {
            throw IllegalStateException(
                "Schedule list is empty. Excel file was not processed successfully."
            )
        }
        return scheduleList
    }

    private fun processExcel() {
        try {
            val parsedData = excelParser.parse(scheduleFile)
            build(parsedData)
        } catch (e: Exception) {
            logger.error(e) {
                "Ошибка обработки Excel файла. file=$scheduleFile"
            }
            throw RuntimeException("Failed to process Excel file", e)
        }
    }

    private fun build(dataFrame: AnyFrame) {
        for (columnName in dataFrame.columnNames()) {
            if (columnName.toDoubleOrNull() != null) {
                processDateColumn(dataFrame, columnName)
            }
        }
    }

    private fun processDateColumn(dataFrame: AnyFrame, dateColumn: String) {
        for (row in dataFrame.select { "Name" and "Lunch" and "Group" and dateColumn }.dropNulls()) {
            val cellValue = row[dateColumn].toString()
            if (cellValue.isBlank()) continue

            try {
                val user = buildUser(row)
                val timeList = buildTimeList(cellValue)
                buildUserScheduleList(user, dateColumn.toDouble(), timeList)
            } catch (e: Exception) {
                logger.error(e) {
                    """
                    Ошибка построения расписания пользователя.
                    dateColumn=$dateColumn
                    name=${row["Name"]}
                    group=${row["Group"]}
                    cellValue=$cellValue
                    """.trimIndent()
                }
                throw RuntimeException("Failed to build user schedule", e)
            }
        }
    }

    private fun buildUser(row: DataRow<Any?>): User {
        try {
            return runBlocking {
                with(UserRepositoryImpl.getInstance()) {
                    try {
                        val user = findByName(row["Name"].toString())
                        logger.info { "User ${user.name} already exists" }
                        user
                    } catch (_: NoSuchElementException) {
                        val user = scheduleBuilderFactory.createUser(
                            name = row["Name"].toString().trim(),
                            lunchTime = row["Lunch"].toString().trim(),
                            groupName = requireNotNull(
                                getUserGroupByName(row["Group"].toString().trim())
                            )
                        )
                        logger.info { "User ${user.name} was successfully created" }
                        user
                    }
                }
            }
        } catch (e: Exception) {
            logger.error(e) {
                """
                Ошибка создания/получения пользователя.
                name=${row["Name"]}
                lunch=${row["Lunch"]}
                group=${row["Group"]}
                """.trimIndent()
            }
            throw RuntimeException("Failed to create User", e)
        }
    }

    private fun buildTimeList(cellValue: String): List<TimeObject> {
        return try {
            val trimmed = cellValue.trim()
            val delimiter = trimmed.getDelimiter()

            if (delimiter != null && trimmed.length > 12) {
                trimmed
                    .split(delimiter)
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .map { scheduleBuilderFactory.createTimeObject(it) }
            } else {
                listOf(scheduleBuilderFactory.createTimeObject(trimmed))
            }
        } catch (e: Exception) {
            logger.error(e) {
                "Ошибка парсинга времени. value='$cellValue'"
            }
            throw RuntimeException("Failed to build time list", e)
        }
    }

    private fun String.getDelimiter(): String? =
        when {
            contains("\n") -> "\n"
            contains(" ") -> " "
            else -> null
        }

    private fun buildSchedule(
        user: User,
        excelDate: Double,
        timeObject: TimeObject
    ): Schedule {
        return try {
            val date = convertExcelValueToDate(excelDate)
            scheduleBuilderFactory.createSchedule(
                LocalDateTime.of(date, timeObject.getStartTime()),
                LocalDateTime.of(date, timeObject.getEndTime()),
                user
            )
        } catch (e: Exception) {
            logger.error(e) {
                """
                Ошибка создания Schedule.
                userId=${user.id}
                excelDate=$excelDate
                timeObject=$timeObject
                """.trimIndent()
            }
            throw RuntimeException("Failed to create Schedule", e)
        }
    }

    private fun buildUserScheduleList(
        user: User,
        date: Double,
        timeList: List<TimeObject>
    ) {
        timeList.forEach { time ->
            try {
                scheduleList.add(buildSchedule(user, date, time))
            } catch (e: Exception) {
                logger.error(e) {
                    "Ошибка добавления расписания в список. userId=${user.id}, date=$date"
                }
                throw RuntimeException("Failed to add schedule to list", e)
            }
        }
    }

    private fun convertExcelValueToDate(excelDate: Double): LocalDate {
        return try {
            LocalDate.parse("1900-01-01").plusDays(excelDate.toLong() - 2)
        } catch (e: Exception) {
            logger.error(e) {
                "Ошибка конвертации Excel даты. excelValue=$excelDate"
            }
            throw RuntimeException("Failed to convert Excel value to date", e)
        }
    }

    // -------- Builder --------

    class ExcelDataProcessorBuilder {
        private var excelParser: ScheduleParser? = null
        private var scheduleBuilderFactory: ScheduleBuilderFactory? = null
        private var scheduleFile: ScheduleFile? = null

        fun setExcelParser(excelParser: ScheduleParser) = apply {
            this.excelParser = excelParser
        }

        fun setScheduleBuilderFactory(factory: ScheduleBuilderFactory) = apply {
            this.scheduleBuilderFactory = factory
        }

        fun setScheduleFile(file: ScheduleFile) = apply {
            this.scheduleFile = file
        }

        fun build(): ExcelDataProcessor {
            requireNotNull(excelParser) { "ExcelParser must be set" }
            requireNotNull(scheduleBuilderFactory) { "ScheduleBuilderFactory must be set" }
            requireNotNull(scheduleFile) { "ScheduleFile must be set" }

            return ExcelDataProcessor(
                excelParser = excelParser!!,
                scheduleBuilderFactory = scheduleBuilderFactory!!,
                scheduleFile = scheduleFile!!
            )
        }
    }
}
