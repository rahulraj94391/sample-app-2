package com.example.instagram.database.model

data class FollowList(
    val profile_id: Long,
    val first_name: String,
    val last_name: String,
    val username: String,
    var photoUrl: String? = null
)
