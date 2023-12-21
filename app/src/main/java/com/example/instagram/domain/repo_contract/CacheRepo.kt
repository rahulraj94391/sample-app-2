package com.example.instagram.domain.repo_contract

import com.example.instagram.database.entity.ImageCache

interface CacheRepo {
    suspend fun insertCacheUrl(imageCache: ImageCache): Long
    suspend fun getCachedImageFileNameIfPresent(url: String): Long?
    suspend fun getCachedPostImages(postId: Long): MutableList<String>
    suspend fun getCachedProfileImage(profileId: Long): String?
    suspend fun updateProfilePicImageCacheUrl(newUrl: String, profileId: Long): Int
    suspend fun deleteProfilePicImageCacheUrl(profileId: Long): Int
    suspend fun getFirstImgFromEachPost(postId: Long): String?
    suspend fun getFileNameForProfilePictureIfPresent(profileId: Long): Long?
}