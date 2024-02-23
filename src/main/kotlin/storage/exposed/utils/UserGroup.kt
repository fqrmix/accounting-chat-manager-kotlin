package org.example.storage.exposed.utils


enum class UserGroup {
    CONNECTION { override fun toString(): String { return "Группа подключения" } },
    SUPPORT { override fun toString(): String { return "Группа сопровождения" } }
}

fun getUserGroupByName(groupName: String): UserGroup? {
    return UserGroup.entries.find { it.name == groupName.uppercase() }
}