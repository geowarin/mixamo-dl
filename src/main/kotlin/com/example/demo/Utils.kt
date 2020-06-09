package com.example.demo

import java.net.URL
import java.nio.file.Files
import java.nio.file.Path

fun downloadFile(url: String, path: Path) {
  Files.createDirectories(path.parent)
  URL(url).openStream().use { urlStream ->
    Files.newOutputStream(path).use { out ->
      urlStream.copyTo(out)
    }
  }
}
