package com.example.instagram.database.model

data class Comment(
    val commentId: Long,
    val profilePicUrl: String,
    val profileId: Long,
    val username: String,
    val time: String,
    val comment: String,
)



