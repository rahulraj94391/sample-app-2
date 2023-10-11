package com.example.instagram.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagram.R

private const val TAG = "TestAdapter_MA2"

class TestAdapter(private val list: MutableList<String>) : RecyclerView.Adapter<TestAdapter.TestVH>() {
    
    inner class TestVH(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.testImageHolder)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.testlayout, parent, false)
        return TestVH(view)
    }
    
    override fun onBindViewHolder(holder: TestVH, position: Int) {
        Glide
            .with(holder.itemView.context)
            .load(list[position])
            .centerCrop()
            .placeholder(R.drawable.person_outlined)
            .into(holder.imageView).also {
                Log.d(TAG, "loading profile images")
            }
    }
    
    override fun getItemCount() = list.size
}