package com.example.instagram.domain.util

object CommentFormatter {
    fun getFormattedCommentCount(count: Int): String {
        return if (count > 1) {
            "$count comments"
        } else {
            "$count comment"
        }
    }
    
}