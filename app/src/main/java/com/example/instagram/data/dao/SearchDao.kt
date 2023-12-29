package com.example.instagram.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.instagram.screen_searchUser.model.UserSearchResult

@Dao
interface SearchDao {
    @Query("SELECT profile.profileId, profile.firstName, profile.lastName, login_credential.username FROM profile LEFT JOIN login_credential ON profile.profileId = login_credential.profileId where profile.profileId Not in (Select blockerId from blocked_users where blockedId = :ownID) AND profile.firstName LIKE '%'||:name||'%' EXCEPT SELECT profile.profileId, profile.firstName, profile.lastName, login_credential.username FROM profile LEFT JOIN login_credential ON profile.profileId = login_credential.profileId where profile.profileId = :ownID")
    suspend fun getSearchResult(name: String, ownID: Long): MutableList<UserSearchResult>

    /*@Query("SELECT profile.firstName, profile.lastName, profile.bio, (SELECT COUNT(profileId) from post where profileId = :profileId), (SELECT COUNT(ownerId) from follow WHERE ownerId = :profileId ) FROM profile")
    suspend fun getProfileSummary(profileId: Long): ProfileSummary*/

}

