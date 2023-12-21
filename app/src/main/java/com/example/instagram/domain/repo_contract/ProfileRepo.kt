package com.example.instagram.domain.repo_contract

import androidx.lifecycle.LiveData
import com.example.instagram.database.entity.Profile
import com.example.instagram.database.model.FullName
import com.example.instagram.database.model.FullNameBio

interface ProfileRepo {
    suspend fun insertNewProfile(profile: Profile): Long
    fun getUsersWithFirstname(name: String): LiveData<MutableList<Profile>>
    suspend fun getFullName(profileId: Long): FullName
    suspend fun getFullNameBio(profileId: Long): FullNameBio
    suspend fun editProfile(firstName: String, lastName: String, bio: String, profileId: Long)
    fun getPostCount(profileId: Long): LiveData<Int>
    suspend fun getProfile(profileId: Long): Profile
    suspend fun deleteProfile(profile: Profile)
    
}