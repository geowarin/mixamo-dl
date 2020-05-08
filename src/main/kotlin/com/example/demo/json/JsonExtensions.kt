package com.example.demo.json

import org.json.JSONArray
import org.json.JSONObject

operator fun JSONObject.set(name: String, value: Boolean) {
    this.put(name, value)
}

operator fun JSONObject.set(name: String, value: String) {
    this.put(name, value)
}

operator fun JSONObject.set(name: String, value: Int) {
    this.put(name, value)
}

operator fun JSONObject.set(name: String, value: Long) {
    this.put(name, value)
}

operator fun JSONObject.set(name: String, value: Double) {
    this.put(name, value)
}

operator fun JSONObject.set(name: String, value: Any) {
    this.put(name, value)
}

fun JSONObject.bool(key: String): Boolean = this.getBoolean(key)
fun JSONObject.string(key: String): String = this.getString(key)
fun JSONObject.int(key: String): Int = this.getInt(key)
fun JSONObject.long(key: String): Long = this.getLong(key)
fun JSONObject.double(key: String): Double = this.getDouble(key)


fun JSONArray?.forEachObj(function: (JSONObject) -> Unit) {
    this?.forEach { el: Any ->
        if (el is JSONObject)
            function(el)
    }
}

fun <T: Any> JSONArray?.mapObj(function: (JSONObject) -> T): List<T> {
    return this!!.mapNotNull { el: Any ->
        if (el is JSONObject)
            function(el)
        else
            null
    }
}