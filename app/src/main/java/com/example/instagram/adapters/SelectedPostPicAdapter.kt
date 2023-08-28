package com.example.instagram.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.R

import com.google.android.material.button.MaterialButton

const val PHOTO_ROW = 33
const val ADD_PHOTO_ROW = 22
const val CAPACITY = 6

private const val TAG = "SelectedPostPicAdapter_CommTag"

class SelectedPostPicAdapter(
    val selectedPhotoList: MutableList<Uri>,
    val addNewPhoto: () -> Unit,
    val deletePhoto: (pos: Int) -> Unit,
    val insListener: (Int) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var mContext: Context
    
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        mContext = recyclerView.context
        super.onAttachedToRecyclerView(recyclerView)
    }
    
    inner class SelectedPicViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image)
        private val deleteBtn: MaterialButton = view.findViewById(R.id.deletePic)
        
        init {
            
            deleteBtn.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION)
                    deletePhoto(adapterPosition)
            }
            
        }
    }
    
    inner class AddPictureViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            view.setOnClickListener { addNewPhoto.invoke() }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SelectedPicViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_selected_photo, parent, false))
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as SelectedPicViewHolder).apply {
            imageView.setImageURI(selectedPhotoList[position])
        }
    }
    
    override fun getItemCount(): Int {
        return selectedPhotoList.size /*+ 1*/
    }
    
    fun addImages(list: MutableList<Uri>) {
        var itemCount = 0
        val currentCapacity = CAPACITY - selectedPhotoList.size
        for (i in 0 until currentCapacity) {
            if (itemCount == list.size) break
            selectedPhotoList.add(list[i])
            itemCount++
        }
        if (list.size > currentCapacity) Toast.makeText(mContext, "Can only add at max 6 images", Toast.LENGTH_SHORT).show()
        insListener(selectedPhotoList.size)
        notifyItemRangeInserted(CAPACITY - currentCapacity, itemCount)
    }
    
    fun deleteImage(pos: Int) {
        selectedPhotoList.removeAt(pos)
        insListener(selectedPhotoList.size)
        notifyItemRemoved(pos)
    }
    
}