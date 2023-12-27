package com.example.instagram.screen_notification.model

import androidx.room.ColumnInfo

data class CommentLog(
    val comment_id: Long,
    val commenter_id: Long,
    val username: String,
    val post_id: Long,
    @ColumnInfo(name = "comment_time")
    val time: Long,
)