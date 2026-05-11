package com.example.multicanvas.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.multicanvas.model.CanvasPoint
import com.example.multicanvas.model.CircleObject
import com.example.multicanvas.model.DrawingObject
import com.example.multicanvas.model.DrawingStyle
import com.example.multicanvas.model.EllipseObject
import com.example.multicanvas.model.LineObject
import com.example.multicanvas.model.PointObject
import com.example.multicanvas.model.RectangleObject
import com.example.multicanvas.model.RgbaColor
import com.example.multicanvas.model.ShapeTool
import com.example.multicanvas.model.SquareObject
import com.example.multicanvas.model.createDrawingObject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun MultiCanvasScreen() {
    val objects = remember { mutableStateListOf<DrawingObject>() }
    var nextId by remember { mutableLongStateOf(1L) }
    var selectedTool by remember { mutableStateOf(ShapeTool.Line) }
    var strokeColor by remember { mutableStateOf(DefaultStrokeColors.first().color) }
    var fillColor by remember { mutableStateOf(DefaultFillColors.first().color) }
    var strokeWidth by remember { mutableStateOf(4f) }
    var fillEnabled by remember { mutableStateOf(true) }
    var draftObject by remember { mutableStateOf<DrawingObject?>(null) }

    val currentStyle = DrawingStyle(
        strokeColor = strokeColor,
        fillColor = fillColor,
        strokeWidth = strokeWidth,
        fillEnabled = fillEnabled,
    )

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding(),
        ) {
            DrawingToolbar(
                selectedTool = selectedTool,
                onToolSelected = { selectedTool = it },
                strokeColor = strokeColor,
                onStrokeColorSelected = { strokeColor = it },
                fillColor = fillColor,
                onFillColorSelected = { fillColor = it },
                strokeWidth = strokeWidth,
                onStrokeWidthChanged = { strokeWidth = it },
                fillEnabled = fillEnabled,
                onFillEnabledChanged = { fillEnabled = it },
                objectCount = objects.size,
                canUndo = objects.isNotEmpty(),
                onUndo = {
                    if (objects.isNotEmpty()) {
                        objects.removeAt(objects.lastIndex)
                    }
                },
                onClear = { objects.clear() },
            )

            HorizontalDivider()

            DrawingBoard(
                objects = objects,
                draftObject = draftObject,
                selectedTool = selectedTool,
                currentStyle = currentStyle,
                nextId = nextId,
                onDraftChanged = { draftObject = it },
                onObjectFinished = { drawingObject ->
                    objects.add(drawingObject)
                    nextId += 1L
                    draftObject = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(12.dp),
            )
        }
    }
}

@Composable
private fun DrawingToolbar(
    selectedTool: ShapeTool,
    onToolSelected: (ShapeTool) -> Unit,
    strokeColor: RgbaColor,
    onStrokeColorSelected: (RgbaColor) -> Unit,
    fillColor: RgbaColor,
    onFillColorSelected: (RgbaColor) -> Unit,
    strokeWidth: Float,
    onStrokeWidthChanged: (Float) -> Unit,
    fillEnabled: Boolean,
    onFillEnabledChanged: (Boolean) -> Unit,
    objectCount: Int,
    canUndo: Boolean,
    onUndo: () -> Unit,
    onClear: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShapeTool.entries.forEach { tool ->
                ToolButton(
                    tool = tool,
                    selected = tool == selectedTool,
                    onClick = { onToolSelected(tool) },
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ColorPalette(
                title = "Vien",
                colors = DefaultStrokeColors,
                selectedColor = strokeColor,
                onColorSelected = onStrokeColorSelected,
            )
            ColorPalette(
                title = "To",
                colors = DefaultFillColors,
                selectedColor = fillColor,
                onColorSelected = onFillColorSelected,
            )
            Row(
                modifier = Modifier.clickable { onFillEnabledChanged(!fillEnabled) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = fillEnabled,
                    onCheckedChange = onFillEnabledChanged,
                )
                Text("To mau")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Day")
            Slider(
                value = strokeWidth,
                onValueChange = { onStrokeWidthChanged(it.coerceIn(1f, 24f)) },
                valueRange = 1f..24f,
                steps = 22,
                modifier = Modifier
                    .widthIn(min = 140.dp)
                    .weight(1f),
            )
            Text("${strokeWidth.roundToInt()} px")
            OutlinedButton(
                enabled = canUndo,
                onClick = onUndo,
            ) {
                Text("Hoan tac")
            }
            Button(
                enabled = objectCount > 0,
                onClick = onClear,
            ) {
                Text("Xoa")
            }
            Text("$objectCount hinh")
        }
    }
}

@Composable
private fun ToolButton(
    tool: ShapeTool,
    selected: Boolean,
    onClick: () -> Unit,
) {
    if (selected) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(tool.label)
        }
    } else {
        OutlinedButton(onClick = onClick) {
            Text(tool.label)
        }
    }
}

