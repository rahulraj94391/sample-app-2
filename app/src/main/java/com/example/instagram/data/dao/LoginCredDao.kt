package com.example.instagram.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.instagram.data.entity.LoginCred

@Dao
interface LoginCredDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNewLoginCred(loginCred: LoginCred): Long

    @Query("SELECT profile_id FROM login_credential where username = :username AND password = :password")
    suspend fun loginWithCred(username: String, password: String): Long?

    @Query("SELECT 1 FROM login_credential WHERE username = :username")
    suspend fun isUsernameUnique(username: String): Int?

    @Query("SELECT username from login_credential WHERE profile_id = :profileId")
    suspend fun getUsername(profileId: Long): String

}