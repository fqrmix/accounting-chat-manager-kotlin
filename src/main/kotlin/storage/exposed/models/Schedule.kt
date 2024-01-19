package org.example.storage.exposed.models

import java.time.LocalDateTime

data class Schedule(
    override val id: Long? = null,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val user: User
) : BaseModel(id)