package com.example.instagram.screen_notification.model

import androidx.room.ColumnInfo

data class LikeLog(
    val profileId: Long,
    val username: String,
    val postId: Long,
    @ColumnInfo(name = "likeTime")
    val time: Long,
)
