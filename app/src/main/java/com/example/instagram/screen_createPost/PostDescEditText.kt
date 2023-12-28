package com.example.instagram.screen_createPost

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText

class PostDescEditText(context: Context, attributeSet: AttributeSet) : AppCompatEditText(context, attributeSet) {
    var suggestionList: View? = null
    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK &&
            event?.action == KeyEvent.ACTION_UP &&
            suggestionList?.visibility == View.VISIBLE
        ) {
            suggestionList!!.visibility = View.GONE
            return true
        }
        return super.onKeyPreIme(keyCode, event)
    }
}