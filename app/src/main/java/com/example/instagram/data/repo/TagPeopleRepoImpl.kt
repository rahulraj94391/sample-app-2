package com.example.instagram.data.repo

import com.example.instagram.database.dao.TagPeopleDao
import com.example.instagram.database.entity.Tag
import com.example.instagram.domain.repo_contract.TagPeopleRepo

class TagPeopleRepoImpl(private val dao: TagPeopleDao) : TagPeopleRepo {
    override suspend fun insertPostTags(tag: MutableList<Tag>) {
        return dao.insertPostTags(tag)
    }
    
    override suspend fun getAllTaggedPostOfProfile(profileId: Long, limit: Int, offset: Int): MutableList<Long> {
        return dao.getAllTaggedPostOfProfile(profileId, limit, offset)
    }
}