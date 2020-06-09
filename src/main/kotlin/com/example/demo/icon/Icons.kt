package com.example.demo.icon

import com.pepperonas.fxiconics.FxIconics
import com.pepperonas.fxiconics.awf.FxFontAwesome
import com.pepperonas.fxiconics.base.FxFontBase
import com.pepperonas.fxiconics.cmd.FxFontCommunity
import com.pepperonas.fxiconics.gmd.FxFontGoogleMaterial
import com.pepperonas.fxiconics.helper.ColorConverter
import com.pepperonas.fxiconics.met.FxFontMeteoconcs
import com.pepperonas.fxiconics.oct.FxFontOcticons
import javafx.event.EventTarget
import javafx.scene.control.Button
import javafx.scene.control.ContentDisplay
import javafx.scene.paint.Color
import javafx.scene.text.Text
import tornadofx.attachTo
import tornadofx.button

const val DEFAULT_SIZE = 13
val DEFAULT_COLOR: Color = Color.BLACK
val DEFAULT_CONTENT_DISPLAY: ContentDisplay = ContentDisplay.RIGHT

private fun EventTarget.iconButton(
  icon: String,
  collection: FxFontBase,
  textSize: Int = DEFAULT_SIZE,
  color: Color = DEFAULT_COLOR,
  contentDisplay: ContentDisplay = DEFAULT_CONTENT_DISPLAY,
  text: String = "",
  op: Button.() -> Unit = {}
) = Button(text).attachTo(this, op) {
  it.font = when (collection) {
    is FxFontGoogleMaterial -> FxIconics.getGoogleMaterialFont(
      textSize
    )
    is FxFontCommunity -> FxIconics.getCommunityMaterialFont(
      textSize
    )
    is FxFontAwesome -> FxIconics.getAwesomeFont(
      textSize
    )
    is FxFontOcticons -> FxIconics.getOcticonsFont(
      textSize
    )
    is FxFontMeteoconcs -> FxIconics.getMeteoconsFont(
      textSize
    )
    else -> FxIconics.getGoogleMaterialFont(textSize)
  }

  it.text = icon
  if (text.isNotEmpty()) {
    val textCompo = Text(text)
    textCompo.style = "-fx-font-size: $textSize"
    it.text = icon
    it.graphic = textCompo
    it.contentDisplay = contentDisplay
  }

  it.style = "-fx-text-fill: " + ColorConverter.toRgbString(color) + ""
}

fun EventTarget.iconButton(
  icon: FxFontGoogleMaterial.Icons,
  textSize: Int = DEFAULT_SIZE,
  color: Color = DEFAULT_COLOR,
  contentDisplay: ContentDisplay = DEFAULT_CONTENT_DISPLAY,
  text: String = "",
  op: Button.() -> Unit = {}
) =
  iconButton(icon.toString(),
    FxFontGoogleMaterial(), textSize, color, contentDisplay, text, op)

fun EventTarget.iconButton(
  icon: FxFontCommunity.Icons,
  textSize: Int = DEFAULT_SIZE,
  color: Color = DEFAULT_COLOR,
  contentDisplay: ContentDisplay = DEFAULT_CONTENT_DISPLAY,
  text: String = "",
  op: Button.() -> Unit = {}
) =
  iconButton(icon.toString(),
    FxFontCommunity(), textSize, color, contentDisplay, text, op)

fun EventTarget.iconButton(
  icon: FxFontAwesome.Icons,
  textSize: Int = DEFAULT_SIZE,
  color: Color = DEFAULT_COLOR,
  contentDisplay: ContentDisplay = DEFAULT_CONTENT_DISPLAY,
  text: String = "",
  op: Button.() -> Unit = {}
) =
  iconButton(icon.toString(),
    FxFontAwesome(), textSize, color, contentDisplay, text, op)

fun EventTarget.iconButton(
  icon: FxFontOcticons.Icons,
  textSize: Int = DEFAULT_SIZE,
  color: Color = DEFAULT_COLOR,
  contentDisplay: ContentDisplay = DEFAULT_CONTENT_DISPLAY,
  text: String = "",
  op: Button.() -> Unit = {}
) =
  iconButton(icon.toString(),
    FxFontOcticons(), textSize, color, contentDisplay, text, op)

fun EventTarget.iconButton(
  icon: FxFontMeteoconcs.Icons,
  textSize: Int = DEFAULT_SIZE,
  color: Color = DEFAULT_COLOR,
  contentDisplay: ContentDisplay = DEFAULT_CONTENT_DISPLAY,
  text: String = "",
  op: Button.() -> Unit = {}
) =
  iconButton(icon.toString(),
    FxFontMeteoconcs(), textSize, color, contentDisplay, text, op)