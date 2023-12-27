package com.example.instagram.data.common_model

import com.example.instagram.data.entity.Location

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
