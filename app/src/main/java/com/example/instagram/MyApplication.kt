package com.example.instagram

import android.app.Application

private const val TAG = "MyApplication_CommTag"

class MyApplication : Application() {
    lateinit var appContainer: AppContainer
    
    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer()
    }
}