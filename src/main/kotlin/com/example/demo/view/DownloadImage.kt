package com.example.demo.view

import com.example.demo.downloadFile
import com.example.demo.sql.Product
import javafx.beans.value.ObservableValue
import javafx.scene.image.Image
import tornadofx.*
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

val pool: ExecutorService = Executors.newCachedThreadPool()

fun downloadImage(product: Product): ObservableValue<Image?> {
  val image = objectProperty<Image>()

  val url = product.thumbnail_animated
  val cacheKey = "${product.id}.${File(url).extension}"
  val cachedImage = File("./imageCache/${product.type}/$cacheKey")

  pool.execute {
    if (!cachedImage.exists())
      downloadFile(url, cachedImage)
    image.set(Image(cachedImage.toURI().toURL().toString(), 75.0, 75.0, true, true, true))
  }
  return image
}