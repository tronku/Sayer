package com.tronku.sayer.utils

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

object Utils {

    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    private fun rad2deg(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }

    fun getDateTime(millis: Long): String {
        val timeZone = "Asia/Kotkata"
        val tz = TimeZone.getTimeZone(timeZone)
        val sdf = SimpleDateFormat("dd MMM yyyy HH:mm")
        sdf.timeZone = tz
        return sdf.format(Date(millis))
    }

    fun getDistance(lat: Double, long: Double): String {
        val myLat = Storage.getUserLocation().second
        val myLong = Storage.getUserLocation().third

        val theta: Double = long - myLong
        var dist = sin(deg2rad(lat)) * sin(deg2rad(myLat)) + cos(deg2rad(lat)) * cos(deg2rad(myLat)) * cos(deg2rad(theta))
        dist = acos(dist)
        dist = rad2deg(dist)
        dist *= 60 * 1.1515 * 5280
        return "${dist.toInt()}ft away"
    }

    fun isConnected(): Boolean {
        val command = "ping -c 1 google.com"
        return Runtime.getRuntime().exec(command).waitFor() == 0
    }

}