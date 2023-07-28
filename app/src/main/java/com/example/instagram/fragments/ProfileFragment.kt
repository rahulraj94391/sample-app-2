package com.example.instagram.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.instagram.ImageUtil
import com.example.instagram.MainViewModel
import com.example.instagram.R
import com.example.instagram.adapters.EDIT_PROFILE
import com.example.instagram.adapters.FOLLOW
import com.example.instagram.adapters.MESSAGE
import com.example.instagram.adapters.SHARE_PROFILE
import com.example.instagram.adapters.UNFOLLOW
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.entity.Follow
import com.example.instagram.database.model.ProfileSummary
import com.example.instagram.databinding.FragmentProfileBinding
import com.example.instagram.viewmodels.ProfileFragViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

private const val TAG = "CommTag_ProfileFragment"

const val POST_ID_OPEN_REQ_KEY = "postId"
const val POST_ID_REF_KEY = "postIdToOpenInNextFrag"

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var viewModel: ProfileFragViewModel
    private var profileId: Long by Delegates.notNull()
    private lateinit var db: AppDatabase
    private val args: ProfileFragmentArgs? by navArgs()
    private lateinit var lastStatusProfSummary: ProfileSummary
    
    private val profilePicUri = registerForActivityResult(ActivityResultContracts.GetContent()) {
        viewModel.uploadProfileImage(mainViewModel.loggedInProfileId!!, it)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())
        viewModel = ViewModelProvider(this)[ProfileFragViewModel::class.java]
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        
        profileId = try {
            args!!.profileId
        } catch (e: Exception) {
            mainViewModel.loggedInProfileId!!
        }
        
        requireActivity().supportFragmentManager.setFragmentResultListener(POST_ID_OPEN_REQ_KEY, requireActivity()) { _, bundle ->
            val result = bundle.getLong(POST_ID_REF_KEY)
            val action = ProfileFragmentDirections.actionProfileFragmentToOnePostFragment(result)
            findNavController().navigate(action)
        }
        lifecycleScope.launch {
            viewModel.getProfileSummary(mainViewModel.loggedInProfileId!!, profileId)
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        return binding.root
    }
    
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnProfileBottomSheet.setOnClickListener { findNavController().navigate(R.id.action_profileFragment_to_profileMenu2) }
        binding.profilePic.setOnLongClickListener {
            profilePicUri.launch("image/*")
            true
        }
        if (::lastStatusProfSummary.isInitialized) {
            bindAllDetails(lastStatusProfSummary)
        }
        
        viewModel.profileSummary.observe(viewLifecycleOwner) {
            binding.loadingProgressBar.visibility = View.GONE
            binding.nestedScroll.visibility = View.VISIBLE
            
            if (::lastStatusProfSummary.isInitialized) {
                bindFollowDetails(it)
            } else {
                bindAllDetails(it)
            }
            this.lastStatusProfSummary = it
            
        }
        
        binding.viewPagerPostAndTagPhoto.adapter = ScreenSlidePagerAdapter(requireActivity())
        TabLayoutMediator(binding.tabLayout, binding.viewPagerPostAndTagPhoto) { tab, position ->
            when (position) {
                0 -> {
                    tab.icon = requireContext().getDrawable(R.drawable.grid)
                }
                
                1 -> {
                    tab.icon = requireContext().getDrawable(R.drawable.tag)
                }
            }
        }.attach()
        
        
    }
    
    private fun bindFollowDetails(it: ProfileSummary) {
        binding.followingCount.text = it.followingCount.toString()
        binding.followersCount.text = it.followerCount.toString()
        btn(binding.btnStart, binding.btnEnd, it.isFollowing)
    }
    
    private fun bindAllDetails(it: ProfileSummary) {
        binding.toolbarProfileUsername.text = it.username
        binding.profileFullName.text = "${it.first_name} ${it.last_name}"
        binding.profileBio.text = it.bio
        binding.postCount.text = it.postCount.toString()
        binding.followersCount.text = it.followerCount.toString()
        binding.followingCount.text = it.followingCount.toString()
        btn(binding.btnStart, binding.btnEnd, it.isFollowing)
        
        CoroutineScope(Dispatchers.IO).launch {
            if (it.profilePic == null) return@launch
            val bitmap = ImageUtil(requireContext()).getBitmap(it.profilePic)
            withContext(Dispatchers.Main) {
                binding.profilePic.setImageBitmap(bitmap)
            }
        }
    }
    
    private fun btn(btnStart: MaterialButton, btnEnd: MaterialButton, isFollowing: Boolean) {
        if (mainViewModel.loggedInProfileId!! == profileId) {
            btnStart.text = EDIT_PROFILE
            btnEnd.text = SHARE_PROFILE
            btnStart.setOnClickListener { editProfile() }
            btnEnd.setOnClickListener { shareProfile() }
        } else if (isFollowing) {
            btnStart.text = UNFOLLOW
            btnEnd.text = MESSAGE
            btnStart.setOnClickListener {
                unFollowProfile()
            }
            btnEnd.setOnClickListener { messageProfile() }
        } else {
            btnStart.text = FOLLOW
            btnEnd.text = MESSAGE
            btnStart.setOnClickListener {
                followProfile()
            }
            btnEnd.setOnClickListener { messageProfile() }
        }
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
    
    private fun messageProfile() {
        Log.d(TAG, "messageProfile")
    }
    
    private fun followProfile() {
        lifecycleScope.launch {
            db.followDao().insertNewFollow(Follow(mainViewModel.loggedInProfileId!!, profileId))
            viewModel.getProfileSummary(mainViewModel.loggedInProfileId!!, profileId)
        }
    }
    
    private fun unFollowProfile() {
        lifecycleScope.launch {
            db.followDao().deleteFollow(mainViewModel.loggedInProfileId!!, profileId)
            viewModel.getProfileSummary(mainViewModel.loggedInProfileId!!, profileId)
        }
    }
    
    
    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int {
            return 2
        }
        
        override fun createFragment(position: Int): Fragment {
            return PhotoGridFragment.newInstance(position, profileId)
        }
    }
    
}