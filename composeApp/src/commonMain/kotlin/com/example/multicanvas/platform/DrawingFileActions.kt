package com.example.multicanvas.platform

import androidx.compose.runtime.Composable
import com.example.multicanvas.model.CanvasDocument

class DrawingFileActions(
    val saveBinary: (CanvasDocument) -> Unit,
    val loadBinary: () -> Unit,
)

@Composable
expect fun rememberDrawingFileActions(
    onDocumentLoaded: (CanvasDocument) -> Unit,
    onMessage: (String) -> Unit,
): DrawingFileActions
