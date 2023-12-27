package com.example.instagram.domain.repo_contract

import androidx.lifecycle.LiveData
import com.example.instagram.data.entity.Profile
import com.example.instagram.data.common_model.FullName
import com.example.instagram.screen_profilePostGridView.model.FullNameBio

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