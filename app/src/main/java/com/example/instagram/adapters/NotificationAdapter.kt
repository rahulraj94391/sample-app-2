package com.example.instagram.adapters

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.ImageUtil
import com.example.instagram.R
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.entity.Follow
import com.example.instagram.database.model.CommentLog
import com.example.instagram.database.model.FollowLog
import com.example.instagram.database.model.LikeLog
import com.example.instagram.viewmodels.Data
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val FOLLOWLOG = 1
const val LIKELOG = 2
const val COMMENTLOG = 3


private const val TAG = "NotifAdapter_CommTag"

class NotificationAdapter(
    private val loggedInId: Long,
    private val spanBuilder: (String, Long, Int) -> SpannableStringBuilder,
    private val openProfile: (Long) -> Unit,
    private val openPost: (Long) -> Unit,
    private val openCommentOnPost: (Long, Long) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var followLogs: List<FollowLog> = mutableListOf()
    private var likeLogs: List<LikeLog> = mutableListOf()
    private var commentLogs: List<CommentLog> = mutableListOf()
    private var placeHolderReference: MutableList<Data> = mutableListOf()
    private lateinit var db: AppDatabase
    private lateinit var imageUtil: ImageUtil
    
    inner class FollowNotificationVH(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.profileImage)
        val log: TextView = view.findViewById(R.id.logText)
        val btn: MaterialButton = view.findViewById(R.id.btnEnd)
    }
    
    inner class LikeNotificationVH(view: View) : RecyclerView.ViewHolder(view) { // this will be used for comment and like Notification
        val otherUserImage: ImageView = view.findViewById(R.id.profileImage)
        val log: TextView = view.findViewById(R.id.logText)
        val postImage: ImageView = view.findViewById(R.id.postImageOne)
    }
    
    inner class CommentNotificationVH(view: View) : RecyclerView.ViewHolder(view) { // this will be used for comment and like Notification
        val otherUserImage: ImageView = view.findViewById(R.id.profileImage)
        val log: TextView = view.findViewById(R.id.logText)
        val postImage: ImageView = view.findViewById(R.id.postImageOne)
    }
    
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        db = AppDatabase.getDatabase(recyclerView.context)
        imageUtil = ImageUtil(recyclerView.context)
        super.onAttachedToRecyclerView(recyclerView)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            FOLLOWLOG -> FollowNotificationVH(LayoutInflater.from(parent.context).inflate(R.layout.row_log_follow, parent, false))
            LIKELOG -> LikeNotificationVH(LayoutInflater.from(parent.context).inflate(R.layout.row_log_comment_and_like, parent, false))
            else -> CommentNotificationVH(LayoutInflater.from(parent.context).inflate(R.layout.row_log_comment_and_like, parent, false))
        }
    }
    
    override fun getItemCount() = placeHolderReference.size
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FollowNotificationVH -> bindFollowRow(holder, position)
            is LikeNotificationVH -> bindLikeRow(holder, position)
            is CommentNotificationVH -> bindCommentRow(holder, position)
        }
    }
    
    private fun bindFollowRow(holder: FollowNotificationVH, pos: Int) {
        val ref = placeHolderReference[pos]
        val originalList = whichList(ref.lstRef) as List<FollowLog>
        val log = originalList[ref.pos]
        holder.log.text = spanBuilder(log.username, log.time, FOLLOWLOG)
        
        holder.itemView.setOnClickListener { openProfile(log.owner_id) }
        
        CoroutineScope(Dispatchers.IO).launch {
            val imageURL = imageUtil.getProfilePictureUrl(log.owner_id) ?: return@launch
            val imageBitmap = imageUtil.getBitmap(imageURL)
            withContext(Dispatchers.Main) {
                holder.imageView.setImageBitmap(imageBitmap)
            }
        }
        holder.btn.setOnClickListener { executeFollowUnfollow(log.owner_id, holder.btn) }
        CoroutineScope(Dispatchers.IO).launch {
            val a = db.followDao().isUserFollowingUser(loggedInId, log.owner_id)
            withContext(Dispatchers.Main) {
                if (a < 1) holder.btn.text = FOLLOW
                else holder.btn.text = UNFOLLOW
                holder.btn.isEnabled = true
            }
        }
    }
    
    private fun executeFollowUnfollow(id: Long, btn: MaterialButton) {
        CoroutineScope(Dispatchers.IO).launch {
            val a = db.followDao().isUserFollowingUser(loggedInId, id/*log.owner_id*/)
            if (a < 1) {
                db.followDao().insertNewFollow(Follow(loggedInId, id, System.currentTimeMillis()))
                withContext(Dispatchers.Main) {
                    btn.text = UNFOLLOW
                }
            } else {
                db.followDao().deleteFollow(loggedInId, id)
                withContext(Dispatchers.Main) {
                    btn.text = FOLLOW
                }
            }
        }
    }
    
    private fun bindLikeRow(holder: LikeNotificationVH, pos: Int) {
        val ref = placeHolderReference[pos]
        val originalList = whichList(ref.lstRef) as List<LikeLog>
        val log = originalList[ref.pos]
        holder.itemView.setOnClickListener { openPost(log.post_id) }
        holder.log.text = spanBuilder(log.username, log.time, LIKELOG)
        CoroutineScope(Dispatchers.IO).launch {
            val imageURL = imageUtil.getProfilePictureUrl(log.profile_id) ?: return@launch
            val bitmap = imageUtil.getBitmap(imageURL)
            withContext(Dispatchers.Main) {
                holder.otherUserImage.setImageBitmap(bitmap)
            }
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            val imageURL = imageUtil.getOneImagePerPost(mutableListOf(log.post_id))[0].imageURl
            val bitmap = imageUtil.getBitmap(imageURL)
            withContext(Dispatchers.Main) {
                holder.postImage.setImageBitmap(bitmap)
            }
        }
    }
    
    private fun bindCommentRow(holder: CommentNotificationVH, pos: Int) {
        val ref = placeHolderReference[pos]
        val originalList = whichList(ref.lstRef) as List<CommentLog>
        val log = originalList[ref.pos]
        holder.log.text = spanBuilder(log.username, log.time, COMMENTLOG)
        holder.itemView.setOnClickListener { openCommentOnPost(log.post_id, log.comment_id) }
        CoroutineScope(Dispatchers.IO).launch {
            val imageURL = imageUtil.getProfilePictureUrl(log.commenter_id) ?: return@launch
            val bitmap = imageUtil.getBitmap(imageURL)
            withContext(Dispatchers.Main) {
                holder.otherUserImage.setImageBitmap(bitmap)
            }
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            val imageURL = imageUtil.getOneImagePerPost(mutableListOf(log.post_id))[0].imageURl
            val bitmap = imageUtil.getBitmap(imageURL)
            withContext(Dispatchers.Main) {
                holder.postImage.setImageBitmap(bitmap)
            }
        }
    }
    
    override fun getItemViewType(position: Int) = placeHolderReference[position].lstRef
    
    fun setNewList(followLogs: List<FollowLog>, likeLogs: List<LikeLog>, commentLogs: List<CommentLog>, placeHolderReference: MutableList<Data>) {
        this.followLogs = followLogs
        this.likeLogs = likeLogs
        this.commentLogs = commentLogs
        this.placeHolderReference = placeHolderReference
        notifyDataSetChanged()
    }
    
    private fun whichList(lstRef: Int): List<Any> {
        return when (lstRef) {
            FOLLOWLOG -> followLogs
            LIKELOG -> likeLogs
            else -> commentLogs
        }
    }
}