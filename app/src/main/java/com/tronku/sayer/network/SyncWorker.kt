package com.tronku.sayer.network

import android.content.Context
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.tronku.sayer.utils.Storage
import com.tronku.sayer.utils.Utils

class SyncWorker(context: Context, workerParameters: WorkerParameters):
    Worker(context, workerParameters) {

    companion object {
        val updateListener = MutableLiveData(false)
    }

    override fun doWork(): Result {
        return if (Utils.isConnected() && Storage.getAllLocations().isNotEmpty()) {
            Log.e("WORKER", "CONNECTED")
            networkSync()
            Result.success()
        } else {
            Log.e("WORKER", "NOT CONNECTED")
            Result.failure()
        }
    }

    private fun networkSync() {
        updateListener.postValue(true)
    }

}