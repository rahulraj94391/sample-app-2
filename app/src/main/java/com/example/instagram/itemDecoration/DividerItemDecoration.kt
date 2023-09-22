package com.example.instagram.itemDecoration

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.R

private const val TAG = "DividerItemDecoration_CommTag"

class DividerItemDecoration(val context: Context) : RecyclerView.ItemDecoration() {
    private val dividerHeight: Int = 30
    private val dividerColor = ContextCompat.getColor(context, R.color.red)
    private val paint = Paint().apply {
        color = dividerColor
        style = Paint.Style.STROKE
        strokeWidth = dividerHeight.toFloat()
    }
    
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft.toFloat()
        val right = parent.width - parent.paddingRight.toFloat()
        Log.d(TAG, "parent.childCount = ${parent.childCount}")
        
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            Log.i(TAG, "adapter pos = ${child.tag as Int}")
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin.toFloat()
            val bottom = top + dividerHeight
            c.drawLine(left, top, right, bottom, paint)
        }
    }
    
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        // Define the space for the divider (e.g., only add a divider below the item)
        outRect.set(0, 0, 0, dividerHeight)
    }
}