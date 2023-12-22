package com.example.instagram.itemDecoration

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.R
import com.example.instagram.database.entity.Chat
import com.example.instagram.domain.util.DateTime

private const val TAG = "ChatItemDecoration_CommTag"

class ChatItemDecoration(private val context: Context, private val chats: List<Chat>) : RecyclerView.ItemDecoration() {
    private val textBounds = Rect()
    private val paddingToBox = 20F
    private val boxTopAndBottomMargin = 20F
    private val textBottomPadding = (0.8 * paddingToBox).toFloat()
    private val topOffsetForViewHolder: Int
    
    private val boxPaint: Paint = Paint().apply {
        color = context.resources.getColor(R.color.date_rect, context.theme)
        style = Paint.Style.FILL
    }
    
    private val textPaint = Paint().apply {
        color = context.resources.getColor(R.color.text_color, context.theme)
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, context.resources.displayMetrics)
        isAntiAlias = true
    }
    
    init {
        val initialText = "SAMPLE"
        textPaint.getTextBounds(initialText, 0, initialText.length, textBounds)
        topOffsetForViewHolder = (textBounds.height() + 2 * boxTopAndBottomMargin + 2 * textBottomPadding).toInt()
    }
    
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.left.toFloat()
        val right = parent.right.toFloat()
        
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val pair = child.tag as Pair<*, *>
            val idx = pair.first as Int
            if (idx == chats.size - 1 || !DateTime.isSameDay(chats[idx].timeStamp, chats[idx + 1].timeStamp)) {
                val text = DateTime.getChatSeparatorTime(chats[idx].timeStamp)
                textPaint.getTextBounds(text, 0, text.length, textBounds)
                val bottom = child.top.toFloat() - boxTopAndBottomMargin
                val top = bottom - textBounds.height() - boxTopAndBottomMargin
                val midOfParent = ((right - left) / 2)
                val midOfBox = ((textBounds.right - textBounds.left) / 2)
                val leftOfBox = midOfParent - (midOfBox + paddingToBox)
                val rightOfBox = midOfParent + (midOfBox + paddingToBox)
                
                c.drawRoundRect(leftOfBox, top, rightOfBox, bottom, 20.0F, 20.0F, boxPaint)
                c.drawText(text, leftOfBox + paddingToBox, bottom - textBottomPadding, textPaint)
            }
        }
    }
    
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val pair = view.tag as Pair<*, *>
        val i = pair.first as Int
        if (i == chats.size - 1 || !DateTime.isSameDay(chats[i].timeStamp, chats[i + 1].timeStamp)) {
            outRect.set(0, topOffsetForViewHolder, 0, 0)
        } else {
            outRect.set(0, 0, 0, 0)
        }
    }
}