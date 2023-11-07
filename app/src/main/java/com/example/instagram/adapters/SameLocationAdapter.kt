package com.example.instagram.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.ImageUtil
import com.example.instagram.R
import com.example.instagram.database.AppDatabase
import com.example.instagram.database.model.OnePhotoPerPost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "SameLocationAdapter_CommTag"

class SameLocationAdapter(
    private val posts: MutableList<OnePhotoPerPost>,
    private val onPostClicked: (Int) -> Unit,
) : RecyclerView.Adapter<SameLocationAdapter.OnePhotoViewHolder>() {
    private lateinit var db: AppDatabase
    private lateinit var imageUtil: ImageUtil
    
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        db = AppDatabase.getDatabase(recyclerView.context)
        imageUtil = ImageUtil(recyclerView.context)
        super.onAttachedToRecyclerView(recyclerView)
    }
    
    inner class OnePhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.gridImage)
        
        init {
            view.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onPostClicked(adapterPosition)
                }
            }
        }
        
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnePhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_grid_photo_one, parent, false)
        return OnePhotoViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: OnePhotoViewHolder, position: Int) {
        holder.apply {
            
            CoroutineScope(Dispatchers.IO).launch {
                val bitmap = imageUtil.getBitmap(posts[adapterPosition].imageURl)
                withContext(Dispatchers.Main) {
                    imageView.setImageBitmap(bitmap)
                }
            }
        }
    }
    
    override fun getItemCount() = posts.size
    
}