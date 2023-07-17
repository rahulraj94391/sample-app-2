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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.instagram.MainViewModel
import com.example.instagram.R
import com.example.instagram.adapters.ProfileAdapter
import com.example.instagram.bottomsheet.ProfileMenu
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.entity.Follow
import com.example.instagram.database.model.ProfileSummary
import com.example.instagram.databinding.FragmentProfileBinding
import com.example.instagram.viewmodels.ProfileFragViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

private const val TAG = "CommTag_ProfileFragment"

const val POST_ID_OPEN_REQ_KEY = "postId"
const val POST_ID_REF_KEY = "postIdToOpenInNextFrag"

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var profileAdapter: ProfileAdapter
    private lateinit var mainViewModel: MainViewModel
    private lateinit var viewModel: ProfileFragViewModel
    private var profileId: Long by Delegates.notNull()
    private lateinit var db: AppDatabase
    private val args: ProfileFragmentArgs? by navArgs()
    private lateinit var profileSummary: ProfileSummary
    private var isFollowing = false


    private val profilePicUri = registerForActivityResult(ActivityResultContracts.GetContent()) {
        viewModel.uploadProfileImage(mainViewModel.loggedInProfileId!!, it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())
        viewModel = ViewModelProvider(requireActivity())[ProfileFragViewModel::class.java]
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        try {
            profileId = args!!.profileId
        } catch (e: Exception) {
            profileId = mainViewModel.loggedInProfileId!!
        }

        requireActivity().supportFragmentManager.setFragmentResultListener(POST_ID_OPEN_REQ_KEY, requireActivity()) { _, bundle ->
            val result = bundle.getLong(POST_ID_REF_KEY)
            val action = ProfileFragmentDirections.actionProfileFragmentToOnePostFragment(result)

            lifecycleScope.launchWhenResumed {
                Log.d(TAG, "launch when resumed.")
                findNavController().navigate(action)
            }
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

    private suspend fun refreshProfileSummary(ownProfileId: Long, userProfileId: Long) {
        val count = db.followDao().isUserFollowingUser(ownProfileId, userProfileId)
        profileSummary = viewModel.getProfileSummary(profileId)
        isFollowing = count > 0
    }

    private suspend fun showImages() {
        val ownProfileId = mainViewModel.loggedInProfileId!!
        val userProfileId = profileId
        refreshProfileSummary(ownProfileId, userProfileId)
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

    override fun onStart() {
        super.onStart()
        (binding.profileRV.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false;

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

    private fun messageProfile() {
        Log.d(TAG, "messageProfile")
    }

    private fun followProfile() {
        lifecycleScope.launch {
            db.followDao().insertNewFollow(Follow(mainViewModel.loggedInProfileId!!, profileId))
            refreshProfileSummary(mainViewModel.loggedInProfileId!!, profileId)
            profileAdapter.setNewSummary(profileSummary)
            profileAdapter.notifyItemChanged(1)
        }
    }

    private fun unFollowProfile() {
        lifecycleScope.launch {
            db.followDao().deleteFollow(mainViewModel.loggedInProfileId!!, profileId)
            refreshProfileSummary(mainViewModel.loggedInProfileId!!, profileId)
            profileAdapter.setNewSummary(profileSummary)
            profileAdapter.notifyItemChanged(1)
        }
    }
}