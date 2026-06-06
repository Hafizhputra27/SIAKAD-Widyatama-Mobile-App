package com.widyatama.siakad

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

class SiakadApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Explicit initialization FirebaseApp untuk memastikan Firebase services tersedia
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
            Log.d("SiakadApp", "FirebaseApp initialized successfully")
        } else {
            Log.d("SiakadApp", "FirebaseApp already initialized")
        }

        // Enable Firebase Crashlytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        Log.d("SiakadApp", "Firebase Crashlytics enabled")
    }
}
