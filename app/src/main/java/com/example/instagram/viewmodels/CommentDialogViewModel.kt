package com.example.instagram.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.instagram.DateTime
import com.example.instagram.ImageUtil
import com.example.instagram.TimeFormatting
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.model.Comment
import kotlinx.coroutines.async
import com.example.instagram.database.entity.Comment as EComment

private const val TAG = "CommTag_CommentDialogViewModel"

class CommentDialogViewModel(val app: Application) : AndroidViewModel(app) {
    private val imageUtil = ImageUtil(app)
    private val db: AppDatabase = AppDatabase.getDatabase(app)
    val comments = MutableLiveData<MutableList<Comment>>()
    val commenterImages = MutableLiveData<MutableList<String>>()

    suspend fun insertComment(commentText: String, profileId: Long, postId: Long) {
        val time = System.currentTimeMillis()
        val comment = EComment(postId, profileId, commentText, time)
        val row = db.commentDao().insertComment(comment)
        Log.d(TAG, "insertComment: row_id = $row")
    }

    suspend fun getComments(postId: Long, myProfileId: Long) {
        val commentsListAsync =
            viewModelScope.async { db.commentDao().getAllComments(postId, myProfileId) }
        val myCommentsAsync =
            viewModelScope.async { db.commentDao().getMyComment(postId, myProfileId) }

        val commentsList = commentsListAsync.await()
        val myComments = myCommentsAsync.await()

        val isAddSuccessful = commentsList.addAll(0, myComments)
        Log.d(TAG, "adding myComments to commentsList is_successful = $isAddSuccessful")

        // commentsList is final from db

        val list = mutableListOf<Comment>()
        val imageList = mutableListOf<String>()
        for (i: EComment in commentsList) {
            val username = db.loginCredDao().getUsername(i.commenter_id)
            val comment = Comment(
                i.comment_id,
                i.commenter_id,
                username,
                DateTime.timeFormatter(i.comment_time, TimeFormatting.COMMENT),
                i.comment_text
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

    suspend fun deleteComment(commentId: Long): Int {
        val row = db.commentDao().deleteCommentById(commentId)
        Log.d(TAG, "deleteComment: row deleted - $row")
        return row
    }
}