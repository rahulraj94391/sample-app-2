package com.example.instagram.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.MainViewModel
import com.example.instagram.R
import com.example.instagram.adapters.SameLocationAdapter
import com.example.instagram.databinding.FragmentSameLocationPhotosBinding
import com.example.instagram.viewmodels.SameLocationPhotosViewModel
import kotlinx.coroutines.launch

private const val TAG = "SameLocationPhotosFrag_CommTag"

class SameLocationPhotosFragment : Fragment() {
    private lateinit var binding: FragmentSameLocationPhotosBinding
    private lateinit var viewModel: SameLocationPhotosViewModel
    private lateinit var mainViewModel: MainViewModel
    private val args: SameLocationPhotosFragmentArgs by navArgs()
    private lateinit var locationPhotoAdapter: SameLocationAdapter
    private lateinit var placeId: String
    
    // recycler suggestionList vars to load more data
    var isScrolling = false
    var currentItems: Int = 0
    var totalItems: Int = 0
    var scrolledOut: Int = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[SameLocationPhotosViewModel::class.java]
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        Log.d(TAG, "args = ${args.placeId}")
        placeId = args.placeId
        locationPhotoAdapter = SameLocationAdapter(viewModel.postsFromSameLocation, ::onImageClicked)
        
        lifecycleScope.launch {
            viewModel.getPostsOfSameLocation(placeId, mainViewModel.loggedInProfileId!!, locationPhotoAdapter.itemCount)
            viewModel.getLocation(placeId)
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_same_location_photos, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationIcon(R.drawable.arrow_back_24)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.placePicRV.apply {
            val gridLayout = GridLayoutManager(context, 3)
            layoutManager = gridLayout
            adapter = this@SameLocationPhotosFragment.locationPhotoAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        isScrolling = true
                    }
                }
                
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    currentItems = gridLayout.childCount
                    totalItems = gridLayout.itemCount
                    scrolledOut = gridLayout.findFirstVisibleItemPosition()
                    if (isScrolling && (currentItems + scrolledOut == totalItems)) {
                        isScrolling = false
                        lifecycleScope.launch {
                            viewModel.getPostsOfSameLocation(placeId, mainViewModel.loggedInProfileId!!, locationPhotoAdapter.itemCount)
                        }
                    }
                }
            })
        }
        
        viewModel.currentLocation.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            binding.locationTitle.text = it.primaryText
        }
        
        viewModel.postRetrieved.observe(viewLifecycleOwner) {
            if (it == 0 && viewModel.postsFromSameLocation.size < 1) {
                binding.instruction.visibility = View.VISIBLE
            }
            if (it > 0) {
                locationPhotoAdapter.notifyItemRangeInserted(locationPhotoAdapter.itemCount, it)
            }
        }
    }
    
    private fun onImageClicked(pos: Int) {
        Log.d(TAG, "image clicked on $pos position.")
        val postId = viewModel.postsFromSameLocation[pos].postId
        val action =
            SameLocationPhotosFragmentDirections.actionSameLocationPhotosFragmentToOnePostFragment(postId, HIDE_DELETE_BTN)
        findNavController().navigate(action)
    }
}