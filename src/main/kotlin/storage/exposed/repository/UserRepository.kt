package org.example.storage.exposed.repository

import org.example.storage.exposed.models.User

interface UserRepository : CrudRepository<User> {

    /**
     * Find user by username
     */
    suspend fun findByName(userName: String): User

}