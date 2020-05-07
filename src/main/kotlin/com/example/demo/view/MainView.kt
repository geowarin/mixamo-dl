package com.example.demo.view

import javafx.beans.binding.Bindings
import javafx.beans.property.*
import javafx.collections.ObservableList
import javafx.scene.paint.Color
import tornadofx.*
import java.io.File
import java.net.URL
import javax.json.Json
import javax.json.JsonObject
import tornadofx.getValue
import tornadofx.setValue

class MainView : View("Hello TornadoFX") {
    val productsController by inject<ProductsController>()

    private val selectedCharacter = SimpleObjectProperty<Product>()
    private val selectedMotionPacks = observableList<Product>()

    override val root = vbox {
        hbox {
            datagrid(productsController.loadCharacters()) {
                cellHeight = 75.0
                cellWidth = 75.0

                cellCache {
                    stackpane {
                        imageview(it.thumbnail) {
                            fitWidth = 70.0
                            fitHeight = 70.0
                        }
                        label(it.name) {
                            textFill = Color.WHITE
                            isWrapText = true
                        }
                    }
                }
                bindSelected(selectedCharacter)
            }

            datagrid(productsController.loadMotionPacks()) {
                cellHeight = 75.0
                cellWidth = 75.0
                multiSelect = true

                cellCache {
                    stackpane {
                        imageview(it.thumbnail) {
                            fitWidth = 70.0
                            fitHeight = 70.0
                        }
                        label(it.name) {
                            textFill = Color.WHITE
                            isWrapText = true
                        }
                    }
                }
                Bindings.bindContent(selectedMotionPacks, selectionModel.selectedItems)
            }
        }

        button("Download").action {
            runAsync {
                productsController.download(selectedMotionPacks, selectedCharacter.get())
            }
        }
    }
}

class ProductsController : Controller() {
    val api: Rest by inject()

    fun download(motionPacks: List<Product>, character: Product) {
        val motions = motionPacks.flatMap { getMotions(it.id) }
        val exportResult = export(motions, character.id)
        if (exportResult.status == "failed") {
            throw IllegalStateException("job failed")
        }

        var monitorResult: OperationResult
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

    private fun monitor(exportResultUuid: String): OperationResult {
        return api.get("characters/${exportResultUuid}/monitor").one().toModel()
    }

    private fun getMotions(motionPackId: String): List<Motion> {
        return api.get("products/${motionPackId}${params.queryString}").one()
                .jsonObject("details")!!
                .getJsonArray("motions")
                .toModel()
    }

    fun export(hashes: List<Motion>, characterId: String): OperationResult {
        val exportParameters = toExportParameters(hashes, characterId)
        return api.post("animations/export", exportParameters).one().toModel()
    }

    private fun toExportParameters(hashes: List<Motion>, characterId: String): JsonObject {
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

    fun loadMotionPacks(): ObservableList<Product> {
        val params = mapOf(
                "page" to 1,
                "limit" to 100,
                "type" to "MotionPack"
        )
        return api.get("products${params.queryString}").one().jsonArray("results")!!.toModel()
    }

    fun loadCharacters(): ObservableList<Product> {
        val params = mapOf(
                "page" to 1,
                "limit" to 100,
                "type" to "Character"
        )
        return api.get("products${params.queryString}").one().jsonArray("results")!!.toModel()
    }
}


class Motion : JsonModel {
//    val supports_inplaceProperty = SimpleBooleanProperty()
//    var supports_inplace by supports_inplaceProperty
//
//    val loopableProperty = SimpleBooleanProperty()
//    var loopable by loopableProperty
//
//    val default_frame_lengthProperty = SimpleIntegerProperty()
//    var default_frame_length by default_frame_lengthProperty
//
//    val durationProperty = SimpleDoubleProperty()
//    var duration by durationProperty

    val nameProperty = SimpleStringProperty()
    var name by nameProperty

    val gms_hashProperty = SimpleObjectProperty<JsonObject>()
    var gms_hash by gms_hashProperty

    override fun updateModel(json: JsonObject) {
        with(json) {
//            supports_inplace = boolean("supports_inplace")
//            loopable = boolean("loopable")
//            default_frame_length = int("default_frame_length")
//            duration = double("duration")
            name = string("name")
            gms_hash = jsonObject("gms_hash")
        }
    }

}

class OperationResult : JsonModel {
    val statusProperty = SimpleStringProperty()
    var status by statusProperty

    val messageProperty = SimpleStringProperty()
    var message by messageProperty

    val uuidProperty = SimpleStringProperty()
    var uuid by uuidProperty

    val jobResultProperty = SimpleStringProperty()
    var jobResult by jobResultProperty

    override fun updateModel(json: JsonObject) {
        with(json) {
            status = string("status")
            message = string("message")
            uuid = string("uuid")
            jobResult = string("job_result")
        }
    }
}

class Product : JsonModel {
    val nameProperty = SimpleStringProperty()
    var name by nameProperty

    val thumbnailProperty = SimpleStringProperty()
    var thumbnail by thumbnailProperty

    val idProperty = SimpleStringProperty()
    var id by idProperty

    override fun updateModel(json: JsonObject) {
        with(json) {
            id = string("id")
            name = string("name")
            thumbnail = string("thumbnail")
        }
    }
}
