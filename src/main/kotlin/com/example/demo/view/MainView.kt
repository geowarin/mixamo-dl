package com.example.demo.view

import com.example.demo.app.Styles.Companion.downloadButton
import com.example.demo.sql.Product
import javafx.beans.property.Property
import javafx.concurrent.Task
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import org.controlsfx.control.StatusBar
import org.controlsfx.glyphfont.FontAwesome.Glyph
import tornadofx.*
import tornadofx.controlsfx.glyph
import tornadofx.controlsfx.statusbar
import java.awt.Desktop

class MainView : View("Mixamo importer") {

  override val root = borderpane {
    setPrefSize(1500.0, 1000.0)

    top = find<MotionPackView>().root

    center = splitpane {
      setDividerPositions(0.20, 0.70, 1.0)

      add<CharacterView>()
      add<MotionsView>()
      add<ProductDetailsView>()
    }

    bottom = find<DownloadView>().root
  }
}

private fun EventTarget.faIcon(glyph: Glyph): org.controlsfx.glyphfont.Glyph {
  return glyph("FontAwesome", glyph)
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

class DownloadView : View() {
  private val productsController by inject<ProductsControllerSql>()
  lateinit var statusBar: StatusBar

  override val root = borderpane {
    center = hbox {
      alignment = Pos.CENTER
      padding = insets(10)
      button("Download") {
        enableWhen {
          selectedMotions.sizeProperty.greaterThan(0).and(selectedCharacter.isNotNull)
            .and(selectedPack.isNotNull)
        }
        addClass(downloadButton)
      }.action {
        val task = fireDownload()

        statusBar.textProperty().bind(task.messageProperty())
        statusBar.progressProperty().bind(task.progressProperty())

        Thread(task).start()
      }
    }

    statusBar = statusbar()

    bottom = statusBar
  }

  private fun fireDownload(): Task<DownloadResult> {
    val packName = selectedPack.value.removeSuffix(".json")
    val character = selectedCharacter.get()
    val task = productsController.downloadTask(
      packName,
      selectedMotions,
      character
    ).success { res ->
      statusBar.textProperty().unbind()
      statusBar.progressProperty().unbind()

      when (res.status) {
        DownloadStatus.SUCCESS ->
          addStatusButton(text = "Finished", res = res, buttonIcon = faIcon(Glyph.CHECK).color(Color.GREEN)) {
            Desktop.getDesktop().open(res.path?.parent?.toFile())
          }
        else ->
          addStatusButton(text = "Error", res = res, buttonIcon = faIcon(Glyph.EXCLAMATION_TRIANGLE).color(Color.RED)) {
            error(
              header = "Error while downloading ${character.name} - $packName : ${res.status}",
              content = res.operationResult.message
            )
          }
      }
    }
    return task
  }

  fun addStatusButton(
    text: String,
    buttonIcon: org.controlsfx.glyphfont.Glyph,
    res: DownloadResult,
    action: () -> Unit
  ) {
    val dlFinishedButton = button(text) {
      icon = buttonIcon
      tooltip(res.path.toString())
    }
    dlFinishedButton.action {
      action()
      statusBar.rightItems.remove(dlFinishedButton)
    }
    statusBar.rightItems.add(dlFinishedButton)
  }

  private val selectedMotions get() = productsController.selectedMotions
  private val selectedPack get() = productsController.selectedPack
  private val selectedCharacter get() = productsController.selectedCharacter
}

class ProductDetailsView : View() {
  private val productsController by inject<ProductsControllerSql>()

  override val root = vbox {
    maxWidth = 240.0

    imageview(downloadImage(selectedMotion, 220.0, 260.0))
    label(selectedMotion.select { it.name.toProperty() }) {
      style = "-fx-font-size: 20px"
    }
    label(selectedMotion.select { it.id.toProperty() }) {
      style = "-fx-font-size: 10px"
    }
    label(selectedMotion.select { it.description.toProperty() })
  }

  private val selectedMotion get() = productsController.selectedMotion
}

class MotionsView : View() {
  private val productsController by inject<ProductsControllerSql>()
  private var paginator = DataGridPaginator(productsController.queryResult, itemsPerPage = 200)

  override val root = borderpane {
    top = hbox(10, Pos.CENTER_LEFT) {
      borderpaneConstraints {
        margin = insets(10)
      }
      textfield().setOnKeyTyped { e ->
        paginator.currentPage = 1
        productsController.setQuery((e.source as TextField).text)
      }
      faIcon(Glyph.SEARCH)
    }

    val datagrid = datagrid(paginator.items) {
      cellHeight = 75.0
      cellWidth = 75.0

      cellCache {
        productPreview(it)
      }
      bindSelected(productsController.selectedMotion)
    }
    datagrid.onDoubleClick {
      productsController.addMotionToSelection(productsController.selectedMotion.get())
    }
    center = datagrid

    bottom = paginator
  }
}

class MotionPackView : View() {
  private val productsController by inject<ProductsControllerSql>()

  private val selectedMotions get() = productsController.selectedMotions
  private val selectedPack get() = productsController.selectedPack
  private val selectedMotion get() = productsController.selectedMotion

  override val root = vbox {
    hbox(10) {
      combobox<String> {
        items = productsController.packs
        bindSelectedBidirectional(selectedPack)
      }
      selectedPack.onChange { selectedPack ->
        productsController.loadPack(selectedPack!!)
      }
      button {
        icon = faIcon(Glyph.PLUS)
      }.action {
        productsController.createPack()
      }
      button {
        icon = faIcon(Glyph.REFRESH)
      }.action {
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
}

class CharacterView : View() {
  private val productsController by inject<ProductsControllerSql>()

  override val root = datagrid(productsController.loadCharacters()) {
    cellHeight = 75.0
    cellWidth = 75.0

    cellCache {
      productPreview(it)
    }
    bindSelected(productsController.selectedCharacter)
  }
}

private fun EventTarget.productPreview(product: Product): StackPane {
  return stackpane {
    imageview(downloadImage(product, 70.0, 70.0)) {
      fitWidth = 70.0
      fitHeight = 70.0
    }
    label(product.name) {
      textFill = Color.WHITE
      isWrapText = true
      style {
        fontScale = 5.0 / product.name.length
      }
    }
  }
}