package com.example.passvault

import android.app.Application

class VaultApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ItemsRepository.initialize(this)
    }
}