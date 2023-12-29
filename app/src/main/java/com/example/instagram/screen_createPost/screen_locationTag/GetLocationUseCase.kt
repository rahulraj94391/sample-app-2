package com.example.instagram.screen_createPost.screen_locationTag

import com.example.instagram.data.entity.Location
import com.example.instagram.data.entity.LocationCache
import com.example.instagram.domain.repo_contract.LocationCacheRepo
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GetLocationUseCase(private val locationCacheRepo: LocationCacheRepo) {
    
    suspend operator fun invoke(placeName: String, token: AutocompleteSessionToken, placesClient: PlacesClient): List<Location> {
        val locations = locationCacheRepo.getLocations(placeName)
        if (locations.isNotEmpty()) return locations.map { it.toLocation() }
        return searchPlace(
            placeName,
            token,
            placesClient
        )
    }
    
    private suspend fun searchPlace(placeName: String, token: AutocompleteSessionToken, placesClient: PlacesClient): List<Location> {
        val locationCache = mutableListOf<LocationCache>()
        val request =
            FindAutocompletePredictionsRequest
                .builder()
                .setSessionToken(token)
                .setQuery(placeName)
                .build()
        
        placesClient
            .findAutocompletePredictions(request)
            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                for (prediction in response.autocompletePredictions) {
                    prediction.apply {
                        locationCache.add(
                            LocationCache(
                                placeId,
                                getFullText(null).toString(),
                                getPrimaryText(null).toString(),
                                getSecondaryText(null).toString()
                            )
                        )
                    }
                }
            }.addOnFailureListener { exception: Exception? ->
                if (exception is ApiException) {
                    println(exception.message)
                }
            }.await()
        
        
        if (locationCache.isNotEmpty()) CoroutineScope(Dispatchers.IO).launch { locationCacheRepo.insert(locationCache) }
        return locationCache.map { it.toLocation() }
    }
}