package com.example.demo.paths

import java.nio.file.Files
import java.nio.file.Path

fun Path.readText() = Files.newBufferedReader(this).readText()
