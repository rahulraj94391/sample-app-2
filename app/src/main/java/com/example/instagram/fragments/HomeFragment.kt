package com.example.instagram.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.MainViewModel
import com.example.instagram.R
import com.example.instagram.adapters.HomeAdapter
import com.example.instagram.databinding.FragmentHomeBinding
import com.example.instagram.viewmodels.HomeFragViewModel
import com.google.android.material.checkbox.MaterialCheckBox

private const val TAG = "CommTag_HomeFragment"

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var homeAdapter: HomeAdapter
    private lateinit var viewModel: HomeFragViewModel
    private lateinit var mainViewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[HomeFragViewModel::class.java]
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        homeAdapter = HomeAdapter()
        binding.btnMessages.setOnClickListener { whenMessagesBtnClicked() }
        binding.btnNotifications.setOnClickListener { whenNotificationBtnClicked() }

        binding.homeRV.adapter = homeAdapter
        binding.homeRV.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    fun onLikeClicked(position: Int, view: MaterialCheckBox) {
        // todo:
        val postId = 393.toLong()
        if (view.isChecked) {
            viewModel.likePost(postId, mainViewModel.loggedInProfileId!!)
        }
        else {
            viewModel.removeLike(postId, mainViewModel.loggedInProfileId!!)
        }
    }

    fun onCommentClicked(position: Int) {
        // todo:
        val postId = 393.toLong() // get the postId from view model list
        val action = HomeFragmentDirections.actionHomeFragmentToCommentSheet(postId)
        findNavController().navigate(action)
    }

    fun onSavePostClicked(position: Int, view: MaterialCheckBox) {
        // todo
        val postId = 393.toLong() // get the postId from view model list

        if (view.isChecked) {
            viewModel.savePost(mainViewModel.loggedInProfileId!!, postId)
        }
        else {
            viewModel.removeSavedPost(mainViewModel.loggedInProfileId!!, postId)
        }
    }


    private fun whenMessagesBtnClicked() {
        Log.d(TAG, "Messages Btn Clicked")
    }

    private fun whenNotificationBtnClicked() {
        Log.d(TAG, "Notification Btn Clicked")
    }
}