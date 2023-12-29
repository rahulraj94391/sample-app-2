package com.example.instagram.screen_notification.model

import androidx.room.ColumnInfo

data class CommentLog(
    val commentId: Long,
    val commenterId: Long,
    val username: String,
    val postId: Long,
    @ColumnInfo(name = "commentTime")
    val time: Long,
)