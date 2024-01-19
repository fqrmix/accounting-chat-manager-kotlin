package org.example.storage.exposed.tabels

import org.jetbrains.exposed.dao.id.IntIdTable

internal object UserTable : IntIdTable("users") {
    val name = varchar("name", 100)
    val lunchTime = varchar("lunch_time", 5)
}