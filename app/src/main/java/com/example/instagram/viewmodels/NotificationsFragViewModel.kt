package com.example.instagram.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.instagram.adapters.COMMENTLOG
import com.example.instagram.adapters.FOLLOWLOG
import com.example.instagram.adapters.LIKELOG
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.model.CommentLog
import com.example.instagram.database.model.FollowLog
import com.example.instagram.database.model.LikeLog

private const val TAG = "NotifyViewModel_CommTag"

data class Notification(val time: Long, val lstRef: Int, val pos: Int)

class NotificationsFragViewModel(app: Application) : AndroidViewModel(app) {
    private val db: AppDatabase = AppDatabase.getDatabase(app)
    lateinit var followLogs: List<FollowLog>
    lateinit var likeLogs: List<LikeLog>
    lateinit var commentLogs: List<CommentLog>
    val placeHolderReference = MutableLiveData<MutableList<Notification>>()
    
    suspend fun getActivityLog(ownerId: Long) {
        followLogs = db.followDao().getFollowLog(ownerId)
        likeLogs = db.likesDao().getLikeLog(ownerId)
        commentLogs = db.commentDao().getCommentLog(ownerId)
        val listOfData: MutableList<Notification> = mutableListOf()
        for (i in followLogs.indices) {
            val log = followLogs[i]
            listOfData.add(Notification(log.time, FOLLOWLOG, i))
        }
        
        for (i in likeLogs.indices) {
            val log = likeLogs[i]
            listOfData.add(Notification(log.time, LIKELOG, i))
        }
        
        for (i in commentLogs.indices) {
            val log = commentLogs[i]
            listOfData.add(Notification(log.time, COMMENTLOG, i))
        }
        listOfData.sortByDescending { it.time }
        placeHolderReference.postValue(listOfData)
    }
}
