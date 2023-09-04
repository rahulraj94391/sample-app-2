package com.example.instagram.database.model

import androidx.room.ColumnInfo

data class CommentLog(
    val commenter_id: Long,
    val username: String,
    val post_id: Long,
    @ColumnInfo(name = "comment_time")
    val time: Long
)