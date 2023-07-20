package com.example.instagram.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.instagram.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox

class HomeAdapter() : RecyclerView.Adapter<HomeAdapter.PostVH>() {


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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_home_screen, parent, false)
        return PostVH(view)
    }

    override fun onBindViewHolder(holder: PostVH, position: Int) {
        if(position % 2 == 0){
            holder.itemView.setBackgroundColor(Color.parseColor("#cbadff"))
        }
        else{
            holder.itemView.setBackgroundColor(Color.parseColor("#c9ffd8"))
        }

    }

    override fun getItemCount(): Int {
        return 10
    }
}