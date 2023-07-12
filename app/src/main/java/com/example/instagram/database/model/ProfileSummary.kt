package com.example.instagram.database.model

data class ProfileSummary(
    val profilePic: String? = null,
    val first_name: String,
    val last_name: String,
    val bio: String,
    val postCount: Int,
    val followerCount: Int,
    val followingCount: Int,
) {
    constructor(
        first_name: String,
        last_name: String,
        bio: String,
        postCount: Int,
        followerCount: Int,
        followingCount: Int,
    ) : this(
        null,
        first_name,
        last_name,
        bio,
        postCount,
        followerCount,
        followingCount
    )
}
