package com.example.multicanvas.model

data class CanvasPoint(
    val x: Float,
    val y: Float,
)

data class RgbaColor(
    val red: Int,
    val green: Int,
    val blue: Int,
    val alpha: Int = 255,
) {
    init {
        require(red in 0..255) { "red must be in 0..255" }
        require(green in 0..255) { "green must be in 0..255" }
        require(blue in 0..255) { "blue must be in 0..255" }
        require(alpha in 0..255) { "alpha must be in 0..255" }
    }
}

data class DrawingStyle(
    val strokeColor: RgbaColor,
    val fillColor: RgbaColor,
    val strokeWidth: Float,
    val fillEnabled: Boolean,
) {
    init {
        require(strokeWidth > 0f) { "strokeWidth must be greater than 0" }
    }
}

enum class ShapeTool {
    Point,
    Line,
    Ellipse,
    Circle,
    Square,
    Rectangle,
}

sealed interface DrawingObject {
    val id: Long
    val style: DrawingStyle
}

data class PointObject(
    override val id: Long,
    override val style: DrawingStyle,
    val position: CanvasPoint,
) : DrawingObject

data class LineObject(
    override val id: Long,
    override val style: DrawingStyle,
    val start: CanvasPoint,
    val end: CanvasPoint,
) : DrawingObject

data class EllipseObject(
    override val id: Long,
    override val style: DrawingStyle,
    val start: CanvasPoint,
    val end: CanvasPoint,
) : DrawingObject

data class CircleObject(
    override val id: Long,
    override val style: DrawingStyle,
    val start: CanvasPoint,
    val end: CanvasPoint,
) : DrawingObject

data class SquareObject(
    override val id: Long,
    override val style: DrawingStyle,
    val start: CanvasPoint,
    val end: CanvasPoint,
) : DrawingObject

data class RectangleObject(
    override val id: Long,
    override val style: DrawingStyle,
    val start: CanvasPoint,
    val end: CanvasPoint,
) : DrawingObject

fun createDrawingObject(
    tool: ShapeTool,
    id: Long,
    start: CanvasPoint,
    end: CanvasPoint,
    style: DrawingStyle,
): DrawingObject {
    return when (tool) {
        ShapeTool.Point -> PointObject(id = id, style = style, position = end)
        ShapeTool.Line -> LineObject(id = id, style = style, start = start, end = end)
        ShapeTool.Ellipse -> EllipseObject(id = id, style = style, start = start, end = end)
        ShapeTool.Circle -> CircleObject(id = id, style = style, start = start, end = end)
        ShapeTool.Square -> SquareObject(id = id, style = style, start = start, end = end)
        ShapeTool.Rectangle -> RectangleObject(id = id, style = style, start = start, end = end)
    }
}
