package com.example.instagram.screen_followerAndFollowingView

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.R
import com.example.instagram.common.util.ImageUtil
import com.example.instagram.screen_followerAndFollowingView.model.Connection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConnnectionsAdapter(
    private val openProfile: (Long, Int) -> Unit,
) : RecyclerView.Adapter<ConnnectionsAdapter.ViewHolder>() {
    private var users: MutableList<Connection> = mutableListOf()
    private lateinit var imageUtil: ImageUtil
    
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        imageUtil = ImageUtil(recyclerView.context)
        super.onAttachedToRecyclerView(recyclerView)
    }
    
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.profileImage)
        val username: TextView = view.findViewById(R.id.username)
        val fullName: TextView = view.findViewById(R.id.fullName)
        
        init {
            view.setOnClickListener {
                val id = users[adapterPosition].profile_id
                openProfile(id, adapterPosition)
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_user_search, parent, false)
        return ViewHolder(view)
    }
    
    override fun getItemCount() = this.users.size
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            fullName.text = "${users[position].first_name} ${users[position].last_name}"
            username.text = users[position].username
            CoroutineScope(Dispatchers.IO).launch {
                val url = users[position].photoUrl ?: return@launch
                val bitmap = imageUtil.getBitmap(url)
                withContext(Dispatchers.Main) {
                    profileImage.setImageBitmap(bitmap)
                }
            }
        }
    }
    
    fun updateList(newList: MutableList<Connection>) {
        this.users = newList
        notifyDataSetChanged()
    }
    
    fun removeUserAt(pos: Int) {
        users.removeAt(pos)
        notifyItemRemoved(pos)
    }
    
}