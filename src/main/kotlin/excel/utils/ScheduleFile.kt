package org.example.excel.utils

import java.io.File
import java.io.FileOutputStream

class ScheduleFile(private val byteArray: ByteArray) {

    private lateinit var tempFile: File

    private fun buildTempFile(): File {
        if (!::tempFile.isInitialized) {
            tempFile = File.createTempFile("tempSchedule", ".xlsx")
        }
        return tempFile
    }

    fun getScheduleFile(): File {
        buildTempFile()

        with(FileOutputStream(tempFile)) {
            this.write(byteArray)
            this.close()
        }

        return tempFile
    }

    fun deleteTempFile(): Boolean {
        if (!tempFile.delete()) {
            tempFile.deleteOnExit()
        }
        return tempFile.delete()
    }

}