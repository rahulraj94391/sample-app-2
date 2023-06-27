package com.example.instagram.database.dao

interface SavedPostDao {
    fun savePostById()
    fun deleteSavedPostById()
    fun getSavedPostByProfileId()
}