package com.example.multicanvas

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Bảng vẽ đa năng",
    ) {
        App()
    }
}
