package com.example.instagram.fragments

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.R
import com.example.instagram.adapters.SearchUserAdapter
import com.example.instagram.adapters.SearchUsernameClickListener
import com.example.instagram.databinding.FragmentSearchBinding
import com.example.instagram.viewmodels.SearchFragViewModel
import kotlinx.coroutines.launch

private const val TAG = "CommTag_SearchFragment"

class SearchFragment : Fragment(), SearchUsernameClickListener {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var searchFragViewModel: SearchFragViewModel
    private lateinit var searchAdapter: SearchUserAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchFragViewModel = ViewModelProvider(this)[SearchFragViewModel::class.java]
        val searchRV = binding.searchRV
        searchAdapter = SearchUserAdapter(mutableListOf(), this, R.layout.row_user_search, mutableListOf())
        searchRV.adapter = searchAdapter
        searchRV.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        searchFragViewModel.searchLiveData.observe(viewLifecycleOwner) {
            searchAdapter.setNewList(it)
            if (searchFragViewModel.searchLiveData.value?.size == 0) {
                binding.startSearchInstruction.visibility = View.VISIBLE
                binding.searchRV.visibility = View.GONE
            }
            else {
                binding.startSearchInstruction.visibility = View.GONE
                binding.searchRV.visibility = View.VISIBLE
            }
        }

        searchFragViewModel.imagesLiveData.observe(viewLifecycleOwner) {
            searchAdapter.setNewList2(it)
        }

        binding.searchViewBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                /*lifecycleScope.launch {
                    searchFragViewModel.getSearchResults(query!!)
                }*/
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                lifecycleScope.launch {
                    searchFragViewModel.getSearchResults(newText!!)
                }

                return true
            }
        })

    }

    override fun onClick(pos: Int) {
//        Toast.makeText(requireContext(), "search res - $pos clicked", Toast.LENGTH_SHORT).show()
        val profileId = searchFragViewModel.searchLiveData.value?.get(pos)?.profile_id!!
        val action = SearchFragmentDirections.actionSearchFragmentToProfileFragment(profileId)
        findNavController().navigate(action)
    }
}