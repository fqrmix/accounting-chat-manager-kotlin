package org.example.storage.exposed.repository.impl

import kotlinx.coroutines.runBlocking
import org.example.storage.exposed.entities.ScheduleEntity
import org.example.storage.exposed.entities.UserEntity
import org.example.storage.exposed.models.Schedule
import org.example.storage.exposed.repository.ScheduleRepository
import org.example.storage.exposed.tables.ScheduleTable
import org.example.storage.exposed.utils.DatabaseSingleton.suspendedTransaction
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.javatime.date
import java.time.LocalDateTime

class ScheduleRepositoryImpl private constructor(): ScheduleRepository {

    companion object {

        @Volatile
        private var instance: ScheduleRepositoryImpl? = null

        fun getInstance(): ScheduleRepositoryImpl {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = ScheduleRepositoryImpl()
                    }
                }
            }
            return instance!!
        }


        lateinit var response: Schedule

        suspend fun createTableIfNotExist() {
            suspendedTransaction {
                SchemaUtils.create(ScheduleTable)
            }
        }
    }

    init {
        runBlocking {
            createTableIfNotExist()
        }
    }

    override suspend fun findAllByDate(date: LocalDateTime): List<Schedule> {
        val result = mutableListOf<Schedule>()
        suspendedTransaction {
            ScheduleEntity.find { ScheduleTable.startDateTime.date() eq date.toLocalDate() }
                .all {
                    result.add(it.toSchedule())
                }
        }
        return result
    }

    override suspend fun create(item: Schedule): Schedule {
        suspendedTransaction {
            response = ScheduleEntity.new {
                this.user = UserEntity.findById(item.user.id!!.toInt())!!
                this.startDateTime = item.startDateTime
                this.endDateTime = item.endDateTime
            }.toSchedule()
        }

        return response
    }

    suspend fun batchCreate(items: List<Schedule>) {
        suspendedTransaction {
            items.forEach {
                ScheduleEntity.new {
                    this.user = UserEntity.findById(it.user.id!!.toInt())!!
                    this.startDateTime = it.startDateTime
                    this.endDateTime = it.endDateTime
                }
            }
        }
    }

    override suspend fun findAll(): List<Schedule>? {
        lateinit var result : List<Schedule>
        suspendedTransaction {
            result = ScheduleEntity.all().map { it.toSchedule() }
        }

        return result
    }

    override suspend fun findById(id: Int): Schedule? {
        suspendedTransaction {
            response = ScheduleEntity[id].toSchedule()
        }
        return response
    }

    override suspend fun update(item: Schedule): Schedule {
        suspendedTransaction {
            ScheduleEntity[item.id!!.toInt()].let {
                it.user = UserEntity.findById(item.user.id!!.toInt())!!
                it.startDateTime = item.startDateTime
                it.endDateTime = item.endDateTime
                response = it.toSchedule()
            }
        }

        return response
    }

    override suspend fun delete(id: Int): Schedule {
        suspendedTransaction {
            ScheduleEntity[id].let {
                it.delete()
                response = it.toSchedule()
            }
        }
        return response
    }

}