package org.example.storage.exposed.utils

enum class UserGroup {
    CONNECTION { override fun toString(): String { return "Группа подключения" } },
    SUPPORT { override fun toString(): String { return "Группа сопровождения" } },
    UNKNOWN { override fun toString(): String { return "Группа неизвестная" } }
}

fun getUserGroupByName(groupName: String): UserGroup {
    return when(groupName) {
        "connection" -> UserGroup.CONNECTION
        "support" -> UserGroup.SUPPORT
        else -> {UserGroup.UNKNOWN}
    }
}