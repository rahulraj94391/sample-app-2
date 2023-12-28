package com.example.instagram.domain.repo_contract

import com.example.instagram.data.entity.LocationCache


interface LocationCacheRepo {
    suspend fun insert(location: List<LocationCache>)
    suspend fun getLocations(keyword: String): List<LocationCache>
}