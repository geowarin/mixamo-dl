package com.example.demo.view

import com.example.demo.sql.ProductType
import com.nfeld.jsonpathlite.extension.read
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertEquals

internal class ProductsControllerSqlTest {
    val productsControllerSql = ProductsControllerSql()

  @Test
  fun `convert parameters of motion pack`() {

    // Slim Shooter Pack
    val motionPackDetails = productsControllerSql.queries.getProductsDetails("c9d585a5-b96c-11e4-a802-0aaa78deedf9").toMotionDetails()
    val exportParameters: List<JSONObject> = productsControllerSql.toExportParameters(motionPackDetails)
    val expected = """
{
  "mirror": false,
  "inplace": false,
  "trim": [
    0,
    100
  ],
  "model-id": 114260901,
  "name": "rifle run",
  "arm-space": 0,
  "params": "1.0, 0.0, 0.0"
}
"""
    assertEquals(
        listOf("rifle run", "strafe", "rifle aiming idle", "walking", "strafe", "reloading", "firing rifle"),
        exportParameters.map { it.read<String>("$.name") }
    )
    JSONAssert.assertEquals(expected, exportParameters.first().toString(), JSONCompareMode.LENIENT)
  }


  @Test
  fun `convert parameters of motion`() {

    // Rifle Run --  Aimed
    val motionDetails = productsControllerSql.queries.getProductsDetails("c9c814a0-b96c-11e4-a802-0aaa78deedf9").toMotionDetails()
    val exportParameters: List<JSONObject> = productsControllerSql.toExportParameters(motionDetails)

    val expected = """
{
  "mirror": false,
  "inplace": false,
  "trim": [
    0,
    100
  ],
  "model-id": 114260901,
  "name": "Rifle Run",
  "arm-space": 0,
  "params": "1.0, 0.0, 0.0"
}
"""
    JSONAssert.assertEquals(expected, exportParameters.first().toString(), JSONCompareMode.LENIENT)
  }

  @Test
  fun `products contain details`() {

    // Rifle Run --  Aimed
    val motionDetails = productsControllerSql.queries.getProducts(ProductType.Motion).find { it.id == "c9c814a0-b96c-11e4-a802-0aaa78deedf9" }!!.toMotionDetails()
    val exportParameters: List<JSONObject> = productsControllerSql.toExportParameters(motionDetails)

    val expected = """
{
  "mirror": false,
  "inplace": false,
  "trim": [
    0,
    100
  ],
  "model-id": 114260901,
  "name": "Rifle Run",
  "arm-space": 0,
  "params": "1.0, 0.0, 0.0"
}
"""
    JSONAssert.assertEquals(expected, exportParameters.first().toString(), JSONCompareMode.LENIENT)
  }
}