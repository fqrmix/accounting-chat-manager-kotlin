package org.example.storage.exposed.models

import org.example.storage.exposed.utils.UserGroup

data class User(
    override val id: Long? = null,
    val name: String,
    val lunchTime: String,
    val groupName: UserGroup,
    val telegramId: Long? = null
) : BaseModel(id)

