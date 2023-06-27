package com.example.instagram.database.dao

interface TagPeopleDao {
    fun insertProfileTagsByPostId()
    fun getProfileTagsByPostId()
    fun deleteProfileTagsByPostId()
    fun updateProfileTagsByPostId()
}