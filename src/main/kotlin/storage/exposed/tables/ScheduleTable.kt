package org.example.storage.exposed.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime

internal object ScheduleTable : IntIdTable("schedule") {
    val startDateTime = datetime("start_date_time")
    val endDateTime = datetime("end_date_time")
    var user = reference(
        "user_id",
        UserTable,
        onUpdate = ReferenceOption.CASCADE,
        onDelete = ReferenceOption.CASCADE
    )
}