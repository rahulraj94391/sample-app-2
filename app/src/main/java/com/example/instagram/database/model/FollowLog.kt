package com.example.instagram.database.model


data class FollowLog(
    val owner_id: Long,
    val time: Long,
    val username: String,
)