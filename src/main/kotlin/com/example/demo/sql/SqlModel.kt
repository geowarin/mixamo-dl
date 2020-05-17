package com.example.demo.sql

import com.example.demo.json.mapObj
import com.nfeld.jsonpathlite.extension.read
import org.json.JSONArray
import org.json.JSONObject
import tornadofx.*

class Product(
    val id: String,
    val data: JSONObject
) {
  val description: String
    get() = data.read("$.description")!!

  val name: String
    get() = data.read("$.name")!!

  val type: ProductType
    get() = ProductType.valueOf(data.read("$.type")!!)

  val thumbnail: String
    get() = data.read("$.thumbnail")!!

  val thumbnail_animated: String
    get() = data.read("$.thumbnail_animated")!!

  val motions: List<MotionDetails>
    get() = when (type) {
      ProductType.Motion -> listOf(MotionDetails(data, "$.details.gms_hash"))
      ProductType.MotionPack -> data.read<JSONArray>("$.details.motions")!!.mapObj { MotionDetails(it, "$.gms_hash") }
      else -> throw Error("Has no motions")
    }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Product

    if (id != other.id) return false

    return true
  }

  override fun hashCode(): Int = id.hashCode()
  override fun toString(): String {
    return "Product(id='$id', type='$type')"
  }
}

class MotionDetails(val data: JSONObject, val gmsPath: String) {
  val gms_hash
    get() = data.read<JSONObject>(gmsPath)!!

  val name: String
    get() = data.read("$.name")!!
}

enum class ProductType {
  Motion,
  MotionPack,
  Character
}
