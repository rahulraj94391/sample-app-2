package com.example.instagram.screen_singlePostView.model

import com.example.instagram.data.entity.Location

data class Post(
    val isPostAlreadyLiked: Boolean,
    val isPostAlreadySaved: Boolean,
    val profileName: String,
    val postText: String,
    val postTime: Long,
    val location: Location?,
)
