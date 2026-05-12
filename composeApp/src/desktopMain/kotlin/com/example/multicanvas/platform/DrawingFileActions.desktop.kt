package com.example.multicanvas.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.multicanvas.model.CanvasDocument
import com.example.multicanvas.model.DrawingBinaryCodec
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
actual fun rememberDrawingFileActions(
    onDocumentLoaded: (CanvasDocument) -> Unit,
    onMessage: (String) -> Unit,
): DrawingFileActions {
    return remember {
        DrawingFileActions(
            saveBinary = { document ->
                val file = chooseFile(
                    title = "Save MultiCanvas drawing",
                    mode = FileDialog.SAVE,
                    defaultFile = "drawing.mcv",
                ) ?: return@DrawingFileActions

                runCatching {
                    val target = file.withExtension("mcv")
                    target.writeBytes(DrawingBinaryCodec.encode(document))
                }.onSuccess {
                    onMessage("Da luu file .mcv")
                }.onFailure {
                    onMessage("Khong luu duoc file")
                }
            },
            loadBinary = {
                val file = chooseFile(
                    title = "Load MultiCanvas drawing",
                    mode = FileDialog.LOAD,
                    defaultFile = "*.mcv",
                ) ?: return@DrawingFileActions

                runCatching {
                    DrawingBinaryCodec.decode(file.readBytes())
                }.onSuccess { document ->
                    onDocumentLoaded(document)
                    onMessage("Da nap file .mcv")
                }.onFailure {
                    onMessage("File khong dung dinh dang MultiCanvas")
                }
            },
        )
    }
}

private fun chooseFile(
    title: String,
    mode: Int,
    defaultFile: String,
): File? {
    val dialog = FileDialog(null as Frame?, title, mode).apply {
        file = defaultFile
        isVisible = true
    }

    val directory = dialog.directory ?: return null
    val fileName = dialog.file ?: return null
    return File(directory, fileName)
}

private fun File.withExtension(extension: String): File {
    return if (name.endsWith(".$extension", ignoreCase = true)) {
        this
    } else {
        File(parentFile, "$name.$extension")
    }
}
