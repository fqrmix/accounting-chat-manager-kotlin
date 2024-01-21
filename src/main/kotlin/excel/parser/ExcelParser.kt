package org.example.excel.parser

import org.jetbrains.kotlinx.dataframe.AnyFrame


interface ExcelParser<T> {
    fun parse(file: T): AnyFrame
}

