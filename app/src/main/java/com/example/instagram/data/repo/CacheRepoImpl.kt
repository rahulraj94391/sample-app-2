package com.example.instagram.data.repo

import com.example.instagram.data.dao.CacheDao
import com.example.instagram.data.entity.ImageCache
import com.example.instagram.domain.repo_contract.CacheRepo

class CacheRepoImpl(private val dao: CacheDao) : CacheRepo {
    override suspend fun insertCacheUrl(imageCache: ImageCache): Long {
        return dao.insertCacheUrl(imageCache)
    }
    
    override suspend fun getCachedImageFileNameIfPresent(url: String): Long? {
        return dao.getCachedImageFileNameIfPresent(url)
    }
    
    override suspend fun getCachedPostImages(postId: Long): MutableList<String> {
        return dao.getCachedPostImages(postId)
    }
    
    override suspend fun getCachedProfileImage(profileId: Long): String? {
        return dao.getCachedProfileImage(profileId)
    }
    
    override suspend fun updateProfilePicImageCacheUrl(newUrl: String, profileId: Long): Int {
        return dao.updateProfilePicImageCacheUrl(newUrl, profileId)
    }
    
    override suspend fun deleteProfilePicImageCacheUrl(profileId: Long): Int {
        return dao.deleteProfilePicImageCacheUrl(profileId)
    }
    
    override suspend fun getFirstImgFromEachPost(postId: Long): String? {
        return dao.getFirstImgFromEachPost(postId)
    }
    
    override suspend fun getFileNameForProfilePictureIfPresent(profileId: Long): Long? {
        return dao.getFileNameForProfilePictureIfPresent(profileId)
    }
}