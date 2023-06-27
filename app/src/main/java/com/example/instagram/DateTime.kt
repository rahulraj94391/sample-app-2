package com.example.instagram

object DateTime {
    private const val ONE_SEC = 1000
    private const val ONE_MIN = 60 * 1000
    private const val ONE_HOUR = 60 * 60 * 1000
    private const val ONE_DAY = 24 * 60 * 60 * 1000
    private const val ONE_WEEK = 7 * 24 * 60 * 60 * 1000

    fun timeFormatter(time: Long, formatting: TimeFormatting): String {
        val currentTime: Long = System.currentTimeMillis()
        val diffMillis: Long = currentTime - time
        var timeToPrint = 0L
        var notation = ""

        when (diffMillis) {
            in 0 until ONE_MIN -> {
                timeToPrint = diffMillis / ONE_SEC
                notation = if (formatting == TimeFormatting.POST) PostTimeNotation.JUST_NOW.timeNotation else CommentTimeNotation.JUST_NOW.timeNotation
            }

            in ONE_MIN until ONE_HOUR -> {
                timeToPrint = diffMillis / ONE_MIN
                notation = if (formatting == TimeFormatting.POST) PostTimeNotation.MIN.timeNotation else CommentTimeNotation.MIN.timeNotation
            }

            in ONE_HOUR until ONE_DAY -> {
                timeToPrint = diffMillis / ONE_HOUR
                notation = if (formatting == TimeFormatting.POST) PostTimeNotation.HOUR.timeNotation else CommentTimeNotation.HOUR.timeNotation
            }

            in ONE_DAY until ONE_WEEK -> {
                timeToPrint = diffMillis / ONE_DAY
                notation = if (formatting == TimeFormatting.POST) PostTimeNotation.DAY.timeNotation else CommentTimeNotation.DAY.timeNotation
            }

            else -> {
                timeToPrint = diffMillis / ONE_WEEK
                notation = if (formatting == TimeFormatting.POST) PostTimeNotation.WEEK.timeNotation else CommentTimeNotation.WEEK.timeNotation
            }
        }

        if (formatting == TimeFormatting.POST && PostTimeNotation.JUST_NOW.timeNotation == notation)
            return notation
        else if (formatting == TimeFormatting.POST) {
            return "$timeToPrint $notation"
        }
        return "$timeToPrint$notation"
    }
}

enum class TimeFormatting {
    POST,
    COMMENT
}

enum class PostTimeNotation(val timeNotation: String) {
    JUST_NOW("just now"),
    MIN("min ago"),
    HOUR("hour ago"),
    DAY("day ago"),
    WEEK("week ago")
}

enum class CommentTimeNotation(val timeNotation: String) {
    JUST_NOW("s"),
    MIN("m"),
    HOUR("h"),
    DAY("d"),
    WEEK("w")
}