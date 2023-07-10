package com.example.instagram.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.instagram.R
import com.example.instagram.adapters.PhotoGridAdapter
import com.example.instagram.databinding.FragmentProfilePostBinding
import com.example.instagram.viewmodels.ProfileViewModel
import kotlin.properties.Delegates

const val LIST_REF: String = "list_ref"

private const val TAG = "CommTag_PhotoGridFragment"

class PhotoGridFragment : Fragment() {
    private lateinit var binding: FragmentProfilePostBinding
    private lateinit var viewModel: ProfileViewModel
    private lateinit var userPostedPhotoAdapter: PhotoGridAdapter
    private var listRef: Int by Delegates.notNull()


    companion object {
        fun newInstance(pos: Int): PhotoGridFragment {
            val args = Bundle()
            args.putInt(LIST_REF, pos)
            val fragment = PhotoGridFragment()
            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireArguments().let {
            listRef = it.getInt(LIST_REF)
            Log.d(TAG, "onCreate: ")
        }


    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_post, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
        userPostedPhotoAdapter = PhotoGridAdapter()

        if (listRef == 0) {
            viewModel.usersPost.observe(viewLifecycleOwner) {
                userPostedPhotoAdapter.setNewList(it)
            }
        }
        else {
            viewModel.usersTaggedPost.observe(viewLifecycleOwner) {
                userPostedPhotoAdapter.setNewList(it)
            }
        }


        binding.profilePosts.adapter = userPostedPhotoAdapter
        binding.profilePosts.layoutManager = GridLayoutManager(requireContext(), 3)
    }


}