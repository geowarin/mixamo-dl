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

  fun toMotionPackDetails() = when(type) {
    ProductType.MotionPack -> MotionPackDetails(this)
    else -> throw Error("Not a motion")
  }

  fun toMotionDetails() = when(type) {
    ProductType.Motion -> MotionDetails(this.data)
    else -> throw Error("Not a motion")
  }
}

class MotionPackDetails(val product: Product) {
  val motions
  get() = product.data.read<JSONArray>("$.details.motions")!!.mapObj { MotionPackMotionDetails(it) }
}

class MotionPackMotionDetails(val data: JSONObject) {
  val gms_hash
  get() = data.read<JSONObject>("$.gms_hash")!!

  val name: String
    get() = data.read("$.name")!!
}

class MotionDetails(val data: JSONObject) {
  val gms_hash
  get() = data.read<JSONObject>("$.details.gms_hash")!!

  val name: String
    get() = data.read("$.name")!!
}

enum class ProductType {
  Motion,
  MotionPack,
  Character
}
