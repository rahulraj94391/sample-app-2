package com.example.instagram.screen_createPost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.R
import com.example.instagram.databinding.FragmentCreatePostBinding


class CreatePostFragment : Fragment() {
    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CreatePostViewModel
    private lateinit var selectedPicAdapter: SelectedPostPicAdapter
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_post, container, false)
        return binding.root
    }
    
    
    override fun onDestroyView() {
        binding.recyclerView.adapter = null
        _binding = null
        super.onDestroyView()
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[CreatePostViewModel::class.java]
        selectedPicAdapter = SelectedPostPicAdapter(viewModel.postImagesUri, ::deletePhoto, ::showHideListAndInstruction)
        binding.recyclerView.apply {
            adapter = selectedPicAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            setItemViewCacheSize(5)
        }
        showHideListAndInstruction(viewModel.postImagesUri.size)
        
        binding.addMorePhotoBtn.setOnClickListener {
            resultLauncherInitial.launch("image/*")
        }
        binding.instruction.setOnClickListener {
            resultLauncherInitial.launch("image/*")
        }
        binding.btnNext.setOnClickListener {
            val action = CreatePostFragmentDirections.actionPostFragmentToCaptionTagFragment()
            findNavController().navigate(action)
        }
    }
    
    private fun deletePhoto(pos: Int) {
        selectedPicAdapter.deleteImage(pos)
    }
    
    private fun showHideListAndInstruction(size: Int) {
        binding.addMorePhotoBtn.isEnabled = (size <= 5)
        binding.btnNext.isEnabled = (size > 0)
        if (size < 1) { // hide recycler suggestionList, addMorePhotosBtn and countPost + show instruction TextView
            binding.apply {
                photoCount.visibility = View.GONE
                recyclerView.visibility = View.GONE
                addMorePhotoBtn.visibility = View.GONE
                instruction.visibility = View.VISIBLE
            }
        } else { // show recycler suggestionList, addMorePhotosBtn and countPost + hide instruction TextView
            binding.apply {
                photoCount.visibility = View.VISIBLE
                instruction.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                addMorePhotoBtn.visibility = View.VISIBLE
            }
        }
        binding.photoCount.text = if (size > 1)
            "$size photos selected"
        else
            "$size photo selected"
    }
    
    private val resultLauncherInitial = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isEmpty()) return@registerForActivityResult
        selectedPicAdapter.addImages(uris.toMutableList())
        //        binding.recyclerView.scrollToPosition(0)
    }
}

