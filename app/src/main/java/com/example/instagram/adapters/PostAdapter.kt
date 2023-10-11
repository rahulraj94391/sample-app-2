package com.example.instagram.adapters

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

private const val TAG = "PostAdapter_CommTag"

class PostAdapter : RecyclerView.Adapter<PostAdapter.MyViewHolder>() {
    private var postImages: MutableList<String> = mutableListOf()
    private lateinit var imageUtil: ImageUtil
    
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        imageUtil = ImageUtil(recyclerView.context)
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
        val imageUrl = postImages[position]
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = imageUtil.getBitmap(imageUrl)
            withContext(Dispatchers.Main) {
                holder.image.setImageBitmap(bitmap)
            }
        }
        
        /*Glide
            .with(holder.itemView.context)
            .load(*//*imageUrl*//*"https://firebasestorage.googleapis.com/v0/b/instagram-9f8d3.appspot.com/o/1696492214301_0?alt=media&token=c44d0e0d-99b7-4bac-9327-19e8c77968e1")
            .centerCrop()
            .into(holder.image).also {
                Log.d(TAG, "loading post images in VP2")
            }*/
        
    }
    
    override fun getItemCount(): Int {
        return postImages.size
    }
    
    fun setNewList(newList: MutableList<String>) {
        this.postImages = newList
        notifyDataSetChanged()
    }
}