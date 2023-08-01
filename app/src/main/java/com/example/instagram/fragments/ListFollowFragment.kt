package com.example.instagram.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.R
import com.example.instagram.adapters.FollowAdapter
import com.example.instagram.databinding.FragmentListFollowBinding
import com.example.instagram.viewmodels.ListFollowFragViewModel
import com.google.android.material.divider.MaterialDividerItemDecoration
import kotlin.math.roundToInt
import kotlin.properties.Delegates

const val TYPE_FOLLOWER = "Follower"
const val TYPE_FOLLOWING = "Following"

private const val TAG = "CommTag_ListFollowFragment"

class ListFollowFragment : Fragment() {
    private lateinit var binding: FragmentListFollowBinding
    private var profileId: Long by Delegates.notNull()
    private val args: ListFollowFragmentArgs by navArgs()
    private lateinit var viewModel: ListFollowFragViewModel
    private lateinit var adapter: FollowAdapter
    
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileId = args.profileId
        viewModel = ViewModelProvider(this)[ListFollowFragViewModel::class.java]
        viewModel.getUsers(args.type, profileId)
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_list_follow, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.type.text = args.type
        adapter = FollowAdapter()
        viewModel.users.observe(viewLifecycleOwner) {
            if (it.size < 1) {
                // show the instruction here
                
            }
            
            adapter.updateList(it)
        }
        val divider = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        divider.dividerInsetStart = dpToPx(84)
        
        binding.users.addItemDecoration(divider)
        binding.users.adapter = adapter
        binding.users.layoutManager = LinearLayoutManager(requireContext())
        
    }
    
    private fun dpToPx(dpValue: Int): Int {
        val density = requireContext().resources.displayMetrics.density
        return (dpValue * density).roundToInt()
    }
}