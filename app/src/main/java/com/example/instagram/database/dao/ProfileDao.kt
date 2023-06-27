package com.example.instagram.database.dao

interface ProfileDao {
    fun getProfileDetails()
    fun insertProfileDetails()
    fun deleteProfileDetails()
    fun updateProfileDetails()
}