package com.example.instagram.database.model

import com.example.instagram.database.entity.Location

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
    var timeOfPost: String,
    var location: Location?,
)