@Composable
private fun ColorPalette(
    title: String,
    colors: List<ColorChoice>,
    selectedColor: RgbaColor,
    onColorSelected: (RgbaColor) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title)
        colors.forEach { choice ->
            ColorSwatch(
                choice = choice,
                selected = choice.color == selectedColor,
                onClick = { onColorSelected(choice.color) },
            )
        }
    }
}

@Composable
private fun ColorSwatch(
    choice: ColorChoice,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(shape)
            .background(choice.color.toComposeColor())
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = shape,
            )
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = choice.name
            },
    )
}

@Composable
private fun DrawingBoard(
    objects: List<DrawingObject>,
    draftObject: DrawingObject?,
    selectedTool: ShapeTool,
    currentStyle: DrawingStyle,
    nextId: Long,
    onDraftChanged: (DrawingObject?) -> Unit,
    onObjectFinished: (DrawingObject) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .background(Color.White),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .pointerInput(selectedTool, currentStyle, nextId) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        val start = down.position.toCanvasPoint()
                        var end = start

                        onDraftChanged(
                            createDrawingObject(
                                tool = selectedTool,
                                id = nextId,
                                start = start,
                                end = end,
                                style = currentStyle,
                            ),
                        )

                        do {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id }
                                ?: event.changes.firstOrNull()
                            if (change != null) {
                                end = change.position.toCanvasPoint()
                                onDraftChanged(
                                    createDrawingObject(
                                        tool = selectedTool,
                                        id = nextId,
                                        start = start,
                                        end = end,
                                        style = currentStyle,
                                    ),
                                )
                                change.consume()
                            }
                        } while (change?.pressed == true)

                        onObjectFinished(
                            createDrawingObject(
                                tool = selectedTool,
                                id = nextId,
                                start = start,
                                end = end,
                                style = currentStyle,
                            ),
                        )
                    }
                },
        ) {
            objects.forEach { drawDrawingObject(it) }
            draftObject?.let { drawDrawingObject(it, isDraft = true) }
        }
    }
}

private val ShapeTool.label: String
    get() = when (this) {
        ShapeTool.Point -> "Diem"
        ShapeTool.Line -> "Duong"
        ShapeTool.Ellipse -> "Ellipse"
        ShapeTool.Circle -> "Tron"
        ShapeTool.Square -> "Vuong"
        ShapeTool.Rectangle -> "Chu nhat"
    }

private data class ColorChoice(
    val name: String,
    val color: RgbaColor,
)

private val DefaultStrokeColors = listOf(
    ColorChoice("Den", RgbaColor(33, 33, 33)),
    ColorChoice("Do", RgbaColor(211, 47, 47)),
    ColorChoice("Xanh duong", RgbaColor(25, 118, 210)),
    ColorChoice("Xanh la", RgbaColor(46, 125, 50)),
    ColorChoice("Tim", RgbaColor(123, 31, 162)),
    ColorChoice("Cam", RgbaColor(239, 108, 0)),
)

private val DefaultFillColors = listOf(
    ColorChoice("Vang", RgbaColor(255, 213, 79)),
    ColorChoice("Hong", RgbaColor(244, 143, 177)),
    ColorChoice("Xanh nhat", RgbaColor(100, 181, 246)),
    ColorChoice("La non", RgbaColor(129, 199, 132)),
    ColorChoice("Tim nhat", RgbaColor(179, 157, 219)),
    ColorChoice("Xam", RgbaColor(189, 189, 189)),
)

private fun Offset.toCanvasPoint(): CanvasPoint {
    return CanvasPoint(x = x, y = y)
}

