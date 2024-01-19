package org.example.storage.exposed.repository.impl

import org.example.storage.exposed.models.User
import org.example.storage.exposed.repository.UserRepository
import org.example.storage.exposed.entities.UserEntity
import org.example.storage.exposed.tabels.UserTable
import org.example.storage.exposed.utils.DatabaseSingleton.suspendedTransaction

open class UserRepositoryImpl: UserRepository {
    companion object {
        lateinit var response: User
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

    override suspend fun getByName(userName: String): User {
        suspendedTransaction {
            response = UserEntity.find { UserTable.name eq userName }
                .first()
                .toUser()
        }
        return response
    }

    override suspend fun delete(id: Int): Boolean {
        suspendedTransaction {
            UserEntity[id].let {
                it.delete()
                response = it.toUser()
            }
        }
        return false
    }

}