package com.example.instagram.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.ImageUtil
import com.example.instagram.R
import com.example.instagram.database.model.TagSearchResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "TagsAdapter_CommTag"

class TagsAdapter(
    private val listOfTags: MutableList<TagSearchResult>,
    private val showInstruction: () -> Unit,
) : RecyclerView.Adapter<TagsAdapter.TagViewHolder>() {
    private lateinit var imageUtil: ImageUtil
    
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        imageUtil = ImageUtil(recyclerView.context)
        super.onAttachedToRecyclerView(recyclerView)
    }
    
    inner class TagViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profilePic: ImageView = view.findViewById(R.id.profileImage)
        val username: TextView = view.findViewById(R.id.username)
        val fullName: TextView = view.findViewById(R.id.fullName)
        val close: ImageView = view.findViewById(R.id.removeTag)
        
        init {
            close.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    deleteTag(adapterPosition)
                }
            }
        }
    }
    
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagsAdapter.TagViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_selected_people_tag, parent, false)
        return TagViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: TagsAdapter.TagViewHolder, position: Int) {
        holder.apply {
            username.text = listOfTags[position].username
            fullName.text = "${listOfTags[position].first_name} ${listOfTags[position].last_name}"
            CoroutineScope(Dispatchers.IO).launch {
                val bitmap = imageUtil.getBitmap(listOfTags[position].profilePicUrl)
                withContext(Dispatchers.Main) {
                    profilePic.setImageBitmap(bitmap)
                }
            }
        }
    }
    
    override fun getItemCount(): Int {
        return listOfTags.size
    }
    
    fun deleteTag(pos: Int) {
        listOfTags.removeAt(pos)
        notifyItemRemoved(pos)
        if (listOfTags.size < 1) {
            showInstruction.invoke()
        }
    }
    
    fun addTag(tag: TagSearchResult) {
        if (listOfTags.contains(tag)) return
        listOfTags.add(tag)
        notifyDataSetChanged()
        Log.d(TAG, "${listOfTags.size}")
    }
}