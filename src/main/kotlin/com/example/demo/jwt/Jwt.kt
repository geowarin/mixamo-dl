package com.example.demo.jwt

import com.example.demo.paths.getCacheDir
import com.example.demo.paths.readText
import com.example.demo.paths.write
import org.json.JSONObject
import java.util.*

class Jwt(val token: String) {
  private val jsonValue = decode(token)
}

fun decode(token: String): JSONObject {
  val decoded = Base64.getDecoder().decode(token)
  return JSONObject(decoded)
}

fun writeToken(arguments: List<String>) {
  getCacheDir().resolve("token").write(arguments.first())
}

fun readToken(): String {
  return getCacheDir().resolve("token").readText().trim()
}