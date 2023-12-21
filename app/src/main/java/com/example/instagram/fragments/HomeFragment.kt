package com.example.instagram.fragments

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.HomeActivity
import com.example.instagram.MainViewModel
import com.example.instagram.MyApplication
import com.example.instagram.R
import com.example.instagram.adapters.PostListAdapter
import com.example.instagram.databinding.FragmentHomeBinding
import com.example.instagram.di.HomeScreenDependencies
import com.example.instagram.domain.util.LikeFormatter
import com.example.instagram.viewmodels.HomeFragViewModel
import com.google.android.material.checkbox.MaterialCheckBox
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


private const val TAG = "HomeFragment_CommTag"


class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var homeAdapter: PostListAdapter
    private lateinit var homeViewModel: HomeFragViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var container: HomeScreenDependencies
    
    // recycler suggestionList vars to load more data
    var isScrolling = false
    var currentItems: Int = 0
    var totalItems: Int = 0
    var scrolledOut: Int = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        
        with(HomeScreenDependencies(requireActivity().application, mainViewModel.loggedInProfileId!!)) {
            (requireActivity().application as MyApplication).appContainer.homeScreenDependencies = this
            container = this
            homeViewModel = ViewModelProvider(this@HomeFragment, viewModelFactory)[HomeFragViewModel::class.java]
        }
        
        homeAdapter = PostListAdapter(
            homeViewModel.listOfPosts,
            false,
            ::openCommentBottomSheet,
            ::openProfile,
            ::onLikeClicked,
            ::onSavePostClicked,
            ::commentCountDelegate,
            ::openPostsFromSamePlaceId,
            ::openHashTag
        ) {
            // do nothing here, we are not showing "option" btn on home screen, so no delete functionality is here.
        }
        
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (homeViewModel.isFirstTime) {
            homeViewModel.isFirstTime = false
            homeViewModel.addNewPostToList(mainViewModel.loggedInProfileId!!, 5, homeAdapter.itemCount)
        }
        binding.btnMessages.setOnClickListener { whenMessagesBtnClicked() }
        binding.btnNotifications.setOnClickListener { whenNotificationBtnClicked() }
        
        binding.homeRV.apply {
            val llManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = homeAdapter
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
                        homeViewModel.addNewPostToList(mainViewModel.loggedInProfileId!!, 5, homeAdapter.itemCount)
                    }
                }
            })
        }
        
        homeViewModel.newPostsLoaded.observe(viewLifecycleOwner) {
            binding.loadingProgressBar.visibility = View.GONE
            binding.homeRV.visibility = View.VISIBLE
            if (it > 0) homeAdapter.notifyNewPostsAdded(it)
            if (homeAdapter.itemCount < 1) binding.followToSeeFeed.visibility = View.VISIBLE
            else binding.followToSeeFeed.visibility = View.GONE
        }
        
        mainViewModel.reloadHomeFeed.observe(viewLifecycleOwner) {
            if (!it) return@observe
            homeAdapter.clearList()
            homeViewModel.addNewPostToList(mainViewModel.loggedInProfileId!!, 5, homeAdapter.itemCount)
            mainViewModel.reloadHomeFeed.postValue(false)
        }
    }
    
    private fun commentCountDelegate(tv: TextView, postId: Long) {
        container.commentRepo.commentCount(postId).observe(viewLifecycleOwner) {
            tv.text = if (it > 1) {
                "$it comments"
            } else {
                "$it comment"
            }
        }
    }
    
    private fun openProfile(pos: Int) {
        val profileId: Long = homeAdapter.getProfileId(pos)
        val action = HomeFragmentDirections.actionHomeFragmentToProfileFragment(profileId, false, -1)
        findNavController().navigate(action)
    }
    
    private fun openPostsFromSamePlaceId(placeId: String?) {
        if (placeId == null) return
        val action = HomeFragmentDirections.actionHomeFragmentToSameLocationPhotosFragment(placeId)
        findNavController().navigate(action)
    }
    
    private fun onLikeClicked(pos: Int, view: View) {
        val checkedState = (view as MaterialCheckBox).checkedState
        val postId = homeAdapter.getPostId(pos)
        val newState = if (checkedState == MaterialCheckBox.STATE_CHECKED) {
            (requireActivity() as HomeActivity).haptics.light()
            homeViewModel.likePost(postId, mainViewModel.loggedInProfileId!!)
            MaterialCheckBox.STATE_CHECKED
        } else {
            homeViewModel.removeLike(postId, mainViewModel.loggedInProfileId!!)
            MaterialCheckBox.STATE_UNCHECKED
        }
        
        lifecycleScope.launch {
            delay(100) // use job to check if the instance is still active or not, this is not appropriate method to do.
            val likeString = LikeFormatter.getFormattedLikeCount(postId, container.likeRepo)
            val likePayload = PostListAdapter.LikePayload(likeString, postId, newState)
            homeAdapter.notifyItemChanged(pos, likePayload)
        }
    }
    
    private fun onSavePostClicked(pos: Int, view: View) {
        val checkedState = (view as MaterialCheckBox).checkedState
        val postId = homeAdapter.getPostId(pos)
        val newState = if (checkedState == MaterialCheckBox.STATE_CHECKED) {
            (requireActivity() as HomeActivity).haptics.light()
            homeViewModel.savePost(mainViewModel.loggedInProfileId!!, postId)
            MaterialCheckBox.STATE_CHECKED
        } else {
            homeViewModel.removeSavedPost(mainViewModel.loggedInProfileId!!, postId)
            MaterialCheckBox.STATE_UNCHECKED
        }
        
        lifecycleScope.launch {
            delay(100)
            val savePayload = PostListAdapter.SavePayload(postId, newState)
            homeAdapter.notifyItemChanged(pos, savePayload)
        }
    }
    
    private fun openCommentBottomSheet(pos: Int) {
        val postId: Long = homeAdapter.getPostId(pos)
        val action = HomeFragmentDirections.actionHomeFragmentToCommentSheet(postId)
        findNavController().navigate(action)
        (requireActivity() as HomeActivity).haptics.light()
    }
    
    private fun whenMessagesBtnClicked() {
        if (findNavController().currentDestination?.id != R.id.homeFragment) return
        val action = HomeFragmentDirections.actionHomeFragmentToLatestChatsFragment()
        findNavController().navigate(action)
    }
    
    private fun openHashTag(hashTag: String) {
        if (findNavController().currentDestination?.id != R.id.homeFragment) return
        val action = HomeFragmentDirections.actionHomeFragmentToHashTagFragment(hashTag)
        findNavController().navigate(action)
    }
    
    private fun whenNotificationBtnClicked() {
        if (findNavController().currentDestination?.id != R.id.homeFragment) return
        val action = HomeFragmentDirections.actionHomeFragmentToNotificationFragment()
        findNavController().navigate(action)
    }
    
    override fun onDestroyView() {
        binding.homeRV.layoutManager = null
        super.onDestroyView()
    }
    
    override fun onDestroy() {
        (requireActivity().application as MyApplication).appContainer.homeScreenDependencies = null
        super.onDestroy()
    }
}
