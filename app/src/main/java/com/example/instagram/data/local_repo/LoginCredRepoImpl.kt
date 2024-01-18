package com.example.instagram.data.local_repo

import com.example.instagram.data.dao.LoginCredDao
import com.example.instagram.data.entity.LoginCred
import com.example.instagram.domain.repo_contract.LoginCredRepo

class LoginCredRepoImpl(private val dao: LoginCredDao) : LoginCredRepo {
    
    override suspend fun insertNewLoginCred(loginCred: LoginCred): Long {
        return dao.insertNewLoginCred(loginCred)
    }
    
    override suspend fun loginWithCred(username: String, password: String): Long? {
        return dao.loginWithCred(username, password)
    }
    
    override suspend fun isUsernameUnique(username: String): Int? {
        return dao.isUsernameUnique(username)
    }
    
    override suspend fun getUsername(profileId: Long): String {
        return dao.getUsername(profileId)
    }
}