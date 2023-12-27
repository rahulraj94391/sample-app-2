package com.example.instagram.domain.repo_contract

import com.example.instagram.data.entity.Tag

interface TagPeopleRepo {
    suspend fun insertPostTags(tag: MutableList<Tag>)
    suspend fun getAllTaggedPostOfProfile(profileId: Long, limit: Int = 15, offset: Int): MutableList<Long>
}