private fun CanvasPoint.toOffset(): Offset {
    return Offset(x = x, y = y)
}

private fun RgbaColor.toComposeColor(alphaMultiplier: Float = 1f): Color {
    val adjustedAlpha = (alpha / 255f * alphaMultiplier).coerceIn(0f, 1f)
    return Color(
        red = red / 255f,
        green = green / 255f,
        blue = blue / 255f,
        alpha = adjustedAlpha,
    )
}

private fun DrawScope.drawDrawingObject(
    drawingObject: DrawingObject,
    isDraft: Boolean = false,
) {
    val style = drawingObject.style
    val strokeColor = style.strokeColor.toComposeColor(alphaMultiplier = if (isDraft) 0.62f else 1f)
    val fillColor = style.fillColor.toComposeColor(alphaMultiplier = if (isDraft) 0.22f else 0.42f)
    val stroke = Stroke(width = style.strokeWidth, cap = StrokeCap.Round)

    when (drawingObject) {
        is PointObject -> drawCircle(
            color = strokeColor,
            radius = max(4f, style.strokeWidth * 1.4f),
            center = drawingObject.position.toOffset(),
        )

        is LineObject -> drawLine(
            color = strokeColor,
            start = drawingObject.start.toOffset(),
            end = drawingObject.end.toOffset(),
            strokeWidth = style.strokeWidth,
            cap = StrokeCap.Round,
        )

        is EllipseObject -> drawOvalShape(
            bounds = drawingObject.start.rectTo(drawingObject.end),
            strokeColor = strokeColor,
            fillColor = fillColor,
            stroke = stroke,
            fillEnabled = style.fillEnabled,
        )

        is CircleObject -> drawOvalShape(
            bounds = drawingObject.start.squareTo(drawingObject.end),
            strokeColor = strokeColor,
            fillColor = fillColor,
            stroke = stroke,
            fillEnabled = style.fillEnabled,
        )

        is SquareObject -> drawRectShape(
            bounds = drawingObject.start.squareTo(drawingObject.end),
            strokeColor = strokeColor,
            fillColor = fillColor,
            stroke = stroke,
            fillEnabled = style.fillEnabled,
        )

        is RectangleObject -> drawRectShape(
            bounds = drawingObject.start.rectTo(drawingObject.end),
            strokeColor = strokeColor,
            fillColor = fillColor,
            stroke = stroke,
            fillEnabled = style.fillEnabled,
        )
    }
}

private fun DrawScope.drawRectShape(
    bounds: Rect,
    strokeColor: Color,
    fillColor: Color,
    stroke: Stroke,
    fillEnabled: Boolean,
) {
    if (bounds.width <= 0f || bounds.height <= 0f) return

    if (fillEnabled) {
        drawRect(
            color = fillColor,
            topLeft = bounds.topLeft,
            size = Size(bounds.width, bounds.height),
        )
    }

    drawRect(
        color = strokeColor,
        topLeft = bounds.topLeft,
        size = Size(bounds.width, bounds.height),
        style = stroke,
    )
}

private fun DrawScope.drawOvalShape(
    bounds: Rect,
    strokeColor: Color,
    fillColor: Color,
    stroke: Stroke,
    fillEnabled: Boolean,
) {
    if (bounds.width <= 0f || bounds.height <= 0f) return

    if (fillEnabled) {
        drawOval(
            color = fillColor,
            topLeft = bounds.topLeft,
            size = Size(bounds.width, bounds.height),
        )
    }

    drawOval(
        color = strokeColor,
        topLeft = bounds.topLeft,
        size = Size(bounds.width, bounds.height),
        style = stroke,
    )
}

private fun CanvasPoint.rectTo(other: CanvasPoint): Rect {
    return Rect(
        left = min(x, other.x),
        top = min(y, other.y),
        right = max(x, other.x),
        bottom = max(y, other.y),
    )
}

private fun CanvasPoint.squareTo(other: CanvasPoint): Rect {
    val width = other.x - x
    val height = other.y - y
    val side = min(abs(width), abs(height))
    val signedX = if (width < 0f) -side else side
    val signedY = if (height < 0f) -side else side
    return rectTo(CanvasPoint(x + signedX, y + signedY))
}
