package com.example.instagram.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.ImageUtil
import com.example.instagram.R
import com.example.instagram.database.model.OnePhotoPerPost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "CommTag_PhotoGridAdapter"

class PhotoGridAdapter(val listener: (Long) -> Unit) : RecyclerView.Adapter<PhotoGridAdapter.PhotoViewVH>() {
    private var listOfImages: MutableList<OnePhotoPerPost> = mutableListOf()
    private lateinit var imageUtil: ImageUtil
    
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        imageUtil = ImageUtil(recyclerView.context)
        super.onAttachedToRecyclerView(recyclerView)
    }
    
    inner class PhotoViewVH(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.gridImage)
        
        init {
            image.setOnClickListener {
                listener.invoke(listOfImages[adapterPosition].postId)
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_photo_one_grid, parent, false)
        return PhotoViewVH(view)
    }
    
    override fun onBindViewHolder(holder: PhotoViewVH, position: Int) {
        if (listOfImages.size == 0) return
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = imageUtil.getBitmap(listOfImages[position].imageURl)
            withContext(Dispatchers.Main) { holder.image.setImageBitmap(bitmap) }
        }
    }
    
    fun setNewList(newList: MutableList<OnePhotoPerPost>) {
        Log.d(TAG, "new list size  = ${newList.size}")
        this.listOfImages = newList
        notifyDataSetChanged()
    }
    
    override fun getItemCount(): Int {
        return listOfImages.size
    }
}