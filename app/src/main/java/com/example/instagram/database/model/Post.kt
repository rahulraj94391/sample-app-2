package com.example.instagram.database.model

data class Post(
    var postId: Long,
    var profileId: Long,
    var profileImageUrl: String?,
    var profileUsername: String,
    var listOfPostPhotos: MutableList<String>,
    var isPostAlreadyLiked: Boolean,
    var isPostAlreadySaved: Boolean,
    var likeCount: String,
    var postDesc: String,
    var commentCount: String,
    var timeOfPost: String,
)
