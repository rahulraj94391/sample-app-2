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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.MainViewModel
import com.example.instagram.R
import com.example.instagram.adapters.HashTagTextAdapter
import com.example.instagram.databinding.FragmentCaptionTagBinding
import com.example.instagram.viewmodels.PostFragViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import kotlin.properties.Delegates

private const val TAG = "CaptionTagFragment_CommTag"

class CaptionTagFragment : Fragment() {
    private var _binding: FragmentCaptionTagBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PostFragViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var hashTagAdapter: HashTagTextAdapter
    private var tagSearchJob: Job? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[PostFragViewModel::class.java]
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        hashTagAdapter = HashTagTextAdapter(viewModel.hashTagList, ::copyHashTagSuggestionInTextView)
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_caption_tag, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.postDesc.suggestionList = binding.tagSuggestions
        binding.postDesc.addTextChangedListener(CustomTextWatcher())
        binding.toolbarCreatePost.setNavigationIcon(R.drawable.arrow_back_24)
        binding.tagSuggestions.apply {
            adapter = hashTagAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        
        
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
        
        
        viewModel.isHashTagListUpdated.observe(viewLifecycleOwner) {
            hashTagAdapter.notifyDataSetChanged()
            binding.tagSuggestions.visibility = if (hashTagAdapter.itemCount < 1) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
    }
    
    private fun copyHashTagSuggestionInTextView(pos: Int) {
        Log.d(TAG, "copyHashTagSuggestionInTextView: ")
        val tag = try {
            viewModel.hashTagList[pos]
        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
            ""
        }
        val str: Editable = (binding.postDesc.text ?: "as") as Editable
        var count = 0
        for (c in str.reversed()) {
            if (c == '#') break
            count++
        }
        str.delete(str.length - count, str.length)
        str.append(tag).append(" ")
        binding.postDesc.text = str
        binding.postDesc.setSelection(str.length)
        binding.tagSuggestions.visibility = View.GONE
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
        
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }
        
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            lastChar = if (s?.isNotEmpty() == true) s[s.length - 1] else 32.toChar()
            val hashTag = findSubstringAfterLastHashTag(s.toString())
            Log.d(TAG, "last tag = $hashTag")
            tagSearchJob?.cancel()
            tagSearchJob = lifecycleScope.launch {
                if (hashTag != null) {
                    viewModel.searchHashTag(hashTag)
                }
            }
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
//                    Log.i(TAG, "post show suggestions. string = $tag")
//                    binding.tagSuggestions.visibility = View.VISIBLE
                    
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
//                Log.d(TAG, "post hide suggestions")
            }
            
            binding.btnPost.isEnabled = s?.isEmpty() != true && isInternetActive(requireContext())
        }
        
        private fun findSubstringAfterLastHashTag(input: String): String? {
            val index = input.lastIndexOf('#')
            return if (index != -1 && index < input.length - 1) {
                input.substring(index + 1)
            } else {
                null
            }
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
        viewModel.hashTagList.clear()
        hashTagAdapter.notifyItemRangeRemoved(0, hashTagAdapter.itemCount)
        _binding = null
        super.onDestroyView()
    }
}