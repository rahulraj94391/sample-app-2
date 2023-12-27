package com.example.instagram.common.adapter

import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.instagram.DELETE
import com.example.instagram.R
import com.example.instagram.common.Haptics
import com.example.instagram.common.util.ImageUtil
import com.example.instagram.common.util.PostDescParser
import com.example.instagram.data.common_model.Post
import com.example.instagram.screen_singlePostView.PostAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PostListAdapter(
    private var list: MutableList<Post>,
    private var showMoreBtn: Boolean,
    val commentListener: (Int) -> Unit,
    val profileListener: (Int) -> Unit,
    val likeListener: (Int, View) -> Unit,
    val saveListener: (Int, View) -> Unit,
    val commentCountDelegate: (TextView, Long) -> Unit,
    val openPostFromSamePlaceId: (String?) -> Unit,
    val openTag: (String) -> Unit,
    val deletePost: (Int) -> Unit,
) : RecyclerView.Adapter<PostListAdapter.PostVH>() {
    private lateinit var mContext: Context
    private lateinit var imageUtil: ImageUtil
    private lateinit var haptics: Haptics
    
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        mContext = recyclerView.context
        imageUtil = ImageUtil(mContext)
        haptics = Haptics(mContext)
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
        val adapter = PostAdapter()
        val counter: TextView = item.findViewById(R.id.counter)
        val btnMore: MaterialButton = item.findViewById(R.id.btnMore)
        val location: TextView = item.findViewById(R.id.locationTag)
        
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
            
            location.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    openPostFromSamePlaceId(list[adapterPosition].location?.placeId)
                }
            }
            
            postImages.adapter = adapter
            btnMore.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.one_post_menu)
                    setForceShowIcon(true)
                    menu.getItem(0).title = SpannableString(DELETE).apply {
                        setSpan(ForegroundColorSpan(Color.RED), 0, DELETE.length, 0)
                    }
                    setOnMenuItemClickListener {
                        deletePost(adapterPosition)
                        true
                    }
                    show()
                }
            }
            
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_posts_screen, parent, false)
        return PostVH(view)
    }
    
    override fun onBindViewHolder(holder: PostVH, position: Int) {
        holder.apply {
            setImage(profileImage, position)
            if (showMoreBtn) holder.btnMore.visibility = View.VISIBLE else View.INVISIBLE
            username.text = list[position].profileUsername
            likeBtn.checkedState = setLikedStat(position)
            savePostBtn.checkedState = setSavedStat(position)
            likeCount.text = list[position].likeCount
//            postDesc.text = list[position].postDesc
            parseString(position, postDesc)
            if (list[position].location != null) {
                location.text = list[position].location?.primaryText
                location.visibility = View.VISIBLE
            } else {
                location.visibility = View.GONE
            }
            
            
            commentCountDelegate(commentCount, list[position].postId)
            timeOfPost.text = list[position].timeOfPost
            list[position].listOfPostPhotos.let {
                adapter.setNewList(it)
            }
            
            if (adapter.itemCount > 1) {
                counter.visibility = View.VISIBLE
            } else {
                counter.visibility = View.INVISIBLE
            }
            
            postImages.setCurrentItem(0, false)
            counter.text = "1/${adapter.itemCount}"
            
            postImages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    counter.text = "${position + 1}/${adapter.itemCount}"
                }
            })
        }
    }
    
    override fun onBindViewHolder(holder: PostVH, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            val item = payloads[0]
            if (item is LikePayload) {
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
                    profileImage.setImageDrawable(ResourcesCompat.getDrawable(mContext.resources, R.drawable.person_outlined, mContext.theme))
                } else {
                    profileImage.setImageBitmap(bitmap)
                }
            }
        }
    }
    
    fun notifyNewPostsAdded(count: Int) {
        notifyItemRangeInserted(itemCount, count)
    }
    
    fun getPostId(position: Int): Long {
        return list[position].postId
    }
    
    fun getProfileId(position: Int): Long {
        return list[position].profileId
    }
    
    fun clearList() {
        list.clear()
        notifyDataSetChanged()
    }
    
    private fun parseString(pos: Int, tv: TextView) {
//         tv.text = list[pos].postDesc
        val text = list[pos].postDesc
        PostDescParser(text, tv, openTag).parsePostDescToShort()
    }
    
    data class LikePayload(val newLikeString: String, val postId: Long, val newState: Int)
    data class SavePayload(val postId: Long, val newState: Int)
}