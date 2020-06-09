package com.example.demo.paths

import java.io.RandomAccessFile
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

fun isAppRunning(): Boolean {
  val file = getCacheDir().resolve("lockFile")
  try {
    val randomAccessFile = RandomAccessFile(file.toFile(), "rw")
    val fileLock = randomAccessFile.channel.tryLock()
    if (fileLock != null) {
      Runtime.getRuntime().addShutdownHook(Thread {
        try {
          fileLock.release()
          randomAccessFile.close()
          file.delete()
        } catch (e: Exception) {
          System.err.println("Unable to remove lock file: $file - ${e.message}")
        }
      })
      return false
    }
  } catch (e: Exception) {
    System.err.println("Unable to create and/or lock file: $file - ${e.message}")
  }
  return true
}

fun Path.readText() = Files.newBufferedReader(this).readText()
fun Path.toURL(): Any = this.toUri().toURL()
fun Path.exists() = Files.exists(this)
val Path.totalSpace: Long
  get() = Files.getFileStore(this).totalSpace

private fun Path.delete() = Files.delete(this)

fun getCacheDir(fs: FileSystem = FileSystems.getDefault()): Path {
  return getEnvPath(fs, "XDG_CACHE_HOME")?.resolve("mixamo-dl")
    ?: fs.getPath(System.getProperty("user.home"), "mixamo-dl", "cache")
}

fun getConfigDir(fs: FileSystem = FileSystems.getDefault()): Path {
  return getEnvPath(fs, "XDG_CONFIG_HOME")?.resolve("mixamo-dl")
    ?: fs.getPath(System.getProperty("user.home"), "mixamo-dl", "config")
}

fun getDataDir(fs: FileSystem = FileSystems.getDefault()): Path {
  return getEnvPath(fs, "XDG_DATA_HOME")?.resolve("mixamo-dl")
    ?: fs.getPath(System.getProperty("user.home"), "mixamo-dl", "data")
}

private fun getEnvPath(fs: FileSystem, name: String): Path? = System.getenv(name)?.let { fs.getPath(it) }
fun Path.write(text: String) {
  Files.newBufferedWriter(this).use {
    it.write(text)
  }
}

fun Path.list(): List<Path> = if (this.exists()) Files.list(this).toList() else emptyList()