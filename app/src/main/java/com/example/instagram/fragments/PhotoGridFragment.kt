package com.example.instagram.fragments

import android.content.res.Configuration
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireArguments().let {
            listRef = it.getInt(LIST_REF_KEY)
            userProfId = it.getLong(USER_PROF_KEY)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_photo_grid, container, false)
        return binding.root
    }

    private fun onPostClicked(postId: Long) {
        requireActivity().supportFragmentManager.setFragmentResult(POST_ID_OPEN_REQ_KEY, bundleOf(POST_ID_REF_KEY to postId))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
    }

    override fun onResume() {
        super.onResume()
        val spanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            5
        }
        else {
            3
        }
        binding.profilePosts.adapter = userPostedPhotoAdapter
        binding.profilePosts.layoutManager = GridLayoutManager(requireContext(), spanCount)
    }
}