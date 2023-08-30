package com.example.instagram.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.R
import com.example.instagram.database.entity.RecentSearch

private const val TAG = "RecentSearchAdapter_CommTag"

class RecentSearchAdapter(val onClick: (Int) -> Unit) : RecyclerView.Adapter<RecentSearchAdapter.RecentSearchViewHolder>() {
    
    val list: MutableList<RecentSearch> = mutableListOf()
    
    inner class RecentSearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.username)
        
        init {
            view.setOnClickListener {
                onClick(adapterPosition)
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentSearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_recent_search, parent, false)
        return RecentSearchViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: RecentSearchViewHolder, position: Int) {
        holder.textView.text = list[position].firstName
    }
    
    override fun getItemCount(): Int {
        Log.d(TAG, "getItemCount: ${list.size}")
        return list.size
    }
    
    fun setNewList(newList: List<RecentSearch>) {
        list.addAll(newList)
        notifyDataSetChanged()
    }
    
    fun clearList() {
        list.clear()
        notifyDataSetChanged()
    }
    
    fun getProfileId(pos: Int): Long = list[pos].profileId
    
    fun isEmpty() = list.isEmpty()
}