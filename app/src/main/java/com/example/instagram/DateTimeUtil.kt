package com.example.instagram

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object DateTime {
    private const val datePattern = "d MMMM yyyy"
    private val locale = Locale("en", "IN")
    private val dayMonthYearFormat = SimpleDateFormat(datePattern, locale)
    
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
        
        if (TimeFormatting.POST == formatting && PostTimeNotation.JUST_NOW.timeNotation == notation)
            return notation
        else if (formatting == TimeFormatting.POST) {
            return "$timeToPrint $notation"
        }
        return "$timeToPrint$notation"
    }
    
    
    fun getChatMessageTime(milliseconds: Long): String {
        val format = SimpleDateFormat("hh:mm aa", locale)
        return format.format(Date(milliseconds))
    }
    
    fun getChatSeparatorTime(millis: Long): String {
        val currentDay = getTimeString(System.currentTimeMillis())
        val startingDayMillis = getMillis(currentDay)
        for (i in 0..6) {
            val startLimit = startingDayMillis - i * ONE_DAY
            val endLimit = startingDayMillis + (i + 1) * ONE_DAY
            if (millis in startLimit until endLimit) {
                return when (i) {
                    0 -> "Today"
                    1 -> "Yesterday"
                    else -> getTimeAsDay(millis)
                }
            }
        }
        return getTimeString(millis)
    }
    
    private fun getTimeAsDay(milliseconds: Long): String {
        val format = SimpleDateFormat("EEEE", locale)
        return format.format(Date(milliseconds))
    }
    
    private fun getTimeString(milliseconds: Long): String {
        return dayMonthYearFormat.format(Date(milliseconds))
    }
    
    private fun getMillis(dateString: String): Long {
        val date: Date = try {
            dayMonthYearFormat.parse(dateString)!!
        } catch (e: ParseException) {
            throw RuntimeException(e)
        }
        return date.time
    }
    
    fun isSameDay(time1: Long, time2: Long): Boolean {
        val date1 = Date(time1)
        val date2 = Date(time2)
        val formattedDate1 = dayMonthYearFormat.format(date1)
        val formattedDate2 = dayMonthYearFormat.format(date2)
        return formattedDate1 == formattedDate2
    }
}

enum class TimeFormatting {
    POST, COMMENT
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