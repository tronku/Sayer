package com.tronku.sayer

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.tronku.sayer.utils.Storage

class SayerApp: Application() {

    private var requestQueue: RequestQueue? = null

    companion object {

        private lateinit var instance: SayerApp

        fun getInstance() = instance

    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Storage.initialize(applicationContext)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    private fun getRequestQueue(): RequestQueue {
        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(applicationContext)
        return requestQueue!!
    }

    fun <T> addToRequestQueue(request: Request<T>) {
        getRequestQueue().add(request)
    }
}