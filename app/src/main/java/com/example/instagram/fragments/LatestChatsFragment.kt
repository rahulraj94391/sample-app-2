package com.example.instagram.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.ChatActivity
import com.example.instagram.LOGGED_IN_ID
import com.example.instagram.MainViewModel
import com.example.instagram.R
import com.example.instagram.USER_ID
import com.example.instagram.USER_LAST_LOGIN
import com.example.instagram.adapters.RecentChatsAdapter
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.model.RecentChats
import com.example.instagram.databinding.FragmentLatestChatsBinding
import com.example.instagram.viewmodels.LatestChatFragViewModel
import kotlinx.coroutines.launch

private const val TAG = "LatestChatsFragment_CommTag"

class LatestChatsFragment : Fragment() {
    private lateinit var binding: FragmentLatestChatsBinding
    private lateinit var viewModel: LatestChatFragViewModel
    private lateinit var db: AppDatabase
    private lateinit var mainViewModel: MainViewModel
    private lateinit var recentChatAdapter: RecentChatsAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        viewModel = ViewModelProvider(this)[LatestChatFragViewModel::class.java]
        db = AppDatabase.getDatabase(requireContext())
        recentChatAdapter = RecentChatsAdapter(mainViewModel.loggedInProfileId!!, ::openChatScreen)
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_latest_chats, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationIcon(R.drawable.arrow_back_24)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        lifecycleScope.launch {
            val fullName = db.profileDao().getFullName(mainViewModel.loggedInProfileId!!)
            binding.myName.text = "${fullName.first_name} ${fullName.last_name}"
        }
        
        binding.recentChats.apply {
            adapter = recentChatAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }
        db.chatDao().getMyLatestChatsPerUser(mainViewModel.loggedInProfileId!!).observe(viewLifecycleOwner) {
            it ?: return@observe
            
            val set = mutableSetOf<RecentChats>()
            for (i in it) {
                set.add(RecentChats(i.senderId, i.receiverId, i.message, i.timeStamp, i.messageType, i.replyToChat, i.rowId))
            }
            
            val list = set.toMutableList()
            list.sortedByDescending { time ->
                time.timeStamp
            }
            recentChatAdapter.setNewList(list)
            
        }
    }
    
    private fun openChatScreen(userId: Long) {
        lifecycleScope.launch {
            val intent = Intent(requireActivity(), ChatActivity::class.java)
            val userLastTime = db.lastOnlineDao().getUserLastOnlineStatus(userId, mainViewModel.loggedInProfileId!!)?.time ?: 0L
            intent.apply {
                putExtra(USER_LAST_LOGIN, userLastTime)
                putExtra(USER_ID, userId)
                putExtra(LOGGED_IN_ID, mainViewModel.loggedInProfileId)
            }
            startActivity(intent)
        }
    }
}