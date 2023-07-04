package com.example.instagram.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.instagram.database.model.SearchResult

@Dao
interface SearchDao {
    @Query("SELECT profile.profile_id, profile.first_name, profile.last_name, login_credential.username FROM profile LEFT JOIN login_credential ON profile.profile_id = login_credential.profile_id where profile.first_name LIKE '%'||:name||'%' EXCEPT SELECT profile.profile_id, profile.first_name, profile.last_name, login_credential.username FROM profile LEFT JOIN login_credential ON profile.profile_id = login_credential.profile_id where profile.profile_id = :ownID")
    suspend fun getSearchResult(name: String, ownID: Long): MutableList<SearchResult>


}

