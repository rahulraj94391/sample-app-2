package com.example.instagram.adapters

import android.content.Context
import android.graphics.PorterDuff
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.DateTime
import com.example.instagram.R
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.entity.Chat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val REGULAR_SENT = 1
const val REPLY_SENT = 2
const val REGULAR_RECEIVED = 3
const val REPLY_RECEIVED = 4
const val DELETE_SENT = 5
const val DELETE_RECEIVED = 6

const val MAX_MSG_SELECTION_IN_SELECT_MODE = 50

private const val TAG = "ChatAdapter_CommTag"

class ChatAdapter(
    private val userLastOnlineTime: Long,
    private val userId: Long,
    private val myId: Long,
    private val highlightOriginalMsg: (chatId: Long) -> Unit,
    private val replyTextBuilder: (chat: Chat) -> SpannableStringBuilder,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var _chats = mutableListOf<Chat>()
    private lateinit var db: AppDatabase
    private lateinit var mContext: Context
    private var _otherMessageCount = MutableLiveData(0)
    private var isToastShown = false
    val chats: List<Chat> get() = _chats
    var lastOtherMsgCount = 0
    val selectedItems = mutableListOf<Chat>()
    var isSelectMode = false
    val selectedMessageCount = MutableLiveData(0)
    val otherMessageCount get() = _otherMessageCount
    
    
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        mContext = recyclerView.context
        db = AppDatabase.getDatabase(mContext)
        super.onAttachedToRecyclerView(recyclerView)
    }
    
    inner class RegularSentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val msg: TextView = view.findViewById(R.id.msg)
        val time: TextView = view.findViewById(R.id.time)
        val tick: ImageView = view.findViewById(R.id.seen)
        
        init {
            view.setOnClickListener {
                onClickViewHolder(adapterPosition, this)
            }
            
            view.setOnLongClickListener {
                if (!isSelectMode) {
                    isSelectMode = true
                    onClickViewHolder(adapterPosition, this)
                    true
                } else
                    false
            }
        }
    }
    
    inner class ReplySentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val replyToTxt: TextView = view.findViewById(R.id.replyToTxt)
        val msg: TextView = view.findViewById(R.id.msg)
        val time: TextView = view.findViewById(R.id.time)
        val tick: ImageView = view.findViewById(R.id.seen)
        
        init {
            view.setOnClickListener {
                onClickViewHolder(adapterPosition, this)
            }
            
            view.setOnLongClickListener {
                if (!isSelectMode) {
                    isSelectMode = true
                    onClickViewHolder(adapterPosition, this)
                    true
                } else
                    false
            }
            
            replyToTxt.setOnClickListener {
                highlightOriginalMsg(_chats[adapterPosition].replyToChat)
            }
        }
    }
    
    inner class RegularReceivedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val msg: TextView = view.findViewById(R.id.msg)
        val time: TextView = view.findViewById(R.id.time)
        
        init {
            view.setOnClickListener {
                onClickViewHolder(adapterPosition, this)
            }
            
            view.setOnLongClickListener {
                if (!isSelectMode) {
                    isSelectMode = true
                    onClickViewHolder(adapterPosition, this)
                    true
                } else
                    false
            }
        }
    }
    
    inner class ReplyReceivedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val msg: TextView = view.findViewById(R.id.msg)
        val time: TextView = view.findViewById(R.id.time)
        val replyToTxt: TextView = view.findViewById(R.id.replyToTxt)
        
        init {
            view.setOnClickListener {
                onClickViewHolder(adapterPosition, this)
            }
            
            view.setOnLongClickListener {
                if (!isSelectMode) {
                    isSelectMode = true
                    onClickViewHolder(adapterPosition, this)
                    true
                } else
                    false
            }
            
            replyToTxt.setOnClickListener {
                highlightOriginalMsg(_chats[adapterPosition].replyToChat)
            }
        }
    }
    
    inner class DeleteSentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            view.setOnClickListener {
                onClickViewHolder(adapterPosition, this)
            }
            
            view.setOnLongClickListener {
                if (!isSelectMode) {
                    isSelectMode = true
                    onClickViewHolder(adapterPosition, this)
                    true
                } else
                    false
            }
        }
    }
    
    inner class DeleteReceivedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            view.setOnClickListener {
                onClickViewHolder(adapterPosition, this)
            }
            
            view.setOnLongClickListener {
                if (!isSelectMode) {
                    isSelectMode = true
                    onClickViewHolder(adapterPosition, this)
                    true
                } else
                    false
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            REGULAR_SENT -> RegularSentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.bubble_sent_txt_msg, parent, false))
            REPLY_SENT -> ReplySentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.bubble_sent_reply_msg, parent, false))
            DELETE_SENT -> DeleteSentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.bubble_sent_msg_deleted, parent, false))
            REGULAR_RECEIVED -> RegularReceivedViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.bubble_received_txt_msg, parent, false))
            REPLY_RECEIVED -> ReplyReceivedViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.bubble_received_reply_msg, parent, false))
            DELETE_RECEIVED -> DeleteReceivedViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.bubble_received_msg_deleted, parent, false))
            else -> throw Exception("onCreateViewHolder: Viewtype($viewType) is unknown for chat recycler view")
        }
    }
    
    /**
     * Here the view holders itemView are tagged with a Pair<Int, Int>.
     * Pair.first denotes the VH adapter position and pair.second denotes
     * the type of the VH as defined as REGULAR_SENT, REPLY_SENT,
     * REGULAR_RECEIVED, REPLY_RECEIVED, DELETE_SENT, DELETE_RECEIVED.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            REGULAR_RECEIVED -> bindRegularReceived(position, holder as RegularReceivedViewHolder)
            REPLY_RECEIVED -> bindReplyReceived(position, holder as ReplyReceivedViewHolder)
            REPLY_SENT -> bindReplySent(position, holder as ReplySentViewHolder)
            REGULAR_SENT -> bindRegularSent(position, holder as RegularSentViewHolder)
            DELETE_SENT -> bindSentDelete(position, holder as DeleteSentViewHolder)
            DELETE_RECEIVED -> bindReceivedDelete(position, holder as DeleteReceivedViewHolder)
        }
    }
    
    private fun bindRegularReceived(position: Int, holder: RegularReceivedViewHolder) {
        holder.itemView.tag = Pair(position, REGULAR_RECEIVED)
        val chat = _chats[position]
        holder.msg.text = chat.message
        holder.time.text = DateTime.getChatMessageTime(_chats[position].timeStamp)
        isChatAlreadyInSelectMode(chat, holder)
    }
    
    private fun bindReplyReceived(position: Int, holder: ReplyReceivedViewHolder) {
        holder.itemView.tag = Pair(position, REPLY_RECEIVED)
        val chat = _chats[position]
        isChatAlreadyInSelectMode(chat, holder)
        CoroutineScope(Dispatchers.IO).launch {
            val originalChat = db.chatDao().getChat(chat.replyToChat)
            withContext(Dispatchers.Main) {
                holder.replyToTxt.text = replyTextBuilder(originalChat)
                holder.msg.text = _chats[position].message
                setReplyMsgContainerWidth(originalChat.message, _chats[position].message, holder.replyToTxt)
                holder.time.text = DateTime.getChatMessageTime(_chats[position].timeStamp)
            }
        }
    }
    
    private fun bindReplySent(position: Int, holder: ReplySentViewHolder) {
        holder.itemView.tag = Pair(position, REPLY_SENT)
        val chat = _chats[position]
        isChatAlreadyInSelectMode(chat, holder)
        CoroutineScope(Dispatchers.IO).launch {
            val originalChat = db.chatDao().getChat(chat.replyToChat)
            withContext(Dispatchers.Main) {
                holder.replyToTxt.text = replyTextBuilder(originalChat)
                holder.msg.text = _chats[position].message
                setReplyMsgContainerWidth(originalChat.message, _chats[position].message, holder.replyToTxt)
                holder.time.text = DateTime.getChatMessageTime(_chats[position].timeStamp)
            }
        }
        isMessageRead(_chats[position].timeStamp, holder.tick)
    }
    
    private fun bindReceivedDelete(position: Int, holder: DeleteReceivedViewHolder) {
        holder.itemView.tag = Pair(position, DELETE_RECEIVED)
        val chat = chats[position]
        isChatAlreadyInSelectMode(chat, holder)
    }
    
    private fun bindSentDelete(position: Int, holder: DeleteSentViewHolder) {
        holder.itemView.tag = Pair(position, DELETE_SENT)
        val chat = chats[position]
        isChatAlreadyInSelectMode(chat, holder)
    }
    
    private fun bindRegularSent(position: Int, holder: RegularSentViewHolder) {
        holder.itemView.tag = Pair(position, REGULAR_SENT)
        val chat = _chats[position]
        isChatAlreadyInSelectMode(chat, holder)
        holder.msg.text = chat.message
        holder.time.text = DateTime.getChatMessageTime(_chats[position].timeStamp)
        isMessageRead(_chats[position].timeStamp, holder.tick)
    }
    
    override fun getItemCount(): Int {
        return _chats.size
    }
    
    override fun getItemViewType(position: Int): Int {
        return if (_chats[position].senderId == userId && _chats[position].messageType == 1) REGULAR_RECEIVED
        else if (_chats[position].senderId == userId && _chats[position].messageType == 2) REPLY_RECEIVED
        else if (_chats[position].senderId == myId && _chats[position].messageType == 2) REPLY_SENT
        else if (_chats[position].senderId == myId && _chats[position].messageType == 3) DELETE_SENT
        else if (_chats[position].senderId == userId && _chats[position].messageType == 3) DELETE_RECEIVED
        else REGULAR_SENT
    }
    
    fun addNewChats(newList: MutableList<Chat>) {
        _chats.addAll(newList)
        notifyItemRangeInserted(_chats.size, newList.size)
    }
    
    fun addSentChat(chat: Chat) {
        _chats.add(0, chat)
        // notifyItemInserted(0)
        notifyDataSetChanged()
    }
    
    private fun setReplyMsgContainerWidth(msgRepliedTo: String, msg: String, replyToTxt: TextView) {
        if (msgRepliedTo.length > msg.length) {
            replyToTxt.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        } else {
            replyToTxt.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }
    
    private fun isMessageRead(chatTime: Long, tick: ImageView) {
        if (chatTime <= userLastOnlineTime) {
            tick.setColorFilter(mContext.resources.getColor(R.color.white, mContext.theme), PorterDuff.Mode.SRC_IN)
        } else {
            tick.setColorFilter(mContext.resources.getColor(R.color.col8, mContext.theme), PorterDuff.Mode.SRC_IN)
        }
    }
    
    private fun onClickViewHolder(position: Int, holder: RecyclerView.ViewHolder) {
        if (!isSelectMode) return
        
        val chat = chats[position]
        
        val isNotMyMessage = chat.senderId == userId || (chat.senderId == myId && chat.messageType == 3)
        
        if (selectedItems.contains(chat)) {
            
            if (isNotMyMessage) {
                _otherMessageCount.postValue(_otherMessageCount.value!! - 1)
            }
            
            holder.itemView.setBackgroundColor(mContext.resources.getColor(android.R.color.transparent, mContext.theme))
            selectedItems.remove(chat)
            selectedMessageCount.postValue(selectedMessageCount.value!! - 1)
        } else {
            if (selectedItems.size >= MAX_MSG_SELECTION_IN_SELECT_MODE) {
                if (!isToastShown) {
                    Toast.makeText(mContext, "Can select at most ${selectedItems.size} at a time.", Toast.LENGTH_SHORT).show()
                    // isToastShown = true // enable this to show toast once.
                }
                return
            }
            
            if (isNotMyMessage) {
                _otherMessageCount.postValue(_otherMessageCount.value!! + 1)
            }
            holder.itemView.setBackgroundColor(mContext.resources.getColor(R.color.col9, mContext.theme))
            selectedItems.add(chat)
            selectedMessageCount.postValue(selectedMessageCount.value!! + 1)
        }
    
        
        
        if (selectedItems.size < 1) {
            isSelectMode = false
        }
    }
    
    fun resetSelectMode() {
        otherMessageCount.postValue(0)
        selectedMessageCount.postValue(0)
        isSelectMode = false
        selectedItems.clear()
        notifyDataSetChanged()
    }
    
    private fun isChatAlreadyInSelectMode(chat: Chat, holder: RecyclerView.ViewHolder) {
        if (selectedItems.contains(chat)) {
            holder.itemView.setBackgroundColor(mContext.resources.getColor(R.color.col9, mContext.theme))
        } else {
            holder.itemView.setBackgroundColor(mContext.resources.getColor(android.R.color.transparent, mContext.theme))
        }
    }
    
}





