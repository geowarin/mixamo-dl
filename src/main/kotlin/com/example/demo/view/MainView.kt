package com.example.demo.view

import com.example.demo.paths.getDataDir
import com.example.demo.paths.list
import com.example.demo.sql.Product
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import tornadofx.*

class MainView : View("Mixamo importer") {
  val productsController by inject<ProductsControllerSql>()

  private val selectedCharacter = SimpleObjectProperty<Product>()
  private val selectedMotion = SimpleObjectProperty<Product>()

  var paginator = DataGridPaginator(productsController.queryResult, itemsPerPage = 200)

  val packsDir = getDataDir().resolve("packs")
  val selectedPack = SimpleStringProperty()

  override val root = borderpane {

    top = vbox {
      var combobox: ComboBox<String>? = null
      hbox {
        val textfield = textfield("New Pack")
        button("Save").action {
          productsController.savePack(packsDir.resolve("${textfield.text}.json"))
          combobox?.asyncItems { listPacks() }
        }
        combobox = combobox {
          asyncItems { listPacks() }
          bindSelected(selectedPack)
        }

        selectedPack.onChange { packName ->
          if (packName != null) {
            productsController.loadPack(packsDir.resolve(packName))
            textfield.text = packName.removeSuffix(".json")
          }
        }
      }
      datagrid(selectedMotions) {
        maxRows = 1
        cellHeight = 75.0
        cellWidth = 75.0
        maxHeight = 90.0
        cellCache {
          productPreview(it)
        }
        bindSelected(selectedMotion)
      }
    }

    center = splitpane {

      setDividerPositions(0.20, 0.70, 1.0)
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

        val datagrid = datagrid(paginator.items) {
          cellHeight = 75.0
          cellWidth = 75.0

          cellCache {
            productPreview(it)
          }
          bindSelected(selectedMotion)
        }
        datagrid.onDoubleClick {
          productsController.addMotionToSelection(selectedMotion.get())
        }
        center = datagrid

        bottom = paginator
      }

      vbox {
        maxWidth = 240.0
        imageview(downloadImage(selectedMotion, 220.0, 260.0))
        text(selectedMotion.select { it.description.toProperty() })

        vbox {
          checkbox("In place") {
//            visibleWhen { it. }
          }
        }
      }
    }

    bottom = hbox {
      button("Download") {
        enableWhen { selectedMotions.sizeProperty.greaterThan(0).and(selectedCharacter.isNotNull) }
      }.action {
        runAsync {
          productsController.download(selectedPack.get().removeSuffix(".json"), selectedMotions, selectedCharacter.get())
        }
      }
    }
  }

  private fun listPacks() = packsDir.list().filter { it.toString().endsWith(".json") }.map { it.fileName.toString() }

  private val selectedMotions get() = productsController.selectedMotions

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

