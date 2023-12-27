package com.example.instagram.data.repo

import com.example.instagram.data.dao.SearchDao
import com.example.instagram.screen_searchUser.model.UserSearchResult
import com.example.instagram.domain.repo_contract.SearchRepo

class SearchRepoImpl(private val dao: SearchDao) : SearchRepo {
    override suspend fun getSearchResult(name: String, ownID: Long): MutableList<UserSearchResult> {
        return dao.getSearchResult(name, ownID)
    }
}