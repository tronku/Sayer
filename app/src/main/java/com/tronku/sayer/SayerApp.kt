package com.tronku.sayer

import android.app.Application
import com.tronku.sayer.utils.Storage

class SayerApp: Application() {

    override fun onCreate() {
        super.onCreate()
        Storage.initialize(applicationContext)
    }
}