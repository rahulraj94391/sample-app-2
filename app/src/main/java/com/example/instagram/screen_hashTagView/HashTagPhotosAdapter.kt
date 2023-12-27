package com.example.instagram.screen_hashTagView

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.R
import com.example.instagram.common.util.ImageUtil
import com.example.instagram.data.common_model.PostPreview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HashTagPhotosAdapter(
    private val list: MutableList<PostPreview>,
    private val onCLick: (Int) -> Unit,
) : RecyclerView.Adapter<HashTagPhotosAdapter.PhotoViewHolder>() {
    
    private lateinit var imageUtil: ImageUtil
    
    inner class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.gridImage)
        
        init {
            view.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onCLick(adapterPosition)
                }
            }
        }
    }
    
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        imageUtil = ImageUtil(recyclerView.context)
        super.onAttachedToRecyclerView(recyclerView)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_grid_photo_one, parent, false)
        return PhotoViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = imageUtil.getBitmap(list[position].imageURl)
            withContext(Dispatchers.Main) {
                holder.image.setImageBitmap(bitmap)
            }
        }
    }
    
    override fun getItemCount() = list.size
    
    fun notifyNewPostsAdded(count: Int) {
        notifyItemRangeInserted(itemCount, count)
    }
}