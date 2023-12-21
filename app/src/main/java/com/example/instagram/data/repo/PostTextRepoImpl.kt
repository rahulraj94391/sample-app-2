package com.example.instagram.data.repo

import com.example.instagram.database.dao.PostTextDao
import com.example.instagram.database.entity.PostText
import com.example.instagram.domain.repo_contract.PostTextRepo

class PostTextRepoImpl(private val dao: PostTextDao) : PostTextRepo {
    override suspend fun insertPostText(postText: PostText): Long {
        return dao.insertPostText(postText)
    }
    
    override suspend fun getPostText(postId: Long): String {
        return dao.getPostText(postId)
    }
}