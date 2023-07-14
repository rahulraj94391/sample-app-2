package com.example.instagram.fragments

import android.content.Context
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
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

const val LIST_REF_KEY: String = "list_ref"
const val USER_PROF_KEY = "userProf"

private const val TAG = "CommTag_PhotoGridFragment"

class PhotoGridFragment : Fragment() {
    private lateinit var binding: FragmentPhotoGridBinding
    private lateinit var viewModel: PhotoGridFragViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var userPostedPhotoAdapter: PhotoGridAdapter
    private var listRef: Int by Delegates.notNull()
    private var userProfId: Long by Delegates.notNull()

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

    override fun onAttach(context: Context) {
        super.onAttach(context)
//        Log.d(TAG, "onAttach")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Log.d(TAG, "onCreate")
        requireArguments().let {
            listRef = it.getInt(LIST_REF_KEY)
            userProfId = it.getLong(USER_PROF_KEY)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        Log.d(TAG, "onCreateView")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_photo_grid, container, false)
        return binding.root
    }

    private fun onPostClicked(postId: Long) {
        Log.d(TAG, "onPostClicked - photo grid fragment")
        requireActivity().supportFragmentManager.setFragmentResult(POST_ID_OPEN_REQ_KEY, bundleOf(POST_ID_REF_KEY to postId))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[PhotoGridFragViewModel::class.java]
        userPostedPhotoAdapter = PhotoGridAdapter(this::onPostClicked)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        if (listRef == 0) {
            lifecycleScope.launch {
                viewModel.getProfilePost(userProfId)
            }
            viewModel.usersPost.observe(viewLifecycleOwner) {
                userPostedPhotoAdapter.setNewList(it)
            }
        }
        else {
            lifecycleScope.launch {
                viewModel.getAllPostInWhichProfileIsTagged(userProfId)
            }
            viewModel.usersTaggedPost.observe(viewLifecycleOwner) {
                userPostedPhotoAdapter.setNewList(it)
            }
        }

        binding.profilePosts.adapter = userPostedPhotoAdapter
        binding.profilePosts.layoutManager = GridLayoutManager(requireContext(), 3)
    }

//    override fun onStart() {
//        super.onStart()
//        Log.d(TAG, "onStart")
//    }
//
//    override fun onResume() {
//        super.onResume()
//        Log.d(TAG, "onResume")
//    }
//
//    override fun onPause() {
//        super.onPause()
//        Log.d(TAG, "onPause")
//    }
//
//    override fun onStop() {
//        super.onStop()
//        Log.d(TAG, "onStop")
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        Log.d(TAG, "onDestroyView")
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        Log.d(TAG, "onDestroy")
//    }
//
//    override fun onDetach() {
//        super.onDetach()
//        Log.d(TAG, "onDetach")
//    }
}