package com.example.demo.sql

import com.nfeld.jsonpathlite.extension.read
import khttp.get
import org.json.JSONArray


val baseURI = "https://www.mixamo.com/api/v1"
val token = """
eyJ4NXUiOiJpbXNfbmExLWtleS0xLmNlciIsImFsZyI6IlJTMjU2In0.eyJpZCI6IjE1ODg4MTIzNTA1MjRfYTJiYTI1OGMtN2IyNy00ZTQzLWEwZjEtYzYyMDlmNGUwMzEyX3VlMSIsImNsaWVudF9pZCI6Im1peGFtbzEiLCJ1c2VyX2lkIjoiQTY1QjQ1MjY1QzAzMDY0QzBBNDk1Q0NGQEFkb2JlSUQiLCJ0eXBlIjoiYWNjZXNzX3Rva2VuIiwiYXMiOiJpbXMtbmExIiwiZmciOiJVTkdTM1laUlhMTzU1NzZLQzZRTFFQUUFJRT09PT09PSIsInNpZCI6IjE1ODg3MTUwNTkyMDFfNTMzYzk4NzEtNDFlZi00ZGNhLWE2OTItNWUzZGRlMjhmZjc1X3VlMSIsInJ0aWQiOiIxNTg4ODEyMzUwNTI0XzU5OTc4ZTdhLTI5MjgtNDkyYy1iZTE3LWU2MTBkNjBlOTMxY191ZTEiLCJvYyI6InJlbmdhKm5hMXIqMTcxZWM5ODc4YTEqME0xQ1M0QVpDWDZZNUFDUUVKSlZZOVhETU0iLCJydGVhIjoiMTU5MDAyMTk1MDUyNCIsIm1vaSI6IjcxZWI4ZjQyIiwiYyI6ImFyRndNYVp1UndsRS9mRXR6cUViTEE9PSIsImV4cGlyZXNfaW4iOiI4NjQwMDAwMCIsInNjb3BlIjoiY3JlYXRpdmVfc2RrLG9wZW5pZCxzYW8ubWl4YW1vIiwiY3JlYXRlZF9hdCI6IjE1ODg4MTIzNTA1MjQifQ.GSrXDRTHM16eJyw5m8Ov9FfzWwc2Q4GiwqTjEw8w1ejMSkLEd4MO73VDzg98C1dLR4vNWm-TQwL5mEDSfgFxOz4HF6UroKuEXxBnkc7eDd7vnYc-OYZhDMyQVUCLHNPvvZ1D17UgP2-E6TaXLiePcIiLkjHc-4QeVm6qzLVL_szc1HX2A7TV22jRBA3F1adeFbqK0jhmnfgE1I2cYEQhEXBpZrCWVD5f5rkDZUexhuWGLy0o3Km8NQ3ZKPRqICuxsxIb2TmU9lCqlLp08vt0Pc4n_OQiViqK9UBnHowbZZjYlPxq-cS1ozMidk2-zyY3eoYERCUVVqy_kf6UoAdKxA
""".trim()
val headers = mapOf(
    "Authorization" to "Bearer $token",
    "X-Api-Key" to "mixamo2"
)

fun main() {
  insertProductDetails()
}

private fun insertProductDetails() {
  val queries = Queries()

  queries.getProducts(ProductType.Motion).forEach { motionPack ->

    val resultObj = get(
        url = "$baseURI/products/${motionPack.id}",
        headers = headers
    ).jsonObject

    println("Inserting details for ${motionPack.name}")
    println(resultObj.toString())
    queries.insertProductDetails(resultObj)

    Thread.sleep(500)
  }
}

private fun insertProducts() {
  val queries = Queries()

  var page = 1
  var maxPage: Int
  do {
    val resultObj = get(
        url = "$baseURI/products",
        headers = headers,
        params = mapOf(
            "page" to page.toString(),
            "limit" to "100",
            "type" to "Motion"
        )
    ).jsonObject

    maxPage = resultObj.read("$.pagination.num_pages")!!
    page++

    val results = resultObj.read<JSONArray>("$.results")!!
    queries.insertProducts(results)

  } while (page <= maxPage)
}

