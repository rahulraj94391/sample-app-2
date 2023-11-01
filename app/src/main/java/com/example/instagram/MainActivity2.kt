package com.example.instagram

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.instagram.databinding.ActivityMain2Binding
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import kotlinx.coroutines.launch

private const val TAG = "MainActivity2_CommTag"

class MainActivity2 : AppCompatActivity() {
    private lateinit var binding: ActivityMain2Binding
    
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main2)
        val token = AutocompleteSessionToken.newInstance()
        
        Places.initialize(this, BuildConfig.PLACES_API_KEY)
        val placesClient = Places.createClient(this)
        
        val request =
            FindAutocompletePredictionsRequest.builder()
                .setSessionToken(token)
                .setQuery("Cevizli")
                .build()
        
        lifecycleScope.launch {
            //delay(10000)
            placesClient
                .findAutocompletePredictions(request)
                .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                    for (prediction in response.autocompletePredictions) {
                        Log.i(TAG, "Full -> " + prediction.getFullText(null).toString())
                        Log.i(TAG, "primary -> " + prediction.getPrimaryText(null).toString())
                        Log.i(TAG, "secondary -> " + prediction.getSecondaryText(null).toString())
                        
                    }
                }.addOnFailureListener { exception: Exception? ->
                    if (exception is ApiException) {
                        Log.e(TAG, "Place not found: ${exception.statusCode}")
                    }
                }
        }
    }
}
