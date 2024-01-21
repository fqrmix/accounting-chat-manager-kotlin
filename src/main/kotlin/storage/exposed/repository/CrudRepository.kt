package org.example.storage.exposed.repository


interface CrudRepository<T> {

    /**
     *
     */
    suspend fun create(item: T): T


    /**
     *
     */
    suspend fun findAll(): List<T>?

    /**
     *
     */
    suspend fun findById(id: Int): T?



    /**
     *
     */
    suspend fun update(item: T): T

    /**
     *
     */
    suspend fun delete(id: Int): T
}