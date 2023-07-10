package com.example.instagram.database.model

data class ProfileSummary(
    val profilePic: String,
    val f_name: String,
    val l_name: String,
    val bio: String,
    val postCount: Int,
    val followerCount: Int,
    val followingCount: Int,
)
