package org.example.storage.exposed.entities

import org.example.storage.exposed.models.Schedule
import org.example.storage.exposed.tables.ScheduleTable
import org.example.storage.exposed.tabels.UserTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID


/**
 * Entity class representing row of `schedule` table
 */
class ScheduleEntity(id: EntityID<Int>) : IntEntity(id) {

    //region Members (with mapping)
    /**
     * Object to be used for mapping
     */
    companion object : IntEntityClass<ScheduleEntity>(ScheduleTable)

    var startDateTime by ScheduleTable.startDateTime
    var endDateTime by ScheduleTable.endDateTime
    var user by UserEntity referencedOn UserTable.id
    //endregion

    /**
     * Mapper function to map ScheduleTable with Schedule
     */
    fun toSchedule() : Schedule {
        return Schedule(
            startDateTime = startDateTime,
            endDateTime = endDateTime,
            user = user.toUser()
        )
    }
}
