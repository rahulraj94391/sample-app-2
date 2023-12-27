package com.example.instagram.screen_profilePostListView

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.HomeActivity
import com.example.instagram.R
import com.example.instagram.common.MainViewModel
import com.example.instagram.common.adapter.PostListAdapter
import com.example.instagram.data.AppDatabase
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class ProfilePostImagesFragment : Fragment() {
    private val args: ProfilePostImagesFragmentArgs by navArgs()
    private var profileId: Long by Delegates.notNull()
    private var openPosition: Int by Delegates.notNull()
    private var type: Int by Delegates.notNull()
    private lateinit var viewModel: ProfilePostImagesViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var db: AppDatabase
    private lateinit var postsAdapter: PostListAdapter
    
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var postRecyclerView: RecyclerView
    private lateinit var progressBar: CircularProgressIndicator
    
    // recycler suggestionList vars to load more data
    var isScrolling = false
    var currentItems: Int = 0
    var totalItems: Int = 0
    var scrolledOut: Int = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileId = args.profileId
        openPosition = args.pos
        type = args.type
        db = AppDatabase.getDatabase(requireContext())
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        viewModel = ViewModelProvider(this, ProfilePostViewModelFactory(profileId, requireActivity().application))[ProfilePostImagesViewModel::class.java]
        val showMoreBtn = mainViewModel.loggedInProfileId!! == profileId && type == 0
        postsAdapter = PostListAdapter(viewModel.listOfPosts, showMoreBtn, ::openCommentBottomSheet, ::openProfile, ::onLikeClicked, ::onSavePostClicked, ::commentCountDelegate, ::openPostsFromSamePlaceId, ::openHashTag, ::deletePostDialog)
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_profile_post, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        toolbar = view.findViewById(R.id.toolbar)
        postRecyclerView  = view.findViewById(R.id.postRV)
        progressBar = view.findViewById(R.id.loadingProgressBar)
        
        toolbar.apply {
            setNavigationIcon(R.drawable.arrow_back_24)
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }
        
        postRecyclerView.apply {
            adapter = postsAdapter
            val llManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            layoutManager = llManager
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        isScrolling = true
                    }
                }
                
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    currentItems = llManager.childCount
                    totalItems = llManager.itemCount
                    scrolledOut = llManager.findFirstVisibleItemPosition()
                    if (isScrolling && (currentItems + scrolledOut == totalItems)) {
                        isScrolling = false
                        viewModel.addPostsToList(profileId, type, 5, postsAdapter.itemCount)
                    }
                }
            })
        }
    }
    
    override fun onResume() {
        super.onResume()
        val limit = if ((openPosition + 1) <= 5) 5 else (openPosition + 1)
        if (postsAdapter.itemCount < 1) {
            viewModel.addPostsToList(profileId, type, limit, 0)
        }
        
        viewModel.newPostsLoaded.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                if (viewModel.isFirstTime) delay(120)
                
                if (postsAdapter.itemCount < 1) {
                    progressBar.visibility = View.VISIBLE
                    return@launch
                }
                progressBar.visibility = View.GONE
                postRecyclerView.visibility = View.VISIBLE
                
                if (viewModel.isFirstTime) {
                    viewModel.isFirstTime = false
                    postRecyclerView.layoutManager?.scrollToPosition(openPosition)
                }
            }
        }
    }
    
    private fun commentCountDelegate(tv: TextView, postId: Long) {
        db.commentDao().commentCount(postId).observe(viewLifecycleOwner) {
            tv.text = if (it > 1) {
                "$it comments"
            } else {
                "$it comment"
            }
        }
    }
    
    private fun openPostsFromSamePlaceId(placeId: String?) {
        if (placeId == null) return
        val action =
            ProfilePostImagesFragmentDirections.actionProfilePostFragmentToSameLocationPhotosFragment(placeId)
        findNavController().navigate(action)
    }
    
    private fun openHashTag(hashTag: String) {
        val action = ProfilePostImagesFragmentDirections.actionProfilePostFragmentToHashTagFragment(hashTag)
        findNavController().navigate(action)
    }
    
    private fun deletePostDialog(pos: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage("Delete this post ?")
            .setCancelable(true)
            .setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launch {
                    deletePost(pos)
                }
            }.setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }.show()
    }
    
    private fun deletePost(pos: Int) {
        val postId = viewModel.listOfPosts[pos].postId
        viewModel.listOfPosts.removeAt(pos)
        postsAdapter.notifyItemRemoved(pos)
        
        lifecycleScope.launch {
            viewModel.deletePost(postId)
            if (postsAdapter.itemCount < 1) {
                findNavController().navigateUp()
            }
        }
    }
    
    private fun openCommentBottomSheet(pos: Int) {
        val postId: Long = postsAdapter.getPostId(pos)
        val action =
            ProfilePostImagesFragmentDirections.actionProfilePostFragmentToCommentSheet(postId)
        findNavController().navigate(action)
        (requireActivity() as HomeActivity).haptics.light()
    }
    
    private fun openProfile(pos: Int) {
        val profileId: Long = postsAdapter.getProfileId(pos)
        val action =
            ProfilePostImagesFragmentDirections.actionProfilePostFragmentToProfileFragment(profileId, false, -1)
        findNavController().navigate(action)
    }
    
    private fun onLikeClicked(pos: Int, view: View) {
        val checkedState = (view as MaterialCheckBox).checkedState
        val postId = postsAdapter.getPostId(pos)
        val newState = if (checkedState == MaterialCheckBox.STATE_CHECKED) {
            (requireActivity() as HomeActivity).haptics.light()
            viewModel.likePost(postId, mainViewModel.loggedInProfileId!!)
            MaterialCheckBox.STATE_CHECKED
        } else {
            viewModel.removeLike(postId, mainViewModel.loggedInProfileId!!)
            MaterialCheckBox.STATE_UNCHECKED
        }
        
        lifecycleScope.launch {
            delay(100)
            val likeString = viewModel.getFormattedLikeCount(postId)
            val likePayload = PostListAdapter.LikePayload(likeString, postId, newState)
            postsAdapter.notifyItemChanged(pos, likePayload)
        }
    }
    
    private fun onSavePostClicked(pos: Int, view: View) {
        val checkedState = (view as MaterialCheckBox).checkedState
        val postId = postsAdapter.getPostId(pos)
        val newState = if (checkedState == MaterialCheckBox.STATE_CHECKED) {
            (requireActivity() as HomeActivity).haptics.light()
            viewModel.savePost(mainViewModel.loggedInProfileId!!, postId)
            MaterialCheckBox.STATE_CHECKED
        } else {
            viewModel.removeSavedPost(mainViewModel.loggedInProfileId!!, postId)
            MaterialCheckBox.STATE_UNCHECKED
        }
        
        lifecycleScope.launch {
            delay(100)
            val savePayload = PostListAdapter.SavePayload(postId, newState)
            postsAdapter.notifyItemChanged(pos, savePayload)
        }
    }
}