package com.example.demo

import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path

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

fun downloadFile(url: String, path: Path) {
  Files.createDirectories(path.parent)
  URL(url).openStream().use { urlStream ->
    Files.newOutputStream(path).use { out ->
      urlStream.copyTo(out)
    }
  }
}
