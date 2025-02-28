package org.example.excel.parser

import org.example.excel.utils.ScheduleFile
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.io.NameRepairStrategy
import org.jetbrains.kotlinx.dataframe.io.readExcel

class ScheduleParser : ExcelParser<ScheduleFile> {
    override fun parse(file: ScheduleFile): DataFrame<*> {
        return DataFrame.readExcel(
            file = file.getScheduleFile(),
            sheetName = "Sheet1",
            nameRepairStrategy = NameRepairStrategy.DO_NOTHING
        ).also {
            file.deleteTempFile()
        }
    }
}