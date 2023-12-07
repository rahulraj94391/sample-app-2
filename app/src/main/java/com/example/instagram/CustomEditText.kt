package com.example.instagram

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText

private const val TAG = "CustomEditText_CommTag"


class CustomEditText(context: Context, attributeSet: AttributeSet) : AppCompatEditText(context, attributeSet) {
    
    var suggestionList: View? = null
    
    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d(TAG, "keycode = $keyCode, keyEvent = $event")
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