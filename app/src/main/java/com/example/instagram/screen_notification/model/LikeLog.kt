package com.example.instagram.screen_notification.model

import androidx.room.ColumnInfo

data class LikeLog(
    val profile_id: Long,
    val username: String,
    val post_id: Long,
    @ColumnInfo(name = "like_time")
    val time: Long,
)
