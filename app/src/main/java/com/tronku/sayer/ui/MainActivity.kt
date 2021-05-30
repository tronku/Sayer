package com.tronku.sayer.ui

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.airbnb.lottie.LottieAnimationView
import com.bridgefy.sdk.client.*
import com.tronku.sayer.R
import com.tronku.sayer.location.LocationService
import com.tronku.sayer.location.UpdateFrequency
import com.tronku.sayer.network.SyncWorker
import com.tronku.sayer.utils.Storage
import java.util.concurrent.TimeUnit
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    companion object {
        val FREQUENCY = "frequency"
    }

    private val TAG = "MainActivity"
    private val BLUETOOTH_PERMISSION = 101
    private val LOCATION_PERMISSION = 102
    private val broadcastDelay = TimeUnit.SECONDS.toMillis(15)

    private lateinit var loaderLottie: LottieAnimationView
    private lateinit var statusRecyclerView: RecyclerView
    private lateinit var loaderText: TextView
    private lateinit var deviceIdText: TextView
    private lateinit var errorLottie: LottieAnimationView
    private lateinit var errorText: TextView
    private lateinit var updateFrequencyGroup: RadioGroup
    private lateinit var restartServiceButton: Button

    private val serviceIntent by lazy { Intent(this@MainActivity, LocationService::class.java) }
    private val handler by lazy { Handler(mainLooper) }
    private val broadcastRunnable by lazy {
        object : Runnable {
            override fun run() {
                val map = hashMapOf<String, Any>()
                val data = Storage.getUserLocation()
                if (data.first == null) return
                map.apply {
                    put(Storage.OTHER_LOCATION, "${data.first};${data.second};${data.third}")
                    put(Storage.DEVICE_ID, Storage.getDeviceId())
                }
                val message = Message.Builder().setContent(map).build()
                Bridgefy.sendBroadcastMessage(message)
                Log.e("MESSAGE", "BROADCASTED")
                handler.postDelayed(this, broadcastDelay)
            }
        }
    }

    private val idMap = hashMapOf<String, String>()

    private val messageListener = object : MessageListener() {
        override fun onBroadcastMessageReceived(message: Message?) {
            message?.let {
                toggleLoader(false)
                toggleError(false)
                handleBroadcastMessage(message)
            }
        }
    }

    private val stateListener = object : StateListener() {
        override fun onStarted() {
            Log.e(TAG, "Bridgefy Started")
            startLocationService()
            //startService(Intent(this@MainActivity, LocationService::class.java))
            broadcastMessage()
        }

        override fun onStartError(message: String?, errorCode: Int) {
            Log.e(TAG, "Start error: $message: $errorCode")
            toggleLoader(false)
            toggleError(true)
            when(errorCode) {
                INSUFFICIENT_PERMISSIONS -> {
                    requestPermissions(arrayOf(
                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                    ), LOCATION_PERMISSION)
                }
            }
        }

        override fun onDeviceDetected(p0: Device?) {}
        override fun onDeviceUnavailable(p0: Device?) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initialize()
    }

    private fun initialize() {
        initViews()
        checkForPermissions()
        initBridgefy()
        initSyncWorker()
    }

    private fun initViews() {
        loaderLottie = findViewById(R.id.loader_lottie)
        statusRecyclerView = findViewById(R.id.status_recyclerview)
        loaderText = findViewById(R.id.loader_text)
        deviceIdText = findViewById(R.id.device_id_text)
        errorLottie = findViewById(R.id.error_lottie)
        errorText = findViewById(R.id.error_text)
        updateFrequencyGroup = findViewById(R.id.update_freq)
        restartServiceButton = findViewById(R.id.restart_service)

        statusRecyclerView.adapter = StatusAdapter()

        restartServiceButton.setOnClickListener {
            startLocationService(getFrequency())
        }
    }

    private fun getFrequency(): UpdateFrequency {
        return when(updateFrequencyGroup.checkedRadioButtonId) {
            R.id.high_freq -> UpdateFrequency.HIGH
            R.id.medium_freq -> UpdateFrequency.MEDIUM
            else -> UpdateFrequency.LOW
        }
    }

    private fun checkForPermissions() {
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Device doesn't support bluetooth", Toast.LENGTH_SHORT).show()
        } else if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, BLUETOOTH_PERMISSION)
        } else if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Need Location permission")
            builder.setMessage("Please grant the access")
            builder.setPositiveButton(android.R.string.ok, null)
            builder.setOnDismissListener {
                requestPermissions(arrayOf(
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                ), LOCATION_PERMISSION)
            }
            builder.show()
        }
    }

    private fun initBridgefy() {
        Bridgefy.initialize(applicationContext, object : RegistrationListener() {
            override fun onRegistrationSuccessful(bridgefyClient: BridgefyClient?) {
                bridgefyClient?.let {
                    startBridge(bridgefyClient.userUuid)
                }
            }

            override fun onRegistrationFailed(errorCode: Int, message: String?) {
                Log.d(TAG, "Registration Failed: $errorCode: $message")
                toggleLoader(false)
                toggleError(true)
            }
        })
    }

    private fun startBridge(userId: String) {
        Log.d(TAG, "Init: Success: $userId")
        Storage.saveDeviceId(userId)
        deviceIdText.text = "Your device id: $userId"

        val builder = Config.Builder()
                .setAutoConnect(true)
                .setEngineProfile(BFEngineProfile.BFConfigProfileLongReach)
                .setEnergyProfile(BFEnergyProfile.BALANCED)
                .setBleProfile(BFBleProfile.EXTENDED_RANGE)
                .setMaxConnectionRetries(5)
                .setAntennaType(Config.Antenna.BLUETOOTH_LE)

        Bridgefy.start(messageListener, stateListener, builder.build())
    }

    private fun startLocationService(updateFrequency: UpdateFrequency = UpdateFrequency.MEDIUM) {
        if (serviceIntent.hasExtra(FREQUENCY))
            serviceIntent.removeExtra(FREQUENCY)
        serviceIntent.putExtra(FREQUENCY, updateFrequency.name)
        startService(serviceIntent)
    }

    private fun stopLocationService() {
        stopService(serviceIntent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startBridge(Storage.getDeviceId())
                }
            }
        }
    }

    private fun initSyncWorker() {
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(10, TimeUnit.MINUTES)
            .setInitialDelay(10, TimeUnit.MINUTES)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                30,
                TimeUnit.SECONDS
            )
            .build()
        WorkManager.getInstance(this).enqueue(syncRequest)
    }

    private fun handleBroadcastMessage(message: Message) {
        val deviceId = message.content[Storage.DEVICE_ID] as String
        val coordinates = (message.content[Storage.OTHER_LOCATION] as String).split(';')
        Storage.saveOtherLocation(deviceId, Triple(coordinates[0].toLong(), coordinates[1].toDouble(), coordinates[2].toDouble()))
        Log.e("MESSAGE", "RECEIVED")
    }

    private fun broadcastMessage() {
        Log.e("BROADCAST", "STARTED")
        handler.post(broadcastRunnable)
    }

    private fun toggleLoader(isVisible: Boolean) {
        loaderLottie.visibility = if(isVisible) View.VISIBLE else View.INVISIBLE
        loaderText.visibility = if(isVisible) View.VISIBLE else View.INVISIBLE
        statusRecyclerView.visibility = if(!isVisible) View.VISIBLE else View.INVISIBLE
        if (isVisible)
            loaderLottie.playAnimation()
        else
            loaderLottie.pauseAnimation()
    }

    private fun toggleError(isVisible: Boolean) {

        errorLottie.visibility = if(isVisible) View.VISIBLE else View.GONE
        errorText.visibility = if(isVisible) View.VISIBLE else View.GONE
        statusRecyclerView.visibility = if(!isVisible) View.VISIBLE else View.INVISIBLE
        if (isVisible)
            errorLottie.playAnimation()
        else
            errorLottie.pauseAnimation()
    }

    override fun onResume() {
        super.onResume()
        if (loaderLottie.isVisible)
            loaderLottie.playAnimation()
        if (errorLottie.isVisible)
            errorLottie.playAnimation()
    }

    override fun onPause() {
        super.onPause()
        loaderLottie.pauseAnimation()
        errorLottie.pauseAnimation()
    }

    override fun onDestroy() {
        super.onDestroy()
        Bridgefy.stop()
        loaderLottie.clearAnimation()
        errorLottie.clearAnimation()
        handler.removeCallbacks(broadcastRunnable)
    }
}