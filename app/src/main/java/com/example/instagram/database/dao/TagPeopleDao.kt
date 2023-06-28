package com.example.instagram.database.dao

import androidx.room.Dao

@Dao
interface TagPeopleDao {
    fun insertProfileTagsByPostId()
    fun getProfileTagsByPostId()
    fun deleteProfileTagsByPostId()
    fun updateProfileTagsByPostId()
}