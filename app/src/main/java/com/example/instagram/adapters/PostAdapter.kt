package com.example.instagram.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.ImageUtil
import com.example.instagram.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PostAdapter : RecyclerView.Adapter<PostAdapter.MyViewHolder>() {
    private var postImages: MutableList<String> = mutableListOf()
    private lateinit var context: Context
    private lateinit var imageUtil: ImageUtil

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        context = recyclerView.context
        imageUtil = ImageUtil(context)
        super.onAttachedToRecyclerView(recyclerView)
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.row_post_for_vp2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_post_view_pager_2, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = imageUtil.getBitmap(postImages[position])
            withContext(Dispatchers.Main) {
                holder.image.setImageBitmap(bitmap)
            }
        }
    }

    override fun getItemCount(): Int {
        return postImages.size
    }

    fun setNewList(newList: MutableList<String>) {
        this.postImages = newList
        notifyDataSetChanged()
    }
}