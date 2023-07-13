package com.example.instagram.database.model

data class OnePost(
    val isPostAlreadyLiked: Boolean,
    val isPostAlreadySaved: Boolean,
    val profileName: String,
    val postText: String,
    val postTime: Long
)
