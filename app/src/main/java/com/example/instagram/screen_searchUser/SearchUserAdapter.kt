package com.example.instagram.screen_searchUser

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.R
import com.example.instagram.common.util.ImageUtil
import com.example.instagram.screen_searchUser.model.UserSearchResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchUserAdapter(
    private var searchResultList: MutableList<UserSearchResult>,
    var listener: (Int) -> Unit,
    private val layoutRes: Int,
    private var imageList: MutableList<String>,
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
    
    fun setNewList(newList: MutableList<UserSearchResult>) {
        this.searchResultList = newList
        notifyDataSetChanged()
    }
    
    fun setImagesList(imagesNewList: MutableList<String>?) {
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
                    listener(adapterPosition)
                }
            }
        }
    }
}