package com.example.instagram.itemDecoration

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.DateTime
import com.example.instagram.R
import com.example.instagram.database.entity.Chat

private const val TAG = "ChatItemDecoration_CommTag"

class ChatItemDecoration(private val context: Context, private val chats: List<Chat>) : RecyclerView.ItemDecoration() {
    private val textBounds = Rect()
    private val paddingToBox = 10F
    private val boxTopAndBottomMargin = 20F
    private val textBottomPadding = (1.7 * paddingToBox).toFloat()
    private val topOffsetForViewHolder: Int
    
    private val boxPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.col9)
        style = Paint.Style.FILL
    }
    
    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, context.resources.displayMetrics)
        isAntiAlias = true
    }
    
    init {
        val initialText = "SAMPLE"
        textPaint.getTextBounds(initialText, 0, initialText.length, textBounds)
        topOffsetForViewHolder = (textBounds.height() + 2 * boxTopAndBottomMargin + 2 * textBottomPadding).toInt()
    }
    
    /**
     * The onDraw method of an item decoration in Android is called multiple times with respect to the currently visible RecyclerView's child items. It's called once for each child view that is currently visible on the screen, including partially visible child views. Here's how it works:
     *
     * 1. Visible Child Views: When the RecyclerView is scrolled or initially laid out, it determines which child views are currently visible within its bounds. These are the child views that are either fully or partially visible on the screen.
     *
     * 2. onDraw Invocation: For each of these visible child views, the onDraw method of the item decoration is called. This allows you to customize the appearance of each visible item or draw decorations around them.
     *
     * 3. Order of Invocation: The onDraw method is called in the order in which the child views are drawn on the screen. It starts with the child view at the top of the visible area and proceeds down the list of visible child views.
     *
     * 4. Complete Lifecycle: The onDraw method is called during the rendering process, and it's part of the rendering lifecycle. It's invoked whenever the RecyclerView's contents change or when the RecyclerView is scrolled, which triggers a re-rendering of the visible items.
     *
     * 5. Partial Visibility: If a child view is only partially visible on the screen (e.g., it's partially scrolled into view), onDraw is still called for that view. This allows you to customize the appearance of the partially visible portion of the item.
     *
     * 6. Drawing Overlapping Decorations: If multiple item decorations are applied to the RecyclerView, they are drawn in the order in which they were added using the addItemDecoration method. Decorations added later will be drawn on top of those added earlier.
     *
     * It's important to note that onDraw is called for visible child views, so the number of times it's invoked depends on the number of visible items and their positions on the screen. If you have a large number of items in your RecyclerView and only a few are visible at a time, onDraw will be called for the visible subset of items. This allows you to optimize the drawing of decorations for the currently visible portion of your RecyclerView.
     */
    
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.left.toFloat()
        val right = parent.right.toFloat()
        
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val idx = child.tag as Int
            
            if (idx == chats.size - 1 || !DateTime.isSameDay(chats[idx].timeStamp, chats[idx + 1].timeStamp)) {
                val text = DateTime.getChatSeparatorTime(chats[child.tag as Int].timeStamp)
                textPaint.getTextBounds(text, 0, text.length, textBounds)
                val bottom = child.top.toFloat() - boxTopAndBottomMargin
                val top = bottom - textBounds.height() - boxTopAndBottomMargin
                val midOfParent = ((right - left) / 2)
                val midOfBox = ((textBounds.right - textBounds.left) / 2)
                val leftOfBox = midOfParent - (midOfBox + paddingToBox)
                val rightOfBox = midOfParent + (midOfBox + paddingToBox)
                
                c.drawRect(leftOfBox, top, rightOfBox, bottom, boxPaint)
                c.drawText(text, leftOfBox + paddingToBox, bottom - textBottomPadding, textPaint)
            }
        }
    }
    
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val i = view.tag as Int
        if (i == chats.size - 1 || !DateTime.isSameDay(chats[i].timeStamp, chats[i + 1].timeStamp)) {
            outRect.set(0, topOffsetForViewHolder, 0, 0)
        } else {
            outRect.set(0, 0, 0, 0)
        }
    }
}