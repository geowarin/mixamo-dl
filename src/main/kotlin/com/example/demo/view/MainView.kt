package com.example.demo.view

import com.example.demo.sql.Product
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.TextField
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import tornadofx.*

class MainView : View("Mixamo importer") {
  val productsController by inject<ProductsControllerSql>()

  private val selectedCharacter = SimpleObjectProperty<Product>()
  private val selectedMotion = SimpleObjectProperty<Product>()

  private val selectedMotions = observableListOf<Product>()

  var paginator = DataGridPaginator(productsController.queryResult, itemsPerPage = 200)

  override val root = borderpane {

    top = datagrid(selectedMotions) {
      maxRows = 1
      cellHeight = 75.0
      cellWidth = 75.0
      maxHeight = 90.0
      cellCache {
        productPreview(it)
      }
      bindSelected(selectedMotion)
    }

    center = splitpane {

      datagrid(productsController.loadCharacters()) {
        cellHeight = 75.0
        cellWidth = 75.0

        cellCache {
          productPreview(it)
        }
        bindSelected(selectedCharacter)
      }

      borderpane {
        top = hbox {
          textfield().setOnKeyTyped { e ->
            paginator.currentPage = 1
            productsController.setQuery((e.source as TextField).text)
          }
        }
        center = hbox {
          datagrid(paginator.items) {
            cellHeight = 75.0
            cellWidth = 75.0

            cellCache {
              productPreview(it)
            }
            bindSelected(selectedMotion)
          }.onDoubleClick {
            val element = selectedMotion.get()
            if (element != null && !selectedMotions.contains(element)) {
              selectedMotions.add(element)
            }
          }
        }

        bottom = paginator
      }

      vbox {
        imageview(downloadImage(selectedMotion, 220.0, 260.0))
        text(selectedMotion.select { it.description.toProperty() })
      }
    }

    bottom = hbox {
      button("Download") {
        enableWhen { selectedMotions.sizeProperty.greaterThan(0).and(selectedCharacter.isNotNull) }
      }.action {
        runAsync {
          productsController.download(selectedMotions, selectedCharacter.get())
        }
      }
    }
  }

  private fun productPreview(product: Product): StackPane {
    return stackpane {
      imageview(downloadImage(product, 70.0, 70.0)) {
        fitWidth = 70.0
        fitHeight = 70.0
      }
      label(product.name) {
        textFill = Color.WHITE
        isWrapText = true
      }
    }
  }
}