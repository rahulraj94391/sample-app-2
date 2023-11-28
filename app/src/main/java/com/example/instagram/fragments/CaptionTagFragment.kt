package com.example.instagram.fragments

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.instagram.MainViewModel
import com.example.instagram.R
import com.example.instagram.databinding.FragmentCaptionTagBinding
import com.example.instagram.viewmodels.PostFragViewModel
import java.util.regex.Pattern
import kotlin.properties.Delegates

private const val TAG = "CaptionTagFragment_CommTag"

class CaptionTagFragment : Fragment() {
    private var _binding: FragmentCaptionTagBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PostFragViewModel
    private lateinit var mainViewModel: MainViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[PostFragViewModel::class.java]
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
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
        
        binding.removeLocation.setOnClickListener {
            viewModel.locationTag = null
            checkAndSetLocation()
        }
        checkAndSetLocation()
        binding.tagLocation.setOnClickListener {
            val action = CaptionTagFragmentDirections.actionCaptionTagFragmentToLocationTagFragment()
            findNavController().navigate(action)
        }
        
        binding.tagPeopleCount.text = when (viewModel.finalTags.size) {
            0 -> "None"
            1 -> viewModel.finalTags[0].username
            else -> "${viewModel.finalTags.size} users"
        }
        
        binding.btnPost.setOnClickListener {
            onPostBtnClicked()
        }
        
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                
                if (binding.tagSuggestions.visibility == View.VISIBLE) {
                    binding.tagSuggestions.visibility = View.GONE
                    return
                }
                
                findNavController().navigateUp()
            }
        })
        
        /*mainViewModel.closeSuggestionList.observe(viewLifecycleOwner) {
            if (!it) return@observe
            Log.d(TAG, "inside close suggestion observer")
            binding.tagSuggestions.visibility = View.GONE
            mainViewModel.closeSuggestionList.postValue(false)
        }*/
    }
    
    private fun checkAndSetLocation() {
        if (viewModel.locationTag == null) {
            binding.removeLocation.visibility = View.INVISIBLE
            binding.locationPrimaryName.text = "Add location"
        } else {
            binding.removeLocation.visibility = View.VISIBLE
            val address = "${viewModel.locationTag!!.primaryText} - ${viewModel.locationTag!!.secondaryText}"
            binding.locationPrimaryName.text = address
        }
    }
    
    private fun onPostBtnClicked() {
        viewModel.finalTextToUpload = binding.postDesc.text.toString()
        viewModel.insertPost()
        Toast.makeText(requireContext(), "Post is being uploaded.", Toast.LENGTH_LONG).show()
        findNavController().popBackStack(R.id.postFragment, true)
    }
    
    inner class CustomTextWatcher : TextWatcher {
        private var lastChar by Delegates.notNull<Char>()
        private val listOfTags = mutableListOf<String>()
        
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//            Log.d(TAG, "beforeTextChanged: $s   start = $start  count = $count  after = $after")
            
        }
        
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//            Log.d(TAG, "onTextChanged: $s   start = $start  before = $before  count = $count")
            lastChar = if (s?.isNotEmpty() == true) s[s.length - 1] else 32.toChar()
        }
        
        override fun afterTextChanged(s: Editable?) {
            val text = s.toString()
            val pattern = Pattern.compile("#[a-zA-Z0-9_]+")
            val matcher = pattern.matcher(text)
            val cursorPosition = binding.postDesc.selectionStart
            while (matcher.find()) {
                if (cursorPosition >= matcher.start() && cursorPosition <= matcher.end()) {
                    val s = matcher.start() + 1
                    val e = matcher.end()
                    val tag = text.subSequence(s, e)
                    Log.i(TAG, "post show suggestions. string = $tag")
                    binding.tagSuggestions.visibility = View.VISIBLE
                    
                    break
                }
            }
            
            val c1 = binding.tagSuggestions.visibility == View.VISIBLE
            val c2 = lastChar !in 'a'..'z'
            val c3 = lastChar !in 'A'..'Z'
            val c4 = lastChar !in '0'..'9'
            val c5 = lastChar != '_'

//          Log.d(TAG, "c1 = $c1\nc2 = $c2\nc3 = $c3\nlast char = $lastChar")
            
            if (c1 && c2 && c3 && c4 && c5) {
//              Log.d(TAG, "afterTextChanged: last char = $lastChar")
                binding.tagSuggestions.visibility = View.GONE
                Log.d(TAG, "post hide suggestions")
            }
            
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