package com.example.instagram

import android.graphics.Canvas
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.example.instagram.adapters.ChatAdapter
import com.example.instagram.adapters.REGULAR_RECEIVED
import com.example.instagram.adapters.REGULAR_SENT
import com.example.instagram.adapters.REPLY_RECEIVED
import com.example.instagram.adapters.REPLY_SENT
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.entity.Chat
import com.example.instagram.database.entity.LastOnline
import com.example.instagram.databinding.ActivityChatBinding
import com.example.instagram.itemDecoration.ChatItemDecoration
import com.example.instagram.viewmodels.ChatViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates


const val USER_ID = "user_chat_id"
const val LOGGED_IN_ID = "my_chat_id"
const val USER_LAST_LOGIN = "user_last_login"
const val IS_ACTIVITY_RECREATING = "is_activity_recreating"

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
    private lateinit var dateDecoration: ItemDecoration
    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var haptics: Haptics
    private var actionMode: ActionMode? = null
    
    // recycler view vars to load more data
    var isScrolling = false
    var currentItems: Int = 0
    var totalItems: Int = 0
    var scrolledOut: Int = 0
    
    inner class TouchHelper(val adapter: ChatAdapter) : ItemTouchHelper.SimpleCallback(0, 0) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }
        
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            Log.e(TAG, "onSwiped: called")
            haptics.light()
            // val animator = ValueAnimator.ofFloat(viewHolder.itemView.translationX, 0f)
            // animator.addUpdateListener { animation ->
            //     viewHolder.itemView.translationX = animation.animatedValue as Float
            // }
            // animator.duration = 200
            // animator.start()
            itemTouchHelper.attachToRecyclerView(null)
            chatViewModel.replyToChat = chatViewModel.chatAdapter?.chats?.get((viewHolder.itemView.tag as Pair<Int, Int>).first)
            bindDataInReplyPreview()
            showReplyPreview()
            itemTouchHelper.attachToRecyclerView(binding.chatRV)
            
            Log.e(TAG, "onSwiped ENDED")
        }
        
        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            val allowedSwipe = viewHolder.itemView.width / 8.toFloat()
            val clampedDX = dX.coerceIn(-allowedSwipe, allowedSwipe)
            super.onChildDraw(c, recyclerView, viewHolder, clampedDX, dY, actionState, isCurrentlyActive)
            
            Log.d(
                TAG, "onChildDraw: \n" +
                        "maxSwipe = $allowedSwipe\n" +
                        "dX = $dX\n" +
                        "clammed = $clampedDX\n" +
                        "dY = $dY\n" +
                        "actionState = $actionState\n" +
                        "isCurrentlyActive = $isCurrentlyActive"
            )
        }
        
        override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            val pair = viewHolder.itemView.tag as Pair<Int, Int>
            return when (pair.second) {
                REGULAR_SENT, REPLY_SENT, REGULAR_RECEIVED, REPLY_RECEIVED -> ItemTouchHelper.RIGHT
                else -> 0
            }
        }
    }
    
    private val mActionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return true
        }
        
        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            if (chatViewModel.chatAdapter?.otherMessageCount?.value!! > 0) {
                menu?.clear()
            } else {
                mode?.menuInflater?.inflate(R.menu.chat_menu, menu)
            }
            return true
        }
        
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when (item?.itemId) {
                R.id.deleteChats -> {
                    showDeleteMessageAlert()
                    return true
                }
            }
            
            return false
        }
        
        override fun onDestroyActionMode(mode: ActionMode?) {
            
            actionMode = null
            chatViewModel.chatAdapter?.resetSelectMode()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        haptics = Haptics(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat)
        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        db = AppDatabase.getDatabase(this)
        setUserIdsFromIntentOrExitActivity()
        isRecreating = savedInstanceState?.getBoolean(IS_ACTIVITY_RECREATING, false) == true
        initializeVariables()
        lifecycleScope.launch {
            myLastOnlineStatus = db.lastOnlineDao().getMyLastOnlineTime(myId, userId)
            if (chatViewModel.replyToChat != null) {
                showReplyPreview()
                bindDataInReplyPreview()
            }
        }
        
        chatViewModel.chatAdapter?.selectedMessageCount?.observe(this) {
            Log.d(TAG, "Selected message count <LIVE> = $it")
            if (it < 1) {
                actionMode?.finish()
                return@observe
            }
            actionMode = actionMode ?: startActionMode(mActionModeCallback)
            actionMode?.title = if (it > 1) "$it messages selected." else "$it message selected."
        }
        
        chatViewModel.chatAdapter?.otherMessageCount?.observe(this) {
            Log.d(TAG, "Other message count <LIVE> = $it")
            val prev = chatViewModel.chatAdapter?.lastOtherMsgCount!!
            val current = it
            if (prev == current) return@observe
            actionMode?.invalidate()
            chatViewModel.chatAdapter?.lastOtherMsgCount = it
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
        setUserProfilePictureAndName()
    }
    
    private fun setUserProfilePictureAndName() {
        lifecycleScope.launch {
            val userprofilePicture = chatViewModel.getUserImage(userId)
            if (userprofilePicture != null) {
                binding.userImage.setImageBitmap(userprofilePicture)
            } else {
                binding.userImage.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.person_24, theme))
            }
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
    
    private fun setUserIdsFromIntentOrExitActivity() {
        val bundle = intent.extras
        if (null != bundle) {
            userId = bundle.getLong(USER_ID)
            myId = bundle.getLong(LOGGED_IN_ID)
            userLastTime = bundle.getLong(USER_LAST_LOGIN)
        } else {
            Toast.makeText(this, "Some error occurred.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun initializeVariables() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        if (chatViewModel.chatAdapter == null) {
            chatViewModel.chatAdapter = ChatAdapter(userLastTime, userId, myId, ::highlightMsg, ::spanBuilder)
            chatViewModel.loadChats(userId, myId, chatViewModel.chatAdapter!!.itemCount)
        }
        itemTouchHelper = ItemTouchHelper(TouchHelper(chatViewModel.chatAdapter!!))
        llManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        dateDecoration = ChatItemDecoration(this@ChatActivity, chatViewModel.chatAdapter!!.chats)
        binding.chatRV.apply {
            addItemDecoration(dateDecoration)
            adapter = chatViewModel.chatAdapter!!
            layoutManager = llManager
            addOnScrollListener(scrollListener)
        }
        itemTouchHelper.attachToRecyclerView(binding.chatRV)
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
            binding.chatRV.invalidateItemDecorations()
            
            binding.messageBox.text.clear()
            binding.chatRV.scrollToPosition(0)
            hideReplyPreview()
            
            chatViewModel.replyToChat = null
        }
    }
    
    
    private fun highlightMsg(chatId: Long) {
        /*val pos = chatViewModel.chatAdapter!!.getPositionForChatId(chatId)
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
    
    //    private fun onLongClick(chat: Chat) {
    //        Log.d(TAG, "onLongClick -->\n" + "chat id = ${chat.rowId}\n" + "chat message = ${chat.message}\n")
    //        chatViewModel.replyToChat = chat
    //        bindDataInReplyPreview()
    //        showReplyPreview()
    //    }
    
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
        binding.replyToTxtPreview.text = spanBuilder(chatViewModel.replyToChat!!)
    }
    
    private fun spanBuilder(chat: Chat): SpannableStringBuilder {
        val name = if (chat.senderId == userId) "${chatViewModel.userFullName.first_name} ${chatViewModel.userFullName.last_name}" else resources.getString(R.string.you)
        val builder = SpannableStringBuilder()
        builder.append(name)
        builder.setSpan(StyleSpan(Typeface.BOLD), 0, name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.append("\n").append(chat.message)
        return builder
    }
    
    private fun markChatsAsDelete() {
        CoroutineScope(Dispatchers.IO).launch {
            val list = chatViewModel.chatAdapter?.selectedItems ?: return@launch
            for (chat in list) {
                chat.messageType = 3 // "3" represents chat as deleted.
            }
            val rowsAffected = db.chatDao().markChatsAsDeleted(list)
            Log.d(TAG, "markChatsAsDelete: rows affected = $rowsAffected")
            list.clear()
            Log.d(TAG, "after clearing list size = ${chatViewModel.chatAdapter?.selectedItems?.size}")
            withContext(Dispatchers.Main) {
                chatViewModel.chatAdapter!!.notifyDataSetChanged()
            }
        }
    }
    
    private fun abortMessageDelete() {
        chatViewModel.chatAdapter?.apply {
            selectedItems.clear()
            notifyDataSetChanged()
        }
    }
    
    private fun showDeleteMessageAlert() {
        if (chatViewModel.chatAdapter?.selectedItems?.size!! < 1) return
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete chats.")
            .setMessage("Delete ${chatViewModel.chatAdapter?.selectedItems?.size} message(s) ?")
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            .setPositiveButton("Yes") { dialog, which ->
                markChatsAsDelete()
                dialog.dismiss()
                chatViewModel.chatAdapter?.resetSelectMode()
            }
            .show()
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

