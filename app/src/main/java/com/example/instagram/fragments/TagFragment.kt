package com.example.instagram.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.R
import com.example.instagram.adapters.TagSearchResultAdapter
import com.example.instagram.adapters.TagsAdapter
import com.example.instagram.database.model.TagSearchResult
import com.example.instagram.databinding.FragmentTagBinding
import com.example.instagram.viewmodels.PostFragViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "TagFragment_CommTag"

class TagFragment : Fragment() {
    private var _binding: FragmentTagBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PostFragViewModel
    private lateinit var tagsAdapter: TagsAdapter
    private lateinit var tagSearchResultAdapter: TagSearchResultAdapter
    private var searchJob: Job? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(requireActivity())[PostFragViewModel::class.java]
        tagsAdapter = TagsAdapter(viewModel.finalTags) {
            binding.yourTagsWillAppearHere.visibility = View.VISIBLE
        }
        tagSearchResultAdapter = TagSearchResultAdapter(::insertFromResultToSet)
        super.onCreate(savedInstanceState)
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tag, container, false)
        return binding.root
    }
    
    override fun onDestroyView() {
        binding.tags.adapter = null
        _binding = null
        super.onDestroyView()
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        if (viewModel.finalTags.size > 0) binding.yourTagsWillAppearHere.visibility = View.INVISIBLE
        
        
        binding.searchViewBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText!!.isEmpty()) {
                    binding.tagResults.visibility = View.INVISIBLE
                    binding.tags.visibility = View.VISIBLE
                    binding.textView2.visibility = View.VISIBLE
                    binding.noUsersFoundIns.visibility = View.INVISIBLE
                    if (viewModel.finalTags.isEmpty()) {
                        binding.yourTagsWillAppearHere.visibility = View.VISIBLE
                    } else {
                        binding.yourTagsWillAppearHere.visibility = View.INVISIBLE
                    }
                    return true
                } else {
                    binding.yourTagsWillAppearHere.visibility = View.INVISIBLE
                    binding.tagResults.visibility = View.VISIBLE
                    binding.tags.visibility = View.INVISIBLE
                    binding.textView2.visibility = View.INVISIBLE
                    searchJob?.cancel()
                    searchJob = lifecycleScope.launch {
                        tagSearchResultAdapter.clearList()
                        delay(500)
                        if (newText == "") return@launch
                        viewModel.getSearchResults(newText)
                    }
                    return true
                }
            }
        })
        
        binding.tagResults.adapter = tagSearchResultAdapter
        binding.tagResults.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        
        binding.tags.adapter = tagsAdapter
        binding.tags.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }
    
    private fun insertFromResultToSet(tag: TagSearchResult) {
        binding.tagResults.visibility = View.INVISIBLE
        binding.tags.visibility = View.VISIBLE
        binding.textView2.visibility = View.VISIBLE
        tagSearchResultAdapter.clearList()
        tagsAdapter.addTag(tag)
        binding.searchViewBar.setQuery(null, false)
    }
    
    override fun onStart() {
        super.onStart()
        viewModel.tagSearchResults.observe(viewLifecycleOwner) {
            if (it.isEmpty() && binding.searchViewBar.query.toString().isNotBlank()) { // show - no users found ins
                binding.noUsersFoundIns.visibility = View.VISIBLE
                return@observe
            } else { // hide - no users found ins
                binding.noUsersFoundIns.visibility = View.INVISIBLE
                tagSearchResultAdapter.setNewTagSearchResult(it)
            }
        }
    }
}