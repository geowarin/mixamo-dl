package com.example.demo

import java.net.URL
import java.nio.file.Files
import java.nio.file.Path

fun downloadFile(url: String, path: Path) {
  Files.createDirectories(path.parent)
  URL(url).openStream().buffered().use { urlStream ->
    Files.newOutputStream(path).buffered().use { out ->
      urlStream.copyTo(out)
    }
  }
}
