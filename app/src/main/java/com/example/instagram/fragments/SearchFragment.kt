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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.MainViewModel
import com.example.instagram.R
import com.example.instagram.adapters.RecentSearchAdapter
import com.example.instagram.adapters.SearchUserAdapter
import com.example.instagram.database.AppDatabase
import com.example.instagram.databinding.FragmentSearchBinding
import com.example.instagram.viewmodels.SearchFragViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "CommTag_SearchFragment"

class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SearchFragViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var searchAdapter: SearchUserAdapter
    private var searchJob: Job? = null
    private lateinit var db: AppDatabase
    private lateinit var recentSearchAdapter: RecentSearchAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())
        viewModel = ViewModelProvider(requireActivity())[SearchFragViewModel::class.java]
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)
        return binding.root
    }
    
    override fun onDestroyView() {
        binding.searchRV.adapter = null
        _binding = null
        super.onDestroyView()
    }
    
    private fun onClearAllClicked() {
        viewModel.deleteAllFromRecent()
        recentSearchAdapter.clearList()
        binding.oldSearchesGroup.visibility = View.GONE
        binding.startSearchInstruction.visibility = View.VISIBLE
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recentSearchAdapter = RecentSearchAdapter { pos ->
            gotoProfileScreen(recentSearchAdapter.getProfileId(pos))
        }
        binding.oldSearches.adapter = recentSearchAdapter
        binding.oldSearches.layoutManager = LinearLayoutManager(requireContext())
        binding.clearAllBtn.setOnClickListener { onClearAllClicked() }
        
        searchAdapter = SearchUserAdapter(mutableListOf(), ::onClick, R.layout.row_user_search, mutableListOf())
        binding.searchRV.adapter = searchAdapter
        binding.searchRV.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        
        
        viewModel.searchLiveData.observe(viewLifecycleOwner) {
            searchAdapter.setNewList(it)
            if (binding.searchViewBar.query.isNotEmpty() && it.size > 0) {
                binding.noUsersFoundIns.visibility = View.GONE
                binding.startSearchInstruction.visibility = View.GONE
                binding.searchRV.visibility = View.VISIBLE
            } else if (binding.searchViewBar.query.isNotEmpty() && it.size == 0) { // when searched but no user is found
                Log.d(TAG, "No users found")
                binding.noUsersFoundIns.visibility = View.VISIBLE
            }
        }
        
        
        viewModel.imagesLiveData.observe(viewLifecycleOwner) {
            searchAdapter.setImagesList(it)
        }
        
        db.recentSearchDao().getAllSearchedNames(mainViewModel.loggedInProfileId!!).observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                return@observe
            }
            recentSearchAdapter.setNewList(it)
            binding.startSearchInstruction.visibility = View.GONE
            binding.oldSearchesGroup.visibility = View.VISIBLE
        }
        
        
        binding.searchViewBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.apply {
                    searchLiveData.value?.clear()
                    imagesLiveData.value?.clear()
                }
                if (newText!!.isBlank()) {
                    if (recentSearchAdapter.isEmpty()) {
                        
                        visibilityConfig3()
                    } else {
                        visibilityConfig2()
                    }
                } else {
                    searchJob?.cancel()
                    visibilityConfig1()
                    searchJob = lifecycleScope.launch {
                        delay(800)
                        viewModel.getSearchResults(newText)
                    }
                }
                return true
            }
        })
    }
    
    /**
     * Execute when "query.isBlank == false"
     */
    fun visibilityConfig1() {
        binding.searchRV.visibility = View.VISIBLE
        binding.startSearchInstruction.visibility = View.GONE
        binding.oldSearchesGroup.visibility = View.GONE
    }
    
    /**
     * Execute when "query.isBlank == true" and "recentSearchList.size == 0" == false
     */
    fun visibilityConfig2() {
        binding.noUsersFoundIns.visibility = View.GONE
        binding.searchRV.visibility = View.GONE
        binding.startSearchInstruction.visibility = View.GONE
        binding.oldSearchesGroup.visibility = View.VISIBLE
    }
    
    /**
     * Execute when "query.isBlank == true" and "recentSearchList.size == 0" == true
     */
    fun visibilityConfig3() {
        binding.searchRV.visibility = View.GONE
        binding.startSearchInstruction.visibility = View.VISIBLE
        binding.oldSearchesGroup.visibility = View.GONE
    }
    
    private fun onClick(pos: Int) {
        val person = viewModel.searchLiveData.value?.get(pos) ?: return
        searchAdapter.setNewList(mutableListOf())
        searchAdapter.setImagesList(mutableListOf())
        gotoProfileScreen(person.profile_id)
        viewModel.addNameToRecentSearch(person.profile_id, person.first_name, mainViewModel.loggedInProfileId!!)
        binding.searchViewBar.setQuery("", false)
        viewModel.apply {
            searchLiveData.value?.clear()
            imagesLiveData.value?.clear()
        }
        // java.lang.IllegalArgumentException: Navigation action/destination com.example.instagram:id/action_searchFragment_to_profileFragment cannot be found from the current destination Destination(com.example.instagram:id/onePostFragment) label=OnePostFragment class=com.example.instagram.fragments.OnePostFragment
    }
    
    
    private fun gotoProfileScreen(profileId: Long?) {
        val action = profileId?.let { SearchFragmentDirections.actionSearchFragmentToProfileFragment(it) }
        if (action != null) {
            findNavController().navigate(action)
        }
    }
}