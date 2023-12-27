package com.example.instagram.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.instagram.screen_searchUser.model.UserSearchResult

@Dao
interface SearchDao {
    @Query("SELECT profile.profile_id, profile.first_name, profile.last_name, login_credential.username FROM profile LEFT JOIN login_credential ON profile.profile_id = login_credential.profile_id where profile.profile_id Not in (Select blockerId from blocked_users where blockedId = :ownID) AND profile.first_name LIKE '%'||:name||'%' EXCEPT SELECT profile.profile_id, profile.first_name, profile.last_name, login_credential.username FROM profile LEFT JOIN login_credential ON profile.profile_id = login_credential.profile_id where profile.profile_id = :ownID")
    suspend fun getSearchResult(name: String, ownID: Long): MutableList<UserSearchResult>

    /*@Query("SELECT profile.first_name, profile.last_name, profile.bio, (SELECT COUNT(profile_id) from post where profile_id = :profileId), (SELECT COUNT(owner_id) from follow WHERE owner_id = :profileId ) FROM profile")
    suspend fun getProfileSummary(profileId: Long): ProfileSummary*/

}

