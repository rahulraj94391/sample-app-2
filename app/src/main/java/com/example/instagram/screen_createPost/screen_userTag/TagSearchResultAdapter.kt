package com.example.instagram.screen_createPost.screen_userTag

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.R
import com.example.instagram.common.util.ImageUtil
import com.example.instagram.screen_createPost.model.TagSearchResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TagSearchResultAdapter(
    var addToTagList: (TagSearchResult) -> Unit,
) : RecyclerView.Adapter<TagSearchResultAdapter.TagSearchResultViewHolder>() {
    private lateinit var imageUtil: ImageUtil
    var list: MutableList<TagSearchResult> = mutableListOf()
    
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        imageUtil = ImageUtil(recyclerView.context)
        super.onAttachedToRecyclerView(recyclerView)
    }
    
    inner class TagSearchResultViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profilePic: ImageView = view.findViewById(R.id.profileImage)
        val userName: TextView = view.findViewById(R.id.username)
        val fullName: TextView = view.findViewById(R.id.fullName)
        
        init {
            view.setOnClickListener {
                if (RecyclerView.NO_POSITION != adapterPosition) {
                    addToTagList(list[adapterPosition])
                }
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagSearchResultViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_user_search, parent, false)
        return TagSearchResultViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: TagSearchResultViewHolder, position: Int) {
        holder.apply {
            userName.text = list[position].username
            fullName.text = "${list[position].first_name} ${list[position].last_name}"
            
            CoroutineScope(Dispatchers.IO).launch {
                val bitmap = imageUtil.getBitmap(list[position].profilePicUrl)
                withContext(Dispatchers.Main) {
                    profilePic.setImageBitmap(bitmap)
                }
            }
        }
    }
    
    override fun getItemCount(): Int {
        return list.size
    }
    
    fun setNewTagSearchResult(newList: MutableList<TagSearchResult>) {
        list = newList
        notifyDataSetChanged()
    }
    
    fun clearList() {
        list.clear()
        notifyDataSetChanged()
    }
    
}