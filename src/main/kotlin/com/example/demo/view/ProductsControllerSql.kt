package com.example.demo.view

import com.example.demo.downloadFile
import com.example.demo.json.mapObj
import com.example.demo.json.string
import com.example.demo.jwt.readToken
import com.example.demo.paths.exists
import com.example.demo.paths.getDataDir
import com.example.demo.paths.list
import com.example.demo.paths.readText
import com.example.demo.sql.MotionDetails
import com.example.demo.sql.Product
import com.example.demo.sql.ProductType
import com.example.demo.sql.Queries
import com.nfeld.jsonpathlite.extension.read
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.TextInputDialog
import org.json.JSONArray
import org.json.JSONObject
import tornadofx.*
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files

val baseURI = "https://www.mixamo.com/api/v1"
fun headers() = mapOf(
    "Authorization" to "Bearer ${readToken()}",
    "X-Api-Key" to "mixamo2"
)

class ProductsControllerSql(fs: FileSystem = FileSystems.getDefault()) : Controller() {
  val queries: Queries = Queries()
  var queryResult = observableListOf<Product>()

  val selectedMotions = observableListOf<Product>()

  init {
    setQuery("")
  }

  fun download(
    packName: String,
    selectedMotions: List<Product>,
    character: Product
  ) {
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
    writeFile(monitorResult.jobResult!!, "${character.name}_${packName}.zip")
  }

  private fun writeFile(jobResult: String, fileName: String) {
    println("Writing file $fileName...")
    downloadFile(
      url = jobResult,
      path = getDataDir().resolve("downloads").resolve(fileName)
    )
    println("Done!")
  }

  private fun monitor(exportResultUuid: String): OperationResult {
    val resultObj = khttp.get(
        url = "${baseURI}/characters/${exportResultUuid}/monitor",
        headers = headers()
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
        headers = headers(),
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

  fun clearSelectedMotions() {
    selectedMotions.clear()
  }

  fun addMotionToSelection(motion: Product?) {
    if (motion != null && !selectedMotions.contains(motion)) {
      selectedMotions.add(motion)
    }
  }

  val packsDir = getDataDir(fs).resolve("packs")

  val packs = observableListOf(listPacks())
  val selectedPack = SimpleStringProperty()

  fun writePackToDisk(packName: String) {
    val savePath = packsDir.resolve(packName)
    val motionsArray = selectedMotions.map { JSONObject().put("id", it.id) }
    val pack = JSONObject()
        .put("motions", motionsArray)

    Files.createDirectories(savePath.parent)
    Files.write(savePath, pack.toString().toByteArray())
  }

  fun loadPack(packName: String) {
    val path = packsDir.resolve(packName)
    val motionsJsonArray = JSONObject(path.readText()).getJSONArray("motions")
    val ids = motionsJsonArray.mapObj { it.string("id") }
    selectedMotions.setAll(queries.getProducts(ids))
  }

  fun refreshPacks() {
    packs.setAll(listPacks())
  }

  fun createPack() {
    TextInputDialog().showAndWait().ifPresent { text ->
      clearSelectedMotions()
      val savePath = packsDir.resolve("${text}.json")
      if (savePath.exists()) {
        confirm("Overwrite $savePath ?") {
          savePackAndRefresh("${text}.json")
        }
      } else {
        savePackAndRefresh("${text}.json")
      }
    }
  }

  fun savePackAndRefresh(packName: String) {
    writePackToDisk(packName)
    selectedPack.value = packName
    refreshPacks()
  }

  private fun listPacks() = packsDir.list().filter { it.toString().endsWith(".json") }.map { it.fileName.toString() }
}

data class OperationResult(
    var status: String,
    var message: String,
    var uuid: String,
    var jobResult: String?
)