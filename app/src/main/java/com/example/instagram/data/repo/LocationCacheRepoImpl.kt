package com.example.instagram.data.repo

import com.example.instagram.data.dao.LocationCacheDao
import com.example.instagram.data.entity.LocationCache
import com.example.instagram.domain.repo_contract.LocationCacheRepo

class LocationCacheRepoImpl(private val dao: LocationCacheDao) : LocationCacheRepo {
    override suspend fun insert(location: List<LocationCache>) {
        dao.insert(location)
    }
    
    override suspend fun getLocations(keyword: String): List<LocationCache> {
        return dao.getLocation(keyword)
    }
}