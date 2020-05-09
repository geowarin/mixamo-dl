package com.example.demo.sql

import com.example.demo.json.mapObj
import com.nfeld.jsonpathlite.extension.read
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Error

class Product(
    val id: String,
    val data: JSONObject
) {
  val name: String
    get() = data.read("$.name")!!

  val type: ProductType
    get() = ProductType.valueOf(data.read("$.type")!!)

  val thumbnail: String
    get() = data.read("$.thumbnail")!!

  val thumbnail_animated: String
    get() = data.read("$.thumbnail_animated")!!

  fun toMotionDetails(): HasMotions = when(type) {
    ProductType.Motion -> MotionDetails(data, "$.details.gms_hash")
    ProductType.MotionPack -> MotionPackDetails(data)
    else -> throw Error("Not a motion")
  }
}

interface HasMotions {
  val motions: List<MotionDetails>
}

class MotionPackDetails(val data: JSONObject): HasMotions {
  override val motions
  get() = data.read<JSONArray>("$.details.motions")!!.mapObj { MotionDetails(it, "$.gms_hash") }
}

class MotionDetails(val data: JSONObject, val gmsPath: String): HasMotions {
  val gms_hash
  get() = data.read<JSONObject>(gmsPath)!!

  val name: String
    get() = data.read("$.name")!!

  override val motions: List<MotionDetails>
    get() = listOf(this)
}

enum class ProductType {
  Motion,
  MotionPack,
  Character
}
