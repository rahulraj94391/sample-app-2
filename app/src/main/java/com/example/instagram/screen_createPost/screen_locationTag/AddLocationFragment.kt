package com.example.instagram.screen_createPost.screen_locationTag

import android.os.Bundle
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
import com.example.instagram.databinding.FragmentLocationTagBinding
import com.example.instagram.screen_createPost.CreatePostViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddLocationFragment : Fragment() {
    private lateinit var binding: FragmentLocationTagBinding
    private lateinit var viewModel: CreatePostViewModel
    private lateinit var locationAdapter: LocationSuggestionAdapter
    private var searchJob: Job? = null
    private lateinit var placesClient: PlacesClient
    private lateinit var token: AutocompleteSessionToken
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Places.initialize(requireContext(), BuildConfig.PLACES_API_KEY)
        placesClient = Places.createClient(requireContext())
        token = AutocompleteSessionToken.newInstance()
        viewModel = ViewModelProvider(requireActivity())[CreatePostViewModel::class.java]
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
        
        locationAdapter = LocationSuggestionAdapter(::onLocationClicked, viewModel.locations)
        
        binding.locationRV.apply {
            adapter = locationAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL))
        }
        
        binding.btnDone.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.searchViewBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(placeName: String?): Boolean {
                searchJob?.cancel()
                searchJob = lifecycleScope.launch(Dispatchers.IO) {
                    delay(500)
                    if (placeName?.isNotBlank() == true) {
                        val location = viewModel.getLocationUseCase(placeName, token, placesClient)
                        viewModel.locations.apply {
                            clear()
                            addAll(location)
                        }
                        withContext(Dispatchers.Main) {
                            locationAdapter.notifyDataSetChanged()
                        }
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
    
    private fun onLocationClicked(pos: Int) {
        viewModel.apply {
            locationTag = locations[pos]
            findNavController().navigateUp()
        }
    }
    
    override fun onDestroy() {
        Places.deinitialize()
        super.onDestroy()
    }
}