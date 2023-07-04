package com.example.instagram.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.instagram.database.entity.Profile

@Dao
interface ProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewProfile(profile: Profile): Long

    @Query("SELECT * FROM profile WHERE profile.first_name = :name")
    fun getUsersWithFirstname(name: String): LiveData<MutableList<Profile>>


}