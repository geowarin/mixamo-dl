package com.example.demo.sql

import com.example.demo.json.mapObj
import com.example.demo.json.string
import com.github.andrewoma.kwery.core.DefaultSession
import com.github.andrewoma.kwery.core.Session
import com.github.andrewoma.kwery.core.dialect.SqliteDialect
import com.nfeld.jsonpathlite.extension.read
import khttp.get
import org.intellij.lang.annotations.Language
import org.json.JSONArray
import org.json.JSONObject
import java.sql.DriverManager


class Queries(private val session: Session) {

  @Language("sql")
  private val createTableQuery = """
CREATE TABLE IF NOT EXISTS products(
    id TEXT NOT NULL,
    data TEXT NOT NULL,

    CONSTRAINT products_pkey PRIMARY KEY (id)
);

CREATE INDEX products_id ON products(id);

CREATE TABLE IF NOT EXISTS productDetails(
    id TEXT NOT NULL,
    data TEXT NOT NULL,

    CONSTRAINT products_details_pkey PRIMARY KEY (id)
);

CREATE INDEX products_details_id ON products(id);
"""

  fun createTables() {
    session.update(createTableQuery)
  }

  @Language("sql")
  private val insertProductQuery = """
insert into products(id, data)
values (:id, json(:data))
"""

  fun insertProducts(array: JSONArray) {
    println("Inserting ${array.length()} products")
    val params: List<Map<String, String>> = array.mapObj {
      mapOf(
          "id" to it.string("id"),
          "data" to it.toString()
      )
    }

    session.batchUpdate(insertProductQuery, params)
  }

  @Language("sql")
  private val insertProductDetailsQuery = """
insert into productDetails(id, data)
values (:id, json(:data))
"""

  fun insertProductDetails(obj: JSONObject) {
      val params = mapOf(
          "id" to obj.string("id"),
          "data" to obj.toString()
      )
    session.update(insertProductDetailsQuery, params)
  }

  @Language("sql")
  private val selectProductsQuery = """
select 
 id,
 data,
 json_extract(data, '$.type') as type
from products
where type = :type
"""

  fun getProducts(type: String): List<Product> {
    val params = mapOf(
        "type" to type
    )
    return session.select(selectProductsQuery, params) {
      Product(it.string("id"), JSONObject(it.string("data")))
    }
  }
}

data class Product(
    val id: String,
    val data: JSONObject
) {
  val name: String?
    get() = data.read("$.name")
}

val baseURI = "https://www.mixamo.com/api/v1"
val token = """
eyJ4NXUiOiJpbXNfbmExLWtleS0xLmNlciIsImFsZyI6IlJTMjU2In0.eyJpZCI6IjE1ODg4MTIzNTA1MjRfYTJiYTI1OGMtN2IyNy00ZTQzLWEwZjEtYzYyMDlmNGUwMzEyX3VlMSIsImNsaWVudF9pZCI6Im1peGFtbzEiLCJ1c2VyX2lkIjoiQTY1QjQ1MjY1QzAzMDY0QzBBNDk1Q0NGQEFkb2JlSUQiLCJ0eXBlIjoiYWNjZXNzX3Rva2VuIiwiYXMiOiJpbXMtbmExIiwiZmciOiJVTkdTM1laUlhMTzU1NzZLQzZRTFFQUUFJRT09PT09PSIsInNpZCI6IjE1ODg3MTUwNTkyMDFfNTMzYzk4NzEtNDFlZi00ZGNhLWE2OTItNWUzZGRlMjhmZjc1X3VlMSIsInJ0aWQiOiIxNTg4ODEyMzUwNTI0XzU5OTc4ZTdhLTI5MjgtNDkyYy1iZTE3LWU2MTBkNjBlOTMxY191ZTEiLCJvYyI6InJlbmdhKm5hMXIqMTcxZWM5ODc4YTEqME0xQ1M0QVpDWDZZNUFDUUVKSlZZOVhETU0iLCJydGVhIjoiMTU5MDAyMTk1MDUyNCIsIm1vaSI6IjcxZWI4ZjQyIiwiYyI6ImFyRndNYVp1UndsRS9mRXR6cUViTEE9PSIsImV4cGlyZXNfaW4iOiI4NjQwMDAwMCIsInNjb3BlIjoiY3JlYXRpdmVfc2RrLG9wZW5pZCxzYW8ubWl4YW1vIiwiY3JlYXRlZF9hdCI6IjE1ODg4MTIzNTA1MjQifQ.GSrXDRTHM16eJyw5m8Ov9FfzWwc2Q4GiwqTjEw8w1ejMSkLEd4MO73VDzg98C1dLR4vNWm-TQwL5mEDSfgFxOz4HF6UroKuEXxBnkc7eDd7vnYc-OYZhDMyQVUCLHNPvvZ1D17UgP2-E6TaXLiePcIiLkjHc-4QeVm6qzLVL_szc1HX2A7TV22jRBA3F1adeFbqK0jhmnfgE1I2cYEQhEXBpZrCWVD5f5rkDZUexhuWGLy0o3Km8NQ3ZKPRqICuxsxIb2TmU9lCqlLp08vt0Pc4n_OQiViqK9UBnHowbZZjYlPxq-cS1ozMidk2-zyY3eoYERCUVVqy_kf6UoAdKxA
""".trim()
val headers = mapOf(
    "Authorization" to "Bearer $token",
    "X-Api-Key" to "mixamo2"
)

fun main() {
  val queries = Queries(session("./mixamo.sqlite"))

  queries.getProducts("Motion").forEach { motionPack ->

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
  val queries = Queries(session("./mixamo.sqlite"))

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

private fun session(dbPath: String): Session {
  val connection = DriverManager.getConnection("jdbc:sqlite:$dbPath");
  return DefaultSession(connection, SqliteDialect())
}