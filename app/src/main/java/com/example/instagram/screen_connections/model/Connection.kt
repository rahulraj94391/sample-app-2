package com.example.instagram.screen_connections.model

data class Connection(
    val profileId: Long,
    val firstName: String,
    val lastName: String,
    val username: String,
    var photoUrl: String? = null
)
