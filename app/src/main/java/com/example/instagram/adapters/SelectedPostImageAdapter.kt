package com.example.instagram.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.R

class SelectedPostImageAdapter(Uris: MutableList<Uri>, listener: KeepAtLeastOneImage) : RecyclerView.Adapter<SelectedPostImageAdapter.MyViewHolder>() {
    private lateinit var context: Context
    private var listener: KeepAtLeastOneImage

    init {
        this.listener = listener
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        context = recyclerView.context
        super.onAttachedToRecyclerView(recyclerView)
    }

    private var listOfImageUris: MutableList<Uri>

    init {
        this.listOfImageUris = Uris
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.gridImage)

        init {
            image.setOnLongClickListener {
                if (listOfImageUris.size < 2) {
                    listener.postAtLeastOnePhoto()
                }
                else {
                    listOfImageUris.removeAt(adapterPosition)
                    notifyItemRemoved(adapterPosition)
                }
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_photo_selected_to_upload, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.image.setImageURI(listOfImageUris[position])
    }

    override fun getItemCount(): Int {
        return listOfImageUris.size
    }

    fun setNewList(newUris: MutableList<Uri>) {
        listOfImageUris = newUris
        notifyDataSetChanged()
    }

}

interface KeepAtLeastOneImage {
    fun postAtLeastOnePhoto()
}