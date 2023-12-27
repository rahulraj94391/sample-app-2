package com.example.instagram.screen_profilePostGridView

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.PopupMenu
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.instagram.BLOCK
import com.example.instagram.EDIT_PROFILE
import com.example.instagram.FOLLOW
import com.example.instagram.HomeActivity
import com.example.instagram.MESSAGE
import com.example.instagram.R
import com.example.instagram.SHARE_PROFILE
import com.example.instagram.TYPE_FOLLOWER
import com.example.instagram.TYPE_FOLLOWING
import com.example.instagram.UNFOLLOW
import com.example.instagram.common.MainViewModel
import com.example.instagram.common.util.ImageUtil
import com.example.instagram.data.AppDatabase
import com.example.instagram.data.entity.Follow
import com.example.instagram.screen_profilePostGridView.model.ProfileSummary
import com.example.instagram.databinding.FragmentProfileBinding
import com.example.instagram.screen_chat.ChatActivity
import com.example.instagram.screen_chat.LOGGED_IN_ID
import com.example.instagram.screen_chat.USER_ID
import com.example.instagram.screen_chat.USER_LAST_LOGIN
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Objects
import kotlin.properties.Delegates

class ProfileFragment : Fragment() {
    private lateinit var mainViewModel: MainViewModel
    private lateinit var viewModel: ProfileViewModel
    private lateinit var db: AppDatabase
    private val binding get() = _binding!!
    private val args: ProfileFragmentArgs? by navArgs()
    private var showUpBtb = false
    private var profileId: Long by Delegates.notNull()
    private var _binding: FragmentProfileBinding? = null
    private var profilePosInFollowingList: Int = -1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        
        profileId = try {
            args!!.profileId
        } catch (e: Exception) {
            mainViewModel.loggedInProfileId!!
        }
        
        showUpBtb = try {
            args!!.upBtn
        } catch (e: Exception) {
            false
        }
        
        profilePosInFollowingList = try {
            args!!.profilePosInFollowingList
        } catch (e: Exception) {
            -1
        }
    }
    
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        binding.viewPagerPostAndTagPhoto.adapter = ScreenSlidePagerAdapter(childFragmentManager, lifecycle)
        return binding.root
    }
    
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        if (mainViewModel.loggedInProfileId!! != profileId || showUpBtb) {
            binding.toolbar.setNavigationIcon(R.drawable.arrow_back_24)
            binding.toolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }
        binding.viewPagerPostAndTagPhoto.isSaveEnabled = true
        setOnClickListener()
