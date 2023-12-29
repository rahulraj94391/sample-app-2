package com.example.instagram.screen_commentView

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.instagram.common.util.DateTime
import com.example.instagram.common.util.ImageUtil
import com.example.instagram.common.util.TimeFormatting
import com.example.instagram.data.AppDatabase
import com.example.instagram.screen_commentView.model.Comment
import kotlinx.coroutines.async
import com.example.instagram.data.entity.Comment as EComment

class CommentBottomSheetModel(val app: Application) : AndroidViewModel(app) {
    private val imageUtil = ImageUtil(app)
    private val db: AppDatabase = AppDatabase.getDatabase(app)
    val comments = MutableLiveData<MutableList<Comment>>()
    val commenterImages = MutableLiveData<MutableList<String>>()
    
    suspend fun insertComment(commentText: String, profileId: Long, postId: Long) {
        val time = System.currentTimeMillis()
        val comment = EComment(postId, profileId, commentText, time)
        db.commentDao().insertComment(comment)
    }
    
    suspend fun getComments(postId: Long, myProfileId: Long) {
        val commentsListAsync = viewModelScope.async { db.commentDao().getAllComments(postId, myProfileId) }
        val myCommentsAsync = viewModelScope.async { db.commentDao().getMyComment(postId, myProfileId) }
        val commentsList = commentsListAsync.await()
        val myComments = myCommentsAsync.await()
        commentsList.addAll(0, myComments)
        val list = mutableListOf<Comment>()
        val imageList = mutableListOf<String>()
        for (i: EComment in commentsList) {
            val username = db.loginCredDao().getUsername(i.commenterId)
            val comment = Comment(
                i.commentId,
                i.commenterId,
                username,
                DateTime.timeFormatter(i.commentTime, TimeFormatting.COMMENT),
                i.commentText
            )
            list.add(comment)
        }
        comments.postValue(list)
        for (i in list) {
            val id = i.profileId
            val imageUrl = imageUtil.getProfilePictureUrl(id) ?: continue
            imageList.add(imageUrl)
        }
        commenterImages.postValue(imageList)
    }
    
    suspend fun deleteComment(commentId: Long) = db.commentDao().deleteCommentById(commentId)
}