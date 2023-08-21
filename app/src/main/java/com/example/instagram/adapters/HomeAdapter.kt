package com.example.instagram.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.instagram.ImageUtil
import com.example.instagram.R
import com.example.instagram.database.model.Post
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "CommTag_HomeAdapter"

class HomeAdapter(
    val commentListener: (Int) -> Unit,
    val profileListener: (Int) -> Unit,
    val likeListener: (Int, View) -> Unit,
    val saveListener: (Int, View) -> Unit,
    val commentCountDelegate: (TextView, Long) -> Unit,
) : RecyclerView.Adapter<HomeAdapter.PostVH>() {
    private lateinit var mContext: Context
    private lateinit var imageUtil: ImageUtil
    private var list: MutableList<Post> = mutableListOf()
    
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        mContext = recyclerView.context
        imageUtil = ImageUtil(mContext)
        super.onAttachedToRecyclerView(recyclerView)
    }
    
    inner class PostVH(item: View) : RecyclerView.ViewHolder(item) {
        val profileImage: ImageView = item.findViewById(R.id.profileImage)
        val username: TextView = item.findViewById(R.id.username)
        val postImages: ViewPager2 = item.findViewById(R.id.allImagesInAPostVP2)
        val likeBtn: MaterialCheckBox = item.findViewById(R.id.likeBtn)
        val commentButton: MaterialButton = item.findViewById(R.id.comment)
        val savePostBtn: MaterialCheckBox = item.findViewById(R.id.btnSavePost)
        val likeCount: TextView = item.findViewById(R.id.likeCount)
        val postDesc: TextView = item.findViewById(R.id.postDesc)
        val commentCount: TextView = item.findViewById(R.id.commentCount)
        val timeOfPost: TextView = item.findViewById(R.id.timeOfPost)
        val adapter: PostAdapter = PostAdapter()
        val indicator: TabLayout = item.findViewById(R.id.indicatorVP)
        
        init {
            profileImage.setOnClickListener { profileListener(adapterPosition) }
            username.setOnClickListener { profileListener(adapterPosition) }
            likeBtn.setOnClickListener { likeListener(adapterPosition, it) }
            savePostBtn.setOnClickListener { saveListener(adapterPosition, it) }
            postDesc.setOnClickListener {
                (it as TextView).apply {
                    ellipsize = null
                    maxLines = Int.MAX_VALUE
                }
            }
            commentButton.setOnClickListener { commentListener(adapterPosition) }
            commentCount.setOnClickListener { commentListener(adapterPosition) }
            postImages.adapter = adapter
            TabLayoutMediator(indicator, postImages) { _, _ -> }.attach()
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeAdapter.PostVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_home_screen, parent, false)
        return PostVH(view)
    }
    
    override fun onBindViewHolder(holder: PostVH, position: Int) {
        holder.apply {
            setImage(profileImage, position)
            username.text = list[position].profileUsername
            likeBtn.checkedState = setLikedStat(position)
            savePostBtn.checkedState = setSavedStat(position)
            likeCount.text = list[position].likeCount
            postDesc.text = list[position].postDesc
            commentCountDelegate(commentCount, list[position].postId)
//            commentCount.text = list[position].commentCount
            timeOfPost.text = list[position].timeOfPost
            
            list[position].listOfPostPhotos.let {
                adapter.setNewList(it)
                if (it.size < 2) {
                    indicator.visibility = View.INVISIBLE
                } else {
                    indicator.visibility = View.VISIBLE
                }
            }
        }
    }
    
    override fun onBindViewHolder(holder: PostVH, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            val item = payloads[0]
            /*if (item is CommentPayload) {
                val postId = item.postId
                val newComment = item.newCommentString
                if (list[position].postId != postId) return
                list[position].commentCount = newComment
                holder.apply {
                    commentCount.text = list[position].commentCount
                }
                
            } else*/ if (item is LikePayload) {
                val postId = item.postId
                val like = item.newLikeString
                val newState = item.newState
                if (list[position].postId != postId) return
                list[position].likeCount = like
                list[position].isPostAlreadyLiked = newState == MaterialCheckBox.STATE_CHECKED
                holder.apply {
                    likeCount.text = list[position].likeCount
                    likeBtn.checkedState = setLikedStat(position)
                }
            } else if (item is SavePayload) {
                val postId = item.postId
                val newState = item.newState
                if (list[position].postId != postId) return
                list[position].isPostAlreadySaved = newState == MaterialCheckBox.STATE_CHECKED
                holder.savePostBtn.checkedState = setSavedStat(position)
            }
        } else
            super.onBindViewHolder(holder, position, payloads)
    }
    
    override fun getItemCount() = list.size
    
    private fun setSavedStat(position: Int): Int {
        val status = list[position].isPostAlreadySaved
        return if (status) MaterialCheckBox.STATE_CHECKED else MaterialCheckBox.STATE_UNCHECKED
    }
    
    private fun setLikedStat(position: Int): Int {
        val status = list[position].isPostAlreadyLiked
        return if (status) MaterialCheckBox.STATE_CHECKED else MaterialCheckBox.STATE_UNCHECKED
    }
    
    private fun setImage(profileImage: ImageView, position: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = list[position].profileImageUrl?.let { imageUtil.getBitmap(it) }
            withContext(Dispatchers.Main) {
                if (bitmap == null) {
                    profileImage.setImageDrawable(mContext.resources.getDrawable(R.drawable.person_outlined))
                } else {
                    profileImage.setImageBitmap(bitmap)
                }
            }
        }
    }
    
    fun addNewPosts(newList: MutableList<Post>) {
        list.addAll(newList)
        notifyItemRangeInserted(itemCount, newList.size)
        //notifyDataSetChanged()
    }
    
    fun updateLikeInList(position: Int, newLikeData: String) {
        list[position].likeCount = newLikeData
        notifyItemChanged(position)
    }
    
    fun getPostId(position: Int): Long {
        return list[position].postId
    }
    
    fun getProfileId(position: Int): Long {
        return list[position].profileId
    }
    
    fun deleteAllPosts() {
        list.clear()
        notifyItemRangeRemoved(0, itemCount)
    }
    
    data class CommentPayload(val newCommentString: String, val postId: Long)
    data class LikePayload(val newLikeString: String, val postId: Long, val newState: Int)
    data class SavePayload(val postId: Long, val newState: Int)
}