//        setViewPagerHeight()
        setObservers()
    }
    
    
    override fun onResume() {
        super.onResume()
        TabLayoutMediator(binding.tabLayout, binding.viewPagerPostAndTagPhoto) { tab, position ->
            when (position) {
                0 -> tab.icon = AppCompatResources.getDrawable(requireContext(), R.drawable.grid)
                1 -> tab.icon = AppCompatResources.getDrawable(requireContext(), R.drawable.tag)
            }
        }.attach()
        lifecycleScope.launch {
            delay(50)
            viewModel.getProfileSummary(mainViewModel.loggedInProfileId!!, profileId)
        }
    }
    
    private fun setProfilePicTransition() {
        binding.profilePic.setOnClickListener {
            val extras = FragmentNavigatorExtras(binding.profilePic to "image_big")
            findNavController().navigate(R.id.action_profileFragment_to_profilePictureFragment, null, null, extras)
        }
    }
    
    private fun setOnClickListener() {
        binding.btnProfileBottomSheet.setOnClickListener abc@{
            // check below condition to avoid crash when 'btnProfileBottomSheet' is tapped quickly.
            if (findNavController().currentDestination?.id == R.id.profileMenu2) return@abc
            
            if (profileId == mainViewModel.loggedInProfileId!!)
                findNavController().navigate(R.id.action_profileFragment_to_profileMenu2)
            else {
                showBlockMenuPopup()
            }
        }
        
        binding.followersCount.setOnClickListener { findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToListFollowFragment(TYPE_FOLLOWER, profileId)) }
        binding.followerLabel.setOnClickListener { findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToListFollowFragment(TYPE_FOLLOWER, profileId)) }
        binding.followingCount.setOnClickListener { findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToListFollowFragment(TYPE_FOLLOWING, profileId)) }
        binding.followingLabel.setOnClickListener { findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToListFollowFragment(TYPE_FOLLOWING, profileId)) }
    }
    
    private fun showBlockMenuPopup() {
        PopupMenu(binding.btnProfileBottomSheet.context, binding.btnProfileBottomSheet).apply {
            inflate(R.menu.block_user_menu)
            setForceShowIcon(true)
            menu.getItem(0).title = SpannableString(BLOCK).apply {
                setSpan(ForegroundColorSpan(Color.RED), 0, BLOCK.length, 0)
            }
            setOnMenuItemClickListener {
                showBlockUserDialog()
                true
            }
            show()
        }
    }
    
    private fun showBlockUserDialog() {
        val profile = viewModel.profileSummary.value
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Block user ?")
            .setMessage("Block ${profile?.first_name} ${profile?.last_name} ?")
            .setCancelable(true)
            .setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launch {
                    blockUser()
                }
            }.setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }.show()
    }
    
    private fun blockUser() {
        lifecycleScope.launch {
            viewModel.blockUser(mainViewModel.loggedInProfileId!!, profileId)
            mainViewModel.startProfileRefresh.postValue(true)
        }
    }
    
    private fun setObservers() {
        mainViewModel.openPost2.observe(viewLifecycleOwner) {
            if (it == Pair(-1, -1)) return@observe
            val action =
                ProfileFragmentDirections.actionProfileFragmentToProfilePostFragment(profileId, it.first, it.second)
            if (findNavController().currentDestination?.id == R.id.profileFragment) {
                findNavController().navigate(action)
            }
            mainViewModel.openPost2.postValue(Pair(-1, -1))
        }
        
        binding.refreshProfile.setOnRefreshListener {
            mainViewModel.startProfileRefresh.postValue(true)
        }
        
        mainViewModel.isProfileRefreshed.observe(viewLifecycleOwner) {
            if (it) {
                mainViewModel.isProfileRefreshed.postValue(false)
                binding.refreshProfile.isRefreshing = false
            }
        }
        
        viewModel.profileSummary.observe(viewLifecycleOwner) {
            binding.loadingProgressBar.visibility = View.GONE
            binding.nestedScroll.visibility = View.VISIBLE
            it?.let {
                bindAllDetails(it)
            }
        }
    }
    
    private fun setViewPagerHeight() {
        var heightToSubtract = 0
        val viewToMeasure0 = binding.toolbar
        val vto0 = viewToMeasure0.viewTreeObserver
        vto0.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewToMeasure0.viewTreeObserver.removeOnGlobalLayoutListener(this)
                heightToSubtract += viewToMeasure0.height
            }
        })
        
        val tabLayoutHeight = (48 * requireContext().resources.displayMetrics.density).toInt()
        
        val viewToMeasure = binding.profileParent
        val vto = viewToMeasure.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewToMeasure.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val height: Int = viewToMeasure.height - heightToSubtract - tabLayoutHeight
                val layoutParams = binding.viewPagerPostAndTagPhoto.layoutParams
                layoutParams.height = height
                binding.viewPagerPostAndTagPhoto.layoutParams = layoutParams
            }
        })
    }
    
    private fun bindAllDetails(it: ProfileSummary) {
        binding.toolbarProfileUsername.text = it.username
        binding.profileFullName.text = requireContext().getString(R.string.full_name, it.first_name, it.last_name)
        binding.profileBio.text = it.bio
        binding.followersCount.text = it.followerCount.toString()
        binding.followingCount.text = it.followingCount.toString()
        binding.postCount.text = it.postCount.toString()
        setButtons(it.isFollowing)
        
        CoroutineScope(Dispatchers.IO).launch {
            if (it.profilePicUrl == null) {
                return@launch
            }
            
            withContext(Dispatchers.Main) {
                setProfilePicTransition()
            }
            mainViewModel.profileImageBitmap = ImageUtil(requireContext()).getBitmap(it.profilePicUrl)
            withContext(Dispatchers.Main) {
                binding.profilePic.setImageBitmap(mainViewModel.profileImageBitmap)
            }
        }
    }
    
    private fun setButtons(isFollowing: Boolean) {
        val btnStart = binding.btnStart
        val btnEnd = binding.btnEnd
        val btnUnblock = binding.btnUnblock
        
        if (mainViewModel.loggedInProfileId!! == profileId) {
            btnStart.text = EDIT_PROFILE
            btnEnd.text = SHARE_PROFILE
            btnStart.setOnClickListener { editProfile() }
            btnEnd.setOnClickListener { shareAction() }
        } else if (viewModel.isUserBlocked) {
            btnStart.visibility = View.INVISIBLE
            btnEnd.visibility = View.INVISIBLE
            btnUnblock.visibility = View.VISIBLE
            btnUnblock.setOnClickListener {
                showUnblockDialog()
            }
        } else if (isFollowing) {
            btnStart.visibility = View.VISIBLE
            btnEnd.visibility = View.VISIBLE
            btnUnblock.visibility = View.INVISIBLE
            
            btnStart.text = UNFOLLOW
            btnEnd.text = MESSAGE
            btnStart.setOnClickListener {
                unFollowProfile()
            }
            btnEnd.setOnClickListener { messageProfile() }
        } else {
            btnStart.visibility = View.VISIBLE
            btnEnd.visibility = View.VISIBLE
            btnUnblock.visibility = View.INVISIBLE
            
            
            btnStart.text = FOLLOW
            btnEnd.text = MESSAGE
            btnStart.setOnClickListener {
                followProfile()
            }
            btnEnd.setOnClickListener { messageProfile() }
        }
    }
    
    private fun showUnblockDialog() {
        val profile = viewModel.profileSummary.value
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Unblock user ?")
            .setMessage("Unblock ${profile?.first_name} ${profile?.last_name} ?")
            .setCancelable(true)
            .setPositiveButton("Yes") { _, _ ->
                unblockUser()
            }.setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }.show()
    }
    
    private fun unblockUser() {
        lifecycleScope.launch {
            viewModel.unblockUser(mainViewModel.loggedInProfileId!!, profileId)
            mainViewModel.startProfileRefresh.postValue(true)
        }
    }
    
    private fun editProfile() = findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
    
    private fun messageProfile() {
        if (findNavController().currentDestination?.id != R.id.profileFragment) return
        (requireActivity() as HomeActivity).haptics.light()
        lifecycleScope.launch {
            val intent = Intent(requireActivity(), ChatActivity::class.java)
            val userLastTime = db.lastOnlineDao().getUserLastOnlineStatus(profileId, mainViewModel.loggedInProfileId!!)?.time ?: 0L
            intent.apply {
                putExtra(USER_LAST_LOGIN, userLastTime)
                putExtra(USER_ID, profileId)
                putExtra(LOGGED_IN_ID, mainViewModel.loggedInProfileId)
            }
            startActivity(intent)
        }
    }
    
    private fun followProfile() {
        (requireActivity() as HomeActivity).haptics.light()
        lifecycleScope.launch {
            db.followDao().insertNewFollow(Follow(mainViewModel.loggedInProfileId!!, profileId, System.currentTimeMillis()))
            viewModel.getProfileSummary(mainViewModel.loggedInProfileId!!, profileId)
            mainViewModel.reloadHomeFeed.postValue(true)
        }
    }
    
    private fun unFollowProfile() {
        (requireActivity() as HomeActivity).haptics.light()
        lifecycleScope.launch {
            db.followDao().deleteFollow(mainViewModel.loggedInProfileId!!, profileId)
            viewModel.getProfileSummary(mainViewModel.loggedInProfileId!!, profileId)
            mainViewModel.reloadHomeFeed.postValue(true)
        }
        mainViewModel.removeProfileFromFollowingList.postValue(profilePosInFollowingList)
    }
    
    private fun shareAction() {
        var bitmap: Bitmap? = null
        try {
            val bitmapDrawable = binding.profilePic.drawable as BitmapDrawable
            bitmap = bitmapDrawable.bitmap
        } catch (e: Exception) {
            println(e.message)
        }
        val share = Intent(Intent.ACTION_SEND)
        val textToShare = "Look at my profile on SociableX\n\nhttps://sociablex.com/uid=${mainViewModel.loggedInProfileId!!}"
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
            println(e.message)
        }
        return uri
    }
    
    private inner class ScreenSlidePagerAdapter(fm: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fm, lifecycle) {
        override fun getItemCount(): Int {
            return 2
        }
        
        override fun createFragment(position: Int): Fragment {
            return PhotoGridFragment.newInstance(position, profileId)
        }
    }
}