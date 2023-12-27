package com.example.instagram.domain.repo_contract

import com.example.instagram.screen_searchUser.model.UserSearchResult

interface SearchRepo {
    suspend fun getSearchResult(name: String, ownID: Long): MutableList<UserSearchResult>
}