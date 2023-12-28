package com.example.instagram.screen_createPost.screen_locationTag

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.R
import com.example.instagram.data.entity.Location

class LocationSuggestionAdapter(
    private val onLocationClicked: (Int) -> Unit,
    private val locationList: MutableList<Location>,
) : RecyclerView.Adapter<LocationSuggestionAdapter.LocationViewHolder>() {
    
    inner class LocationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val primaryLocation: TextView = view.findViewById(R.id.locationPrimaryName)
        val secondaryLocation: TextView = view.findViewById(R.id.locationSecondaryName)
        
        init {
            view.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onLocationClicked(adapterPosition)
                }
            }
        }
        
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_location, parent, false)
        return LocationViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.apply {
            primaryLocation.text = locationList[position].primaryText
            secondaryLocation.text = locationList[position].secondaryText
        }
    }
    
    override fun getItemCount() = locationList.size
    
    fun setNewList() {
        notifyDataSetChanged()
    }
    
}