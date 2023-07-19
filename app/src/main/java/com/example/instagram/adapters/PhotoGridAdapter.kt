package com.example.instagram.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.R
import com.example.instagram.database.model.OnePhotoPerPost
import com.squareup.picasso.Picasso
import java.net.URL

private const val TAG = "CommTag_PhotoGridAdapter"

class PhotoGridAdapter(val listener: (Long) -> Unit) : RecyclerView.Adapter<PhotoGridAdapter.PhotoViewVH>() {
    private var listOfImages: MutableList<OnePhotoPerPost> = mutableListOf()

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


        /*CoroutineScope(Dispatchers.IO).launch {
            val bitmap = downloadBitmap(listOfImages[position].imageURl)
            if (bitmap != null)
                withContext(Dispatchers.Main) { holder.image.setImageBitmap(bitmap) }
            else
                withContext(Dispatchers.Main) { holder.image.setImageDrawable(holder.image.context.resources.getDrawable(R.drawable.ic_launcher_background)) }
        }*/
        // TODO: REMOVE PICASSO
        Picasso.get().load(listOfImages[position].imageURl).resize(720, 720).centerCrop().into(holder.image)
    }

    fun setNewList(newList: MutableList<OnePhotoPerPost>) {
        this.listOfImages = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return listOfImages.size
    }

    private fun downloadBitmap(imageUrl: String): Bitmap? {
        return try {
            val conn = URL(imageUrl).openConnection()
            conn.connect()
            val inputStream = conn.getInputStream()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            bitmap
        } catch (e: Exception) {
            Log.d(TAG, "Exception $e")
            null
        }
    }
}