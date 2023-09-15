package com.example.instagram.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
const val DATE = 5

private const val TAG = "ChatAdapter_CommTag"

class ChatAdapter(
    private val userId: Long,
    private val myId: Long,
    private val onLongClick: (chat: Chat) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var chats = mutableListOf<Chat>()
    private lateinit var db: AppDatabase
    
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        db = AppDatabase.getDatabase(recyclerView.context)
        super.onAttachedToRecyclerView(recyclerView)
    }
    
    inner class RegularSentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val msg: TextView = view.findViewById(R.id.msg)
        val time: TextView = view.findViewById(R.id.time)
        val tick: ImageView = view.findViewById(R.id.seen)
        
        init {
            view.setOnLongClickListener {
                onLongClick(chats[adapterPosition])
                true
            }
        }
        
    }
    
    inner class ReplySentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val replyToTxt: TextView = view.findViewById(R.id.replyToTxt)
        val msg: TextView = view.findViewById(R.id.msg)
        val time: TextView = view.findViewById(R.id.time)
        val tick: ImageView = view.findViewById(R.id.seen)
        
        init {
            view.setOnLongClickListener {
                onLongClick(chats[adapterPosition])
                true
            }
        }
    }
    
    inner class RegularReceivedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val msg: TextView = view.findViewById(R.id.msg)
        val time: TextView = view.findViewById(R.id.time)
        
        init {
            view.setOnLongClickListener {
                onLongClick(chats[adapterPosition])
                true
            }
        }
    }
    
    inner class ReplyReceivedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val msg: TextView = view.findViewById(R.id.msg)
        val time: TextView = view.findViewById(R.id.time)
        val replyToTxt: TextView = view.findViewById(R.id.replyToTxt)
        
        init {
            view.setOnLongClickListener {
                onLongClick(chats[adapterPosition])
                true
            }
        }
    }
    
    inner class DateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView = view.findViewById(R.id.dateSeparator)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            REGULAR_SENT -> RegularSentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.bubble_sent_txt_msg, parent, false))
            REPLY_SENT -> ReplySentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.bubble_sent_reply_msg, parent, false))
            REGULAR_RECEIVED -> RegularReceivedViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.bubble_received_txt_msg, parent, false))
            REPLY_RECEIVED -> ReplyReceivedViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.bubble_received_reply_msg, parent, false))
            DATE -> DateViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.chat_date_seperator, parent, false))
            else -> throw Exception("Viewtype($viewType) is unknown for chat recycler view.")
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            REGULAR_RECEIVED -> {
                (holder as RegularReceivedViewHolder).apply {
                    msg.text = chats[position].message
                    time.text = DateTime.getChatMessageTime(chats[position].timeStamp)
                }
            }
            
            REPLY_RECEIVED -> {
                (holder as ReplyReceivedViewHolder).apply {
                    bindReplyReceived(position, holder)
                }
            }
            
            REPLY_SENT -> {
                (holder as ReplySentViewHolder).apply {
                    bindReplySent(position, holder)
                }
            }
            
            REGULAR_SENT -> {
                (holder as RegularSentViewHolder).apply {
                    msg.text = chats[position].message
                    time.text = DateTime.getChatMessageTime(chats[position].timeStamp)
                    /*tick*/
                }
            }
        }
    }
    
    private fun bindReplyReceived(position: Int, holder: ReplyReceivedViewHolder) {
        val replyChatId = chats[position].replyToChat
        
        CoroutineScope(Dispatchers.IO).launch {
            val originalChat = db.chatDao().getChat(replyChatId)
            withContext(Dispatchers.Main) {
                holder.replyToTxt.text = originalChat?.let {
                    it.message
                }
            }
        }
        
        holder.msg.text = chats[position].message
        holder.time.text = DateTime.getChatMessageTime(chats[position].timeStamp)
    }
    
    
    private fun bindReplySent(position: Int, holder: ReplySentViewHolder) {
        val replyChatId = chats[position].replyToChat
        Log.d(TAG, "is replyChatId Null = $replyChatId")
        
        CoroutineScope(Dispatchers.IO).launch {
            val originalChat = db.chatDao().getChat(replyChatId)
            Log.d(TAG, "is originalChat Null = $originalChat")
            withContext(Dispatchers.Main) {
                holder.replyToTxt.text = originalChat?.let {
                    it.message
                }
                holder.msg.text = chats[position].message
                holder.time.text = DateTime.getChatMessageTime(chats[position].timeStamp)
            }
        }
        
        // implement the function here which turn the color of tick to GREEN
    }
    
    override fun getItemCount(): Int {
        return chats.size
    }
    
    override fun getItemViewType(position: Int): Int {
        return if (chats[position].senderId == userId && chats[position].messageType == 1) {
            REGULAR_RECEIVED
        } else if (chats[position].senderId == userId && chats[position].messageType == 2) {
            REPLY_RECEIVED
        } else if (chats[position].senderId == myId && chats[position].messageType == 2) {
            REPLY_SENT
        } else {
            REGULAR_SENT
        }
    }
    
    fun addNewChats(newList: MutableList<Chat>) {
        chats.addAll(newList)
        notifyItemRangeInserted(chats.size, newList.size)
        logChatList()
    }
    
    fun addSentChat(chat: Chat) {
        chats.add(0, chat)
        notifyItemInserted(0)
        logChatList()
    }
    
    private fun logChatList(){
        Log.e(TAG, "logChatList:")
        for(i in chats.indices){
            val chat = chats[i]
            Log.i(TAG, "    $i -> $chat")
        }
    }
    
}