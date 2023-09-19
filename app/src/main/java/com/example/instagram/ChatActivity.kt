package com.example.instagram

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.adapters.ChatAdapter
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.entity.Chat
import com.example.instagram.database.entity.LastOnline
import com.example.instagram.databinding.ActivityChatBinding
import com.example.instagram.viewmodels.ChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

const val USER_ID = "user_chat_id"
const val LOGGED_IN_ID = "my_chat_id"
const val USER_LAST_LOGIN = "user_last_login"
const val IS_ACTIVITY_RECREATING = "is_recreating"

private const val TAG = "ChatActivity_CommTag"

class ChatActivity : AppCompatActivity() {
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var binding: ActivityChatBinding
    private lateinit var llManager: LinearLayoutManager
    private lateinit var db: AppDatabase
    private var myLastOnlineStatus: LastOnline? = null
    private var userId: Long by Delegates.notNull()
    private var myId: Long by Delegates.notNull()
    private var userLastTime: Long = 0L
    private var isRecreating: Boolean = false
    
    // recycler view vars to load more data
    var isScrolling = false
    var currentItems: Int = 0
    var totalItems: Int = 0
    var scrolledOut: Int = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat)
        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        db = AppDatabase.getDatabase(this)
        
        val bundle = intent.extras
        if (null != bundle) {
            userId = bundle.getLong(USER_ID)
            myId = bundle.getLong(LOGGED_IN_ID)
            userLastTime = bundle.getLong(USER_LAST_LOGIN)
        } else {
            Toast.makeText(this, "Some error occurred.", Toast.LENGTH_SHORT).show()
            finish()
        }
        isRecreating = savedInstanceState?.getBoolean(IS_ACTIVITY_RECREATING, false) == true
        
        if (chatViewModel.chatAdapter == null) {
            chatViewModel.chatAdapter = ChatAdapter(userLastTime, userId, myId, ::onLongClick, ::highlightMsg)
            chatViewModel.loadChats(userId, myId, chatViewModel.chatAdapter!!.itemCount)
        }
        llManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        binding.chatRV.apply {
            adapter = chatViewModel.chatAdapter!!
            layoutManager = llManager
            addOnScrollListener(scrollListener)
        }
        
        lifecycleScope.launch {
            myLastOnlineStatus = db.lastOnlineDao().getMyLastOnlineStatus(myId, userId)
            if (chatViewModel.replyToChat != null) {
                showReplyPreview()
                bindDataInReplyPreview()
            }
        }
        
        chatViewModel.chatsLive.observe(this) {
            if (it.isEmpty() || isRecreating) return@observe
            chatViewModel.chatAdapter!!.addNewChats(it)
        }
        
        binding.sendBtn.setOnClickListener { onSendButtonClicked() }
        binding.discardBtn.setOnClickListener {
            chatViewModel.replyToChat = null
            hideReplyPreview()
        }
        
        lifecycleScope.launch {
            val userprofilePicture = chatViewModel.getUserImage(userId)
            binding.userImage.setImageBitmap(userprofilePicture)
        }
        
        lifecycleScope.launch {
            if (!chatViewModel.isUserNameInitialized()) {
                chatViewModel.userFullName = db.profileDao().getFullName(userId)
            }
            val name = chatViewModel.userFullName.first_name + " " + chatViewModel.userFullName.last_name
            withContext(Dispatchers.Main) {
                binding.fullName.text = name
            }
        }
    }
    
    private fun onSendButtonClicked() {
        val text = binding.messageBox.text.toString().trim()
        if (text.isBlank()) {
            Toast.makeText(this, "Message cannot be blank.", Toast.LENGTH_SHORT).show()
            return
        }
        val chat = if (chatViewModel.replyToChat != null) {
            Chat(myId, userId, text, System.currentTimeMillis(), 2, chatViewModel.replyToChat!!.rowId)
        } else {
            Chat(myId, userId, text, System.currentTimeMillis(), 1)
        }
    
        lifecycleScope.launch {
            chat.rowId = db.chatDao().insertNewChat(chat)
            chatViewModel.chatAdapter!!.addSentChat(chat)
            withContext(Dispatchers.Main) {
                binding.messageBox.text.clear()
                binding.chatRV.scrollToPosition(0)
                hideReplyPreview()
            }
            chatViewModel.replyToChat = null
        }
    }
    
    private fun highlightMsg(chatId: Long) {/*val pos = chatViewModel.chatAdapter!!.getPositionForChatId(chatId)
        if (pos == -1) return
        Log.d(TAG, "pos = $pos")
        binding.chatRV.scrollToPosition(pos)
        val vh = binding.chatRV.findViewHolderForLayoutPosition(pos) ?: return
        Log.d(TAG, "ViewHolder is not null")
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                vh.itemView.setBackgroundColor(resources.getColor(R.color.grey, theme))
                delay(700)
                vh.itemView.setBackgroundColor(resources.getColor(android.R.color.transparent, theme))
            }
        }*/
    }
    
    private fun onLongClick(chat: Chat) {
        Log.d(TAG, "onLongClick -->\n" + "chat id = ${chat.rowId}\n" + "chat message = ${chat.message}\n")
        chatViewModel.replyToChat = chat
        bindDataInReplyPreview()
        showReplyPreview()
    }
    
    private fun showReplyPreview() {
        findViewById<TextView>(R.id.replyToTxtPreview).visibility = View.VISIBLE
        findViewById<ImageView>(R.id.discardBtn).visibility = View.VISIBLE
        // binding.replyToTxtPreview.visibility = View.VISIBLE
        // binding.discardBtn.visibility = View.VISIBLE
    }
    
    private fun hideReplyPreview() {
        findViewById<TextView>(R.id.replyToTxtPreview).visibility = View.GONE
        findViewById<ImageView>(R.id.discardBtn).visibility = View.GONE
        // binding.replyToTxtPreview.visibility = View.GONE
        // binding.discardBtn.visibility = View.GONE
    }
    
    private fun bindDataInReplyPreview() {
        Log.d(TAG, "bindDataInReplyPreview: inside")
        val chat = chatViewModel.replyToChat!!
        val name = if (chat.senderId == userId) "${chatViewModel.userFullName.first_name} ${chatViewModel.userFullName.last_name}" else resources.getString(R.string.you)
        val builder = SpannableStringBuilder()
        builder.append(name)
        builder.setSpan(StyleSpan(Typeface.BOLD), 0, name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.append("\n").append(chat.message)
        Log.e(TAG, "Previous data = ${binding.replyToTxtPreview.text}")
        binding.replyToTxtPreview.text = builder
        Log.e(TAG, "Current data = ${binding.replyToTxtPreview.text}")
    }
    
    override fun onPause() {
        chatViewModel.updateMyLastOnlineStatus(myLastOnlineStatus, myId, userId)
        chatViewModel.state = llManager.onSaveInstanceState()
        super.onPause()
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_ACTIVITY_RECREATING, true)
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
            currentItems = llManager.childCount
            totalItems = llManager.itemCount
            scrolledOut = llManager.findFirstVisibleItemPosition()
            if (isScrolling && (currentItems + scrolledOut == totalItems)) {
                isScrolling = false
                chatViewModel.loadChats(userId, myId, chatViewModel.chatAdapter!!.itemCount)
            }
        }
    }
}

