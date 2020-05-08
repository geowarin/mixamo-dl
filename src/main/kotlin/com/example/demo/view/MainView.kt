package com.example.demo.view

import com.example.demo.sql.Product
import javafx.beans.binding.Bindings
import javafx.beans.property.*
import javafx.scene.paint.Color
import tornadofx.*
import javax.json.JsonObject
import tornadofx.getValue
import tornadofx.setValue

class MainView : View("Hello TornadoFX") {
    val productsController by inject<ProductsControllerSql>()

    private val selectedCharacter = SimpleObjectProperty<Product>()
    private val selectedMotionPacks = observableListOf<Product>()

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


class MotionJson : JsonModel {
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

class OperationResultJson : JsonModel {
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

class ProductJson : JsonModel {
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
