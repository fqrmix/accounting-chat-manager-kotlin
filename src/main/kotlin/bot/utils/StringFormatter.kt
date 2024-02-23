package org.example.bot.utils

enum class FormatAction {
    BOLD {
        override val actionChar: String
            get() = "*"
    },
    ITALIC {
        override val actionChar: String
            get() = "/"
    },
    MONOSPACE {
        override val actionChar: String
            get() = "`"
    };

    abstract val actionChar: String
}

fun String.formatTo(formatAction: FormatAction): String {
    return "${formatAction.actionChar}$this${formatAction.actionChar}"
}