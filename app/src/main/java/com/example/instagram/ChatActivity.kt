package com.example.instagram

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.adapters.ChatAdapter
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.entity.Chat
import com.example.instagram.databinding.ActivityChatBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

const val USER_ID = "user_chat_id"
const val LOGGED_IN_ID = "my_chat_id"

private const val TAG = "ChatActivity_CommTag"

class ChatActivity : AppCompatActivity() {
    private var userId: Long by Delegates.notNull()
    private var loggedInUserId: Long by Delegates.notNull()
    private lateinit var binding: ActivityChatBinding
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var db: AppDatabase
    private var chatsLive = MutableLiveData<MutableList<Chat>>()
    private lateinit var imageUtil: ImageUtil
    private lateinit var manager: LinearLayoutManager
    
    
    // recycler view vars to load more data
    var isScrolling = false
    var currentItems: Int = 0
    var totalItems: Int = 0
    var scrolledOut: Int = 0
    
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat)
        
        imageUtil = ImageUtil(this)
        val bundle = intent.extras
        if (null != bundle) {
            userId = bundle.getLong(USER_ID)
            loggedInUserId = bundle.getLong(LOGGED_IN_ID)
            
        } else {
            Toast.makeText(this, "Some error occurred.", Toast.LENGTH_SHORT).show()
            finish()
        }
        
        db = AppDatabase.getDatabase(this)
        chatAdapter = ChatAdapter(userId, loggedInUserId)
        binding.sendBtn.setOnClickListener { onSendButtonClicked() }
        
        
        
        lifecycleScope.launch {
            val url = imageUtil.getProfilePictureUrl(userId)
            val bitmap = url?.let { imageUtil.getBitmap(it) }
            withContext(Dispatchers.Main) {
                binding.userImage.setImageBitmap(bitmap)
            }
        }
        
        lifecycleScope.launch {
            val fullName = db.profileDao().getFullName(userId)
            val name = fullName.first_name + " " + fullName.last_name
            withContext(Dispatchers.Main) {
                binding.fullName.text = name
            }
        }
        
        binding.userImage.setOnClickListener { loadMoreChats() }
        
        lifecycleScope.launch {
            val chats = db.chatDao().getChats(userId, loggedInUserId, chatAdapter.itemCount)
            chatsLive.postValue(chats)
        }
        
        chatsLive.observe(this) {
            if (it.isEmpty()) return@observe
            chatAdapter.addNewChats(it)
        }
    }
    
    private fun onSendButtonClicked() {
        val text = binding.messageBox.text.toString()
        if (text.isBlank()) {
            Toast.makeText(this, "Message cannot be blank.", Toast.LENGTH_SHORT).show()
            return
        }
        val chat = Chat(loggedInUserId, userId, text, System.currentTimeMillis(), 1)
        chatAdapter.addSentChat(chat)
        lifecycleScope.launch { db.chatDao().insertNewChat(chat) }
        binding.messageBox.text.clear()
        binding.chatRV.smoothScrollToPosition(0)
    }
    
    private fun loadMoreChats() {
        lifecycleScope.launch {
            val chats = db.chatDao().getChats(userId, loggedInUserId, chatAdapter.itemCount)
            chatsLive.postValue(chats)
        }
    }
    
    override fun onStart() {
        super.onStart()
        manager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        binding.chatRV.apply {
            adapter = chatAdapter
            layoutManager = manager
            addOnScrollListener(scrollListener)
        }
    }
    
    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                isScrolling = true
            }
        }
        
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            currentItems = manager.childCount
            totalItems = manager.itemCount
            scrolledOut = manager.findFirstVisibleItemPosition()
            if (isScrolling && (currentItems + scrolledOut == totalItems)) {
                isScrolling = false
                loadMoreChats()
            }
        }
    }
}