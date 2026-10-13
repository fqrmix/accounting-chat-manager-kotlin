package org.example.web.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class LoginResponse(val token: String)

@Serializable
data class ErrorResponse(val error: String)

@Serializable
data class UserDto(
    val id: Long? = null,
    val name: String,
    val lunchTime: String,
    val groupName: String,
    val telegramId: Long? = null
)

@Serializable
data class ScheduleDto(
    val id: Long? = null,
    val startDateTime: String,
    val endDateTime: String,
    val userId: Long,
    val userName: String? = null
)

@Serializable
data class UploadResult(val createdCount: Int, val deletedCount: Int)
