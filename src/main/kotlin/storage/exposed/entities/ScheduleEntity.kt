package org.example.storage.exposed.repository.entities

import org.example.database.models.UserId
import org.example.storage.exposed.models.User
import org.example.storage.exposed.tabels.ScheduleTable
import org.example.storage.exposed.tabels.UserTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class ScheduleEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ScheduleEntity>(ScheduleTable)

    var startDateTime by ScheduleTable.startDateTime
    var endDateTime by ScheduleTable.endDateTime
    var user by UserEntity referencedOn UserTable.id
}
