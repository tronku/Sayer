package com.tronku.sayer.network

import android.content.Context
import android.os.Looper
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters

class SyncWorker(val context: Context, workerParameters: WorkerParameters):
    Worker(context, workerParameters) {

    override fun doWork(): Result {
        return if (isConnected()) {
            //Toast.makeText(context, "Synced!", Toast.LENGTH_SHORT).show()
            Result.success()
        } else {
            Result.failure()
        }
    }

    private fun isConnected(): Boolean {
        val command = "ping -c 1 google.com"
        return Runtime.getRuntime().exec(command).waitFor() == 0
    }
}