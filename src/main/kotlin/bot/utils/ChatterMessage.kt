package org.example.bot.utils

import com.github.kotlintelegrambot.entities.ParseMode
import org.example.storage.exposed.models.Schedule
import org.example.storage.exposed.models.User

class ChatterMessage private constructor (
    type: Type,
    scheduleList: List<Schedule>? = null,
    user: User? = null
) {
    private var text: String = type.getTitle() + type.getSubtitle()
    private var parseMode: ParseMode = ParseMode.MARKDOWN

    init {
        text = when (type) {
            Type.MORNING_MESSAGE,
            Type.TODAY_MESSAGE -> {
                String.format(text, getChattersString(scheduleList!!))
            }

            Type.OUT_FOR_LUNCH_MESSAGE,
            Type.JOIN_TO_CHAT_MESSAGE,
            Type.OUT_OF_CHAT_MESSAGE -> {
                String.format(text, user!!.name, user.telegramId.toString())
            }
        }
    }

    fun getText(): String = text

    fun getParseMode(): ParseMode = parseMode

    private fun getChattersString(scheduleList: List<Schedule>): String {

        var resultString = ""

        if (scheduleList.isEmpty()) {
            resultString = "Список сотрудников пустой".formatTo(FormatAction.MONOSPACE)
        } else {
            val mappedScheduleList = scheduleList.splitByGroup()
            mappedScheduleList.forEach { (userGroup, scheduleList) ->
                resultString += "\n" + userGroup.toString().formatTo(FormatAction.BOLD) + "\n"
                resultString += scheduleList.joinToString (separator = "\n") {
                    (
                            "${it.startDateTime.toLocalTime()}" +
                                    " - " +
                                    "${it.endDateTime.toLocalTime()}"
                            ).formatTo(FormatAction.MONOSPACE) +
                            " | " +
                            "[${it.user.name}](tg://user?id=${it.user.telegramId})"
                }
                resultString += "\n"
            }
        }

        return resultString
    }

    enum class Type {

        MORNING_MESSAGE {
            override fun getTitle(): String {
                return "Доброе утро \uD83C\uDF05\n\n" +
                "Сегодня в чатах:".formatTo(FormatAction.BOLD) + "\n"
            }

            override fun getSubtitle(): String {
                return "%s"
            }
        },

        TODAY_MESSAGE {
            override fun getTitle(): String {
                return "Сегодня в чатах:\n".formatTo(FormatAction.BOLD)
            }

            override fun getSubtitle(): String {
                return "%s"
            }
        },

        JOIN_TO_CHAT_MESSAGE {
            override fun getTitle(): String {
                return "[%s](tg://user?id=%s), заходи, пожалуйста, в чаты".formatTo(FormatAction.BOLD)
            }

            override fun getSubtitle(): String {
                return "\uD83D\uDCAC"
            }
        },

        OUT_OF_CHAT_MESSAGE {
            override fun getTitle(): String {
                return "[%s](tg://user?id=%s), выходи, пожалуйста, из чатов".formatTo(FormatAction.BOLD)
            }

            override fun getSubtitle(): String {
                return "\uD83C\uDFC3\uD83C\uDFC3\u200D♂\uFE0F\uD83C\uDFC3\u200D♂\uFE0F"
            }
        },

        OUT_FOR_LUNCH_MESSAGE {
            override fun getTitle(): String {
                return "[%s](tg://user?id=%s) ушел(-ла) на обед".formatTo(FormatAction.BOLD)
            }

            override fun getSubtitle(): String {
                return "\uD83C\uDF7D"
            }
        };

        abstract fun getTitle(): String
        abstract fun getSubtitle(): String
    }

    internal class Builder (
        private var type: Type? = null,
        private var scheduleList: List<Schedule>? = null,
        private var user: User? = null
    ) {

        fun setType(type: Type): Builder {
            this.type = type
            return this
        }

        fun setScheduleList(scheduleList: List<Schedule>): Builder {
            this.scheduleList = scheduleList
            return this
        }

        fun setUser(user: User): Builder {
            this.user = user
            return this
        }

        fun build(): ChatterMessage {
            when (type) {
                Type.MORNING_MESSAGE,
                Type.TODAY_MESSAGE -> {
                    requireNotNull(scheduleList) { "Schedule list must be set for type $type" }
                    return ChatterMessage(type!!, scheduleList = scheduleList!!)
                }

                Type.JOIN_TO_CHAT_MESSAGE,
                Type.OUT_OF_CHAT_MESSAGE,
                Type.OUT_FOR_LUNCH_MESSAGE -> {
                    requireNotNull(user) { "Schedule list must be set for type $type" }
                    return ChatterMessage(type!!, user = user!!)
                }

                null -> throw IllegalArgumentException("Type of message must be set")

            }
        }
    }
}

