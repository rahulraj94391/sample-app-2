package com.example.instagram.screen_profile.model

data class ProfileSummary(
    val username: String,
    val profilePicUrl: String?,
    val firstName: String,
    val lastName: String,
    val bio: String,
    val postCount: Int,
    val followerCount: Int,
    val followingCount: Int,
    val isFollowing: Boolean
)