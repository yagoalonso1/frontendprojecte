package com.example.app

import android.app.Application
import com.example.app.utils.TokenManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TokenManager.init(this)
    }
} 