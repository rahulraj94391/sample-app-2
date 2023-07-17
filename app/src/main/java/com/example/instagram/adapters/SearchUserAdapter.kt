package com.example.instagram.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.ImageUtil
import com.example.instagram.R
import com.example.instagram.database.model.SearchResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchUserAdapter(
    var searchResultList: MutableList<SearchResult>,
    var listener: SearchUsernameClickListener,
    val layoutRes: Int,
    var imageList: MutableList<String>,
) : RecyclerView.Adapter<SearchUserAdapter.SearchResultViewHolder>() {
    private lateinit var imgUtil: ImageUtil
    private lateinit var context: Context

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        context = recyclerView.context
        imgUtil = ImageUtil(context)
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return SearchResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.apply {
            CoroutineScope(Dispatchers.IO).launch {
                val bitmap = imgUtil.getBitmap(imageList[position])
                withContext(Dispatchers.Main) {
                    profImg.setImageBitmap(bitmap)
                }
            }
            username.text = searchResultList[adapterPosition].username
            fullName.text = searchResultList[adapterPosition].first_name + " " + searchResultList[adapterPosition].last_name

        }
    }

    override fun getItemCount(): Int {
        return searchResultList.size
    }

    fun setNewList(newList: MutableList<SearchResult>) {
        this.searchResultList = newList
        notifyDataSetChanged()
    }

    fun setNewList2(imagesNewList: MutableList<String>?) {
        this.imageList = imagesNewList!!
        notifyDataSetChanged()
    }

    inner class SearchResultViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var profImg: ImageView = view.findViewById(R.id.profileImage)
        var username: TextView = view.findViewById(R.id.username)
        var fullName: TextView = view.findViewById(R.id.fullName)

        init {
            view.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onClick(adapterPosition)
                }
            }
        }
    }
}

interface SearchUsernameClickListener {
    fun onClick(pos: Int)
}