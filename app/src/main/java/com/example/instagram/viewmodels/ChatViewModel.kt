package com.example.instagram.viewmodels

import android.app.Application
import android.graphics.Bitmap
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.instagram.ImageUtil
import com.example.instagram.adapters.ChatAdapter
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.entity.Chat
import com.example.instagram.database.entity.LastOnline
import com.example.instagram.database.model.FullName
import kotlinx.coroutines.launch

private const val TAG = "ChatViewModel_CommTag"

class ChatViewModel(app: Application) : AndroidViewModel(app) {
    var chatsLive = MutableLiveData<MutableList<Chat>>()
    val db = AppDatabase.getDatabase(app)
    private val imageUtil = ImageUtil(app)
    lateinit var userFullName: FullName
    var chatAdapter: ChatAdapter? = null
    var state: Parcelable? = null
    
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
                Log.d(TAG, "inside else-block")
                db.lastOnlineDao().insertMyLastOnlineStatus(LastOnline(myId, userId, System.currentTimeMillis()))
            }
        }
    }
    
    suspend fun getUserImage(userId: Long): Bitmap? {
        val url = imageUtil.getProfilePictureUrl(userId)
        return url?.let { imageUtil.getBitmap(it) }
    }
}