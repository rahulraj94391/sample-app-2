package com.example.instagram.adapters

import android.content.Context
import android.graphics.PorterDuff
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

private const val TAG = "ChatAdapter_CommTag"

class ChatAdapter(
    private val userLastOnlineTime: Long,
    private val userId: Long,
    private val myId: Long,
    private val onLongClick: (chat: Chat) -> Unit,
    private val highlightOriginalMsg: (chatId: Long) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var chats = mutableListOf<Chat>()
    private lateinit var db: AppDatabase
    private lateinit var mContext: Context
    
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        mContext = recyclerView.context
        db = AppDatabase.getDatabase(mContext)
        super.onAttachedToRecyclerView(recyclerView)
    }
    
    inner class RegularSentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val msg: TextView = view.findViewById(R.id.msg)
        val time: TextView = view.findViewById(R.id.time)
        val tick: ImageView = view.findViewById(R.id.seen)
        val dateSeparator: TextView = view.findViewById(R.id.dateSeparator)
        
        init {
            view.setOnLongClickListener {
                onLongClick(chats[adapterPosition])
                true
            }
        }
        
        
        //        init {
        //            view.setOnLongClickListener {
        //                onLongClick(chats[adapterPosition])
        //                true
        //            }
        //        }
        
    }
    
    inner class ReplySentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val replyToTxt: TextView = view.findViewById(R.id.replyToTxt)
        val msg: TextView = view.findViewById(R.id.msg)
        val time: TextView = view.findViewById(R.id.time)
        val tick: ImageView = view.findViewById(R.id.seen)
        val dateSeparator: TextView = view.findViewById(R.id.dateSeparator)
        
        
        init {
            view.setOnLongClickListener {
                onLongClick(chats[adapterPosition])
                true
            }
            replyToTxt.setOnClickListener {
                highlightOriginalMsg(chats[adapterPosition].replyToChat)
            }
        }
    }
    
    inner class RegularReceivedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val msg: TextView = view.findViewById(R.id.msg)
        val time: TextView = view.findViewById(R.id.time)
        val dateSeparator: TextView = view.findViewById(R.id.dateSeparator)
        
        
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
        val dateSeparator: TextView = view.findViewById(R.id.dateSeparator)
        
        
        init {
            view.setOnLongClickListener {
                onLongClick(chats[adapterPosition])
                true
            }
            
            replyToTxt.setOnClickListener {
                highlightOriginalMsg(chats[adapterPosition].replyToChat)
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            REGULAR_SENT -> RegularSentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.bubble_sent_txt_msg, parent, false))
            REPLY_SENT -> ReplySentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.bubble_sent_reply_msg, parent, false))
            REGULAR_RECEIVED -> RegularReceivedViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.bubble_received_txt_msg, parent, false))
            REPLY_RECEIVED -> ReplyReceivedViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.bubble_received_reply_msg, parent, false))
            else -> throw Exception("Viewtype($viewType) is unknown for chat recycler view.")
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            REGULAR_RECEIVED -> bindRegularReceived(position, holder as RegularReceivedViewHolder)
            REPLY_RECEIVED -> bindReplyReceived(position, holder as ReplyReceivedViewHolder)
            REPLY_SENT -> bindReplySent(position, holder as ReplySentViewHolder)
            REGULAR_SENT -> bindRegularSent(position, holder as RegularSentViewHolder)
        }
    }
    
    private fun bindRegularSent(position: Int, holder: RegularSentViewHolder) {
        holder.msg.text = chats[position].message
        holder.time.text = DateTime.getChatMessageTime(chats[position].timeStamp)
        showTimeStamp(holder.dateSeparator, position)
        isMessageRead(chats[position].timeStamp, holder.tick)
    }
    
    private fun bindRegularReceived(position: Int, holder: RegularReceivedViewHolder) {
        holder.msg.text = chats[position].message
        holder.time.text = DateTime.getChatMessageTime(chats[position].timeStamp)
        showTimeStamp(holder.dateSeparator, position)
    }
    
    private fun bindReplyReceived(position: Int, holder: ReplyReceivedViewHolder) {
        val replyChatId = chats[position].replyToChat
        CoroutineScope(Dispatchers.IO).launch {
            val originalChat = db.chatDao().getChat(replyChatId)
            withContext(Dispatchers.Main) {
                holder.replyToTxt.text = originalChat.message
                holder.msg.text = chats[position].message
                setReplyMsgContainerWidth(originalChat.message, chats[position].message, holder.replyToTxt)
                holder.time.text = DateTime.getChatMessageTime(chats[position].timeStamp)
            }
        }
        showTimeStamp(holder.dateSeparator, position)
    }
    
    private fun showTimeStamp(dateSeparator: TextView, position: Int) {
        val dateToDisplay = DateTime.getChatSeparatorTime(chats[position].timeStamp)
        if (position == chats.size - 1) {
            dateSeparator.visibility = View.VISIBLE
            dateSeparator.text = dateToDisplay
            return
        }
        val isSameDay = DateTime.isSameDay(chats[position].timeStamp, chats[position + 1].timeStamp)
        if (isSameDay) {
            dateSeparator.visibility = View.GONE
        } else {
            dateSeparator.text = dateToDisplay
            dateSeparator.visibility = View.VISIBLE
        }
    }
    
    private fun bindReplySent(position: Int, holder: ReplySentViewHolder) {
        val replyChatId = chats[position].replyToChat
        CoroutineScope(Dispatchers.IO).launch {
            val originalChat = db.chatDao().getChat(replyChatId)
            withContext(Dispatchers.Main) {
                holder.replyToTxt.text = originalChat.message
                holder.msg.text = chats[position].message
                setReplyMsgContainerWidth(originalChat.message, chats[position].message, holder.replyToTxt)
                holder.time.text = DateTime.getChatMessageTime(chats[position].timeStamp)
            }
        }
        
        showTimeStamp(holder.dateSeparator, position)
        isMessageRead(chats[position].timeStamp, holder.tick)
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
    }
    
    fun addSentChat(chat: Chat) {
        chats.add(0, chat)
        notifyItemInserted(0)
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
            tick.setColorFilter(mContext.resources.getColor(R.color.msg_read, mContext.theme), PorterDuff.Mode.SRC_IN)
        } else {
            tick.setColorFilter(mContext.resources.getColor(R.color.white, mContext.theme), PorterDuff.Mode.SRC_IN)
        }
    }
}





