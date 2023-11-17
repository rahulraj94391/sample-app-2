package com.example.instagram.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.MainViewModel
import com.example.instagram.R
import com.example.instagram.adapters.PhotoGridAdapter
import com.example.instagram.databinding.FragmentPhotoGridBinding
import com.example.instagram.viewmodels.PhotoGridFragViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

const val LIST_REF_KEY: String = "list_ref"
const val USER_PROF_KEY = "userProf"
const val DEL_POST_REQ_KEY = "deletePostReqKey"

private const val TAG = "PhotoGridFragment_CommTag"
//private const val TAG = "MEM_LEAK"

class PhotoGridFragment : Fragment() {
    private var _binding: FragmentPhotoGridBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PhotoGridFragViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var userPostedPhotoAdapter: PhotoGridAdapter
    
    /**
     * If listRef == 0 it means the fragment is a myPost fragment.
     * If listRef == 1 it means the fragment is taggedPost fragment.
     * This reference is used to choose the correct query and get
     * the data in the List<Post> to show to user.
     */
    private var listRef: Int by Delegates.notNull()
    private var userProfId: Long by Delegates.notNull()
    
    // recycler view vars to load more data
    var isScrolling = false
    var currentItems: Int = 0
    var totalItems: Int = 0
    var scrolledOut: Int = 0
    
    companion object {
        fun newInstance(pos: Int, userProfId: Long): PhotoGridFragment {
            val args = Bundle()
            args.putInt(LIST_REF_KEY, pos)
            args.putLong(USER_PROF_KEY, userProfId)
            val fragment = PhotoGridFragment()
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireArguments().let {
            listRef = it.getInt(LIST_REF_KEY)
            userProfId = it.getLong(USER_PROF_KEY)
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_photo_grid, container, false)
        return binding.root
    }
    
    private fun onPostClicked(pos: Int) {
        // val postId = userPostedPhotoAdapter.getPostId(pos)
        // mainViewModel.openPost.postValue(Pair(postId, pos))
        mainViewModel.openPost2.postValue(Pair(listRef, pos))
        
        // old method - results in crash.
        /*(requireActivity() as HomeActivity).navHostFragment.childFragmentManager.setFragmentResult(POST_OPEN_REQ_KEY, bundleOf(POST_ID to postId, POST_POS to pos))*/
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        viewModel = ViewModelProvider(this)[PhotoGridFragViewModel::class.java]
        userPostedPhotoAdapter = PhotoGridAdapter(this::onPostClicked)
        
        mainViewModel.startProfileRefresh.observe(viewLifecycleOwner) {
            if (!it) return@observe
            
            if (listRef == 0) {
                Log.d(TAG, "refresh page 0")
                lifecycleScope.launch {
                    userPostedPhotoAdapter.clearList()
                    viewModel.getProfilePost(userProfId, mainViewModel.loggedInProfileId!!, 0)
                }
            } else if (listRef == 1) {
                Log.d(TAG, "refresh page 1")
                lifecycleScope.launch {
                    userPostedPhotoAdapter.clearList()
                    viewModel.getAllPostWhereProfileIsTagged(userProfId, mainViewModel.loggedInProfileId!!, 0)
                }
            }
        }
        
        
        if (listRef == 0) {
            lifecycleScope.launch {
                delay(50)
                userPostedPhotoAdapter.clearList()
                viewModel.getProfilePost(userProfId, mainViewModel.loggedInProfileId!!, 0)
            }
            
            viewModel.usersPost.observe(viewLifecycleOwner) {
                Log.d(TAG, "Inside observer 0")
                mainViewModel.startProfileRefresh.postValue(false)
                mainViewModel.isProfileRefreshed.postValue(true)
                
                // when refreshed and no new user's picture
                // is fetched then return.
                // if (it.size == userPostedPhotoAdapter.itemCount) {
                //     return@observe
                // }
                
                binding.loadingProgressBar.visibility = View.GONE
                
                // when there is no image to show, then show the
                // instruction label on the screen.
                if (it.size == 0 && userPostedPhotoAdapter.itemCount == 0) {
                    Log.d(TAG, "INS nO post 0")
                    binding.ins1.visibility = View.VISIBLE
                    return@observe
                }
                
                binding.ins1.visibility = View.INVISIBLE
                binding.gridOfPosts.visibility = View.VISIBLE
                
                if (it.size != 0)
                    userPostedPhotoAdapter.addNewPosts(it)
            }
        } else {
            lifecycleScope.launch {
                delay(50)
                userPostedPhotoAdapter.clearList()
                viewModel.getAllPostWhereProfileIsTagged(userProfId, mainViewModel.loggedInProfileId!!, 0)
            }
            viewModel.usersTaggedPost.observe(viewLifecycleOwner) {
                Log.d(TAG, "Inside observer 1")
                binding.loadingProgressBar.visibility = View.GONE
                if (it.size == 0 && userPostedPhotoAdapter.itemCount == 0) {
                    Log.d(TAG, "INS nO post 1")
                    
                    binding.ins2.visibility = View.VISIBLE
                    return@observe
                }
                binding.ins2.visibility = View.INVISIBLE
                binding.gridOfPosts.visibility = View.VISIBLE
                if (it.size != 0)
                    userPostedPhotoAdapter.addNewPosts(it)
            }
        }
        
        
        val spanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 5 else 3
        binding.gridOfPosts.adapter = userPostedPhotoAdapter
        val gridLayoutManager = GridLayoutManager(requireContext(), spanCount)
        binding.gridOfPosts.layoutManager = gridLayoutManager
        binding.gridOfPosts.setHasFixedSize(false)
        binding.gridOfPosts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    isScrolling = true
                }
            }
            
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                currentItems = gridLayoutManager.childCount
                totalItems = gridLayoutManager.itemCount
                scrolledOut = gridLayoutManager.findFirstVisibleItemPosition()
                if (isScrolling && (currentItems + scrolledOut == totalItems)) {
                    isScrolling = false
                    if (listRef == 0) {
                        lifecycleScope.launch {
                            viewModel.getProfilePost(userProfId, mainViewModel.loggedInProfileId!!, userPostedPhotoAdapter.itemCount)
                        }
                        
                    } else {
                        lifecycleScope.launch {
                            viewModel.getAllPostWhereProfileIsTagged(userProfId, mainViewModel.loggedInProfileId!!, userPostedPhotoAdapter.itemCount)
                        }
                    }
                }
            }
        })
        
    }
    
    fun onUserUnblocked() {
    
    }
    
    override fun onStart() {
        super.onStart()
        requireActivity().supportFragmentManager.setFragmentResultListener(DEL_POST_REQ_KEY, requireActivity()) { _, bundle ->
            val pos = bundle.getInt(POST_POS)
            userPostedPhotoAdapter.deletePostAt(pos)
        }
    }
    
    override fun onDestroyView() {
        binding.gridOfPosts.adapter = null
        super.onDestroyView()
    }
}