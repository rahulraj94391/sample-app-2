package com.example.instagram.database.model

data class Post(
    val postId: Long,
    val profileId: Long,
    val profileImageUrl: String?,
    val profileUsername: String,
    val listOfPostPhotos: MutableList<String>,
    val isPostAlreadyLiked: Boolean,
    val isPostAlreadySaved: Boolean,
    val likeCount: String,
    val postDesc: String,
    val commentCount: String,
    val timeOfPost: String,
)
