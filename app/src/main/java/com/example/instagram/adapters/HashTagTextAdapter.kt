package com.example.instagram.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.R

class HashTagTextAdapter(private val tags: MutableList<String>, private val clickListener: (Int) -> Unit) : RecyclerView.Adapter<HashTagTextAdapter.HashTagViewHolder>() {
    
    inner class HashTagViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tagName: TextView = view.findViewById(R.id.tagName)
        
        init {
            view.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    clickListener(adapterPosition)
                }
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HashTagViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_hash_tag, parent, false)
        return HashTagViewHolder(view)
    }
    
    override fun getItemCount(): Int {
        return tags.size
    }
    
    override fun onBindViewHolder(holder: HashTagViewHolder, position: Int) {
        holder.tagName.text = tags[position]
    }
}