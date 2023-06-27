package com.example.instagram.database.dao

interface LoginCredDao {
    fun isValidUser(): Boolean
    fun insertNewCredential()
    fun deleteAccount()
}