package com.example.multicanvas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import org.jetbrains.compose.resources.painterResource
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

import multicanvas.composeapp.generated.resources.Res
import multicanvas.composeapp.generated.resources.compose_multiplatform

private const val FILE_MAGIC = 0x4D434E56
private const val FILE_VERSION = 1

private enum class DrawTool(val label: String) {
    Point("Điểm"),
    Line("Đường"),
    Ellipse("Ellipse"),
    Circle("Tròn"),
    Square("Vuông"),
    Rectangle("Chữ nhật"),
}

private data class DrawingObject(
    val tool: DrawTool,
    val start: Offset,
    val end: Offset,
    val strokeColor: Int,
    val fillColor: Int,
    val strokeWidth: Float,
    val filled: Boolean,
)

private object DrawingBinaryCodec {
    fun encode(objects: List<DrawingObject>, canvasSize: IntSize): ByteArray {
        val buffer = ByteArrayOutputStream()
        DataOutputStream(buffer).use { output ->
            output.writeInt(FILE_MAGIC)
            output.writeInt(FILE_VERSION)
            output.writeInt(canvasSize.width)
            output.writeInt(canvasSize.height)
            output.writeInt(objects.size)
            objects.forEach { obj ->
                output.writeInt(obj.tool.ordinal)
                output.writeFloat(obj.start.x)
                output.writeFloat(obj.start.y)
                output.writeFloat(obj.end.x)
                output.writeFloat(obj.end.y)
                output.writeInt(obj.strokeColor)
                output.writeInt(obj.fillColor)
                output.writeFloat(obj.strokeWidth)
                output.writeBoolean(obj.filled)
            }
        }
        return buffer.toByteArray()
    }

    fun decode(bytes: ByteArray): List<DrawingObject> {
        DataInputStream(ByteArrayInputStream(bytes)).use { input ->
            require(input.readInt() == FILE_MAGIC)
            require(input.readInt() == FILE_VERSION)
            input.readInt()
            input.readInt()
            val count = input.readInt()
            require(count >= 0)
            return List(count) {
                val toolOrdinal = input.readInt()
                val tool = DrawTool.entries.getOrNull(toolOrdinal) ?: error("Unknown tool")
                DrawingObject(
                    tool = tool,
                    start = Offset(input.readFloat(), input.readFloat()),
                    end = Offset(input.readFloat(), input.readFloat()),
                    strokeColor = input.readInt(),
                    fillColor = input.readInt(),
                    strokeWidth = input.readFloat(),
                    filled = input.readBoolean(),
                )
            }
        }
    }
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { showContent = !showContent }) {
                Text("Click me!")
            }
            AnimatedVisibility(showContent) {
                val greeting = remember { Greeting().greet() }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text("Compose: $greeting")
                }
            }
        }
    }
}
