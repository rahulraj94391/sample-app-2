package com.example.instagram.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.BuildConfig
import com.example.instagram.HomeActivity
import com.example.instagram.R
import com.example.instagram.adapters.LocationsAdapter
import com.example.instagram.database.entity.Location
import com.example.instagram.databinding.FragmentLocationTagBinding
import com.example.instagram.viewmodels.PostFragViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "LocationTagFragment_CommTag"

class LocationTagFragment : Fragment() {
    private lateinit var binding: FragmentLocationTagBinding
    private lateinit var viewModel: PostFragViewModel
    private lateinit var locationAdapter: LocationsAdapter
    private var searchJob: Job? = null
    private lateinit var placesClient: PlacesClient
    private lateinit var token: AutocompleteSessionToken
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[PostFragViewModel::class.java]
        Places.initialize(requireContext(), BuildConfig.PLACES_API_KEY)
        placesClient = Places.createClient(requireContext())
        token = AutocompleteSessionToken.newInstance()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_location_tag, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationIcon(R.drawable.arrow_back_24)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        locationAdapter = LocationsAdapter(::onLocationClicked, viewModel.locations)
        
        binding.locationRV.apply {
            adapter = locationAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL))
        }
        
        binding.searchViewBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(placeName: String?): Boolean {
                Log.d(TAG, "Text Searched = $placeName")
                searchJob?.cancel()
                searchJob = lifecycleScope.launch(Dispatchers.IO) {
                    delay(500)
                    if (placeName?.isNotBlank() == true) {
                        searchPlace(placeName)
                    }
                }
                return true
            }
        })
        
    }
    
    
    override fun onResume() {
        super.onResume()
        binding.searchViewBar.requestFocus()
        (requireActivity() as HomeActivity).openKeyboard()
    }
    
    private fun searchPlace(placeName: String) {
        val request =
            FindAutocompletePredictionsRequest.builder()
                .setSessionToken(token)
                .setQuery(placeName)
                .build()
        
        placesClient
            .findAutocompletePredictions(request)
            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                viewModel.locations.clear()
                
                for (prediction in response.autocompletePredictions) {
                    prediction.apply {
                        viewModel.locations.add(Location(placeId, getPrimaryText(null).toString(), getSecondaryText(null).toString()))
                    }
                }
                locationAdapter.notifyDataSetChanged()
            }.addOnFailureListener { exception: Exception? ->
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: ${exception.statusCode}")
                }
            }
        
    }
    
    
    private fun onLocationClicked(pos: Int) {
        viewModel.apply {
            locationTag = locations[pos]
            findNavController().navigateUp()
        }
    }
    
}