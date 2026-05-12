package com.example.multicanvas.platform

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.multicanvas.model.CanvasDocument
import com.example.multicanvas.model.DrawingBinaryCodec

@Composable
actual fun rememberDrawingFileActions(
    onDocumentLoaded: (CanvasDocument) -> Unit,
    onMessage: (String) -> Unit,
): DrawingFileActions {
    val context = LocalContext.current
    var pendingSave by remember { mutableStateOf<CanvasDocument?>(null) }

    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri ->
        val document = pendingSave
        pendingSave = null
        if (uri != null && document != null) {
            runCatching {
                val bytes = DrawingBinaryCodec.encode(document)
                context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                    ?: error("Cannot open output stream")
            }.onSuccess {
                onMessage("Da luu file .mcv")
            }.onFailure {
                onMessage("Khong luu duoc file")
            }
        }
    }

    val loadLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: error("Cannot open input stream")
                DrawingBinaryCodec.decode(bytes)
            }.onSuccess { document ->
                onDocumentLoaded(document)
                onMessage("Da nap file .mcv")
            }.onFailure {
                onMessage("File khong dung dinh dang MultiCanvas")
            }
        }
    }

    return remember(saveLauncher, loadLauncher) {
        DrawingFileActions(
            saveBinary = { document ->
                pendingSave = document
                saveLauncher.launch("drawing.mcv")
            },
            loadBinary = {
                loadLauncher.launch(arrayOf("application/octet-stream", "*/*"))
            },
        )
    }
}
