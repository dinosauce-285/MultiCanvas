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

data class CanvasDocument(
    val width: Int,
    val height: Int,
    val objects: List<DrawingObject>,
)

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

object DrawingBinaryCodec {
    private const val FileMagic = 0x4D434E56
    private const val FileVersion = 1

    fun encode(document: CanvasDocument): ByteArray {
        val writer = BinaryWriter()
        writer.writeInt(FileMagic)
        writer.writeInt(FileVersion)
        writer.writeInt(document.width)
        writer.writeInt(document.height)
        writer.writeInt(document.objects.size)
        document.objects.forEach { writer.writeObject(it) }
        return writer.toByteArray()
    }

    fun decode(bytes: ByteArray): CanvasDocument {
        val reader = BinaryReader(bytes)
        require(reader.readInt() == FileMagic) { "Invalid MultiCanvas file" }
        require(reader.readInt() == FileVersion) { "Unsupported MultiCanvas version" }

        val width = reader.readInt()
        val height = reader.readInt()
        val count = reader.readInt()
        require(count >= 0) { "Invalid object count" }

        val objects = List(count) { reader.readObject() }
        return CanvasDocument(width = width, height = height, objects = objects)
    }

    private fun BinaryWriter.writeObject(obj: DrawingObject) {
        writeInt(obj.toolCode)
        writeLong(obj.id)
        writeStyle(obj.style)

        when (obj) {
            is PointObject -> {
                writePoint(obj.position)
                writePoint(obj.position)
            }
            is LineObject -> {
                writePoint(obj.start)
                writePoint(obj.end)
            }
            is EllipseObject -> {
                writePoint(obj.start)
                writePoint(obj.end)
            }
            is CircleObject -> {
                writePoint(obj.start)
                writePoint(obj.end)
            }
            is SquareObject -> {
                writePoint(obj.start)
                writePoint(obj.end)
            }
            is RectangleObject -> {
                writePoint(obj.start)
                writePoint(obj.end)
            }
        }
    }

    private fun BinaryReader.readObject(): DrawingObject {
        val tool = readInt().toShapeTool()
        val id = readLong()
        val style = readStyle()
        val start = readPoint()
        val end = readPoint()
        return createDrawingObject(
            tool = tool,
            id = id,
            start = start,
            end = end,
            style = style,
        )
    }

    private fun BinaryWriter.writeStyle(style: DrawingStyle) {
        writeColor(style.strokeColor)
        writeColor(style.fillColor)
        writeFloat(style.strokeWidth)
        writeBoolean(style.fillEnabled)
    }

    private fun BinaryReader.readStyle(): DrawingStyle {
        return DrawingStyle(
            strokeColor = readColor(),
            fillColor = readColor(),
            strokeWidth = readFloat(),
            fillEnabled = readBoolean(),
        )
    }

    private fun BinaryWriter.writePoint(point: CanvasPoint) {
        writeFloat(point.x)
        writeFloat(point.y)
    }

    private fun BinaryReader.readPoint(): CanvasPoint {
        return CanvasPoint(x = readFloat(), y = readFloat())
    }

    private fun BinaryWriter.writeColor(color: RgbaColor) {
        writeInt(color.red)
        writeInt(color.green)
        writeInt(color.blue)
        writeInt(color.alpha)
    }

    private fun BinaryReader.readColor(): RgbaColor {
        return RgbaColor(
            red = readInt(),
            green = readInt(),
            blue = readInt(),
            alpha = readInt(),
        )
    }

    private val DrawingObject.toolCode: Int
        get() = when (this) {
            is PointObject -> 0
            is LineObject -> 1
            is EllipseObject -> 2
            is CircleObject -> 3
            is SquareObject -> 4
            is RectangleObject -> 5
        }

    private fun Int.toShapeTool(): ShapeTool {
        return when (this) {
            0 -> ShapeTool.Point
            1 -> ShapeTool.Line
            2 -> ShapeTool.Ellipse
            3 -> ShapeTool.Circle
            4 -> ShapeTool.Square
            5 -> ShapeTool.Rectangle
            else -> error("Unknown shape tool: $this")
        }
    }
}

private class BinaryWriter {
    private val bytes = mutableListOf<Byte>()

    fun writeBoolean(value: Boolean) {
        bytes.add((if (value) 1 else 0).toByte())
    }

    fun writeFloat(value: Float) {
        writeInt(value.toRawBits())
    }

    fun writeLong(value: Long) {
        for (shift in 56 downTo 0 step 8) {
            bytes.add(((value ushr shift) and 0xFF).toByte())
        }
    }

    fun writeInt(value: Int) {
        for (shift in 24 downTo 0 step 8) {
            bytes.add(((value ushr shift) and 0xFF).toByte())
        }
    }

    fun toByteArray(): ByteArray {
        return bytes.toByteArray()
    }
}

private class BinaryReader(
    private val bytes: ByteArray,
) {
    private var position = 0

    fun readBoolean(): Boolean {
        return readByte().toInt() != 0
    }

    fun readFloat(): Float {
        return Float.fromBits(readInt())
    }

    fun readLong(): Long {
        var value = 0L
        repeat(8) {
            value = (value shl 8) or (readByte().toLong() and 0xFF)
        }
        return value
    }

    fun readInt(): Int {
        var value = 0
        repeat(4) {
            value = (value shl 8) or (readByte().toInt() and 0xFF)
        }
        return value
    }

    private fun readByte(): Byte {
        require(position < bytes.size) { "Unexpected end of file" }
        return bytes[position++]
    }
}
