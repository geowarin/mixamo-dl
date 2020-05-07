package com.example.demo.sql

import com.github.andrewoma.kwery.core.DefaultSession
import com.github.andrewoma.kwery.core.dialect.SqliteDialect
import org.intellij.lang.annotations.Language
import tornadofx.*
import java.sql.DriverManager


@Language("sql")
val createTableQuery = """
CREATE TABLE products(
    id TEXT NOT NULL,
    data TEXT NOT NULL,

    CONSTRAINT products_pkey PRIMARY KEY (id)
);

CREATE INDEX products_id ON products(id);
"""

@Language("sql")
val insertProductQuery = """
insert into products(id, data)
values (:id, json(:data))
"""

@Language("sql")
val selectProductsQuery = """
select 
 json_extract(data, '$.name') as name
from products
"""

class Toto : Rest() {


}

fun main() {

//    val api = Rest()
//    api.baseURI = "https://www.mixamo.com/api/v1"
//    api.engine.requestInterceptor = { request ->
//        val token = "eyJ4NXUiOiJpbXNfbmExLWtleS0xLmNlciIsImFsZyI6IlJTMjU2In0.eyJpZCI6IjE1ODg4MTIzNTA1MjRfYTJiYTI1OGMtN2IyNy00ZTQzLWEwZjEtYzYyMDlmNGUwMzEyX3VlMSIsImNsaWVudF9pZCI6Im1peGFtbzEiLCJ1c2VyX2lkIjoiQTY1QjQ1MjY1QzAzMDY0QzBBNDk1Q0NGQEFkb2JlSUQiLCJ0eXBlIjoiYWNjZXNzX3Rva2VuIiwiYXMiOiJpbXMtbmExIiwiZmciOiJVTkdTM1laUlhMTzU1NzZLQzZRTFFQUUFJRT09PT09PSIsInNpZCI6IjE1ODg3MTUwNTkyMDFfNTMzYzk4NzEtNDFlZi00ZGNhLWE2OTItNWUzZGRlMjhmZjc1X3VlMSIsInJ0aWQiOiIxNTg4ODEyMzUwNTI0XzU5OTc4ZTdhLTI5MjgtNDkyYy1iZTE3LWU2MTBkNjBlOTMxY191ZTEiLCJvYyI6InJlbmdhKm5hMXIqMTcxZWM5ODc4YTEqME0xQ1M0QVpDWDZZNUFDUUVKSlZZOVhETU0iLCJydGVhIjoiMTU5MDAyMTk1MDUyNCIsIm1vaSI6IjcxZWI4ZjQyIiwiYyI6ImFyRndNYVp1UndsRS9mRXR6cUViTEE9PSIsImV4cGlyZXNfaW4iOiI4NjQwMDAwMCIsInNjb3BlIjoiY3JlYXRpdmVfc2RrLG9wZW5pZCxzYW8ubWl4YW1vIiwiY3JlYXRlZF9hdCI6IjE1ODg4MTIzNTA1MjQifQ.GSrXDRTHM16eJyw5m8Ov9FfzWwc2Q4GiwqTjEw8w1ejMSkLEd4MO73VDzg98C1dLR4vNWm-TQwL5mEDSfgFxOz4HF6UroKuEXxBnkc7eDd7vnYc-OYZhDMyQVUCLHNPvvZ1D17UgP2-E6TaXLiePcIiLkjHc-4QeVm6qzLVL_szc1HX2A7TV22jRBA3F1adeFbqK0jhmnfgE1I2cYEQhEXBpZrCWVD5f5rkDZUexhuWGLy0o3Km8NQ3ZKPRqICuxsxIb2TmU9lCqlLp08vt0Pc4n_OQiViqK9UBnHowbZZjYlPxq-cS1ozMidk2-zyY3eoYERCUVVqy_kf6UoAdKxA"
//        request.addHeader("Authorization", "Bearer $token")
//        request.addHeader("X-Api-Key", "mixamo2 ")
//    }
//    val params = mapOf(
//            "page" to 1,
//            "limit" to 10,
//            "type" to "MotionPack"
//    )
//    api.execute()
//    val results = api.get("products${params.queryString}").one().jsonArray("results")!!
//
//    println(results)
//    test()
}

fun test() {
    val connection = DriverManager.getConnection("jdbc:sqlite:./test.db");
    val session = DefaultSession(connection, SqliteDialect()) // Standard JDBC connection


//    session.update(createTableQuery)


    val productData = """{
  "id": "c9c69fb6-b96c-11e4-a802-0aaa78deedf9",
  "type": "Motion",
  "description": "Buckled Stand And Praying",
  "category": "",
  "character_type": "human",
  "name": "Praying",
  "thumbnail": "https://d99n9xvb9513w.cloudfront.net/thumbnails/motions/103120902/static.png",
  "thumbnail_animated": "https://d99n9xvb9513w.cloudfront.net/thumbnails/motions/103120902/animated.gif",
  "motion_id": "c9c69fb6-b96c-11e4-a802-0aaa78deedf9",
  "motions": null,
  "source": "system"
}
"""

//    session.update(insertProductQuery, mapOf("id" to "c9c69fb6-b96c-11e4-a802-0aaa78deedf9", "data" to productData))

    session.select(selectProductsQuery) {
        println(it.string("name"))
    }
}