package com.example.instagram.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
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
import java.io.File
import java.io.FileOutputStream
import java.util.Objects
import kotlin.properties.Delegates

private const val TAG = "ProfileFragment_CommTag"

const val POST_OPEN_REQ_KEY = "postId"
const val POST_ID = "postIdToOpen"
const val POST_POS = "postIdPosition"

class ProfileFragment : Fragment() {
    private lateinit var lastStatusProfSummary: ProfileSummary
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainViewModel: MainViewModel
    private lateinit var viewModel: ProfileFragViewModel
    private lateinit var db: AppDatabase
    private var profileId: Long by Delegates.notNull()
    private val args: ProfileFragmentArgs? by navArgs()
    
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
        
        requireActivity().supportFragmentManager.setFragmentResultListener(POST_OPEN_REQ_KEY, requireActivity()) { _, bundle ->
            val postId = bundle.getLong(POST_ID)
            val pos = bundle.getInt(POST_POS)
            val action = ProfileFragmentDirections.actionProfileFragmentToOnePostFragment(postId, pos)
            findNavController().navigate(action)
        }
        lifecycleScope.launch {
            viewModel.getProfileSummary(mainViewModel.loggedInProfileId!!, profileId)
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        return binding.root
    }
    
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
    
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (profileId != mainViewModel.loggedInProfileId!!) {
            binding.btnProfileBottomSheet.visibility = View.INVISIBLE
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
        
        // todo: IF App crashed due to viewpager in profile fragment
        binding.viewPagerPostAndTagPhoto.isSaveEnabled = true
        
        setOnClickListener()
        setViewPagerHeight()
        setObservers()
    }
    
    private fun setProfilePicTransition() {
        binding.profilePic.setOnLongClickListener {
            val extras = FragmentNavigatorExtras(binding.profilePic to "image_big")
            findNavController().navigate(R.id.action_profileFragment_to_profilePictureFragment, null, null, extras)
            true
        }
    }
    
