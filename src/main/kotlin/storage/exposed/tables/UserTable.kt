package org.example.storage.exposed.tables

import org.jetbrains.exposed.dao.id.IntIdTable

internal object UserTable : IntIdTable("users") {
    val name = varchar("name", 100)
    val lunchTime = varchar("lunch_time", 5)
}