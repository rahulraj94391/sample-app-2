package com.example.instagram.data.repo

import com.example.instagram.database.dao.LocationDao
import com.example.instagram.database.entity.Location
import com.example.instagram.domain.repo_contract.LocationRepo

class LocationRepoImpl(private val dao: LocationDao) : LocationRepo {
    
    override suspend fun insert(location: Location) {
        return dao.insert(location)
    }
    
    override suspend fun delete(location: Location): Int {
        return dao.delete(location)
    }
    
    override suspend fun getLocation(placeId: String): Location {
        return dao.getLocation(placeId)
    }
}