package org.example.excel.utils

import org.intellij.lang.annotations.Pattern
import java.time.LocalTime

class TimeObject(
    @Pattern("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]\$-^([0-1]?[0-9]|2[0-3]):[0-5][0-9]\$")
    private val time: String,
) {

    private val startTime = time.split("-")[0].trim()
    private val endTime = time.split("-")[1].trim()

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