package com.example.instagram.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.MainViewModel
import com.example.instagram.R
import com.example.instagram.adapters.ProfileAdapter
import com.example.instagram.bottomsheet.ProfileMenu
import com.example.instagram.database.AppDatabase
import com.example.instagram.databinding.FragmentProfileBinding
import com.example.instagram.viewmodels.ProfileFragViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

private const val TAG = "CommTag_ProfileFragment"
private const val PROFILE_ID_KEY = "profileId"
const val POST_ID_OPEN_REQ_KEY = "postId"
const val POST_ID_REF_KEY = "postIdToOpenInNextFrag"

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var profileAdapter: ProfileAdapter
    private lateinit var mainViewModel: MainViewModel
    private lateinit var viewModel: ProfileFragViewModel
    private var profileId: Long by Delegates.notNull()
    private lateinit var db: AppDatabase

    /*companion object {
        fun newInstance(profileId: Long): ProfileFragment {
            val args = Bundle()
            args.putLong(PROFILE_ID_KEY, profileId)
            val fragment = ProfileFragment()
            fragment.arguments = args
            return fragment
        }
    }*/

    val profilePicUri = registerForActivityResult(ActivityResultContracts.GetContent()) {
        viewModel.uploadProfileImage(mainViewModel.loggedInProfileId!!, it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())
        viewModel = ViewModelProvider(requireActivity())[ProfileFragViewModel::class.java]
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        val id = savedInstanceState?.getLong(PROFILE_ID_KEY, mainViewModel.loggedInProfileId!!)
        profileId = id ?: mainViewModel.loggedInProfileId!!


        requireActivity().supportFragmentManager.setFragmentResultListener(POST_ID_OPEN_REQ_KEY, requireActivity()) { requestKey, bundle ->
            val result = bundle.getLong(POST_ID_REF_KEY)
            val action = ProfileFragmentDirections.actionProfileFragmentToOnePostFragment(result)
            findNavController().navigate(action)
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnProfileBottomSheet.setOnClickListener { showMenu() }
        lifecycleScope.launch { showImages() }
    }

    private suspend fun showImages() {
        val ownProfileId = mainViewModel.loggedInProfileId!!
        val userProfileId = profileId
        val count = db.followDao().isUserFollowingUser(ownProfileId, userProfileId)
        val isFollowing = count > 0
        val profileSummary = viewModel.getProfileSummary(profileId)
        withContext(Dispatchers.Main) {
            if (ownProfileId != userProfileId) binding.btnProfileBottomSheet.visibility = View.INVISIBLE
            binding.toolbarProfileUsername.text = "${profileSummary.first_name} ${profileSummary.last_name}"
        }

        profileAdapter = ProfileAdapter(
            profileSummary = profileSummary,
            isFollowing = isFollowing,
            ownId = ownProfileId,
            userProfileId = userProfileId,
            OnFollowViewClicked = this::showFollowerFragment,
            OnFollowingViewClicked = this::showFollowingFragment,
            OnEditProfileClicked = this::editProfile,
            OnShareProfileClicked = this::shareProfile,
            OnUnfollowClicked = this::unFollowProfile,
            OnFollowClicked = this::followProfile,
            OnMessageClicked = this::messageProfile
        )
        binding.profileRV.adapter = profileAdapter
        binding.profileRV.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    private fun showMenu() {
        val profileMenu = ProfileMenu()
        profileMenu.show(parentFragmentManager, "profile_menu")
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
        profilePicUri.launch("image/*")
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