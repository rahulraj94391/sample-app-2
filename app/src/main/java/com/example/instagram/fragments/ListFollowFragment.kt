package com.example.instagram.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.MainViewModel
import com.example.instagram.R
import com.example.instagram.adapters.FollowAdapter
import com.example.instagram.databinding.FragmentListFollowBinding
import com.example.instagram.viewmodels.ListFollowFragViewModel
import com.google.android.material.divider.MaterialDividerItemDecoration
import kotlin.math.roundToInt
import kotlin.properties.Delegates

const val TYPE_FOLLOWER = "Follower"
const val TYPE_FOLLOWING = "Following"
const val NO_FOLLOWINGS = "No Following"
const val NO_FOLLOWERS = "No Follower"

private const val TAG = "CommTag_ListFollowFragment"

class ListFollowFragment : Fragment() {
    private var _binding: FragmentListFollowBinding? = null
    private val binding get() = _binding!!
    private var profileId: Long by Delegates.notNull()
    private val args: ListFollowFragmentArgs by navArgs()
    private lateinit var viewModel: ListFollowFragViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var adapter: FollowAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileId = args.profileId
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        viewModel = ViewModelProvider(this)[ListFollowFragViewModel::class.java]
        viewModel.getUsers(args.type, profileId)
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_list_follow, container, false)
        return binding.root
    }
    
    override fun onDestroyView() {
        binding.usersRV.adapter = null
        _binding = null
        super.onDestroyView()
    }
    
    override fun onResume() {
        super.onResume()
        mainViewModel.removeProfileFromFollowingList.observe(viewLifecycleOwner) removeNotRequired@{
            if (it == -1) return@removeNotRequired
            adapter.removeUserAt(it)
            showInst()
            mainViewModel.removeProfileFromFollowingList.postValue(-1)
        }
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.type.text = args.type
        adapter = FollowAdapter(::openProfile)
        binding.toolbar.setNavigationIcon(R.drawable.arrow_back_24)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        
        val divider = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        divider.dividerInsetStart = dpToPx(84)
        binding.usersRV.addItemDecoration(divider)
        binding.usersRV.adapter = adapter
        binding.usersRV.layoutManager = LinearLayoutManager(requireContext())
        
        viewModel.users.observe(viewLifecycleOwner) {
            if (it.size < 1) {
                val ins = if (args.type == TYPE_FOLLOWER) NO_FOLLOWERS
                else NO_FOLLOWINGS
                binding.ins.text = ins
                binding.ins.visibility = View.VISIBLE
                return@observe
            }
            binding.usersRV.visibility = View.VISIBLE
            adapter.updateList(it)
        }
    }
    
    private fun showInst() {
        if (adapter.itemCount < 1) {
            val ins = if (args.type == TYPE_FOLLOWER) NO_FOLLOWERS else NO_FOLLOWINGS
            binding.ins.text = ins
            binding.ins.visibility = View.VISIBLE
        }
    }
    
    private fun openProfile(profileId: Long, pos: Int) {
        val action = if (mainViewModel.profileOpenCount == 1) { // popInclusive = true
            ListFollowFragmentDirections.actionListFollowFragmentToProfileFragmentPopInclusiveTrue(profileId, true, -1)
        } else { // popInclusive = false
            ListFollowFragmentDirections.actionListFollowFragmentToProfileFragment(profileId, true, pos)
        }
        findNavController().navigate(action)
        
        if (mainViewModel.profileOpenCount == 0)
            mainViewModel.profileOpenCount = 1
    }
    
    private fun dpToPx(dpValue: Int): Int {
        val density = requireContext().resources.displayMetrics.density
        return (dpValue * density).roundToInt()
    }
}