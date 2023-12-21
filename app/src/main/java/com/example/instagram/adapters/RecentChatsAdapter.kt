package com.example.instagram.adapters

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.domain.util.DateTime
import com.example.instagram.ImageUtil
import com.example.instagram.R
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.model.RecentChats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecentChatsAdapter(private val myId: Long, val openMessageScreen: (Long) -> Unit) : RecyclerView.Adapter<RecentChatsAdapter.RecentChatVH>() {
    private lateinit var imageUtil: ImageUtil
    private var chats = mutableListOf<RecentChats>()
    private lateinit var mContext: Context
    private lateinit var db: AppDatabase
    
    inner class RecentChatVH(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.profileImage)
        val fullName: TextView = view.findViewById(R.id.fullName)
        val lastMsg: TextView = view.findViewById(R.id.lastMessage)
        val lastMsgDate: TextView = view.findViewById(R.id.lastTime)
        val unreadMsgCount: TextView = view.findViewById(R.id.unreadMsgCount)
        
        init {
            
            view.setOnClickListener {
                val chat = chats[adapterPosition]
                val userId = if (myId == chat.receiverId) chat.senderId else chat.receiverId
                openMessageScreen(userId)
            }
        }
    }
    
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        mContext = recyclerView.context
        imageUtil = ImageUtil(mContext)
        db = AppDatabase.getDatabase(mContext)
        super.onAttachedToRecyclerView(recyclerView)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentChatVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_recent_chat, parent, false)
        return RecentChatVH(view)
    }
    
    override fun onBindViewHolder(holder: RecentChatVH, position: Int) {
        val chat = chats[position]
        val userId = if (myId == chat.receiverId) chat.senderId else chat.receiverId
        val sender = chat.senderId
        CoroutineScope(Dispatchers.IO).launch {
            val imageUrl = imageUtil.getProfilePictureUrl(userId) ?: return@launch
            if (chat.isBlocked) return@launch
            val bitmap = imageUtil.getBitmap(imageUrl)
            withContext(Dispatchers.Main) {
                holder.profileImage.setImageBitmap(bitmap)
            }
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            /*if (chat.isBlocked) { // enable this to hide name
                withContext(Dispatchers.Main) {
                    holder.fullName.text = "SocialbleX User"
                }
                return@launch
            }*/
            
            val fullName = db.profileDao().getFullName(userId)
            withContext(Dispatchers.Main) {
                holder.fullName.text = "${fullName.first_name} ${fullName.last_name}"
            }
        }
        holder.lastMsg.text = if (sender == myId && chat.messageType != 3) {
            "You: ${chat.message}"
        } else if (sender == myId) {
            holder.lastMsg.setTypeface(null, Typeface.ITALIC)
            mContext.getString(R.string.you_deleted_the_message)
        } else if (chat.messageType != 3) {
            chat.message
        } else {
            holder.lastMsg.setTypeface(null, Typeface.ITALIC)
            mContext.getString(R.string.this_message_was_deleted)
        }
        
        holder.lastMsgDate.text = DateTime.getChatMessageTime(chat.timeStamp)
        // set unread message count here
    }
    
    override fun getItemCount(): Int {
        return chats.size
    }
    
    fun setNewList(chats: MutableList<RecentChats>) {
        this.chats = chats
        notifyDataSetChanged()
    }
    
}