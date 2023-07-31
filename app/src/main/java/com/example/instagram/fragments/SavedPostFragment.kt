package com.example.instagram.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.instagram.MainViewModel
import com.example.instagram.R
import com.example.instagram.adapters.SavedPostAdapter
import com.example.instagram.databinding.FragmentSavedPostBinding
import com.example.instagram.viewmodels.SavedPostsViewModel
import kotlinx.coroutines.launch


private const val TAG = "CommTag_SavedPostFragment"

class SavedPostFragment : Fragment() {
    private lateinit var binding: FragmentSavedPostBinding
    private lateinit var viewModel: SavedPostsViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var savedPostAdapter: SavedPostAdapter
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel = ViewModelProvider(this)[SavedPostsViewModel::class.java]
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_saved_post, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedPostAdapter = SavedPostAdapter(this::onSavedPostClicked, this::onLongClick)
        binding.savedPostRV.adapter = savedPostAdapter
        
        lifecycleScope.launch {
            viewModel.getSavedPosts(mainViewModel.loggedInProfileId!!)
        }
        viewModel.listOfSavedPosts.observe(viewLifecycleOwner) {
            if (it.size == 0) {
                binding.loadingProgressBar.visibility = View.GONE
                binding.nothingHereIns.visibility = View.VISIBLE
            }
            savedPostAdapter.updateList(it)
            binding.loadingProgressBar.visibility = View.GONE
            binding.savedPostRV.visibility = View.VISIBLE
        }
    }
    
    override fun onResume() {
        super.onResume()
        val spanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            5
        } else {
            3
        }
        
        binding.savedPostRV.layoutManager = GridLayoutManager(requireContext(), spanCount)
    }
    
    private fun onSavedPostClicked(pos: Int) {
        val postId = viewModel.listOfSavedPosts.value!![pos].postId
        val action = SavedPostFragmentDirections.actionSavedPostFragmentToOnePostFragment(postId)
        findNavController().navigate(action)
    }
    
    private fun onLongClick(pos: Int) {
        Log.d(TAG, "onLongClick: long clicked at post id = ${viewModel.listOfSavedPosts.value!![pos].postId}")
    }
}