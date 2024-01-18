package com.example.instagram.data.network_repo

import com.example.instagram.data.entity.LocationCache
import com.example.instagram.screen_createPost.domain.networkRepo.NetworkLocationRepo
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.tasks.await

class NetworkLocationRepoImpl(
    private val token: AutocompleteSessionToken,
    private val placesClient: PlacesClient,
) : NetworkLocationRepo {
    override suspend fun searchPlace(placeName: String): List<LocationCache> {
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
        return locationCache
    }
}