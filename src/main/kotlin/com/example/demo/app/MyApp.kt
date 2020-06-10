package com.example.demo.app

import com.example.demo.jwt.writeToken
import com.example.demo.paths.isAppRunning
import com.example.demo.view.MainView
import javafx.scene.image.Image
import tornadofx.App
import tornadofx.launch

val icon = Image(MyApp::class.java.getResourceAsStream("/mixamo.png"))

class MyApp : App(icon, MainView::class, Styles::class)

fun main(args: Array<String>) {
  if (args.isNotEmpty()) {
    val split = args[0].removePrefix("mixamo://").split("/")
    executeCommand(split.first(), split.drop(1))
  }

  if (!isAppRunning()) {
    launch<MyApp>(args)
  }
}

fun executeCommand(command: String, arguments: List<String>) {
  println("$command : ${arguments.joinToString(",")}")
  when (command) {
    "token" -> writeToken(arguments)
  }
}
