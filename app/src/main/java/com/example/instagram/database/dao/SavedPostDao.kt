package com.example.instagram.database.dao

import androidx.room.Dao

@Dao
interface SavedPostDao {
    fun savePostById()
    fun deleteSavedPostById()
    fun getSavedPostByProfileId()
}