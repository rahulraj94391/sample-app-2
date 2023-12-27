package com.example.instagram.domain.repo_contract

import com.example.instagram.data.entity.LoginCred

interface LoginCredRepo {
    suspend fun insertNewLoginCred(loginCred: LoginCred): Long
    suspend fun loginWithCred(username: String, password: String): Long?
    suspend fun isUsernameUnique(username: String): Int?
    suspend fun getUsername(profileId: Long): String
}