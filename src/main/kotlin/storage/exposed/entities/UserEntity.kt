package org.example.storage.exposed.entities

import org.example.storage.exposed.tabels.UserTable
import org.example.storage.exposed.models.User
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

/**
 * Entity class representing row of `user` table
 */
class UserEntity(id: EntityID<Int>) : IntEntity(id) {

    //region Members (with mapping)
    /**
     * Object to be used for mapping
     */
    companion object : IntEntityClass<UserEntity>(UserTable)

    var name by UserTable.name
    var lunchTime by UserTable.lunchTime
    //endregion

    /**
     * Mapper function to map UserTable with User
     */
    fun toUser() : User {
        return User(
            id.value.toLong(),
            name = name,
            lunchTime = lunchTime
        )
    }
}