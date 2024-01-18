package com.example.instagram.screen_createPost.screen_locationTag

import com.example.instagram.data.entity.LocationCache

class GetLocationUseCase(
    private val locationManager: LocationManager,
) {
    suspend operator fun invoke(placeName: String): List<LocationCache> {
        return locationManager.getLocations(placeName)
    }
}