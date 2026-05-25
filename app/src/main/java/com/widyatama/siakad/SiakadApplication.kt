package com.example.myapplication

import android.app.Application

class SiakadApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        UserSession.init(this)
    }
}
