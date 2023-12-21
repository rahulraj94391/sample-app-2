package com.example.instagram.domain.usecase

import com.example.instagram.database.model.Post
import com.example.instagram.domain.repo_contract.CacheRepo
import com.example.instagram.domain.repo_contract.LikeRepo
import com.example.instagram.domain.repo_contract.LocationRepo
import com.example.instagram.domain.repo_contract.PostRepo
import com.example.instagram.domain.repo_contract.PostTextRepo
import com.example.instagram.domain.repo_contract.SavedPostRepo
import com.example.instagram.domain.util.DateTime
import com.example.instagram.domain.util.LikeFormatter
import com.example.instagram.domain.util.TimeFormatting

class GetPostUseCase(
    private val currentProfile: Long,
    private val cacheRepo: CacheRepo,
    private val postRepo: PostRepo,
    private val locationRepo: LocationRepo,
    private val postTextRepo: PostTextRepo,
    private val likeRepo: LikeRepo,
    private val savedPostRepo: SavedPostRepo,
    
    ) {
    suspend operator fun invoke(postId: Long): Post {
        val profileId = postRepo.getProfileId(postId)
        
        return Post(
            postId = postId,
            profileId = profileId,
            profileImageUrl = cacheRepo.getCachedProfileImage(profileId),
            profileUsername = postRepo.getUsername(postId),
            listOfPostPhotos = cacheRepo.getCachedPostImages(postId),
            isPostAlreadyLiked = likeRepo.checkLikeByProfile(postId, currentProfile) > 0,
            isPostAlreadySaved = savedPostRepo.isPostSavedByProfile(currentProfile, postId) > 0,
            likeCount = LikeFormatter.getFormattedLikeCount(postId, likeRepo),
            postDesc = postTextRepo.getPostText(postId),
            timeOfPost = DateTime.timeFormatter(postRepo.getPostTime(postId), TimeFormatting.POST),
            location = postRepo.getLocationId(postId)?.let { locationRepo.getLocation(it) }
        )
    }
}