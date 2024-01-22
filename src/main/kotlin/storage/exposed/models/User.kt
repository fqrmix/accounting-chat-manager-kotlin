package org.example.storage.exposed.models

data class User(
    override val id: Long? = null,
    val name: String,
    val lunchTime: String,
    val telegramId: Long? = null
) : BaseModel(id)

