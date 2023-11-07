package com.example.instagram.database.model

import com.example.instagram.database.entity.Location

data class OnePost(
    val isPostAlreadyLiked: Boolean,
    val isPostAlreadySaved: Boolean,
    val profileName: String,
    val postText: String,
    val postTime: Long,
    val location: Location?,
)
