package com.example.instagram.domain.repo_contract

import com.example.instagram.data.entity.Location

interface LocationRepo {
    suspend fun insert(location: Location)
    suspend fun delete(location: Location): Int
    suspend fun getLocation(placeId: String): Location
}