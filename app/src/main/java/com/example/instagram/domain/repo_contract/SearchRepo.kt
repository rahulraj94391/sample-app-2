package com.example.instagram.domain.repo_contract

import com.example.instagram.database.model.SearchResult

interface SearchRepo {
    suspend fun getSearchResult(name: String, ownID: Long): MutableList<SearchResult>
}