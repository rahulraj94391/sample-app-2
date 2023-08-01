package com.example.instagram.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.example.instagram.DateTime
import com.example.instagram.ImageUtil
import com.example.instagram.MainViewModel
import com.example.instagram.R
import com.example.instagram.TimeFormatting
import com.example.instagram.adapters.PostAdapter
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.entity.Likes
import com.example.instagram.database.entity.SavedPost
import com.example.instagram.databinding.FragmentOnePostBinding
import com.example.instagram.viewmodels.OnePostFragViewModel
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

private const val POST_ID = "postId"
private const val TAG = "CommTag_OnePostFragment"

class OnePostFragment : Fragment() {
    private lateinit var binding: FragmentOnePostBinding
    private var postId: Long by Delegates.notNull()
    private lateinit var viewModel: OnePostFragViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var postPhotoAdapter: PostAdapter
    private lateinit var db: AppDatabase
    private val args: OnePostFragmentArgs? by navArgs()
    private var profileId = 0.toLong()
    private lateinit var imageUtil: ImageUtil
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())
        postId = args!!.postId
        lifecycleScope.launch {
            profileId = db.postDao().getProfileId(postId)
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        db = AppDatabase.getDatabase(requireContext())
        viewModel = ViewModelProvider(this)[OnePostFragViewModel::class.java]
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_one_post, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            postDesc.setOnClickListener { onDescClicked(it as TextView) }
            comment.setOnClickListener { onCommentClicked() }
            commentCount.setOnClickListener { onCommentClicked() }
            btnSavePost.setOnClickListener { onSavePostClicked(it as MaterialCheckBox) }
            likeBtn.setOnClickListener { onLikeClicked(it as MaterialCheckBox) }
        }
        imageUtil = ImageUtil(requireContext())
        postPhotoAdapter = PostAdapter()
        binding.allImagesInAPostVP2.adapter = postPhotoAdapter
        TabLayoutMediator(binding.indicatorVP, binding.allImagesInAPostVP2) { _, _ -> }.attach()
        
        lifecycleScope.launch {
            with(viewModel) {
                getProfilePictureByPostId(postId)
                getPostImages(postId)
                getCommentCount(postId)
                getLikeCount(postId)
            }
        }
        
        lifecycleScope.launch {
            val details = viewModel.getPostDetails(postId, mainViewModel.loggedInProfileId!!)
            withContext(Dispatchers.Main) {
                binding.apply {
                    likeBtn.isChecked = details.isPostAlreadyLiked
                    // setLikeColorAsPerState(likeBtn, details.isPostAlreadyLiked)
                    btnSavePost.isChecked = details.isPostAlreadySaved
                    postDesc.text = details.postText
                    timeOfPost.text = DateTime.timeFormatter(details.postTime, TimeFormatting.POST)
                    username.text = details.profileName
                }
            }
        }
        
        binding.profileImage.setOnClickListener {
            openProfile()
        }
        
        binding.allImagesInAPostVP2.setOnClickListener {
            viewPagerDoubleClicked(it as ViewPager2)
        }
        
        
        viewModel.profileImageUrl.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            CoroutineScope(Dispatchers.IO).launch {
                val bitmap = imageUtil.getBitmap(it)
                withContext(Dispatchers.Main) {
                    binding.profileImage.setImageBitmap(bitmap)
                }
            }
        }
        
        viewModel.postImagesUrl.observe(viewLifecycleOwner) {
            // add VP2 adapter new list here
            postPhotoAdapter.setNewList(it)
        }
        
        viewModel.likeCount.observe(viewLifecycleOwner) {
            binding.likeCount.text = "$it likes"
        }
        
        viewModel.commentCount.observe(viewLifecycleOwner) {
            binding.commentCount.text = when (it) {
                0 -> "0 comment"
                1 -> "View 1 comment"
                else -> "View all $it comments"
            }
        }
    }
    
    private fun openProfile() {
        /*val a = OnePostFragmentDirections.actionOnePostFragmentToSearchDetailFragment(profileId)
        findNavController().navigate(a)*/
    }
    
    private fun openProfileFromFrag() {
    
    }
    
    private fun viewPagerDoubleClicked(viewPager2: ViewPager2) {
        // Todo: double tap on viewpage to like post
    }
    
    private fun onSavePostClicked(it: MaterialCheckBox) {
        if (it.isChecked) {
            lifecycleScope.launch {
                db.savedPostDao().savePost(
                    SavedPost(
                        mainViewModel.loggedInProfileId!!,
                        postId,
                        System.currentTimeMillis()
                    )
                )
            }
        } else {
            lifecycleScope.launch {
                db.savedPostDao().deleteSavedPost(postId, mainViewModel.loggedInProfileId!!)
            }
        }
    }
    
    private fun onDescClicked(view: TextView) {
        view.ellipsize = null
        view.maxLines = Int.MAX_VALUE
    }
    
    private fun onLikeClicked(it: MaterialCheckBox) {
        if (it.isChecked) {
            //            setLikeColorAsPerState(it, true)
            lifecycleScope.launch {
                db.likesDao().insertNewLike(
                    Likes(
                        postId,
                        mainViewModel.loggedInProfileId!!,
                        System.currentTimeMillis()
                    )
                )
                viewModel.getLikeCount(postId)
            }
        } else {
            //            setLikeColorAsPerState(it, false)
            lifecycleScope.launch {
                db.likesDao().deleteLike(mainViewModel.loggedInProfileId!!, postId)
                viewModel.getLikeCount(postId)
            }
        }
    }
    
    private fun setLikeColorAsPerState(it: MaterialCheckBox, state: Boolean) {
        if (state) {
            it.buttonTintList = ColorStateList.valueOf(resources.getColor(R.color.red))
        } else {
            it.buttonTintList = ColorStateList.valueOf(resources.getColor(R.color.black))
        }
    }
    
    private fun onCommentClicked() {
        val action = OnePostFragmentDirections.actionOnePostFragmentToCommentSheet(postId)
        findNavController().navigate(action)
    }
}