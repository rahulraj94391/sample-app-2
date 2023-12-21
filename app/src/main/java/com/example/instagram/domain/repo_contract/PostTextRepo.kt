package com.example.instagram.domain.repo_contract

import com.example.instagram.database.entity.PostText

interface PostTextRepo {
    suspend fun insertPostText(postText: PostText): Long
    suspend fun getPostText(postId: Long): String
}