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
import com.example.instagram.MainViewModel
import com.example.instagram.R
import com.example.instagram.adapters.PostAdapter
import com.example.instagram.databinding.FragmentOnePostBinding
import com.example.instagram.viewmodels.OnePostFragViewModel
import com.google.android.material.button.MaterialButton
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

private const val POST_ID = "postId"
private const val TAG = "CommTag_OnePostFragment"

class OnePostFragment : Fragment() {
    private lateinit var binding: FragmentOnePostBinding
    private var postId: Long by Delegates.notNull()
    private lateinit var viewModel: OnePostFragViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var postPhotoAdapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postId = it.getLong(POST_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProvider(this)[OnePostFragViewModel::class.java]
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_one_post, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            postDesc.setOnClickListener { onDescClicked(it as TextView) }
            comment.setOnClickListener { onCommentClicked() }
            viewAllComments.setOnClickListener { onCommentClicked() }
            btnSavePost.setOnClickListener { onSavePostClicked() }
            likeBtn.setOnClickListener { onLikeClicked(it as MaterialButton) }
        }

        postPhotoAdapter = PostAdapter()
        binding.allImagesInAPostVP2.adapter = postPhotoAdapter


        lifecycleScope.launch {
            viewModel.getProfilePicture(mainViewModel.loggedInProfileId!!)
            viewModel.getPostImages(postId)
        }


        viewModel.profileImageUrl.observe(viewLifecycleOwner) {
            // TODO: REMOVE PICASSO
            Picasso.get().load(it).resize(240, 240).centerCrop().into(binding.profileImage)
        }
        viewModel.postImagesUrl.observe(viewLifecycleOwner) {
            // add VP2 adapter new list here
            postPhotoAdapter.setNewList(it)
            Log.d(TAG, "Post Photots RAHUL = $it")
        }

    }

    private fun onSavePostClicked() {

    }

    private fun onDescClicked(view: TextView) {
        view.ellipsize = null
        view.maxLines = Int.MAX_VALUE
    }

    private fun onLikeClicked(it: MaterialButton) {

    }

    private fun onCommentClicked() {

    }
}