package com.example.instagram.domain.repo_contract

import com.example.instagram.database.entity.HashTag

interface HashTagRepo {
    suspend fun inset(hashTags: MutableList<HashTag>): List<Long>
    suspend fun getHashTags(tag: String): MutableList<String>
    suspend fun getPostIds(hashTag: String, myId: Long, limit: Int, offset: Int): MutableList<Long>
    
}