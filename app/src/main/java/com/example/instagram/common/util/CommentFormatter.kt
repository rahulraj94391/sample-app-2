package com.example.instagram.common.util

object CommentFormatter {
    fun getFormattedCommentCount(count: Int): String {
        return if (count > 1) {
            "$count comments"
        } else {
            "$count comment"
        }
    }
    
}