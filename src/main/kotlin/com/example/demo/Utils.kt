package com.example.demo

import java.io.File
import java.net.URL

fun downloadFile(url: String, fileName: String) {
  downloadFile(url, File(fileName))
}

fun downloadFile(url: String, file: File) {
  file.parentFile.mkdirs()
  URL(url).openStream().use { urlStream ->
    file.outputStream().use { out ->
      urlStream.copyTo(out)
    }
  }
}
