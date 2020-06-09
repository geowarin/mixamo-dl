package com.example.demo.view

import com.example.demo.paths.readText
import com.example.demo.sql.ProductType
import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import com.nfeld.jsonpathlite.extension.read
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONCompare
import org.skyscreamer.jsonassert.JSONCompareMode
import org.skyscreamer.jsonassert.JSONParser
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals

internal class ProductsControllerSqlTest {
  val fs: FileSystem = Jimfs.newFileSystem(Configuration.forCurrentPlatform())
  val controller = ProductsControllerSql(fs)

  @Test
  fun `convert parameters of motion pack`() {

    // Slim Shooter Pack
    val motions = controller.queries.getProductsDetails("c9d585a5-b96c-11e4-a802-0aaa78deedf9").motions
    val exportParameters: List<JSONObject> = motions.map(controller::toExportParameters)
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
    jsonAssert(expected, exportParameters.first().toString())
  }


  private val RIFLE_RUN_MOTION_ID = "c9c814a0-b96c-11e4-a802-0aaa78deedf9"

  @Test
  fun `convert parameters of motion`() {

    val motions = controller.queries.getProductsDetails(RIFLE_RUN_MOTION_ID).motions
    val exportParameters: List<JSONObject> = motions.map(controller::toExportParameters)

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
    jsonAssert(expected, exportParameters.first().toString())
  }

  @Test
  fun `products contain details`() {

    // Rifle Run --  Aimed
    val motions = controller.queries.getProducts(ProductType.Motion).find { it.id == RIFLE_RUN_MOTION_ID }!!.motions
    val exportParameters: List<JSONObject> = motions.map(controller::toExportParameters)

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
    jsonAssert(expected, exportParameters.first().toString())
  }

  @Test
  fun `save pack`() {
    val motion = controller.queries.getProducts(ProductType.Motion).find { it.id == RIFLE_RUN_MOTION_ID }

    controller.addMotionToSelection(motion)
    controller.writePackToDisk("myPack.json")

    val expected = """
          {
            "motions": [
              {"id":  ${RIFLE_RUN_MOTION_ID}}
            ]
          }
      """
    jsonAssert(expected, controller.packsDir.resolve("myPack.json").readText())
  }

  @Test
  fun `load pack`() {
    val contents = """
          {
            "motions": [
              {"id":  ${RIFLE_RUN_MOTION_ID}}
            ]
          }
      """
    controller.packsDir.resolve("myPack.json").write(contents)

    controller.loadPack("myPack.json")

    val motion = controller.queries.getProducts(ProductType.Motion).find { it.id == RIFLE_RUN_MOTION_ID }
    assertThat(controller.selectedMotions).containsExactly(motion)
  }
}

fun jsonAssert(@Language("json") expected: String, json: String) {
  val result = JSONCompare.compareJSON(expected, json, JSONCompareMode.LENIENT)
  if (result.failed()) {
    Assertions.assertEquals(prettyJSON(expected), prettyJSON(json))
  }
}

fun String.isJsonEqual(@Language("json") expected: String) {
  val result = JSONCompare.compareJSON(this, expected, JSONCompareMode.LENIENT)
  if (result.failed()) {
    Assertions.assertEquals(prettyJSON(expected), prettyJSON(this))
  }
}

fun prettyJSON(jsonObject: String): String {
  return when (val json = JSONParser.parseJSON(jsonObject)) {
    is JSONObject -> json.toString(2)
    is JSONArray -> json.toString(2)
    else -> "<Unparsable json>"
  }
}

fun Path.write(text: String) {
  Files.createDirectories(this.parent)
  Files.write(this, text.toByteArray())
}