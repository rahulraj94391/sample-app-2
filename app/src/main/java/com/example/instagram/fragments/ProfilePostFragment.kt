package com.example.instagram.fragments

import android.os.Bundle
import android.util.Log
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.HomeActivity
import com.example.instagram.MainViewModel
import com.example.instagram.R
import com.example.instagram.adapters.PostListAdapter
import com.example.instagram.database.AppDatabase
import com.example.instagram.databinding.FragmentProfilePostBinding
import com.example.instagram.viewModelFactory.ProfilePostViewModelFactory
import com.example.instagram.viewmodels.ProfilePostViewModel
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

private const val TAG = "ProfilePostFragment_CommTag"

class ProfilePostFragment : Fragment() {
    private lateinit var binding: FragmentProfilePostBinding
    private val args: ProfilePostFragmentArgs by navArgs()
    private var profileId: Long by Delegates.notNull()
    private var openPosition: Int by Delegates.notNull()
    private var type: Int by Delegates.notNull()
    private lateinit var viewModel: ProfilePostViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var db: AppDatabase
    private lateinit var postsAdapter: PostListAdapter
    
    
    // recycler view vars to load more data
    var isScrolling = false
    var currentItems: Int = 0
    var totalItems: Int = 0
    var scrolledOut: Int = 0
    
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileId = args.profileId
        openPosition = args.pos
        type = args.type
        Log.i(TAG, "profileId = $profileId\nopenPosition = $openPosition")
        db = AppDatabase.getDatabase(requireContext())
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        viewModel = ViewModelProvider(this, ProfilePostViewModelFactory(profileId, requireActivity().application))[ProfilePostViewModel::class.java]
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val limit = if ((openPosition + 1) <= 5) 5 else (openPosition + 1)
        viewModel.addPostsToList(profileId, type, limit, 0)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_post, container, false)
        return binding.root
    }
    
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationIcon(R.drawable.arrow_back_24)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        val showMoreBtn = mainViewModel.loggedInProfileId!! == profileId && type == 0
        postsAdapter = PostListAdapter(viewModel.listOfPosts, showMoreBtn, ::openCommentBottomSheet, ::openProfile, ::onLikeClicked, ::onSavePostClicked, ::commentCountDelegate, ::deletePostDialog)
        
        binding.postRV.apply {
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
        
        viewModel.newPostsLoaded.observe(viewLifecycleOwner) {
            if (it < 1) return@observe
            Log.d(TAG, "new posts loaded = $it")
            binding.loadingProgressBar.visibility = View.GONE
            binding.postRV.visibility = View.VISIBLE
            
            if (it > 0) {
                postsAdapter.notifyNewPostsAdded(it)
            }
            
            if (viewModel.isFirstTime) {
                viewModel.isFirstTime = false
                Log.d(TAG, "is layout manager null? = ${binding.postRV.layoutManager == null}")
                binding.postRV.layoutManager?.scrollToPosition(openPosition)
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
    
    
    /*private fun showDeleteDialog(pos: Int) {
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
    }*/
    
    private fun deletePostDialog(pos: Int) {
        MaterialAlertDialogBuilder(requireContext()).setMessage("Delete this post ?").setCancelable(true).setPositiveButton("Yes") { _, _ ->
            lifecycleScope.launch {
                deletePost(pos)
            }
        }.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.cancel()
        }.show()
    }
    
    private fun deletePost(pos: Int) {
        postsAdapter.notifyItemRemoved(pos)
        lifecycleScope.launch {
            val postId = viewModel.listOfPosts[pos].postId
            viewModel.deletePost(postId)
            viewModel.listOfPosts.removeAt(pos)
            if (postsAdapter.itemCount < 1) {
                findNavController().navigateUp()
            }
        }
    }
    
    private fun openCommentBottomSheet(pos: Int) {
        val postId: Long = postsAdapter.getPostId(pos)
        val action = ProfilePostFragmentDirections.actionProfilePostFragmentToCommentSheet(postId)
        findNavController().navigate(action)
        (requireActivity() as HomeActivity).haptics.light()
    }
    
    private fun openProfile(pos: Int) {
        val profileId: Long = postsAdapter.getProfileId(pos)
        val action = ProfilePostFragmentDirections.actionProfilePostFragmentToProfileFragment(profileId, false, -1)
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
            // view.setButtonIconTintList()
            MaterialCheckBox.STATE_UNCHECKED
        }
        
        lifecycleScope.launch {
            delay(100)
            val savePayload = PostListAdapter.SavePayload(postId, newState)
            postsAdapter.notifyItemChanged(pos, savePayload)
        }
        
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.listOfPosts.clear()
        postsAdapter.notifyItemRangeRemoved(0, postsAdapter.itemCount)
    }
    
}