package com.example.instagram.domain.repo_contract

import androidx.lifecycle.LiveData
import com.example.instagram.database.entity.RecentSearch

interface RecentSearchRepo {
    suspend fun insertRecentSearch(search: RecentSearch): Long
    suspend fun deleteAllSearches()
    fun getAllSearchedNames(ownerId: Long): LiveData<List<RecentSearch>>
    suspend fun deleteIfExist(profileId: Long, ownerId: Long)
    suspend fun insertAndDeleteIfExist(search: RecentSearch)
    
}