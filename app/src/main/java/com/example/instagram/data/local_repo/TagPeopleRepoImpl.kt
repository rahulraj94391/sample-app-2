package com.example.instagram.data.local_repo

import com.example.instagram.data.dao.TagPeopleDao
import com.example.instagram.data.entity.Tag
import com.example.instagram.domain.repo_contract.TagPeopleRepo

class TagPeopleRepoImpl(private val dao: TagPeopleDao) : TagPeopleRepo {
    override suspend fun insertPostTags(tag: MutableList<Tag>) {
        return dao.insertPostTags(tag)
    }
    
    override suspend fun getAllTaggedPostOfProfile(profileId: Long, limit: Int, offset: Int): MutableList<Long> {
        return dao.getAllTaggedPostOfProfile(profileId, limit, offset)
    }
}