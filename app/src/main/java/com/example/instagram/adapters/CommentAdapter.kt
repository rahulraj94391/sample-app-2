package com.example.instagram.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.ImageUtil
import com.example.instagram.R
import com.example.instagram.database.model.Comment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "CommTag_CommentAdapter"

class CommentAdapter(
    val listener: (pos: Int) -> Unit,
    val loggedInProfileId: Long,
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {
    private lateinit var mContext: Context
    private var commentList: MutableList<Comment> = mutableListOf()
    private lateinit var imageUtil: ImageUtil
    private var profileImages: MutableList<String> = mutableListOf()
    
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        mContext = recyclerView.context
        imageUtil = ImageUtil(mContext)
        super.onAttachedToRecyclerView(recyclerView)
    }
    
    inner class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.profileImage)
        val username: TextView = view.findViewById(R.id.username)
        val commentTime: TextView = view.findViewById(R.id.commentTime)
        val commentText: TextView = view.findViewById(R.id.commentText)
        
        init {
            view.setOnLongClickListener {
                return@setOnLongClickListener if (commentList[adapterPosition].profileId != loggedInProfileId) {
                    false
                } else {
                    listener(adapterPosition)
                    true
                }
            }
        }
        
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_comment, parent, false)
        return CommentViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.apply {
            username.text = commentList[position].username
            commentTime.text = commentList[position].time
            commentText.text = commentList[position].comment
        }
    }
    
    
    fun addImageUrlToList(url: String) {
        profileImages.add(0, url)
    }
    
    
    override fun onBindViewHolder(
        holder: CommentViewHolder,
        position: Int,
        payload: MutableList<Any>,
    ) {
        if (profileImages.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val bitmap = imageUtil.getBitmap(profileImages[position])
                    withContext(Dispatchers.Main) {
                        holder.profileImage.setImageBitmap(bitmap)
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "onBindViewHolder: ${e.message}")
                }
            }
        }
        super.onBindViewHolder(holder, position, payload)
    }
    
    override fun getItemCount(): Int {
        return commentList.size
    }
    
    fun updateImages(newList: MutableList<String>) {
        profileImages = newList
        notifyItemRangeChanged(0, commentList.size, profileImages)
    }
    
    fun updateList(newList: MutableList<Comment>) {
        this.commentList = newList
        notifyDataSetChanged()
    }
}