package com.example.demo.view

import com.example.demo.downloadFile
import com.example.demo.sql.Product
import javafx.beans.property.ObjectProperty
import javafx.beans.value.ObservableValue
import javafx.scene.image.Image
import tornadofx.*
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

val pool: ExecutorService = Executors.newCachedThreadPool()

fun downloadImage(product: Product, width: Double, height: Double): ObservableValue<Image?> {
  val image = objectProperty<Image>()

  dl(product, image, width, height)
  return image
}

fun downloadImage(productObservable: ObservableValue<Product?>, width: Double, height: Double): ObservableValue<Image?> {
  val image = objectProperty<Image>()

  productObservable.onChange { product ->
    if (product == null) {
      return@onChange
    }

    val url = product.thumbnail_animated
    val cacheKey = "${product.id}.${File(url).extension}"
    val cachedImage = File("./imageCache/${product.type}/$cacheKey")
    pool.execute {
      if (!cachedImage.exists() || cachedImage.totalSpace == 0L)
        downloadFile(url, cachedImage)
      image.set(Image(cachedImage.toURI().toURL().toString(), width, height, true, true, true))
    }
  }

  return image
}

private fun dl(product: Product, image: ObjectProperty<Image>, width: Double, height: Double) {
  val url = product.thumbnail_animated
  val cacheKey = "${product.id}.${File(url).extension}"
  val cachedImage = File("./imageCache/${product.type}/$cacheKey")

  pool.execute {
    if (!cachedImage.exists() || cachedImage.totalSpace == 0L)
      downloadFile(url, cachedImage)
    image.set(Image(cachedImage.toURI().toURL().toString(), width, height, true, true, true))
  }
}