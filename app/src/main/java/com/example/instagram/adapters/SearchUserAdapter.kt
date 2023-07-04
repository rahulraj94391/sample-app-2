package com.example.instagram.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.R
import com.example.instagram.database.model.SearchResult

class SearchUserAdapter(
    var searchResultList: MutableList<SearchResult>,
    var listener: SearchUsernameClickListener,
) : RecyclerView.Adapter<SearchUserAdapter.SearchResultViewHolder>() {

    private lateinit var context: Context

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        context = recyclerView.context
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_user_search, parent, false)
        return SearchResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.apply {
            profImg.background = context.resources.getDrawable(R.drawable.ic_launcher_background)
            profImg.setImageDrawable(context.resources.getDrawable(R.drawable.ic_launcher_foreground))
            username.text = searchResultList[adapterPosition].username
            fullName.text = searchResultList[adapterPosition].first_name + searchResultList[adapterPosition].last_name

        }
    }

    override fun getItemCount(): Int {
        return searchResultList.size
    }

    fun setNewList(newList: MutableList<SearchResult>) {
        this.searchResultList = newList
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