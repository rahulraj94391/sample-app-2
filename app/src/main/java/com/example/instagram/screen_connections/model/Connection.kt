package com.example.instagram.screen_connections.model

data class Connection(
    val profile_id: Long,
    val first_name: String,
    val last_name: String,
    val username: String,
    var photoUrl: String? = null
)
