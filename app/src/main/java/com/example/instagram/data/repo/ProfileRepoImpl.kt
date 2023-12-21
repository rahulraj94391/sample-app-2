package com.example.instagram.data.repo

import androidx.lifecycle.LiveData
import com.example.instagram.database.dao.ProfileDao
import com.example.instagram.database.entity.Profile
import com.example.instagram.database.model.FullName
import com.example.instagram.database.model.FullNameBio
import com.example.instagram.domain.repo_contract.ProfileRepo

class ProfileRepoImpl(private val dao: ProfileDao) : ProfileRepo {
    
    override suspend fun insertNewProfile(profile: Profile): Long {
        return dao.insertNewProfile(profile)
    }
    
    override fun getUsersWithFirstname(name: String): LiveData<MutableList<Profile>> {
        return dao.getUsersWithFirstname(name)
    }
    
    override suspend fun getFullName(profileId: Long): FullName {
        return dao.getFullName(profileId)
    }
    
    override suspend fun getFullNameBio(profileId: Long): FullNameBio {
        return dao.getFullNameBio(profileId)
    }
    
    override suspend fun editProfile(firstName: String, lastName: String, bio: String, profileId: Long) {
        return dao.editProfile(firstName, lastName, bio, profileId)
    }
    
    override fun getPostCount(profileId: Long): LiveData<Int> {
        return dao.getPostCount(profileId)
    }
    
    override suspend fun getProfile(profileId: Long): Profile {
        return dao.getProfile(profileId)
    }
    
    override suspend fun deleteProfile(profile: Profile) {
        return dao.deleteProfile(profile)
    }
}