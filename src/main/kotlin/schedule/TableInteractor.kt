package org.example.schedule

import org.example.bot.MessageScheduler
import org.example.bot.MessageScheduler.createScheduledTask
import org.example.bot.MyRunnableTask
import org.example.storage.exposed.models.Schedule
import org.example.storage.exposed.models.User
import org.example.storage.exposed.utils.DatabaseSingleton
import org.intellij.lang.annotations.Pattern
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.select
import org.jetbrains.kotlinx.dataframe.io.readExcel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime


class TableInteractor {

    fun openFile(path: String): AnyFrame {
        return DataFrame.readExcel(
            path,
            sheetName = "Sheet1"
        )
    }

    private fun convertExcelValueToDate(excelDate: Double) : LocalDate {
        return LocalDate
            .parse("1900-01-01")
            .plusDays(excelDate.toLong() - 2)
    }

    class TimeObject(
        @Pattern("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]\$-^([0-1]?[0-9]|2[0-3]):[0-5][0-9]\$")
        private val time: String,
    ) {

        private val startTime = time.split("-")[0]
        private val endTime = time.split("-")[1]

        private fun getLocalTime(
            @Pattern("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]\$")
            time: String,
        ): LocalTime? = LocalTime.of(
                time.split(":")[0].toInt(),
                time.split(":")[1].toInt()
            )

        fun getStartTime() = getLocalTime(startTime)
        fun getEndTime() = getLocalTime(endTime)

    }


    fun testBuildSchedule(dataFrame: AnyFrame) {
        for(date in dataFrame.columnNames()){
            if (date.toDoubleOrNull() != null)
                for (item in dataFrame.select{ "Name" and "Lunch" and date }) {
                    if (item[date] != "") {

                        val buildedUser = User(
                            name = item["Name"].toString(),
                            lunchTime = item["Lunch"].toString().replace(".", ":")
                        )

                        val currentItemTimeList = mutableListOf<TimeObject>()
                        if (item[date].toString().contains("\n")) {
                            item[date].toString().split("\n").forEach {
                                currentItemTimeList.add(TimeObject(it))
                            }
                        } else {
                            currentItemTimeList.add(
                                TimeObject(item[date].toString())
                            )
                        }

                        currentItemTimeList.forEach {
                            val buildedSchedule = Schedule(
                                startDateTime = LocalDateTime.of(
                                    convertExcelValueToDate(date.toDouble()),
                                    it.getStartTime()
                                ),
                                endDateTime = LocalDateTime.of(
                                    convertExcelValueToDate(date.toDouble()),
                                    it.getEndTime()
                                ),
                                user = buildedUser
                            )
                            println(buildedSchedule)
                        }

                    }

                }
        }
    }
}

fun main() {
    DatabaseSingleton.init()
    val tableInteractor = TableInteractor()

    tableInteractor.testBuildSchedule(
        tableInteractor.openFile("src/main/kotlin/schedule/schedule.xlsx")
    )

    MessageScheduler.init()

    createScheduledTask(
        MyRunnableTask { println("Test1") },
        LocalDateTime.now().plusSeconds(3)
    )

    createScheduledTask(
        MyRunnableTask { println("Test2") },
        LocalDateTime.now().plusSeconds(4)
    )

    createScheduledTask(
        MyRunnableTask { println("Test5") },
        LocalDateTime.now().plusSeconds(5)
    )

}