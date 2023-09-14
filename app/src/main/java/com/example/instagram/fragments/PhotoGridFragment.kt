package com.example.instagram.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.instagram.MainViewModel
import com.example.instagram.R
import com.example.instagram.adapters.PhotoGridAdapter
import com.example.instagram.databinding.FragmentPhotoGridBinding
import com.example.instagram.viewmodels.PhotoGridFragViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

const val LIST_REF_KEY: String = "list_ref"
const val USER_PROF_KEY = "userProf"
const val DEL_POST_REQ_KEY = "deletePostReqKey"

private const val TAG = "CommTag_PhotoGridFragment"

class PhotoGridFragment : Fragment() {
    private var _binding: FragmentPhotoGridBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PhotoGridFragViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var userPostedPhotoAdapter: PhotoGridAdapter
    private var listRef: Int by Delegates.notNull()
    private var userProfId: Long by Delegates.notNull()
    private var firebaseFireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    
    
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
        val postId = userPostedPhotoAdapter.getPostId(pos)
        requireActivity().supportFragmentManager.setFragmentResult(POST_OPEN_REQ_KEY, bundleOf(POST_ID to postId, POST_POS to pos))
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        viewModel = ViewModelProvider(this)[PhotoGridFragViewModel::class.java]
        userPostedPhotoAdapter = PhotoGridAdapter(this::onPostClicked)
        
        if (listRef == 0) {
            lifecycleScope.launch {
                viewModel.getProfilePost(userProfId)
            }
            viewModel.usersPost.observe(viewLifecycleOwner) {
                binding.loadingProgressBar.visibility = View.GONE
                if (it.size == 0) {
                    binding.ins1.visibility = View.VISIBLE
                    return@observe
                }
                binding.gridOfPosts.visibility = View.VISIBLE
                userPostedPhotoAdapter.setNewList(it)
            }
        } else {
            lifecycleScope.launch {
                viewModel.getAllPostWhereProfileIsTagged(userProfId)
            }
            viewModel.usersTaggedPost.observe(viewLifecycleOwner) {
                binding.loadingProgressBar.visibility = View.GONE
                if (it.size == 0) {
                    binding.ins2.visibility = View.VISIBLE
                    return@observe
                }
                binding.gridOfPosts.visibility = View.VISIBLE
                userPostedPhotoAdapter.setNewList(it)
            }
        }
        
        
        val spanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 5 else 3
        binding.gridOfPosts.adapter = userPostedPhotoAdapter
        binding.gridOfPosts.layoutManager = GridLayoutManager(requireContext(), spanCount)
        binding.gridOfPosts.setHasFixedSize(true)
    }
    
    override fun onStart() {
        super.onStart()
        requireActivity().supportFragmentManager.setFragmentResultListener(DEL_POST_REQ_KEY, requireActivity()) { _, bundle ->
            val pos = bundle.getInt(POST_POS)
            userPostedPhotoAdapter.deletePostAt(pos)
        }
    }
    
    override fun onResume() {
        super.onResume()
        /*firebaseFireStore.collection("postImages").addSnapshotListener { value, error ->
            if (error != null) {
                Log.w(TAG, "Listen failed.", error)
                return@addSnapshotListener
            }
            Log.d(TAG, "changed snapshot size = ${value!!.size()}")
        }*/
    }
    
    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView: ")
        binding.gridOfPosts.adapter = null
        _binding = null
        super.onDestroyView()
    }
}