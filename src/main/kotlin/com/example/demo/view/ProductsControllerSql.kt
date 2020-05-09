package com.example.demo.view

import com.example.demo.downloadFile
import com.example.demo.json.string
import com.example.demo.sql.*
import com.nfeld.jsonpathlite.extension.read
import org.json.JSONArray
import org.json.JSONObject
import tornadofx.*
import java.io.File
import java.net.URL

val baseURI = "https://www.mixamo.com/api/v1"
val token = """
eyJ4NXUiOiJpbXNfbmExLWtleS0xLmNlciIsImFsZyI6IlJTMjU2In0.eyJpZCI6IjE1ODg5NTk5NzkyMTRfM2M3NzRlOGUtOTJmOS00MzMwLTllYmMtOTUxYTAxZjU5ZDZjX3VlMSIsImNsaWVudF9pZCI6Im1peGFtbzEiLCJ1c2VyX2lkIjoiQTY1QjQ1MjY1QzAzMDY0QzBBNDk1Q0NGQEFkb2JlSUQiLCJzdGF0ZSI6IiIsInR5cGUiOiJhY2Nlc3NfdG9rZW4iLCJhcyI6Imltcy1uYTEiLCJmZyI6IlVOTE1WWVpSWFBPNTU3NktDNllMUVBRQUpZPT09PT09Iiwic2lkIjoiMTU4ODk1OTk3NzQ2NV81ZTQ3YjI2Mi00ZWMxLTQ4YzItYTlmMy02NGZlZjU2ZWIzODZfdWUxIiwicnRpZCI6IjE1ODg5NTk5NzkyMTRfYjMwYWExODgtOThiNi00YmY2LWJhMDctYTQ1M2M2YmZhMTU0X3VlMSIsIm9jIjoicmVuZ2EqbmExcioxNzFmNTY1MWVkYSpSQjdCNzVBUzE1MDZaOFI1WDRQSEpISjIxQyIsInJ0ZWEiOiIxNTkwMTY5NTc5MjE0IiwibW9pIjoiYWFjYzA0YWYiLCJjIjoic05ZZDRFR3NQQ3hhMi9XT3RKZkp2Zz09IiwiZXhwaXJlc19pbiI6Ijg2NDAwMDAwIiwiY3JlYXRlZF9hdCI6IjE1ODg5NTk5NzkyMTQiLCJzY29wZSI6Im9wZW5pZCxBZG9iZUlELGZldGNoX3NhbyxzYW8uY3JlYXRpdmVfY2xvdWQifQ.AyMPT--SNM0wogfsbjWCtXO_CjZ-hCqnQYaYAHhcUjnhS_B9xppkfU5XWIxCdIBvyjgfsW9GtC_vcx8n4470n4jeyxgANkVPhgnIhtNU032vPFi1ItB5NHPqLAP-ebF-ircZdfysCtAfkGqdCcvEUEftYIF4995DejBd7e_AtOFCwXEnm0-aEJ5cE70KpiuZs5mp0rBWUHun3HD4EBZGBRuj8iTP7faOj7j2aU8lkt9X0SAj1gwL3XGbxssRf4hSGMrt1otZkdUENrBDOlG-qiPHzpN7OEqm-M6xx43GBZ_Gg-B3OYvCkeYIcvjdXb3sYa1cCKp7URIrNRbOiGzj9A
""".trim()
val headers = mapOf(
    "Authorization" to "Bearer $token",
    "X-Api-Key" to "mixamo2"
)

class ProductsControllerSql : Controller() {
  val queries: Queries = Queries()
  var queryResult = observableListOf<Product>()

  init {
    setQuery("")
  }

  fun download(motionPacks: List<Product>, character: Product) {
    val motions = motionPacks.map { getMotionPackDetails(it.id) }
    val exportResult = export(motions, character.id)
    if (exportResult.status == "failed") {
      throw IllegalStateException("job failed")
    }

    var monitorResult: OperationResult
    do {
      println("Waiting for pack...")
      Thread.sleep(5000)
      monitorResult = monitor(exportResult.uuid)
    } while (monitorResult.status == "processing")

    if (monitorResult.status == "failed") {
      throw Error("job failed")
    }
    val motionPackName = motionPacks.joinToString("-") { it.name }
    writeFile(monitorResult.jobResult!!, "downloads/${character.name}_${motionPackName}.zip")
  }

  private fun writeFile(jobResult: String, fileName: String) {
    println("Writing file $fileName...")
    downloadFile(jobResult, fileName)
    println("Done!")
  }

  private fun monitor(exportResultUuid: String): OperationResult {
    val resultObj = khttp.get(
        url = "${baseURI}/characters/${exportResultUuid}/monitor",
        headers = headers
    ).jsonObject
    return operationResult(resultObj)
  }

  private fun operationResult(resultObj: JSONObject): OperationResult {
    if (resultObj.has("error_code") || resultObj.has("error")) {
      throw Error(resultObj.string("message"))
    }
    return OperationResult(
        status = resultObj.string("status"),
        message = resultObj.string("message"),
        uuid = resultObj.string("uuid"),
        jobResult = resultObj.optString("job_result")
    )
  }

  private fun getMotionPackDetails(motionPackId: String): HasMotions {
    return queries.getProductsDetails(motionPackId).toMotionDetails()
  }

  private fun export(motions: List<HasMotions>, characterId: String): OperationResult {
    val payload = combineExportParameters(motions, characterId)
    val resultObj = khttp.post(
        url = "${baseURI}/animations/export",
        headers = headers,
        json = payload
    ).jsonObject
    return operationResult(resultObj)
  }

  fun toExportParameters(motionPackDetails: HasMotions): List<JSONObject> {
    return motionPackDetails.motions.map { motionDetails ->
      motionDetails.gms_hash
          .put("name", motionDetails.name)
          // "params": [["Posture", 1.0], ["Step Width", 1.0], ["Overdrive", 0.0]] => 1.0,1.0,1.0
          .put("params", motionDetails.gms_hash.read<JSONArray>("$.params")!!
              .joinToString { (it as JSONArray).getDouble(1).toString() })
    }
  }

  fun combineExportParameters(motionPackDetails: List<HasMotions>, characterId: String): JSONObject {
    val motionOptionsArray: List<JSONObject> = motionPackDetails.flatMap { toExportParameters(it) }
    return JSONObject()
        .put("gms_hash", motionOptionsArray)
        .put("preferences",
            JSONObject()
                .put("format", "fbx7_unity")
                .put("mesh_motionpack", "t-pose")
                .put("fps", "30")
                .put("reducekf", "0")
        )
        .put("character_id", characterId)
        .put("type", "MotionPack")
        .put("product_name", "Locomotion Pack")
  }

  fun loadMotionPacks(): List<Product> = queries.getProducts(ProductType.MotionPack)

  fun loadCharacters(): List<Product> = queries.getProducts(ProductType.Character)

  fun loadMotions(): List<Product> = queries.getProducts(ProductType.Motion)

  fun setQuery(searchText: String?) {
    queryResult.setAll(queries.searchProduct(ProductType.Motion, searchText))
  }
}

data class OperationResult(
    var status: String,
    var message: String,
    var uuid: String,
    var jobResult: String?
)