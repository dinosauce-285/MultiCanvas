package com.example.multicanvas.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import java.awt.BasicStroke
import java.awt.Color
import java.awt.FileDialog
import java.awt.Frame
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
actual fun rememberDrawingFileActions(
    onDocumentLoaded: (CanvasDocument) -> Unit,
    onMessage: (String) -> Unit,
): DrawingFileActions {
    return remember {
        DrawingFileActions(
            saveBinary = { document ->
                val file = chooseFile(
                    title = "Lưu bản vẽ MultiCanvas",
                    mode = FileDialog.SAVE,
                    defaultFile = "drawing.mcv",
                ) ?: return@DrawingFileActions

                runCatching {
                    val target = file.withExtension("mcv")
                    target.writeBytes(DrawingBinaryCodec.encode(document))
                }.onSuccess {
                    onMessage("Đã lưu file .mcv")
                }.onFailure {
                    onMessage("Không lưu được file")
                }
            },
            loadBinary = {
                val file = chooseFile(
                    title = "Nạp bản vẽ MultiCanvas",
                    mode = FileDialog.LOAD,
                    defaultFile = "*.mcv",
                ) ?: return@DrawingFileActions

                runCatching {
                    DrawingBinaryCodec.decode(file.readBytes())
                }.onSuccess { document ->
                    onDocumentLoaded(document)
                    onMessage("Đã nạp file .mcv")
                }.onFailure {
                    onMessage("File không đúng định dạng MultiCanvas")
                }
            },
            exportImage = { document, format ->
                val extension = when (format) {
                    ImageExportFormat.Png -> "png"
                    ImageExportFormat.Jpeg -> "jpeg"
                }
                val file = chooseFile(
                    title = "Xuất ảnh MultiCanvas",
                    mode = FileDialog.SAVE,
                    defaultFile = "canvas.$extension",
                ) ?: return@DrawingFileActions

                runCatching {
                    val target = file.withExtension(extension)
                    val image = renderDocumentImage(document)
                    ImageIO.write(image, extension, target)
                }.onSuccess {
                    onMessage("Đã xuất file ${extension.uppercase()}")
                }.onFailure {
                    onMessage("Không xuất được file ảnh")
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

private fun renderDocumentImage(document: CanvasDocument): BufferedImage {
    val image = BufferedImage(
        document.width.coerceAtLeast(1),
        document.height.coerceAtLeast(1),
        BufferedImage.TYPE_INT_RGB,
    )
    val graphics = image.createGraphics()
    graphics.use {
        it.color = Color.WHITE
        it.fillRect(0, 0, image.width, image.height)
        it.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        document.objects.forEach(it::drawObject)
    }
    return image
}

private fun Graphics2D.drawObject(obj: DrawingObject) {
    val style = obj.style
    stroke = BasicStroke(style.strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
    color = style.strokeColor.toAwtColor()

    when (obj) {
        is PointObject -> fill(
            Ellipse2D.Float(
                obj.position.x - max(4f, style.strokeWidth * 1.4f),
                obj.position.y - max(4f, style.strokeWidth * 1.4f),
                max(4f, style.strokeWidth * 1.4f) * 2f,
                max(4f, style.strokeWidth * 1.4f) * 2f,
            ),
        )
        is LineObject -> drawLine(
            obj.start.x.toInt(),
            obj.start.y.toInt(),
            obj.end.x.toInt(),
            obj.end.y.toInt(),
        )
        is EllipseObject -> drawOvalShape(obj.start.rectTo(obj.end), style.fillColor, style.fillEnabled)
        is CircleObject -> drawOvalShape(obj.start.squareTo(obj.end), style.fillColor, style.fillEnabled)
        is SquareObject -> drawRectShape(obj.start.squareTo(obj.end), style.fillColor, style.fillEnabled)
        is RectangleObject -> drawRectShape(obj.start.rectTo(obj.end), style.fillColor, style.fillEnabled)
    }
}

private fun Graphics2D.drawRectShape(
    rect: Rectangle2D.Float,
    fillColor: RgbaColor,
    fillEnabled: Boolean,
) {
    if (rect.width <= 0f || rect.height <= 0f) return
    val strokeColor = color
    if (fillEnabled) {
        color = fillColor.toAwtColor(alphaMultiplier = 0.42f)
        fill(rect)
        color = strokeColor
    }
    draw(rect)
}

private fun Graphics2D.drawOvalShape(
    rect: Rectangle2D.Float,
    fillColor: RgbaColor,
    fillEnabled: Boolean,
) {
    if (rect.width <= 0f || rect.height <= 0f) return
    val oval = Ellipse2D.Float(rect.x, rect.y, rect.width, rect.height)
    val strokeColor = color
    if (fillEnabled) {
        color = fillColor.toAwtColor(alphaMultiplier = 0.42f)
        fill(oval)
        color = strokeColor
    }
    draw(oval)
}

private fun RgbaColor.toAwtColor(alphaMultiplier: Float = 1f): Color {
    val adjustedAlpha = (alpha * alphaMultiplier).toInt().coerceIn(0, 255)
    return Color(red, green, blue, adjustedAlpha)
}

private fun CanvasPoint.rectTo(other: CanvasPoint): Rectangle2D.Float {
    val left = min(x, other.x)
    val top = min(y, other.y)
    return Rectangle2D.Float(
        left,
        top,
        abs(other.x - x),
        abs(other.y - y),
    )
}

private fun CanvasPoint.squareTo(other: CanvasPoint): Rectangle2D.Float {
    val width = other.x - x
    val height = other.y - y
    val side = min(abs(width), abs(height))
    val signedX = if (width < 0f) -side else side
    val signedY = if (height < 0f) -side else side
    return rectTo(CanvasPoint(x + signedX, y + signedY))
}

private inline fun Graphics2D.use(block: (Graphics2D) -> Unit) {
    try {
        block(this)
    } finally {
        dispose()
    }
}
