package com.example.instagram.screen_savedPosts

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

class SavedPostAdapter(
    val clickListener: (Int) -> Unit,
    val longClickListener: (Int) -> Unit,
) : RecyclerView.Adapter<SavedPostAdapter.SavedPostVH>() {
    private lateinit var imageUtil: ImageUtil
    private var listOfSavedPost = mutableListOf<PostPreview>()

    inner class SavedPostVH(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.gridImage)

        init {
            view.setOnClickListener {
                clickListener.invoke(adapterPosition)
            }

            view.setOnLongClickListener {
                longClickListener.invoke(adapterPosition)
                true
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        imageUtil = ImageUtil(recyclerView.context)
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedPostVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_grid_photo_one, parent, false)
        return SavedPostVH(view)
    }

    override fun onBindViewHolder(holder: SavedPostVH, position: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = imageUtil.getBitmap(listOfSavedPost[position].imageURl)
            withContext(Dispatchers.Main) {
                holder.image.setImageBitmap(bitmap)
            }
        }
    }

    override fun getItemCount(): Int {
        return listOfSavedPost.size
    }

    fun updateList(newList: MutableList<PostPreview>) {
        this.listOfSavedPost = newList
        notifyDataSetChanged()
    }
}