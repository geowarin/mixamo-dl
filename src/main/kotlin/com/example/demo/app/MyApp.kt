package com.example.demo.app

import com.example.demo.view.MainView
import tornadofx.*
import java.io.File

class MyApp : App(MainView::class, Styles::class)

fun main(args: Array<String>) {
    launch<MyApp>(args)
}
