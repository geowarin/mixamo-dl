package com.example.demo.view

import com.example.demo.sql.Product
import javafx.beans.binding.Bindings
import javafx.beans.property.*
import javafx.scene.paint.Color
import tornadofx.*
import javax.json.JsonObject
import tornadofx.getValue
import tornadofx.setValue

class MainView : View("Mixamo importer") {
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

            datagrid(productsController.loadMotions()) {
                cellHeight = 75.0
                cellWidth = 75.0
                multiSelect = true

                cellCache {
                    stackpane {
                        imageview(it.thumbnail_animated) {
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