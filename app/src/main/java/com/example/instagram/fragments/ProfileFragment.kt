package com.example.instagram.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.R
import com.example.instagram.adapters.ProfileAdapter
import com.example.instagram.bottomsheet.ProfileMenu
import com.example.instagram.database.model.ProfileSummary
import com.example.instagram.databinding.FragmentProfileBinding

const val EDIT_PROFILE = "Edit Profile"
const val SHARE_PROFILE = "Share Profile"
const val FOLLOW = "Follow"
const val UNFOLLOW = "Unfollow"
const val MESSAGE = "Message"

private const val TAG = "CommTag_ProfileFragment"

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var parentRecyclerView: RecyclerView
    private lateinit var profileAdapter: ProfileAdapter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnProfileBottomSheet.setOnClickListener { showMenu() }
        profileAdapter = ProfileAdapter(
            ProfileSummary("hrrdas", "Raj", "Rahul", "asdhas", 23, 3430, 2333),
            ::showFollowerFragment, ::showFollowingFragment, buttonConfigurator(1, 2, true)
        )
        binding.profileRV.adapter = profileAdapter
        binding.profileRV.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    private fun showMenu() {
        val profileMenu = ProfileMenu()
        profileMenu.show(parentFragmentManager, "profile_menu")
    }

    private fun buttonConfigurator(currentUserId: Long, profileUserId: Long, isCurrentUserFollowingProfileUser: Boolean): Pair<Pair<String, String>, Pair<() -> Unit, () -> Unit>> {
        val listener1: (() -> Unit)?
        val listener2: (() -> Unit)?
        val btnText1: String?
        val btnText2: String?


        if (currentUserId == profileUserId) {
            btnText1 = EDIT_PROFILE
            btnText2 = SHARE_PROFILE
            listener1 = ::editProfile
            listener2 = ::shareProfile
        }
        else if (isCurrentUserFollowingProfileUser) {
            // current user is following the other user

            btnText1 = UNFOLLOW
            btnText2 = MESSAGE
            listener1 = ::unFollowProfile
            listener2 = ::messageProfile
        }
        else {
            btnText1 = FOLLOW
            btnText2 = MESSAGE
            listener1 = ::followProfile
            listener2 = ::messageProfile
        }
        return Pair(Pair(btnText1, btnText2), Pair(listener1, listener2))
    }

    private fun showFollowingFragment() {
        Log.d(TAG, "Showing Following List")
    }

    private fun showFollowerFragment() {
        Log.d(TAG, "Showing Follower List")
    }

    private fun editProfile() {
        Log.d(TAG, "editProfile clicked.")
    }

    private fun shareProfile() {
        Log.d(TAG, "shareProfile clicked.")
    }

    private fun unFollowProfile() {
        Log.d(TAG, "unFollowProfile")
    }

    private fun messageProfile() {
        Log.d(TAG, "messageProfile")
    }

    private fun followProfile() {
        Log.d(TAG, "followProfile")
    }
}