package org.example.storage.exposed.tables

import org.example.storage.exposed.tabels.UserTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime

internal object ScheduleTable : IntIdTable("schedule") {
    val startDateTime = datetime("start_date_time")
    val endDateTime = datetime("end_date_time")
    val user = reference(
        "user",
        UserTable,
        onUpdate = ReferenceOption.CASCADE,
        onDelete = ReferenceOption.CASCADE
    )
}