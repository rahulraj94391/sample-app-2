package com.example.instagram.database.model

data class ProfileSummary(
    val username: String,
    val profilePic: String?,
    val first_name: String,
    val last_name: String,
    val bio: String,
    val postCount: Int,
    val followerCount: Int,
    val followingCount: Int,
    val isFollowing: Boolean
)