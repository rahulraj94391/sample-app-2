package com.example.instagram.fragments

import android.os.Bundle
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
import com.example.instagram.adapters.HashTagPhotosAdapter
import com.example.instagram.databinding.FragmentHashTagBinding
import com.example.instagram.viewmodels.HashTagFragViewModel
import kotlinx.coroutines.launch

const val RECYCLER_VIEW_LIMIT = 9

private const val TAG = "HashTagFragment_CommTag"

class HashTagFragment : Fragment() {
    private val args: HashTagFragmentArgs by navArgs()
    private lateinit var binding: FragmentHashTagBinding
    private lateinit var viewmodel: HashTagFragViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var adapter: HashTagPhotosAdapter
    
    // recycler view vars to load more data
    var isScrolling = false
    var currentItems: Int = 0
    var totalItems: Int = 0
    var scrolledOut: Int = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel = ViewModelProvider(this)[HashTagFragViewModel::class.java]
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        adapter = HashTagPhotosAdapter(viewmodel.photos, ::onClick)
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_hash_tag, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationIcon(R.drawable.arrow_back_24)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.tagName.text = "#${args.tagName}"
        lifecycleScope.launch {
            viewmodel.getPosts(args.tagName, mainViewModel.loggedInProfileId!!, RECYCLER_VIEW_LIMIT, adapter.itemCount)
        }
        binding.postFromHashTagRV.apply {
            adapter = this@HashTagFragment.adapter
            val gridLayoutManager = GridLayoutManager(requireContext(), 3)
            layoutManager = gridLayoutManager
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                        lifecycleScope.launch {
                            viewmodel.getPosts(args.tagName, mainViewModel.loggedInProfileId!!, RECYCLER_VIEW_LIMIT, this@HashTagFragment.adapter.itemCount)
                        }
                    }
                }
            })
        }
        
        viewmodel.listOfPostFromSameHashTag.observe(viewLifecycleOwner) {
            adapter.notifyNewPostsAdded(it)
            if (adapter.itemCount > 0) {
                binding.loadingProgressBar.visibility = View.GONE
                binding.postFromHashTagRV.visibility = View.VISIBLE
            } else {
                binding.loadingProgressBar.visibility = View.GONE
                binding.instruction.visibility = View.VISIBLE
            }
        }
        
        
    }
    
    private fun onClick(pos: Int) {
        val action = HashTagFragmentDirections.actionHashTagFragmentToOnePostFragment(viewmodel.photos[pos].postId, HIDE_DELETE_BTN)
        if (findNavController().currentDestination?.id == R.id.hashTagFragment) {
            findNavController().navigate(action)
        }
    }
    
}