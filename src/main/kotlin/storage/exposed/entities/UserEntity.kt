package org.example.storage.exposed.repository.entities

import org.example.storage.exposed.tabels.UserTable
import org.example.storage.exposed.models.User
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class UserEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserEntity>(UserTable)

    var name by UserTable.name
    var lunchTime by UserTable.lunchTime
}