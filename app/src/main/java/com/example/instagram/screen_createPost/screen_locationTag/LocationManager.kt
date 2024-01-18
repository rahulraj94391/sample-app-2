package com.example.instagram.screen_createPost.screen_locationTag

import com.example.instagram.data.entity.LocationCache
import com.example.instagram.domain.repo_contract.LocalLocationRepo
import com.example.instagram.screen_createPost.domain.networkRepo.NetworkLocationRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationManager(
    private val localLocationRepo: LocalLocationRepo,
    private val networkLocationRepo: NetworkLocationRepo,
) {
    suspend fun getLocations(placeName: String): List<LocationCache> {
        val locations = localLocationRepo.getLocations(placeName)
        return locations.ifEmpty {
            val locationNetwork = mutableListOf<LocationCache>()
            try {
                locationNetwork.addAll(networkLocationRepo.searchPlace(placeName))
            } catch (e: Exception) {
                println("class: LocationManager\n${e.localizedMessage}")
            }
            
            if (locationNetwork.isNotEmpty())
                CoroutineScope(Dispatchers.IO).launch {
                    localLocationRepo.insert(locationNetwork)
                }
            locationNetwork
        }
    }
}