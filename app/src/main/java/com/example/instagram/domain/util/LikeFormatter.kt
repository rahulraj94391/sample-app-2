package com.example.instagram.domain.util

import com.example.instagram.domain.repo_contract.LikeRepo

object LikeFormatter {
    suspend fun getFormattedLikeCount(postId: Long, likeRepo: LikeRepo): String {
        val count = likeRepo.getLikeCount(postId)
        return if (count > 1) {
            "$count likes"
        } else {
            "$count like"
        }
    }
}