package com.example.demo.view

import com.example.demo.downloadFile
import com.example.demo.json.string
import com.example.demo.sql.MotionDetails
import com.example.demo.sql.Product
import com.example.demo.sql.ProductType
import com.example.demo.sql.Queries
import com.nfeld.jsonpathlite.extension.read
import org.json.JSONArray
import org.json.JSONObject
import tornadofx.*
import java.nio.file.Files
import java.nio.file.Path

val baseURI = "https://www.mixamo.com/api/v1"
val token = """
eyJ4NXUiOiJpbXNfbmExLWtleS0xLmNlciIsImFsZyI6IlJTMjU2In0.eyJpZCI6IjE1ODkxMTI3MjEyNTJfZjcwMGE2NzMtNTQ4My00MGEyLTkwZGUtM2Q5OGY0OTdjZDEzX3VlMSIsImNsaWVudF9pZCI6Im1peGFtbzEiLCJ1c2VyX2lkIjoiQTY1QjQ1MjY1QzAzMDY0QzBBNDk1Q0NGQEFkb2JlSUQiLCJzdGF0ZSI6IiIsInR5cGUiOiJhY2Nlc3NfdG9rZW4iLCJhcyI6Imltcy1uYTEiLCJmZyI6IlVOUUxYWFZRWFBPNTU3NktDNllMUVBRQUpZPT09PT09Iiwic2lkIjoiMTU4OTExMjcxOTYzOV9mZjFiZTRlYy05NDQzLTQwNTYtOGNmYy00OTUyNGFhNDRhMzVfdWUxIiwicnRpZCI6IjE1ODkxMTI3MjEyNTJfZTYwZTgwMzItNzI3ZS00Nzk4LThkYTEtMjVhMWU3NmQ5OTY4X3VlMSIsIm9jIjoicmVuZ2EqbmExcioxNzFmZTdmYzdkOSpHWEo0NkY4MDhYMktIQlNRQ0FXQjNHOUdUNCIsInJ0ZWEiOiIxNTkwMzIyMzIxMjUyIiwibW9pIjoiNjUyMzdiZWMiLCJjIjoiMHVLMkxTbVFSZmJNZFRuTUlPT2JDdz09IiwiZXhwaXJlc19pbiI6Ijg2NDAwMDAwIiwic2NvcGUiOiJvcGVuaWQsQWRvYmVJRCxmZXRjaF9zYW8sc2FvLmNyZWF0aXZlX2Nsb3VkIiwiY3JlYXRlZF9hdCI6IjE1ODkxMTI3MjEyNTIifQ.RjfqgQboXAVH72BZ_hC7Udqc-IOR8gTnMaKp67imrCrIkuWBeVEwXYr-fi9uTe9ricmK2tuaVW1KY_v3t-uYHAU_BFi2qCpzZ4mmqkIvEnWDhmJRxUGH6lVgnXj_lD38m1SkGEWaBAgRKVUTwR8LBgDUK-j7SoC3zsK8j32BZTuW9yh65yMIbgnenhbg-pxyg0ODf6y8TTU56B0gFN_bn14F9LivugFiaXwJTbmRrEKHtzeJEY1b9HAAiBJbL7sKFH4Ltn3g6obc-W2m5RJAwribdD4rZj1XWy03fhxMDHrX7fBy3qLJwxhpvYee6NtSEk-JMX5o_1cYEeMdiiXx5A
""".trim()
val headers = mapOf(
    "Authorization" to "Bearer $token",
    "X-Api-Key" to "mixamo2"
)

class ProductsControllerSql : Controller() {
  val queries: Queries = Queries()
  var queryResult = observableListOf<Product>()

  val selectedMotions = observableListOf<Product>()

  init {
    setQuery("")
  }

  fun download(selectedMotions: List<Product>, character: Product) {
    val motions: List<MotionDetails> = selectedMotions.flatMap { it.motions }
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
    writeFile(monitorResult.jobResult!!, "downloads/${character.name}_custom.zip")
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

  private fun export(motions: List<MotionDetails>, characterId: String): OperationResult {
    val payload = combineExportParameters(motions, characterId)
    val resultObj = khttp.post(
        url = "${baseURI}/animations/export",
        headers = headers,
        json = payload
    ).jsonObject
    return operationResult(resultObj)
  }

  fun combineExportParameters(motions: List<MotionDetails>, characterId: String): JSONObject {
    val motionOptionsArray: List<JSONObject> = motions.map(this::toExportParameters)
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

  fun toExportParameters(motionDetails: MotionDetails): JSONObject {
    return motionDetails.gms_hash
        .put("name", motionDetails.name)
        // "params": [["Posture", 1.0], ["Step Width", 1.0], ["Overdrive", 0.0]] => 1.0,1.0,1.0
        .put("params", motionDetails.gms_hash.read<JSONArray>("$.params")!!
            .joinToString { (it as JSONArray).getDouble(1).toString() })
  }

  fun loadCharacters(): List<Product> = queries.getProducts(ProductType.Character)

  fun setQuery(searchText: String?) {
    queryResult.setAll(queries.searchProduct(ProductType.Motion, searchText))
  }

  fun addMotionToSelection(motion: Product?) {
    if (motion != null && !selectedMotions.contains(motion)) {
      selectedMotions.add(motion)
    }
  }

  fun savePack(path: Path) {
    val motionsArray = selectedMotions.map { JSONObject().put("id", it.id) }
    val pack = JSONObject()
        .put("motions", motionsArray)

    Files.createDirectories(path.parent)
    Files.write(path, pack.toString().toByteArray())
  }

}

data class OperationResult(
    var status: String,
    var message: String,
    var uuid: String,
    var jobResult: String?
)