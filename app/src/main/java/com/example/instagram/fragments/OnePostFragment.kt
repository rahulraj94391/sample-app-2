package com.example.instagram.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

private const val TAG = "CommTag_OnePostFragment"
const val HIDE_DELETE_BTN = -22

class OnePostFragment : Fragment() {
    private lateinit var binding: FragmentOnePostBinding
    private var postId: Long by Delegates.notNull()
    private var postPos: Int by Delegates.notNull()
    private lateinit var viewModel: OnePostFragViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var postPhotoAdapter: PostAdapter
    private lateinit var db: AppDatabase
    private val args: OnePostFragmentArgs? by navArgs()
    private var profileId = MutableLiveData<Long>()
    private lateinit var imageUtil: ImageUtil
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())
        postId = args!!.postId
        postPos = args!!.pos
        lifecycleScope.launch {
            val profileId = db.postDao().getProfileId(postId)
            this@OnePostFragment.profileId.postValue(profileId)
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
            comment.setOnClickListener { onCommentClicked() }
            commentCount.setOnClickListener { onCommentClicked() }
            btnSavePost.setOnClickListener { onSavePostClicked(it as MaterialCheckBox) }
            likeBtn.setOnClickListener { onLikeClicked(it as MaterialCheckBox) }
            
        }
        
        profileId.observe(viewLifecycleOwner) {
            if (it == mainViewModel.loggedInProfileId!! && postPos != HIDE_DELETE_BTN) {
                binding.btnDeletePost.visibility = View.VISIBLE
                binding.btnDeletePost.setOnClickListener { deleteBtn ->
                    deleteBtn.isEnabled = false
                    deletePostDialog()
                }
            }
        }
        
        imageUtil = ImageUtil(requireContext())
        postPhotoAdapter = PostAdapter()
        binding.allImagesInAPostVP2.adapter = postPhotoAdapter
        TabLayoutMediator(binding.indicatorVP, binding.allImagesInAPostVP2) { _, _ -> }.attach()
        
        lifecycleScope.launch {
            with(viewModel) {
                getProfilePictureByPostId(postId)
                getPostImages(postId)
                getLikeCount(postId)
            }
        }
        
        lifecycleScope.launch {
            val details = viewModel.getPostDetails(postId, mainViewModel.loggedInProfileId!!)
            withContext(Dispatchers.Main) {
                binding.apply {
                    likeBtn.isChecked = details.isPostAlreadyLiked // setLikeColorAsPerState(likeBtn, details.isPostAlreadyLiked)
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
            if (it.size < 2) binding.indicatorVP.visibility = View.INVISIBLE
            postPhotoAdapter.setNewList(it)
        }
        
        viewModel.likeCount.observe(viewLifecycleOwner) {
            binding.likeCount.text = "$it likes"
        }
        
        db.commentDao().commentCount(postId).observe(viewLifecycleOwner) {
            binding.commentCount.text = when (it) {
                0 -> "0 comment"
                1 -> "View 1 comment"
                else -> "View all $it comments"
            }
        }
    }
    
    private fun openProfile() {
        val a = OnePostFragmentDirections.actionOnePostFragmentToProfileFragment(profileId.value!!)
        findNavController().navigate(a)
    }
    
    private fun viewPagerDoubleClicked(viewPager2: ViewPager2) { // Todo: double tap on viewpage to like post
    }
    
    private fun onSavePostClicked(it: MaterialCheckBox) {
        if (it.isChecked) {
            lifecycleScope.launch {
                db.savedPostDao().savePost(SavedPost(mainViewModel.loggedInProfileId!!, postId, System.currentTimeMillis()))
            }
        } else {
            lifecycleScope.launch {
                db.savedPostDao().deleteSavedPost(postId, mainViewModel.loggedInProfileId!!)
            }
        }
    }
    
    private fun onLikeClicked(it: MaterialCheckBox) {
        if (it.isChecked) { // setLikeColorAsPerState(it, true)
            lifecycleScope.launch {
                db.likesDao().insertNewLike(Likes(postId, mainViewModel.loggedInProfileId!!, System.currentTimeMillis()))
                viewModel.getLikeCount(postId)
            }
        } else { // setLikeColorAsPerState(it, false)
            lifecycleScope.launch {
                db.likesDao().deleteLike(mainViewModel.loggedInProfileId!!, postId)
                viewModel.getLikeCount(postId)
            }
        }
    }
    
    private fun deletePostDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext()).setMessage("Delete this post ?").setCancelable(true).setPositiveButton("Yes") { _, _ ->
            lifecycleScope.launch {
                viewModel.deletePost(postId)
                requireActivity().supportFragmentManager.setFragmentResult(DEL_POST_REQ_KEY, bundleOf(POST_POS to postPos))
                findNavController().navigateUp()
            }
        }.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.cancel()
            binding.btnDeletePost.isEnabled = true
        }.show()
        dialog.setOnCancelListener {
            binding.btnDeletePost.isEnabled = true
        }
    }
    
    private fun onCommentClicked() {
        val action = OnePostFragmentDirections.actionOnePostFragmentToCommentSheet(postId)
        findNavController().navigate(action)
    }
}