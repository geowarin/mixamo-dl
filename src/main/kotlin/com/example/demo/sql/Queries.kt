package com.example.demo.sql

import com.example.demo.json.mapObj
import com.example.demo.json.string
import com.github.andrewoma.kwery.core.DefaultSession
import com.github.andrewoma.kwery.core.Session
import com.github.andrewoma.kwery.core.dialect.SqliteDialect
import org.intellij.lang.annotations.Language
import org.json.JSONArray
import org.json.JSONObject
import java.sql.DriverManager

class Queries {
  private val session: Session = session("/mixamo.sqlite")

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
 json_extract(data, '$.type') as type,
 json_extract(data, '$.name') as name
from detailedProducts
where type = :type
order by name
"""

  fun getProducts(type: ProductType): List<Product> {
    val params = mapOf(
        "type" to type
    )
    return session.select(selectProductsQuery, params) {
      Product(it.string("id"), JSONObject(it.string("data")))
    }
  }

  @Language("sql")
  private val selectProductsDetailsQuery = """
select 
 id,
 data
from productDetails
where id = :id
"""

  fun getProductsDetails(id: String): Product {
    val params = mapOf(
        "id" to id
    )
    return session.select(selectProductsDetailsQuery, params) {
      Product(it.string("id"), JSONObject(it.string("data")))
    }.first()
  }


  @Language("sql")
  private val searchProductsQuery = """
select 
 id,
 data,
 json_extract(data, '$.type') as type,
 json_extract(data, '$.name') as name,
 json_extract(data, '$.description') as description
from detailedProducts
where type = :type
and (name like :searchText or description like :searchText )
order by name
"""

  fun searchProduct(type: ProductType, searchText: String? = null): List<Product> {
    if (searchText.isNullOrBlank()) {
      return getProducts(type);
    }
    val params = mapOf(
        "type" to type,
        "searchText" to "%$searchText%"
    )
    return session.select(searchProductsQuery, params) {
      Product(it.string("id"), JSONObject(it.string("data")))
    }
  }

  @Language("sql")
  private val selectMultiProductsQuery = """
select 
 id,
 data,
 json_extract(data, '$.type') as type,
 json_extract(data, '$.name') as name
from detailedProducts
where id in (:ids)
order by name
"""

  fun getProducts(ids: List<String>): List<Product> {
    val params = mapOf(
      "ids" to ids
    )
    return session.select(selectMultiProductsQuery, params) {
      Product(it.string("id"), JSONObject(it.string("data")))
    }
  }
}

private fun session(dbPath: String): Session {
  val resourcePath = Queries::class.java.getResource(dbPath)
  val connection = DriverManager.getConnection("jdbc:sqlite::resource:$resourcePath");
  return DefaultSession(connection, SqliteDialect())
}