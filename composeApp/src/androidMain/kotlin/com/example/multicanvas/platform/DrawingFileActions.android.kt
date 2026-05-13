package com.example.multicanvas.platform

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.RectF
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.multicanvas.model.CanvasDocument
import com.example.multicanvas.model.CanvasPoint
import com.example.multicanvas.model.CircleObject
import com.example.multicanvas.model.DrawingBinaryCodec
import com.example.multicanvas.model.DrawingObject
import com.example.multicanvas.model.EllipseObject
import com.example.multicanvas.model.LineObject
import com.example.multicanvas.model.PointObject
import com.example.multicanvas.model.RectangleObject
import com.example.multicanvas.model.RgbaColor
import com.example.multicanvas.model.SquareObject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
actual fun rememberDrawingFileActions(
    onDocumentLoaded: (CanvasDocument) -> Unit,
    onMessage: (String) -> Unit,
): DrawingFileActions {
    val context = LocalContext.current
    var pendingSave by remember { mutableStateOf<CanvasDocument?>(null) }
    var pendingExport by remember { mutableStateOf<CanvasDocument?>(null) }

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
                onMessage("Đã lưu file .mcv")
            }.onFailure {
                onMessage("Không lưu được file")
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
                onMessage("Đã nạp file .mcv")
            }.onFailure {
                onMessage("File không đúng định dạng MultiCanvas")
            }
        }
    }

    val pngExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("image/png"),
    ) { uri ->
        val document = pendingExport
        pendingExport = null
        if (uri != null && document != null) {
            runCatching {
                val bytes = renderDocumentImage(document, Bitmap.CompressFormat.PNG)
                context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                    ?: error("Cannot open output stream")
            }.onSuccess {
                onMessage("Đã xuất file PNG")
            }.onFailure {
                onMessage("Không xuất được PNG")
            }
        }
    }

    val jpegExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("image/jpeg"),
    ) { uri ->
        val document = pendingExport
        pendingExport = null
        if (uri != null && document != null) {
            runCatching {
                val bytes = renderDocumentImage(document, Bitmap.CompressFormat.JPEG)
                context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                    ?: error("Cannot open output stream")
            }.onSuccess {
                onMessage("Đã xuất file JPEG")
            }.onFailure {
                onMessage("Không xuất được JPEG")
            }
        }
    }

    return remember(saveLauncher, loadLauncher, pngExportLauncher, jpegExportLauncher) {
        DrawingFileActions(
            saveBinary = { document ->
                pendingSave = document
                saveLauncher.launch("drawing.mcv")
            },
            loadBinary = {
                loadLauncher.launch(arrayOf("application/octet-stream", "*/*"))
            },
            exportImage = { document, format ->
                pendingExport = document
                when (format) {
                    ImageExportFormat.Png -> pngExportLauncher.launch("canvas.png")
                    ImageExportFormat.Jpeg -> jpegExportLauncher.launch("canvas.jpeg")
                }
            },
        )
    }
}

private fun renderDocumentImage(
    document: CanvasDocument,
    format: Bitmap.CompressFormat,
): ByteArray {
    val bitmap = Bitmap.createBitmap(
        document.width.coerceAtLeast(1),
        document.height.coerceAtLeast(1),
        Bitmap.Config.ARGB_8888,
    )
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)
    document.objects.forEach { canvas.drawObject(it) }

    return java.io.ByteArrayOutputStream().use { output ->
        bitmap.compress(format, if (format == Bitmap.CompressFormat.JPEG) 95 else 100, output)
        output.toByteArray()
    }
}

private fun android.graphics.Canvas.drawObject(obj: DrawingObject) {
    val style = obj.style
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = style.strokeColor.toArgb()
        strokeWidth = style.strokeWidth
        strokeCap = Paint.Cap.ROUND
        this.style = Paint.Style.STROKE
    }
    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = style.fillColor.toArgb(alphaMultiplier = 0.42f)
        this.style = Paint.Style.FILL
    }

    when (obj) {
        is PointObject -> drawCircle(
            obj.position.x,
            obj.position.y,
            max(4f, style.strokeWidth * 1.4f),
            strokePaint,
        )
        is LineObject -> drawLine(obj.start.x, obj.start.y, obj.end.x, obj.end.y, strokePaint)
        is EllipseObject -> drawOvalShape(obj.start.rectTo(obj.end), fillPaint, strokePaint, style.fillEnabled)
        is CircleObject -> drawOvalShape(obj.start.squareTo(obj.end), fillPaint, strokePaint, style.fillEnabled)
        is SquareObject -> drawRectShape(obj.start.squareTo(obj.end), fillPaint, strokePaint, style.fillEnabled)
        is RectangleObject -> drawRectShape(obj.start.rectTo(obj.end), fillPaint, strokePaint, style.fillEnabled)
    }
}

private fun android.graphics.Canvas.drawRectShape(
    rect: RectF,
    fillPaint: Paint,
    strokePaint: Paint,
    fillEnabled: Boolean,
) {
    if (rect.width() <= 0f || rect.height() <= 0f) return
    if (fillEnabled) drawRect(rect, fillPaint)
    drawRect(rect, strokePaint)
}

private fun android.graphics.Canvas.drawOvalShape(
    rect: RectF,
    fillPaint: Paint,
    strokePaint: Paint,
    fillEnabled: Boolean,
) {
    if (rect.width() <= 0f || rect.height() <= 0f) return
    if (fillEnabled) drawOval(rect, fillPaint)
    drawOval(rect, strokePaint)
}

private fun RgbaColor.toArgb(alphaMultiplier: Float = 1f): Int {
    val adjustedAlpha = (alpha * alphaMultiplier).toInt().coerceIn(0, 255)
    return android.graphics.Color.argb(adjustedAlpha, red, green, blue)
}

private fun CanvasPoint.rectTo(other: CanvasPoint): RectF {
    return RectF(
        min(x, other.x),
        min(y, other.y),
        max(x, other.x),
        max(y, other.y),
    )
}

private fun CanvasPoint.squareTo(other: CanvasPoint): RectF {
    val width = other.x - x
    val height = other.y - y
    val side = min(abs(width), abs(height))
    val signedX = if (width < 0f) -side else side
    val signedY = if (height < 0f) -side else side
    return rectTo(CanvasPoint(x + signedX, y + signedY))
}
