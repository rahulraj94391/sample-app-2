package com.example.instagram.data.local_repo

import com.example.instagram.data.dao.LocationCacheDao
import com.example.instagram.data.entity.LocationCache
import com.example.instagram.domain.repo_contract.LocalLocationRepo

class LocationCacheRepoImpl(private val dao: LocationCacheDao) : LocalLocationRepo {
    override suspend fun insert(location: List<LocationCache>) {
        dao.insert(location)
    }
    
    override suspend fun getLocations(keyword: String): List<LocationCache> {
        return dao.getLocation(keyword)
    }
}