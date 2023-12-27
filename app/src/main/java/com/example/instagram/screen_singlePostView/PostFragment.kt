package com.example.instagram.screen_singlePostView

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.example.instagram.DELETE
import com.example.instagram.DEL_POST_REQ_KEY
import com.example.instagram.HIDE_DELETE_BTN
import com.example.instagram.HomeActivity
import com.example.instagram.KEY
import com.example.instagram.OPEN_AND_LOCATE_COMMENT_KEY
import com.example.instagram.POST_POS
import com.example.instagram.R
import com.example.instagram.common.MainViewModel
import com.example.instagram.common.util.DateTime
import com.example.instagram.common.util.ImageUtil
import com.example.instagram.common.util.PostDescParser
import com.example.instagram.common.util.TimeFormatting
import com.example.instagram.data.AppDatabase
import com.example.instagram.data.entity.Likes
import com.example.instagram.data.entity.SavedPost
import com.example.instagram.databinding.FragmentOnePostBinding
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

class PostFragment : Fragment() {
    private lateinit var binding: FragmentOnePostBinding
    private lateinit var viewModel: PostViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var postPhotoAdapter: PostAdapter
    private lateinit var imageUtil: ImageUtil
    private lateinit var db: AppDatabase
    private var postId: Long by Delegates.notNull()
    private var postPos: Int by Delegates.notNull()
    private val args: PostFragmentArgs by navArgs()
    private var profileId = MutableLiveData<Long>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())
        postId = args.postId
        postPos = args.pos
        lifecycleScope.launch {
            val profileId = db.postDao().getProfileId(postId)
            this@PostFragment.profileId.postValue(profileId)
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        db = AppDatabase.getDatabase(requireContext())
        viewModel = ViewModelProvider(this)[PostViewModel::class.java]
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_one_post, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationIcon(R.drawable.arrow_back_24)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.apply {
            comment.setOnClickListener { onCommentClicked() }
            commentCount.setOnClickListener { onCommentClicked() }
            btnSavePost.setOnClickListener { onSavePostClicked(it as MaterialCheckBox) }
            likeBtn.setOnClickListener { onLikeClicked(it as MaterialCheckBox) }
            likeCount.setOnClickListener { likeBtn.performClick() }
        }
        
        profileId.observe(viewLifecycleOwner) {
            if (it == mainViewModel.loggedInProfileId!! && postPos != HIDE_DELETE_BTN) {
                binding.btnMore.visibility = View.VISIBLE
                binding.btnMore.setOnClickListener { moreBtn ->
                    PopupMenu(requireContext(), moreBtn).apply {
                        inflate(R.menu.one_post_menu)
                        setForceShowIcon(true)
                        menu.getItem(0).title = SpannableString(DELETE).apply {
                            setSpan(ForegroundColorSpan(Color.RED), 0, DELETE.length, 0)
                        }
                        setOnMenuItemClickListener {
                            deletePostDialog()
                            true
                        }
                        show()
                    }
                }
            }
        }
        
        imageUtil = ImageUtil(requireContext())
        postPhotoAdapter = PostAdapter()
        binding.allImagesInAPostVP2.adapter = postPhotoAdapter
        binding.counter.text = "${1}/${postPhotoAdapter.itemCount}"
        binding.allImagesInAPostVP2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.counter.text = "${position + 1}/${postPhotoAdapter.itemCount}"
            }
        })
        
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
                    likeBtn.isChecked = details.isPostAlreadyLiked
                    btnSavePost.isChecked = details.isPostAlreadySaved
                    PostDescParser(details.postText, postDesc, ::openHashTag).parsePostDesc()
                    timeOfPost.text = DateTime.timeFormatter(details.postTime, TimeFormatting.POST)
                    username.text = details.profileName
                    if (details.location != null) {
                        locationTag.text = details.location.primaryText
                        locationTag.visibility = View.VISIBLE
                    } else {
                        locationTag.visibility = View.GONE
                    }
                }
                
                
                binding.locationTag.setOnClickListener {
                    val placeId = details.location?.placeId ?: return@setOnClickListener
                    val action =
                        PostFragmentDirections.actionOnePostFragmentToSameLocationPhotosFragment(placeId)
                    findNavController().navigate(action)
                }
            }
        }
        
        binding.profileImage.setOnClickListener {
            openProfile()
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
            if (it.size < 2) binding.counter.visibility = View.INVISIBLE
            postPhotoAdapter.setNewList(it)
        }
        
        viewModel.likeCount.observe(viewLifecycleOwner) {
            binding.likeCount.text = when (it) {
                0, 1 -> "$it like"
                else -> "$it likes"
            }
        }
        
        db.commentDao().commentCount(postId).observe(viewLifecycleOwner) {
            binding.commentCount.text = when (it) {
                0, 1 -> "$it comment"
                else -> "$it comments"
            }
        }
    }
    
    private fun openHashTag(hashTag: String) {
        val action = PostFragmentDirections.actionOnePostFragmentToHashTagFragment(hashTag)
        findNavController().navigate(action)
    }
    
    
    override fun onResume() {
        super.onResume()
        (requireActivity() as HomeActivity).navHostFragment.childFragmentManager.setFragmentResultListener(KEY, requireActivity()) { _, bundle ->
            (requireActivity() as HomeActivity).navHostFragment.childFragmentManager.setFragmentResult(OPEN_AND_LOCATE_COMMENT_KEY, bundle)
            binding.comment.postDelayed({
                onCommentClicked()
                // prepare here FR API B
                
            }, 200)
        }
        registerForContextMenu(binding.btnMore)
    }
    
    private fun openProfile() {
        val action = PostFragmentDirections.actionOnePostFragmentToProfileFragment(profileId.value!!, false, -1)
        findNavController().navigate(action)
    }
    
    override fun onPause() {
        super.onPause()
        unregisterForContextMenu(binding.btnMore)
    }
    
    private fun onSavePostClicked(it: MaterialCheckBox) {
        if (it.isChecked) {
            (requireActivity() as HomeActivity).haptics.light()
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
        if (it.isChecked) {
            (requireActivity() as HomeActivity).haptics.light()
            lifecycleScope.launch {
                db.likesDao().insertNewLike(Likes(postId, mainViewModel.loggedInProfileId!!, System.currentTimeMillis()))
                viewModel.getLikeCount(postId)
            }
        } else {
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
            binding.btnMore.isEnabled = true
        }.show()
        dialog.setOnCancelListener {
            binding.btnMore.isEnabled = true
        }
    }
    
    override fun onDestroy() {
        (requireActivity() as HomeActivity).navHostFragment.childFragmentManager.clearFragmentResultListener(KEY)
        super.onDestroy()
    }
    
    private fun onCommentClicked() {
        if (findNavController().currentDestination?.id != R.id.onePostFragment) return
        (requireActivity() as HomeActivity).haptics.light()
        val action = PostFragmentDirections.actionOnePostFragmentToCommentSheet(postId)
        findNavController().navigate(action)
    }
}