package com.example.instagram.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.instagram.R
import com.example.instagram.databinding.FragmentHomeBinding

private const val TAG = "CommTag_HomeFragment"

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnMessages.setOnClickListener { whenMessagesBtnClicked() }
        binding.btnNotifications.setOnClickListener { whenNotificationBtnClicked() }

    }


    private fun whenMessagesBtnClicked() {
        Log.d(TAG, "Messages Btn Clicked")
    }

    private fun whenNotificationBtnClicked() {
        Log.d(TAG, "Notification Btn Clicked")
    }

}