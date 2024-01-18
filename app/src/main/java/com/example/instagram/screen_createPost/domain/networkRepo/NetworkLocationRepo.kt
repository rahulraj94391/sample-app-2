package com.example.instagram.screen_createPost.domain.networkRepo

import com.example.instagram.data.entity.LocationCache

interface NetworkLocationRepo {
    suspend fun searchPlace(placeName: String): List<LocationCache>
}