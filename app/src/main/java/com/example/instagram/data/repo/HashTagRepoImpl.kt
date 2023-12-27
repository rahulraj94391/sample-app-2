package com.example.instagram.data.repo

import com.example.instagram.data.dao.HashTagDao
import com.example.instagram.data.entity.HashTag
import com.example.instagram.domain.repo_contract.HashTagRepo

class HashTagRepoImpl(private val dao: HashTagDao) : HashTagRepo {
    override suspend fun inset(hashTags: MutableList<HashTag>): List<Long> {
        return dao.inset(hashTags)
    }
    
    override suspend fun getHashTags(tag: String): MutableList<String> {
        return dao.getHashTags(tag)
    }
    
    override suspend fun getPostIds(hashTag: String, myId: Long, limit: Int, offset: Int): MutableList<Long> {
        return dao.getPostIds(hashTag, myId, limit, offset)
    }
}