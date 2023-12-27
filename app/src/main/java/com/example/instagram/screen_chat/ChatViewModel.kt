package com.example.instagram.screen_chat

import android.app.Application
import android.graphics.Bitmap
import android.os.Parcelable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.instagram.common.util.ImageUtil
import com.example.instagram.data.AppDatabase
import com.example.instagram.data.common_model.FullName
import com.example.instagram.data.entity.Chat
import com.example.instagram.data.entity.LastOnline
import kotlinx.coroutines.launch



class ChatViewModel(app: Application) : AndroidViewModel(app) {
    var chatsLive = MutableLiveData<MutableList<Chat>>()
    val db = AppDatabase.getDatabase(app)
    private val imageUtil = ImageUtil(app)
    lateinit var userFullName: FullName
    var chatAdapter: ChatAdapter? = null
    var state: Parcelable? = null
    var amIBlockedLive = MutableLiveData(0)
    
    // message reply vars
    var replyToChat: Chat? = null
    
    
    
    fun isUserNameInitialized(): Boolean = ::userFullName.isInitialized
    
    fun loadChats(userId: Long, myId: Long, chatsLoaded: Int) {
        viewModelScope.launch {
            val chats = db.chatDao().getChats(userId, myId, chatsLoaded)
            chatsLive.postValue(chats)
        }
    }
    
    fun updateMyLastOnlineStatus(myLastOnlineStatus: LastOnline?, myId: Long, userId: Long) {
        viewModelScope.launch {
            if (myLastOnlineStatus != null) { // record exists, just update myLast online time
                myLastOnlineStatus.time = System.currentTimeMillis()
                db.lastOnlineDao().updateMyLastOnlineStatus(myLastOnlineStatus)
            } else { // record doesn't exists, insert a new LastOnline in db
                db.lastOnlineDao().insertMyLastOnlineStatus(LastOnline(myId, userId, System.currentTimeMillis()))
            }
        }
    }
    
    suspend fun getUserImage(userId: Long): Bitmap? {
        val url = imageUtil.getProfilePictureUrl(userId)
        return url?.let { imageUtil.getBitmap(it) }
    }
    
    fun unblockUser(myId: Long, userId: Long) {
        viewModelScope.launch {
            val isDeleted = db.blockDao().unblockUser(myId, userId)
            if (isDeleted < 1) return@launch
            checkBlockedStatus(userId, myId)
        }
    }
    
    /**
     * Returns -1 when current logged-in user is blocked by another user
     *
     * returns 1 when another user is blocked by current logged-in user
     *
     * return 0 when no one is blocked.
     */
    suspend fun checkBlockedStatus(userA: Long, userB: Long) {
        
        
        val isUserBlocked = db.blockDao().isBlocked(userA, userB)
        val amIBlocked = db.blockDao().isBlocked(userB, userA)
        
        val value = if (isUserBlocked > 0) 1
        else if (amIBlocked > 0) -1
        else 0
        amIBlockedLive.postValue(value)
    }
}