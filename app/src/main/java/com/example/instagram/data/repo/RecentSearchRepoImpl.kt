package com.example.instagram.data.repo

import androidx.lifecycle.LiveData
import com.example.instagram.data.dao.RecentSearchDao
import com.example.instagram.data.entity.RecentSearch
import com.example.instagram.domain.repo_contract.RecentSearchRepo

class RecentSearchRepoImpl(private val dao: RecentSearchDao) : RecentSearchRepo {
    override suspend fun insertRecentSearch(search: RecentSearch): Long {
        return dao.insertRecentSearch(search)
    }
    
    override suspend fun deleteAllSearches() {
        dao.deleteAllSearches()
    }
    
    override fun getAllSearchedNames(ownerId: Long): LiveData<List<RecentSearch>> {
        return dao.getAllSearchedNames(ownerId)
    }
    
    override suspend fun deleteIfExist(profileId: Long, ownerId: Long) {
        return dao.deleteIfExist(profileId, ownerId)
    }
    
    override suspend fun insertAndDeleteIfExist(search: RecentSearch) {
        return dao.insertAndDeleteIfExist(search)
    }
}