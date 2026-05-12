package com.example.multicanvas.platform

import androidx.compose.runtime.Composable
import com.example.multicanvas.model.CanvasDocument

enum class ImageExportFormat {
    Png,
    Jpeg,
}

class DrawingFileActions(
    val saveBinary: (CanvasDocument) -> Unit,
    val loadBinary: () -> Unit,
    val exportImage: (CanvasDocument, ImageExportFormat) -> Unit,
)

@Composable
expect fun rememberDrawingFileActions(
    onDocumentLoaded: (CanvasDocument) -> Unit,
    onMessage: (String) -> Unit,
): DrawingFileActions
