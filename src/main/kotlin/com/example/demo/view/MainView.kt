package com.example.demo.view

import com.example.demo.icon.iconButton
import com.example.demo.sql.Product
import com.pepperonas.fxiconics.gmd.FxFontGoogleMaterial
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import tornadofx.*

class MainView : View("Mixamo importer") {
  private val productsController by inject<ProductsControllerSql>()

  private val selectedCharacter = SimpleObjectProperty<Product>()
  private val selectedMotion = SimpleObjectProperty<Product>()

  private var paginator = DataGridPaginator(productsController.queryResult, itemsPerPage = 200)

  override val root = borderpane {

    top = vbox {
      hbox {
        combobox<String> {
          items = productsController.packs
          bindSelectedBidirectional(selectedPack)
        }
        selectedPack.onChange { selectedPack ->
          productsController.loadPack(selectedPack!!)
        }
        iconButton(FxFontGoogleMaterial.Icons.gmd_add).action {
          productsController.createPack()
        }
        iconButton(FxFontGoogleMaterial.Icons.gmd_autorenew).action {
          productsController.refreshPacks()
        }
        button("Save")
          .enableWhen { selectedPack.isNotNull }
          .action {
            productsController.savePackAndRefresh(selectedPack.get())
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
        text(selectedMotion.select { it.name.toProperty() }) {
          style = "-fx-font-size: 20px"
        }
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
        enableWhen {
          selectedMotions.sizeProperty.greaterThan(0).and(selectedCharacter.isNotNull)
            .and(selectedPack.isNotNull)
        }
      }.action {
        runAsync {
          productsController.download(
            selectedPack.value.removeSuffix(".json"),
            selectedMotions,
            selectedCharacter.get()
          )
        }
      }
    }
  }

  private val selectedMotions get() = productsController.selectedMotions
  private val selectedPack get() = productsController.selectedPack

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

private fun <T> ComboBox<T>.bindSelectedBidirectional(selectionProp: Property<T>) {
  selectionModel.selectedItemProperty().onChange { value ->
    if (value != null) { // By avoiding null this does not trigger on refresh
      selectionProp.value = value
    }
  }
  selectionProp.onChange {
    selectionModel.select(it)
  }
}

