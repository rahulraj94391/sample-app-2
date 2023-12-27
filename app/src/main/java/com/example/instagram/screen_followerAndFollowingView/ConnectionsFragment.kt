package com.example.instagram.screen_followerAndFollowingView

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
import com.example.instagram.NO_FOLLOWERS
import com.example.instagram.NO_FOLLOWINGS
import com.example.instagram.R
import com.example.instagram.TYPE_FOLLOWER
import com.example.instagram.common.MainViewModel
import com.example.instagram.databinding.FragmentListFollowBinding
import com.google.android.material.divider.MaterialDividerItemDecoration
import kotlin.math.roundToInt
import kotlin.properties.Delegates


class ConnectionsFragment : Fragment() {
    private var _binding: FragmentListFollowBinding? = null
    private val binding get() = _binding!!
    private var profileId: Long by Delegates.notNull()
    private val args: ConnectionsFragmentArgs by navArgs()
    private lateinit var viewModel: ConnectionsViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var adapter: ConnnectionsAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileId = args.profileId
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        viewModel = ViewModelProvider(this)[ConnectionsViewModel::class.java]
        viewModel.getUsers(args.type, profileId)
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_list_follow, container, false)
        return binding.root
    }
    
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.type.text = args.type
        adapter = ConnnectionsAdapter(::openProfile)
        
        binding.toolbar.apply {
            setNavigationIcon(R.drawable.arrow_back_24)
            setNavigationOnClickListener { findNavController().navigateUp() }
        }
        
        val divider = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL).apply {
            dividerThickness = 2
            dividerColor = requireContext().resources.getColor(R.color.divider, context?.theme)
            dividerInsetStart = dpToPx(84)
        }
        
        binding.usersRV.apply {
            addItemDecoration(divider)
            adapter = this@ConnectionsFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        
        
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
    
    
    override fun onResume() {
        super.onResume()
        mainViewModel.removeProfileFromFollowingList.observe(viewLifecycleOwner) removeNotRequired@{
            if (it == -1) return@removeNotRequired
            adapter.removeUserAt(it)
            showInst()
            mainViewModel.removeProfileFromFollowingList.postValue(-1)
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
        val action = ConnectionsFragmentDirections.actionListFollowFragmentToProfileFragment(profileId, true, -1)
        findNavController().navigate(action)
    }
    
    private fun dpToPx(dpValue: Int): Int {
        val density = requireContext().resources.displayMetrics.density
        return (dpValue * density).roundToInt()
    }
    
    
    override fun onDestroyView() {
        binding.usersRV.adapter = null
        _binding = null
        super.onDestroyView()
    }
}