package com.example.instagram

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import java.util.regex.Matcher
import java.util.regex.Pattern


const val MORE: String = "...more"
const val HASH_TAG_PATTERN = "#[a-zA-Z0-9_]+"

private const val TAG = "PostDescParser_CommTag"

class PostDescParser(private val str: String, private val tv: TextView, private val openTag: (String) -> Unit) {
    private val context = tv.context
    private val resource = context.resources
    
    fun parsePostDescToShort() {
        if (str.length > 100) {
            val builder = SpannableStringBuilder().apply {
                append(str, 0, 100)
                append(MORE)
            }
            
            setClickListenerOnHashTags(builder, builder.length)
            setClickListenerOnMore(builder)
            tv.apply {
                text = builder
                movementMethod = LinkMovementMethod.getInstance()
            }
        } else {
            parsePostDesc()
        }
    }
    
    fun parsePostDesc() {
        val builder = SpannableStringBuilder().apply {
            append(str, 0, str.length)
        }
        
        setClickListenerOnHashTags(builder, builder.length)
        
        tv.apply {
            text = builder
            movementMethod = LinkMovementMethod.getInstance()
        }
    }
    
    private fun setClickListenerOnHashTags(ssb: SpannableStringBuilder, end: Int): SpannableStringBuilder {
        val start = 0
        val p: Pattern = Pattern.compile(HASH_TAG_PATTERN)
        val m: Matcher = p.matcher(ssb.subSequence(start, end))
        
        while (m.find()) {
            val startIdxOfTag = m.start()
            val endIdxOfTag = m.end()
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(view: View) {
                    openTag(str.subSequence(startIdxOfTag + 1, endIdxOfTag).toString()) // +1 bcz it excludes '#' character.
                }
                
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                }
            }
            ssb.setSpan(clickableSpan, startIdxOfTag, endIdxOfTag, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            ssb.setSpan(ForegroundColorSpan(resource.getColor(R.color.tag_color, context.theme)), startIdxOfTag, endIdxOfTag, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return ssb
    }
    
    private fun setClickListenerOnMore(ssb: SpannableStringBuilder) {
        val start = 100
        val end = 107
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val newSSB = SpannableStringBuilder(str)
                setClickListenerOnHashTags(newSSB, newSSB.length)
                tv.text = newSSB
            }
        }
        ssb.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssb.setSpan(ForegroundColorSpan(resource.getColor(R.color.tag_color, context.theme)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}