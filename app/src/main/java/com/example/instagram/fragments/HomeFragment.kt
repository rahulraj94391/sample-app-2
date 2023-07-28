package com.example.instagram.fragments

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.MainViewModel
import com.example.instagram.R
import com.example.instagram.adapters.HomeAdapter
import com.example.instagram.database.AppDatabase
import com.example.instagram.databinding.FragmentHomeBinding
import com.example.instagram.viewModelFactory.ViewModelFactory
import com.example.instagram.viewmodels.HomeFragViewModel
import com.google.android.material.checkbox.MaterialCheckBox
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "CommTag_HomeFragment"

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var homeAdapter: HomeAdapter
    private lateinit var viewModel: HomeFragViewModel
    private lateinit var mainViewModel: MainViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        val currentUser = mainViewModel.loggedInProfileId!!
        val db = AppDatabase.getDatabase(requireContext())
        viewModel = ViewModelProvider(this, ViewModelFactory(currentUser, db))[HomeFragViewModel::class.java]
        viewModel.addNewPostToList(mainViewModel.loggedInProfileId!!)
    }
    
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        homeAdapter = HomeAdapter(::openCommentBottomSheet, ::openProfile, ::onLikeClicked, ::onSavePostClicked)
        binding.btnMessages.setOnClickListener { whenMessagesBtnClicked() }
        binding.btnNotifications.setOnClickListener { whenNotificationBtnClicked() }
        
        binding.homeRV.adapter = homeAdapter
        binding.homeRV.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        
        viewModel.postsToShow.observe(viewLifecycleOwner) {
            binding.loadingProgressBar.visibility = View.GONE
            binding.homeRV.visibility = View.VISIBLE
            homeAdapter.addNewPosts(it)
        }
    }
    
    private fun openProfile(pos: Int) {
        val profileId: Long = homeAdapter.getProfileId(pos)
        val action = HomeFragmentDirections.actionHomeFragmentToProfileFragment(profileId)
        findNavController().navigate(action)
    }
    
    private fun onLikeClicked(pos: Int, view: View) {
        val checkedState = (view as MaterialCheckBox).checkedState
        val postId = homeAdapter.getPostId(pos)
        val newState = if (checkedState == MaterialCheckBox.STATE_CHECKED) {
            viewModel.likePost(postId, mainViewModel.loggedInProfileId!!)
            MaterialCheckBox.STATE_CHECKED
        } else {
            viewModel.removeLike(postId, mainViewModel.loggedInProfileId!!)
            MaterialCheckBox.STATE_UNCHECKED
        }
        
        lifecycleScope.launch { // Todo: adding delay is just a work around, this might break in other phones.
            delay(100)
            val likeString = viewModel.getFormattedLikeCount(postId)
            val likePayload = HomeAdapter.LikePayload(likeString, postId, newState)
            homeAdapter.notifyItemChanged(pos, likePayload)
        }
    }
    
    private fun onSavePostClicked(pos: Int, view: View) {
        view as MaterialCheckBox
        val checkedState = view.checkedState
        val postId = homeAdapter.getPostId(pos)
        val newState = if (checkedState == MaterialCheckBox.STATE_CHECKED) {
            viewModel.savePost(mainViewModel.loggedInProfileId!!, postId)
            MaterialCheckBox.STATE_CHECKED
        } else {
            viewModel.removeSavedPost(mainViewModel.loggedInProfileId!!, postId) //            view.setButtonIconTintList()
            MaterialCheckBox.STATE_UNCHECKED
        }
        
        lifecycleScope.launch { // Todo: adding delay is just a work around, this might break in other phones.
            delay(100)
            val savePayload = HomeAdapter.SavePayload(postId, newState)
            homeAdapter.notifyItemChanged(pos, savePayload)
        }
        
    }
    
    private fun openCommentBottomSheet(pos: Int) {
        val postId: Long = homeAdapter.getPostId(pos)
        val action = HomeFragmentDirections.actionHomeFragmentToCommentSheet(postId)
        findNavController().navigate(action)
    }
    
    private fun whenMessagesBtnClicked() {
        Log.d(TAG, "Messages Btn Clicked")
    }
    
    private fun whenNotificationBtnClicked() {
        Log.d(TAG, "Notification Btn Clicked")
    }
}