package org.example.storage.exposed.repository.impl

import kotlinx.coroutines.runBlocking
import org.example.storage.exposed.models.User
import org.example.storage.exposed.repository.UserRepository
import org.example.storage.exposed.entities.UserEntity
import org.example.storage.exposed.tables.UserTable
import org.example.storage.exposed.utils.DatabaseSingleton.suspendedTransaction
import org.jetbrains.exposed.sql.SchemaUtils

class UserRepositoryImpl private constructor(): UserRepository {
    companion object {

        @Volatile
        private var instance: UserRepositoryImpl? = null

        fun getInstance(): UserRepositoryImpl {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = UserRepositoryImpl()
                    }
                }
            }
            return instance!!
        }

        lateinit var response: User

        suspend fun createTableIfNotExist() {
            suspendedTransaction {
                SchemaUtils.create(UserTable)
            }
        }
    }

    init {
        runBlocking {
            createTableIfNotExist()
        }
    }

    override suspend fun create(item: User): User {
        suspendedTransaction {
            response = UserEntity.new {
                name = item.name
                lunchTime = item.lunchTime
            }.toUser()
        }

        return response
    }

    override suspend fun update(item: User): User {
        suspendedTransaction {
            UserEntity[item.id!!.toInt()].let {
                it.name = item.name
                it.lunchTime = item.lunchTime
                response = it.toUser()
            }
        }

        return response
    }

    override suspend fun findAll(): List<User> {
        lateinit var result : List<User>
        suspendedTransaction {
            result = UserEntity.all().map { it.toUser() }
        }

        return result
    }

    override suspend fun findById(id: Int): User? {
        suspendedTransaction {
            response = UserEntity[id].toUser()
        }
        return response
    }

    override suspend fun findByName(userName: String): User {
        suspendedTransaction {
            response = UserEntity.find { UserTable.name eq userName }
                .first()
                .toUser()
        }
        return response
    }

    override suspend fun delete(id: Int): User {
        suspendedTransaction {
            UserEntity[id].let {
                it.delete()
                response = it.toUser()
            }
        }
        return response
    }

}