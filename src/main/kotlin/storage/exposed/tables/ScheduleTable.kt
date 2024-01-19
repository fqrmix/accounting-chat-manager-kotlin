package org.example.storage.exposed.tabels

import org.example.storage.exposed.repository.entities.UserEntity
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
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