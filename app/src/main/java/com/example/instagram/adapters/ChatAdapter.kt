package com.example.instagram.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.R

const val REGULAR_SENT = 1
const val REPLY_SENT = 2
const val REGULAR_RECEIVED = 3
const val REPLY_RECEIVED = 4
const val DATE = 5


class ChatAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val messages = mutableListOf<String>()
    
    inner class RegularSentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    
    }
    
    inner class ReplySentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    
    }
    
    inner class RegularReceivedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    
    }
    
    inner class ReplyReceivedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    
    }
    
    inner class DateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            REGULAR_SENT -> RegularSentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.bubble_sent_txt_msg, parent, false))
            REPLY_SENT -> ReplySentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.bubble_sent_reply_msg, parent, false))
            REGULAR_RECEIVED -> RegularReceivedViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.bubble_received_txt_msg, parent, false))
            REPLY_RECEIVED -> ReplyReceivedViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.bubble_received_reply_msg, parent, false))
            DATE -> DateViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.chat_date_seperator, parent, false))
            else -> throw Exception("Viewtype($viewType) is unknown for chat recycler view.")
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    
    }
    
    override fun getItemCount(): Int {
        return 20
    }
    
    override fun getItemViewType(position: Int): Int {
        return if (position % 5 == 0) REGULAR_SENT
        else if (position % 5 == 1) REGULAR_RECEIVED
        else if (position % 5 == 2) REPLY_RECEIVED
        else if (position % 5 == 3) REPLY_SENT
        else DATE
    }
}