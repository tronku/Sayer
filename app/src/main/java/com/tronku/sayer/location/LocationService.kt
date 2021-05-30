package com.tronku.sayer.location

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.tronku.sayer.ui.MainActivity
import com.tronku.sayer.utils.Storage
import java.util.concurrent.TimeUnit


class LocationService: Service() {

    private var locationDelay: Long = 0L
    private val locationHandler by lazy { Handler(mainLooper) }
    private val fusedLocationProviderClient by lazy { FusedLocationProviderClient(applicationContext) }
    private val ACTION_CANCEL = "service_stop"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        checkToCancel(intent?.action)

        val frequency = intent?.getStringExtra(MainActivity.FREQUENCY) ?: UpdateFrequency.MEDIUM.name
        locationDelay = getDelay(UpdateFrequency.valueOf(frequency))
        locationHandler.post(object : Runnable {
            override fun run() {
                locationHandler.postDelayed(this, locationDelay)
                getLocation()
            }
        })

        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel("sayer_loc_service", "Location Service for Sayer")
        } else {
            ""
        }

        val startActivity = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, startActivity, 0)

        val cancelIntent = Intent(this, LocationService::class.java)
        cancelIntent.action = ACTION_CANCEL
        val cancelPendingIntent = PendingIntent.getService(this, 0, cancelIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        val notification =
            NotificationCompat.Builder(applicationContext, channelId)
                .setContentTitle("Sayer")
                .setContentText("GPS location is getting used")
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", cancelPendingIntent)
                .setSmallIcon(android.R.mipmap.sym_def_app_icon)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()

        startForeground(1, notification)
        Log.e("SERVICE", "STARTED: $this: $frequency")
        return START_STICKY
    }

    private fun checkToCancel(action: String?) {
        if (action != null && action == ACTION_CANCEL) {
            stopService(applicationContext)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.GREEN
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    private fun getDelay(frequency: UpdateFrequency): Long {
        return when(frequency) {
            UpdateFrequency.HIGH -> TimeUnit.MINUTES.toMillis(5)
            UpdateFrequency.MEDIUM -> TimeUnit.MINUTES.toMillis(20)
            UpdateFrequency.LOW -> TimeUnit.MINUTES.toMillis(40)
        }
    }

    private fun getLocation() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(
                applicationContext,
                "Permission not granted, try again",
                Toast.LENGTH_SHORT
            ).show()
            stopService(applicationContext)
        }

        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener {
                it?.let {
                    val lat = it.latitude
                    val long = it.longitude
                    Storage.saveUserLocation(Triple(System.currentTimeMillis(), lat, long))
                    Log.e("SERVICE", "LOCATION FETCHED: $lat $long")
                }
            }
    }

    override fun onBind(intent: Intent?): IBinder? { return null }

    override fun onDestroy() {
        super.onDestroy()
        stopService(applicationContext)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopService(applicationContext)
    }

    private fun stopService(context: Context) {
        context.stopService(Intent(context, LocationService::class.java))
        Log.e("SERVICE", "STOPPED")
    }
}