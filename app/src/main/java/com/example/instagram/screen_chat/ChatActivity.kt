package com.example.instagram.screen_chat

import android.content.Context
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
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.example.instagram.R
import com.example.instagram.common.Haptics
import com.example.instagram.data.AppDatabase
import com.example.instagram.data.entity.Chat
import com.example.instagram.data.entity.LastOnline
import com.example.instagram.databinding.ActivityChatBinding
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
    private var blockedStatus: Int by Delegates.notNull()
    
    // recycler suggestionList vars to load more data
    var isScrolling = false
    var currentItems: Int = 0
    var totalItems: Int = 0
    var scrolledOut: Int = 0
    
    inner class TouchHelper(val adapter: ChatAdapter) : ItemTouchHelper.SimpleCallback(0, 0) {
        private var replyPreviewFlag = true
        private var hapticFlag = true
        
        
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }
        
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            Log.e(TAG, "onSwiped: called")
            //            adapter.notifyItemChanged(viewHolder.adapterPosition)
            //            haptics.light()
            /*val animator = ValueAnimator.ofFloat(viewHolder.itemView.translationX, 0f)
            animator.addUpdateListener { animation ->
                viewHolder.itemView.translationX = animation.animatedValue as Float
            }
            animator.duration = 200
            animator.start()*//*itemTouchHelper.attachToRecyclerView(null)
            itemTouchHelper.attachToRecyclerView(binding.chatRV)*/
            
            /*val translationX = ObjectAnimator.ofFloat(viewHolder.itemView, View.TRANSLATION_X, ((viewHolder.itemView.width / 5).toFloat()), 0f)
            translationX.duration = 120
            translationX.start()*//*chatViewModel.replyToChat = chatViewModel.chatAdapter?.chats?.get(viewHolder.adapterPosition)
            bindDataInReplyPreview()
            showReplyPreview()*/
            
            // Log.e(TAG, "onSwiped ENDED")
        }
        
        override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
            return /*.10f*/ Float.MAX_VALUE
        }
        
        override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
            return Float.MAX_VALUE
        }
        
        override fun getSwipeVelocityThreshold(defaultValue: Float): Float {
            return 0.001f
        }
        
        //        val icon = ContextCompat.getDrawable(this@ChatActivity, R.drawable.round_reply_24)!!
        
        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) { // Log.d(TAG, "onChildDraw: ")
            val allowedSwipe = (viewHolder.itemView.width / 5).toFloat()
            val clampedDX = dX.coerceIn(-allowedSwipe, allowedSwipe)
            super.onChildDraw(c, recyclerView, viewHolder, clampedDX, dY, actionState, isCurrentlyActive)
            
            /*val itemView = viewHolder.itemView
            val iconMargin: Int = (itemView.height - icon.intrinsicHeight) / 2
            val iconTop: Int = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
            val iconBottom: Int = iconTop + icon.intrinsicHeight
            val iconRight = itemView.left + iconMargin
            val iconLeft = itemView.left + iconMargin + icon.intrinsicWidth
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            icon.draw(c)*/
            
            
            if (clampedDX >= (viewHolder.itemView.width * .1) && isCurrentlyActive && replyPreviewFlag) {
                replyPreviewFlag = false
                if (hapticFlag) {
                    hapticFlag = false
                    haptics.heavy()
                }
            }
            if (clampedDX < (viewHolder.itemView.width * .1) && isCurrentlyActive) {
                replyPreviewFlag = true
            }
            if (clampedDX == 0f && !isCurrentlyActive) {
                hapticFlag = true
            }
            
            /*Log.d(
                TAG, "onChildDraw: \n" +
                        "maxSwipe = $allowedSwipe\n" +
                        "dX = $dX\n" +
                        "clammed = $clampedDX\n" +
                        "dY = $dY\n" +
                        "actionState = $actionState\n" +
                        "isCurrentlyActive = $isCurrentlyActive"
            )*/
        }
        
        
        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            if (!replyPreviewFlag) {
                replyPreviewFlag = true
                chatViewModel.replyToChat = chatViewModel.chatAdapter?.chats?.get(viewHolder.adapterPosition)
                bindDataInReplyPreview()
                showReplyPreview()
            }
            
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
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
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
        
        lifecycleScope.launch {
            chatViewModel.checkBlockedStatus(userId, myId)
        }
        
        chatViewModel.amIBlockedLive.observe(this) {
            //            binding.sendBtn.isEnabled = !it
            blockedStatus = it
            
            
            when (it) {
                // when no one is blocked
                0 -> binding.apply {
                    sendBtn.isEnabled = true
                    sendMsgLayout.visibility = View.VISIBLE
                    btnUnblock.visibility = View.GONE
                    setChatInstructionOrRV()
                    binding.sendHiBtn.apply {
                        visibility = View.VISIBLE
                        setOnClickListener {
                            onSendButtonClicked("hi ${chatViewModel.userFullName.first_name}")
                        }
                    }
                    
                    itemTouchHelper.attachToRecyclerView(binding.chatRV)
                }
                
                // when i have blocked the other user
                1 -> {
                    binding.sendBtn.isEnabled = false
                    itemTouchHelper.attachToRecyclerView(null)
                    binding.sendHiBtn.apply {
                        visibility = View.INVISIBLE
                    }
                }
                
                //  when the other user has blocked me
                else -> binding.apply {
                    sendBtn.isEnabled = false
                    sendMsgLayout.visibility = View.GONE
                    btnUnblock.visibility = View.VISIBLE
                    btnUnblock.setOnClickListener {
                        showUnblockDialog()
                    }
                    itemTouchHelper.attachToRecyclerView(null)
                    binding.sendHiBtn.apply {
                        visibility = View.INVISIBLE
                    }
                }
            }
            setUserProfilePictureAndName()
        }
        
        chatViewModel.chatAdapter?.selectedMessageCount?.observe(this) { // Log.d(TAG, "Selected message count <LIVE> = $it")
            if (it < 1) {
                actionMode?.finish()
                return@observe
            }
            actionMode = actionMode ?: startActionMode(mActionModeCallback)
            actionMode?.title = if (it > 1) "$it messages selected." else "$it message selected."
        }
        
        chatViewModel.chatAdapter?.otherMessageCount?.observe(this) { // Log.d(TAG, "Other message count <LIVE> = $it")
            val prev = chatViewModel.chatAdapter?.lastOtherMsgCount!!
            val current = it
            if (prev == current) return@observe
            actionMode?.invalidate()
            chatViewModel.chatAdapter?.lastOtherMsgCount = it
        }
        
        chatViewModel.chatsLive.observe(this) {
            if (it.isEmpty() || isRecreating) {
                return@observe
            }
            chatViewModel.chatAdapter!!.addNewChats(it)
            setChatInstructionOrRV()
        }
        binding.sendBtn.setOnClickListener { onSendButtonClicked(binding.messageBox.text.toString().trim()) }
        binding.discardBtn.setOnClickListener {
            chatViewModel.replyToChat = null
            hideReplyPreview()
        }
        // setUserProfilePictureAndName()
    }
    
    private fun showUnblockDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Unblock user ?")
            .setMessage("Unblock user ?")
            .setCancelable(true)
            .setPositiveButton("Yes") { _, _ ->
                chatViewModel.unblockUser(myId, userId)
            }.setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }.show()
    }
    
    private fun setChatInstructionOrRV() {
        if (chatViewModel.chatAdapter?.itemCount == 0) {
            (binding.chatRV.layoutParams as LinearLayout.LayoutParams).weight = 0F
            (binding.instruction.layoutParams as LinearLayout.LayoutParams).weight = 1F
        } else {
            (binding.chatRV.layoutParams as LinearLayout.LayoutParams).weight = 1F
            (binding.instruction.layoutParams as LinearLayout.LayoutParams).weight = 0F
        }
    }
    
    private fun setUserProfilePictureAndName() {
        lifecycleScope.launch {
            val userprofilePicture = chatViewModel.getUserImage(userId)
            if (userprofilePicture != null && blockedStatus == 0) {
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
        if (null == bundle) {
            Toast.makeText(this, "Some error occurred.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        userId = bundle.getLong(USER_ID)
        myId = bundle.getLong(LOGGED_IN_ID)
        userLastTime = bundle.getLong(USER_LAST_LOGIN)
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
    }
    
    private fun onSendButtonClicked(text: String) {
        if (blockedStatus == -1) {
            Toast.makeText(this, "You are now allowed to send messages to this user.", Toast.LENGTH_SHORT).show()
            return
        }
        
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
            
            setChatInstructionOrRV()
            
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
    
    private fun showReplyPreview() {
        findViewById<TextView>(R.id.replyToTxtPreview).visibility = View.VISIBLE
        findViewById<ImageView>(R.id.discardBtn).visibility = View.VISIBLE
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
        binding.messageBox.requestFocus()
    }
    
    private fun hideReplyPreview() {
        findViewById<TextView>(R.id.replyToTxtPreview).visibility = View.GONE
        findViewById<ImageView>(R.id.discardBtn).visibility = View.GONE
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
            //            Log.d(TAG, "markChatsAsDelete: rows affected = $rowsAffected")
            list.clear()
            //            Log.d(TAG, "after clearing list size = ${chatViewModel.chatAdapter?.selectedItems?.size}")
            withContext(Dispatchers.Main) {
                chatViewModel.chatAdapter!!.notifyDataSetChanged()
            }
        }
    }
    
    private fun showDeleteMessageAlert() {
        if (chatViewModel.chatAdapter?.selectedItems?.size!! < 1) return
        MaterialAlertDialogBuilder(this).setTitle("Delete chats.").setMessage("Delete ${chatViewModel.chatAdapter?.selectedItems?.size} message(s) ?").setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }.setPositiveButton("Yes") { dialog, which ->
            markChatsAsDelete()
            dialog.dismiss()
            chatViewModel.chatAdapter?.resetSelectMode()
        }.show()
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

