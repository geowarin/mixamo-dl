package com.example.demo.view

import javafx.collections.ObservableList
import tornadofx.*
import java.io.File
import java.net.URL
import javax.json.Json
import javax.json.JsonObject

class ProductsController : Controller() {
    val api: Rest by inject()

    fun download(motionPacks: List<ProductJson>, character: ProductJson) {
        val motions = motionPacks.flatMap { getMotions(it.id) }
        val exportResult = export(motions, character.id)
        if (exportResult.status == "failed") {
            throw IllegalStateException("job failed")
        }

        var monitorResult: OperationResultJson
        do {
            println("Waiting for pack...")
            monitorResult = monitor(exportResult.uuid)
            Thread.sleep(5000)
        } while (monitorResult.status == "processing")

        if (monitorResult.status == "failed") {
            throw IllegalStateException("job failed")
        }
        val motionPackName = motionPacks.joinToString { it.name }
        writeFile(monitorResult.jobResult, "downloads/${character.name}_${motionPackName}.zip")
    }

    private fun writeFile(jobResult: String, fileName: String) {
        println("Writing file $fileName...")
        URL(jobResult).openStream().use { urlStream ->
            File(fileName).outputStream().use { out ->
                urlStream.copyTo(out)
            }
        }
        println("Done!")
    }

    private fun monitor(exportResultUuid: String): OperationResultJson {
        return api.get("characters/${exportResultUuid}/monitor").one().toModel()
    }

    private fun getMotions(motionPackId: String): List<MotionJson> {
        return api.get("products/${motionPackId}${params.queryString}").one()
                .jsonObject("details")!!
                .getJsonArray("motions")
                .toModel()
    }

    fun export(hashes: List<MotionJson>, characterId: String): OperationResultJson {
        val exportParameters = toExportParameters(hashes, characterId)
        return api.post("animations/export", exportParameters).one().toModel()
    }

    private fun toExportParameters(hashes: List<MotionJson>, characterId: String): JsonObject {
        var motionOptionsArray = Json.createArrayBuilder()
        hashes.forEach { motion ->
            val motionOptions = Json.createObjectBuilder(motion.gms_hash)
                    .add("name", motion.name)
                    // "params": [["Posture", 1.0], ["Step Width", 1.0], ["Overdrive", 0.0]] => 1.0,1.0,1.0
                    .add("params", motion.gms_hash.jsonArray("params")?.joinToString { it.asJsonArray()[1].toString() } ?: "0")

            motionOptionsArray = motionOptionsArray.add(motionOptions)
        }
        val objectBuilder = Json.createObjectBuilder()
                .add("gms_hash", motionOptionsArray)
                .add("preferences",
                        Json.createObjectBuilder()
                                .add("format", "fbx7_unity")
                                .add("mesh_motionpack", "t-pose")
                                .add("fps", "30")
                                .add("reducekf", "0")
                )
                .add("character_id", characterId)
                .add("type", "MotionPack")
                .add("product_name", "Locomotion Pack")
        return objectBuilder.build()
    }

    fun loadMotionPacks(): ObservableList<ProductJson> {
        val params = mapOf(
                "page" to 1,
                "limit" to 100,
                "type" to "MotionPack"
        )
        return api.get("products${params.queryString}").one().jsonArray("results")!!.toModel()
    }

    fun loadCharacters(): ObservableList<ProductJson> {
        val params = mapOf(
                "page" to 1,
                "limit" to 100,
                "type" to "Character"
        )
        return api.get("products${params.queryString}").one().jsonArray("results")!!.toModel()
    }
}