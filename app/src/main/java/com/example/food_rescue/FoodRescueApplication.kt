package com.example.food_rescue

import android.app.Application
import com.google.firebase.FirebaseApp

class FoodRescueApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize FirebaseApp if it hasn't been initialized already
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }
    }
}