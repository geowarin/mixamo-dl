package com.example.demo.view

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

    private val selectedCharacter: ObjectProperty<Product> = SimpleObjectProperty()
    private val selectedMotionPack: ObjectProperty<Product> = SimpleObjectProperty()


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
                        }
                    }
                }
                bindSelected(selectedCharacter)
            }

            datagrid(productsController.loadMotionPacks()) {
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
                        }
                    }
                }
                bindSelected(selectedMotionPack)
            }
        }

        button("Download").action {
            runAsync {
                productsController.download(selectedMotionPack.get(), selectedCharacter.get())
            }
        }
    }
}

class ProductsController : Controller() {
    val api: Rest by inject()

    fun download(motionPack: Product, character: Product) {
        val motions = getGmsHashes(motionPack.id, character.id)
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
        writeFile(monitorResult.jobResult, motionPack, character)
    }

    private fun writeFile(jobResult: String, motionPack: Product, character: Product) {
        println("Writing file")
        URL(jobResult).openStream().use { urlStream ->
            File("${character.name}_${motionPack.name}.zip").outputStream().use { out ->
                urlStream.copyTo(out)
            }
        }
    }

    private fun monitor(exportResultUuid: String): OperationResult {
        return api.get("characters/${exportResultUuid}/monitor").one().toModel()
    }

    private fun getGmsHashes(motionPackId: String, characterId: String): List<Motion> {
        val params = mapOf(
                "character_id" to characterId
        )
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
        hashes.forEach {
            val motionOptions = Json.createObjectBuilder(it.gms_hash)
                    .add("name", it.name)
                    .add("params", "0")
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
