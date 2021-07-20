package com.tronku.sayer.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlin.collections.HashMap

class Storage {

    companion object {

        private lateinit var context: Context
        private lateinit var gson: Gson

        private const val SAYER_PREFS = "sayer_prefs"
        const val DEVICE_ID = "device_id"
        const val LOCATION = "location"
        const val OTHER_LOCATION = "other_locations"
        const val CONVERSATION_ID = "conversation_id"
        const val CHAT_PAYLOAD = "chat_payload"
        const val CONVERSATION_EXTRA = "conversation_extra"
        private const val IS_BRIDGEGY = "bridgefy_working"

        fun initialize(context: Context) {
            this.context = context
            gson = Gson()
        }

        fun saveDeviceId(userId: String) {
            savePref(DEVICE_ID, userId)
        }

        fun getDeviceId(): String {
            return getPref(DEVICE_ID, UUID.randomUUID().toString())
        }

        fun saveUserLocation(coordinate: Triple<Long, Double, Double>) {
            val data = gson.toJson(coordinate)
            savePref(LOCATION, data)
        }

        fun getUserLocation(): Triple<Long, Double, Double> {
            val type = object : TypeToken<Triple<Long, Double, Double>>() {}.type
            return gson.fromJson(getPref(LOCATION, "{}"), type)
        }

        fun saveOtherLocation(userId: String, coordinate: Triple<Long, Double, Double>) {
            val type = object : TypeToken<HashMap<String, Triple<Long, Double, Double>>>() {}.type
            val map: HashMap<String, Triple<Long, Double, Double>> = gson.fromJson(getPref(OTHER_LOCATION, "{}"), type)
            map[userId] = coordinate
            savePref(OTHER_LOCATION, gson.toJson(map))
        }

        fun getAllLocations(): HashMap<String, String> {
            val resultMap = hashMapOf<String, String>()
            val type = object : TypeToken<HashMap<String, Triple<Long, Double, Double>>>() {}.type
            val map: HashMap<String, Triple<Long, Double, Double>> = gson.fromJson(getPref(OTHER_LOCATION, "{}"), type)
            if (getUserLocation() != null && getUserLocation().first != null) {
                map[getDeviceId()] = getUserLocation()
                map.forEach { (userId, data) ->
                    resultMap[userId] = "${data.first};${data.second};${data.third}"
                }
            }
            return resultMap
        }

        fun clearAllLocations() {
            clearPref(OTHER_LOCATION)
        }

        fun setBridgefy(value: Boolean) {
            savePref(IS_BRIDGEGY, "$value")
        }

        fun getBridgefy(): Boolean {
            return getPref(IS_BRIDGEGY, "false") == "true"
        }

        private fun savePref(key: String, value: String) {
            context.getSharedPreferences(SAYER_PREFS, Context.MODE_PRIVATE).edit().putString(key, value).apply()
        }

        private fun getPref(key: String, default: String): String {
            return context.getSharedPreferences(SAYER_PREFS, Context.MODE_PRIVATE).getString(key, default) ?: ""
        }

        private fun clearPref(key: String) {
            context.getSharedPreferences(SAYER_PREFS, Context.MODE_PRIVATE).edit().remove(key).apply()
        }
    }

}