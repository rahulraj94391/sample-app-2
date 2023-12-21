package com.example.instagram.data.repo

import com.example.instagram.database.dao.SearchDao
import com.example.instagram.database.model.SearchResult
import com.example.instagram.domain.repo_contract.SearchRepo

class SearchRepoImpl(private val dao: SearchDao) : SearchRepo {
    override suspend fun getSearchResult(name: String, ownID: Long): MutableList<SearchResult> {
        return dao.getSearchResult(name, ownID)
    }
}