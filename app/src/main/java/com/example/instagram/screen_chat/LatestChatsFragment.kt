package com.example.instagram.screen_chat

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
import com.example.instagram.R
import com.example.instagram.common.MainViewModel
import com.example.instagram.data.AppDatabase
import com.example.instagram.data.entity.Chat
import com.example.instagram.databinding.FragmentLatestChatsBinding
import com.example.instagram.screen_chat.model.RecentChats
import kotlinx.coroutines.launch

class LatestChatsFragment : Fragment() {
    private lateinit var binding: FragmentLatestChatsBinding
    private lateinit var db: AppDatabase
    private lateinit var mainViewModel: MainViewModel
    private lateinit var recentChatAdapter: RecentChatsAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
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
            binding.myName.text = "${fullName.firstName} ${fullName.lastName}"
        }
        
        binding.recentChats.apply {
            adapter = recentChatAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }
        db.chatDao().getMyLatestChatsPerUser(mainViewModel.loggedInProfileId!!).observe(viewLifecycleOwner) {
            it ?: return@observe
            lifecycleScope.launch {
                getRecentChats(it)
            }
        }
        
    }
    
    private suspend fun getRecentChats(chats: List<Chat>) {
        val set = mutableSetOf<RecentChats>()
        for (i in chats) {
            val isBlocked = db.blockDao().isBlocked(i.senderId, i.receiverId) == 1 || db.blockDao().isBlocked(i.receiverId, i.senderId) == 1
            set.add(
                RecentChats(
                    i.senderId,
                    i.receiverId,
                    i.message,
                    i.timeStamp,
                    i.messageType,
                    i.replyToChat,
                    i.rowId,
                    isBlocked
                )
            )
        }
        
        val list = set.toMutableList()
        list.sortedByDescending { time ->
            time.timeStamp
        }
        recentChatAdapter.setNewList(list)
        
        if (chats.isEmpty() && recentChatAdapter.itemCount == 0) {
            binding.noMsg.visibility = View.VISIBLE
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