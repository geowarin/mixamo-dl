package com.example.demo.view

import com.example.demo.sql.Product
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.TextField
import javafx.scene.paint.Color
import tornadofx.*

class MainView : View("Mixamo importer") {
  val productsController by inject<ProductsControllerSql>()

  private val selectedCharacter = SimpleObjectProperty<Product>()
  private val selectedMotionPacks = observableListOf<Product>()

  var paginator = DataGridPaginator(productsController.queryResult, itemsPerPage = 200)

  override val root = vbox {
    hbox {
      datagrid(productsController.loadCharacters()) {
        cellHeight = 75.0
        cellWidth = 75.0

        cellCache {
          stackpane {
            imageview(downloadImage(it)) {
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

      vbox {
        textfield().setOnKeyTyped { e ->
          paginator.currentPage = 1
          productsController.setQuery((e.source as TextField).text)
        }
        datagrid(paginator.items) {
          cellHeight = 75.0
          cellWidth = 75.0
          multiSelect = true

          cellCache {
            stackpane {
              imageview(downloadImage(it)) {
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
        add(paginator)
      }
    }

    button("Download").action {
      runAsync {
        productsController.download(selectedMotionPacks, selectedCharacter.get())
      }
    }
  }
}