    private fun setOnClickListener() {
        binding.btnProfileBottomSheet.setOnClickListener {
            // check below condition to avoid crash when 'btnProfileBottomSheet' is tapped quickly.
            if (findNavController().currentDestination?.id == R.id.profileMenu2) return@setOnClickListener
            findNavController().navigate(R.id.action_profileFragment_to_profileMenu2)
        }
        if (::lastStatusProfSummary.isInitialized) {
            bindAllDetails(lastStatusProfSummary)
        }
        binding.followersCount.setOnClickListener { findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToListFollowFragment(TYPE_FOLLOWER, profileId)) }
        binding.followerLabel.setOnClickListener { findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToListFollowFragment(TYPE_FOLLOWER, profileId)) }
        binding.followingCount.setOnClickListener { findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToListFollowFragment(TYPE_FOLLOWING, profileId)) }
        binding.followingLabel.setOnClickListener { findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToListFollowFragment(TYPE_FOLLOWING, profileId)) }
    }
    
    private fun setObservers() {
        viewModel.profileSummary.observe(viewLifecycleOwner) {
            binding.loadingProgressBar.visibility = View.GONE
            binding.nestedScroll.visibility = View.VISIBLE
            if (::lastStatusProfSummary.isInitialized) bindFollowDetails(it)
            else bindAllDetails(it)
            this.lastStatusProfSummary = it
        }
        
        db.profileDao().getPostCount(profileId).observe(viewLifecycleOwner) {
            binding.postCount.text = it.toString()
        }
    }
    
    private fun setViewPagerHeight() {
        var heightToMinus = 0
        val viewToMeasure0 = binding.toolbar
        val vto0 = viewToMeasure0.viewTreeObserver
        vto0.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewToMeasure0.viewTreeObserver.removeOnGlobalLayoutListener(this)
                heightToMinus += viewToMeasure0.height
            }
        })
        
        val tabLayoutHeight = (48 * requireContext().resources.displayMetrics.density).toInt()
        
        val viewToMeasure = binding.profileParent
        val vto = viewToMeasure.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewToMeasure.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val height: Int = viewToMeasure.height - heightToMinus - tabLayoutHeight
                
                val layoutParams = binding.viewPagerPostAndTagPhoto.layoutParams
                layoutParams.height = height
                binding.viewPagerPostAndTagPhoto.layoutParams = layoutParams
            }
        })
    }
    
    private fun bindFollowDetails(it: ProfileSummary) {
        binding.followingCount.text = it.followingCount.toString()
        binding.followersCount.text = it.followerCount.toString()
        btn(binding.btnStart, binding.btnEnd, it.isFollowing)
    }
    
    private fun bindAllDetails(it: ProfileSummary) {
        binding.toolbarProfileUsername.text = it.username
        binding.profileFullName.text = requireContext().getString(R.string.full_name, it.first_name, it.last_name)
        binding.profileBio.text = it.bio
        binding.followersCount.text = it.followerCount.toString()
        binding.followingCount.text = it.followingCount.toString()
        btn(binding.btnStart, binding.btnEnd, it.isFollowing)
        
        CoroutineScope(Dispatchers.IO).launch {
            if (it.profilePicUrl == null) return@launch
            setProfilePicTransition()
            
            mainViewModel.profileImageBitmap = ImageUtil(requireContext()).getBitmap(it.profilePicUrl)
            
            withContext(Dispatchers.Main) {
                binding.profilePic.setImageBitmap(ImageUtil(requireContext()).getBitmap(it.profilePicUrl))
            }
        }
    }
    
    private fun btn(btnStart: MaterialButton, btnEnd: MaterialButton, isFollowing: Boolean) {
        if (mainViewModel.loggedInProfileId!! == profileId) {
            btnStart.text = EDIT_PROFILE
            btnEnd.text = SHARE_PROFILE
            btnStart.setOnClickListener { editProfile() }
            btnEnd.setOnClickListener { shareAction() }
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
    
    private fun editProfile() {
        findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
    }
    
    private fun messageProfile() {
        Log.d(TAG, "messageProfile")
    }
    
    private fun followProfile() {
        lifecycleScope.launch {
            db.followDao().insertNewFollow(Follow(mainViewModel.loggedInProfileId!!, profileId, System.currentTimeMillis()))
            viewModel.getProfileSummary(mainViewModel.loggedInProfileId!!, profileId)
        }
    }
    
    private fun unFollowProfile() {
        lifecycleScope.launch {
            db.followDao().deleteFollow(mainViewModel.loggedInProfileId!!, profileId)
            viewModel.getProfileSummary(mainViewModel.loggedInProfileId!!, profileId)
        }
    }
    
    private fun shareAction() {
        var bitmap: Bitmap? = null
        try {
            val bitmapDrawable = binding.profilePic.drawable as BitmapDrawable
            bitmap = bitmapDrawable.bitmap
        } catch (e: Exception) {
            Log.d(TAG, "shareAction: ${e.message}")
        }
        val share = Intent(Intent.ACTION_SEND)
        val textToShare = "Look at my profile on Instagram\n\nhttps://instagram.com/uid=${mainViewModel.loggedInProfileId!!}"
        share.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val bmpUri: Uri? = saveImage(bitmap)
        share.type = if (bmpUri == null) {
            "text/plain"
        } else {
            share.putExtra(Intent.EXTRA_STREAM, bmpUri)
            "image/jpeg"
        }
        share.putExtra(Intent.EXTRA_SUBJECT, "New App")
        share.putExtra(Intent.EXTRA_TEXT, textToShare)
        startActivity(Intent.createChooser(share, "Share: "))
    }
    
    private fun saveImage(image: Bitmap?): Uri? {
        if (image == null) return null
        val imagesFolder = File(requireActivity().cacheDir, "images")
        var uri: Uri? = null
        try {
            imagesFolder.mkdirs()
            val file = File(imagesFolder, "shared_images.jpg")
            val stream = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
            uri = FileProvider.getUriForFile(Objects.requireNonNull(requireActivity().applicationContext), "com.example.instagram" + ".provider", file)
        } catch (e: Exception) {
            Log.d(TAG, "Exception: + ${e.message}")
        }
        return uri
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