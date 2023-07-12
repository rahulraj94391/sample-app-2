package com.example.instagram.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.R
import com.squareup.picasso.Picasso

class PostAdapter() : RecyclerView.Adapter<PostAdapter.MyViewHolder>() {

    private var postImages: MutableList<String> = mutableListOf()

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.row_post_vp2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_post_view_pager_2, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // TODO: REMOVE PICASSO
        Picasso.get().load(postImages[position]).resize(720, 720).centerCrop().into(holder.image)
    }

    override fun getItemCount(): Int {
        return postImages.size
    }

    fun setNewList(newList: MutableList<String>) {
        this.postImages = newList
        notifyDataSetChanged()
    }
}