package com.example.instagram.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.instagram.data.common_model.FullName
import com.example.instagram.data.entity.Profile
import com.example.instagram.screen_profile.model.FullNameBio

@Dao
interface ProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewProfile(profile: Profile): Long
    
    @Query("SELECT * FROM profile WHERE profile.firstName = :name")
    fun getUsersWithFirstname(name: String): LiveData<MutableList<Profile>>
    
    @Query("SELECT firstName, lastName FROM profile WHERE profileId = :profileId")
    suspend fun getFullName(profileId: Long): FullName
    
    @Query("SELECT firstName, lastName, bio FROM profile WHERE profileId = :profileId")
    suspend fun getFullNameBio(profileId: Long): FullNameBio
    
    @Query("UPDATE profile SET firstName = :firstName, lastName = :lastName, bio = :bio WHERE profileId = :profileId")
    suspend fun editProfile(firstName: String, lastName: String, bio: String, profileId: Long)
    
    @Query("SELECT COUNT(postId) FROM post WHERE profileId = :profileId")
    fun getPostCount(profileId: Long): LiveData<Int>
    
    @Query("SELECT * FROM profile where profileId = :profileId")
    suspend fun getProfile(profileId: Long): Profile
    
    @Delete
    suspend fun deleteProfile(profile: Profile)
}
