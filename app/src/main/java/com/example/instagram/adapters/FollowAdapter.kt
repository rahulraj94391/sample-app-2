package com.example.instagram.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.ImageUtil
import com.example.instagram.R
import com.example.instagram.database.model.FollowList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FollowAdapter() : RecyclerView.Adapter<FollowAdapter.ViewHolder>() {
    private var users: MutableList<FollowList> = mutableListOf()
    private lateinit var imageUtil: ImageUtil
    
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        imageUtil = ImageUtil(recyclerView.context)
        super.onAttachedToRecyclerView(recyclerView)
    }
    
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.profileImage)
        val username: TextView = view.findViewById(R.id.username)
        val fullName: TextView = view.findViewById(R.id.fullName)
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
    
    fun updateList(newList: MutableList<FollowList>) {
        this.users = newList
        notifyDataSetChanged()
    }
}