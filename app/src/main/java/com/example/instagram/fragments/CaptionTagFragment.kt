package com.example.instagram.fragments

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.instagram.R
import com.example.instagram.databinding.FragmentCaptionTagBinding
import com.example.instagram.viewmodels.PostFragViewModel

private const val TAG = "CaptionTagFragment_CommTag"

class CaptionTagFragment : Fragment() {
    private var _binding: FragmentCaptionTagBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PostFragViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[PostFragViewModel::class.java]
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_caption_tag, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.postDesc.addTextChangedListener(CustomTextWatcher())
        binding.toolbarCreatePost.setNavigationIcon(R.drawable.arrow_back_24)
        binding.toolbarCreatePost.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.tagPeopleCard.setOnClickListener {
            val action = CaptionTagFragmentDirections.actionCaptionTagFragmentToTagFragment()
            findNavController().navigate(action)
        }
        
        binding.tagPeopleCount.text = when (viewModel.finalTags.size) {
            0 -> "None"
            1 -> viewModel.finalTags[0].username
            else -> "${viewModel.finalTags.size} users"
        }
        
        binding.btnPost.setOnClickListener {
            onPostClicked()
        }
    }
    
    private fun onPostClicked() {
        val action = CaptionTagFragmentDirections.actionCaptionTagFragmentToUploadDialog()
        findNavController().navigate(action)
        viewModel.finalTextToUpload = binding.postDesc.text.toString()
        viewModel.insertPost()
    }
    
    inner class CustomTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            
        }
        
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            
        }
        
        override fun afterTextChanged(s: Editable?) {
            binding.btnPost.isEnabled = s?.isEmpty() != true && isInternetActive(requireContext())
        }
    }
    
    private fun isInternetActive(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
            return true
        }
        return false
    }
    
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}