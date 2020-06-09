package com.example.demo.app

import javafx.scene.paint.Color
import tornadofx.Stylesheet
import tornadofx.cssclass
import tornadofx.px

class Styles : Stylesheet() {
  companion object {
    val downloadButton by cssclass()
  }

  init {
    root {
      fontFamily = "Verdana"
    }

    downloadButton {

      backgroundColor += Color.BLUE
      fontSize = 20.px
      textFill = Color.WHITE

      and(hover) {
        backgroundColor += Color.DARKBLUE
      }
    }
  }
}