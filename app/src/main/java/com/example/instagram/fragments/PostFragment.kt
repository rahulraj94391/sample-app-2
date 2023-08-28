package com.example.instagram.fragments

import android.os.Bundle
import android.util.Log
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
import com.example.instagram.adapters.SelectedPostPicAdapter
import com.example.instagram.databinding.FragmentPostBinding
import com.example.instagram.viewmodels.PostFragViewModel

private const val TAG = "PostFragment_CommTag"

class PostFragment : Fragment() {
    private lateinit var binding: FragmentPostBinding
    private lateinit var viewModel: PostFragViewModel
    private lateinit var selectedPicAdapter: SelectedPostPicAdapter
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_post, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[PostFragViewModel::class.java]
        Log.d(TAG, "selected images size = ${viewModel.postImagesUri.size}")
        selectedPicAdapter = SelectedPostPicAdapter(viewModel.postImagesUri, ::addNewPhoto, ::deletePhoto, ::showHideListAndInstruction)
        binding.recyclerView.apply {
            adapter = selectedPicAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
        showHideListAndInstruction(viewModel.postImagesUri.size)
        
        binding.addMorePhotoBtn.setOnClickListener {
            resultLauncherInitial.launch("image/*")
        }
        binding.instruction.setOnClickListener {
            resultLauncherInitial.launch("image/*")
        }
        binding.btnPost.setOnClickListener {
            val action = PostFragmentDirections.actionPostFragmentToCaptionTagFragment()
            findNavController().navigate(action)
        }
    }
    
    private fun addNewPhoto() {
        resultLauncherInitial.launch("image/*")
    }
    
    private fun deletePhoto(pos: Int) {
        selectedPicAdapter.deleteImage(pos)
    }
    
    private fun showHideListAndInstruction(size: Int) {
        binding.addMorePhotoBtn.isEnabled = (size <= 5)
        binding.btnPost.isEnabled = (size > 0)
        if (size < 1) { // hide recycler view, addMorePhotosBtn and countPost + show instruction TextView
            binding.apply {
                photoCount.visibility = View.GONE
                recyclerView.visibility = View.GONE
                addMorePhotoBtn.visibility = View.GONE
                instruction.visibility = View.VISIBLE
            }
        } else { // show recycler view, addMorePhotosBtn and countPost + hide instruction TextView
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

