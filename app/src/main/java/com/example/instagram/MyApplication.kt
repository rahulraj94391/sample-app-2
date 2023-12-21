package com.example.instagram

import android.app.Application
import android.content.res.Configuration

private const val TAG = "MyApplication_CommTag"

class MyApplication : Application() {
    lateinit var appContainer: AppContainer
    
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
    
    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer()
    }
}