package com.example.instagram.adapters

import android.content.Context
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

class CommentAdapter(

) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {
    private lateinit var mContext: Context
    private var commentList: MutableList<Comment> = mutableListOf()
    private lateinit var imageUtil: ImageUtil

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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.apply {

            CoroutineScope(Dispatchers.IO).launch {
                val bitmap = imageUtil.getBitmap(commentList[position].profilePicUrl)
                withContext(Dispatchers.Main) {
                    profileImage.setImageBitmap(bitmap)
                }
            }

            username.text = commentList[position].username
            commentTime.text = commentList[position].time
            commentText.text = commentList[position].comment
        }
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    fun updateList(newList: MutableList<Comment>) {
        this.commentList = newList
        notifyDataSetChanged()
    }
}