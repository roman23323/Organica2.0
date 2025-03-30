package com.example.organica20.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.ViewGroup
import com.example.organica20.R
import com.example.organica20.utils.getCircumcircleRadius
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class PlaceLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    var placementString: String = ""
    var linesString: String = ""
    var linesOffset: Int = 0
    var elementsSpacing: Int = 0
    var linesSpacing: Int = 10
    var zeroElementPosition: Int = 0
    var zeroElementOffsetX: Int = 0
    var zeroElementOffsetY: Int = 0
    private val paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    init {
        setBackgroundColor(Color.TRANSPARENT)
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PlaceLayout)
        val placementStringArgument = typedArray.getString(
            R.styleable.PlaceLayout_placement
        )
        if (placementStringArgument != null) {
            placementString = placementStringArgument
        }
        val linesArgument = typedArray.getString(
            R.styleable.PlaceLayout_lines
        )
        if (linesArgument != null) {
            linesString = linesArgument
        }
        linesOffset = typedArray.getDimensionPixelSize(
            R.styleable.PlaceLayout_linesOffset,
            linesOffset
        )
        elementsSpacing = typedArray.getDimensionPixelSize(
            R.styleable.PlaceLayout_elementsSpacing,
            elementsSpacing
        )
        linesSpacing = typedArray.getDimensionPixelSize(
            R.styleable.PlaceLayout_linesSpacing,
            linesSpacing
        )
        zeroElementPosition = typedArray.getInt(
            R.styleable.PlaceLayout_zeroElementPosition,
            zeroElementPosition
        )
        zeroElementOffsetX = typedArray.getDimensionPixelSize(
            R.styleable.PlaceLayout_zeroElementOffsetX,
            0
        )
        zeroElementOffsetY = typedArray.getDimensionPixelSize(
            R.styleable.PlaceLayout_zeroElementOffsetY,
            0
        )
        typedArray.recycle()
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
        }
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (childCount == 0) return

        val parentWidth = r - l
        val parentHeight = b - t
        val child0 = getChildAt(0)
        var child0Left: Int
        var child0Top: Int
        when (zeroElementPosition) {
            0 -> { // LeftTop
                child0Left = 0
                child0Top = 0
            }
            1 -> { // CenterTop
                child0Left = (parentWidth - child0.measuredWidth) / 2
                child0Top = 0
            }
            2 -> { // RightTop
                child0Left = parentWidth - child0.measuredWidth
                child0Top = 0
            }
            3 -> { // LeftCenter
                child0Left = 0
                child0Top = (parentHeight - child0.measuredHeight) / 2
            }
            4 -> { // Center
                child0Left = (parentWidth - child0.measuredWidth) / 2
                child0Top = (parentHeight - child0.measuredHeight) / 2
            }
            5 -> { // RightCenter
                child0Left = parentWidth - child0.measuredWidth
                child0Top = (parentHeight - child0.measuredHeight) / 2
            }
            6 -> { // LeftBottom
                child0Left = 0
                child0Top = parentHeight - child0.measuredHeight
            }
            7 -> { // CenterBottom
                child0Left = (parentWidth - child0.measuredWidth) / 2
                child0Top = parentHeight - child0.measuredHeight
            }
            8 -> { // RightBottom
                child0Left = parentWidth - child0.measuredWidth
                child0Top = parentHeight - child0.measuredHeight
            }
            else -> { // По умолчанию
                child0Left = 0
                child0Top = 0
            }
        }
        child0Left += zeroElementOffsetX
        child0Top += zeroElementOffsetY

        child0.layout(
            child0Left,
            child0Top,
            child0Left + child0.measuredWidth,
            child0Top + child0.measuredHeight
        )

        val placements = parsePlacementString(placementString)

        for (placement in placements) {
            val mainChild = getChildAt(placement.fromIndex)
            val sideChild = getChildAt(placement.toIndex)
            val mCenterX = mainChild.left + mainChild.width / 2
            val mCenterY = mainChild.top + mainChild.height / 2

            var sCenterX = mCenterX
            var sCenterY = mCenterY

            if (placement.direction != null) {
                when (placement.direction) {
                    "R" -> sCenterX += 2 * elementsSpacing
                    "L" -> sCenterX -= 2 * elementsSpacing
                    "U" -> sCenterY -= 2 * elementsSpacing
                    "D" -> sCenterY += 2 * elementsSpacing
                }
            }
            if (placement.angle != null) {
                val mRadius = getCircumcircleRadius(mainChild.width, mainChild.height)
                val sRadius = getCircumcircleRadius(sideChild.measuredWidth, sideChild.measuredHeight)
                val radialSpacing = elementsSpacing + (mRadius + sRadius) / 2
                sCenterX = mCenterX + (cos(Math.toRadians(placement.angle.toDouble())) * radialSpacing).toInt()
                sCenterY = mCenterY + (sin(Math.toRadians(placement.angle.toDouble())) * radialSpacing).toInt()
            }
            sideChild.layout(sCenterX - sideChild.measuredWidth / 2, sCenterY - sideChild.measuredHeight / 2,
                sCenterX + sideChild.measuredWidth / 2, sCenterY + sideChild.measuredHeight / 2)
        }
    }


    override fun onDraw(canvas: Canvas) {
        val linesInfo = parseLinesString(linesString)

        for (lineInfo in linesInfo) {
            val mainChild = getChildAt(lineInfo.fromIndex)
            val sideChild = getChildAt(lineInfo.toIndex)
            val mCenterX = (mainChild.left + mainChild.width / 2).toFloat()
            val mCenterY = (mainChild.top + mainChild.height / 2).toFloat()
            val sCenterX = (sideChild.left + sideChild.width / 2).toFloat()
            val sCenterY = (sideChild.top + sideChild.height / 2).toFloat()

            val lines = calculateLineCoords(mCenterX, mCenterY, sCenterX, sCenterY, linesOffset, linesSpacing, lineInfo.lineCount)

            for (line in lines) {
                canvas.drawLine(line.sx, line.sy, line.ex, line.ey, paint)
            }
        }
    }
}

fun parsePlacementString(input: String): List<Placement> {
    return input.split(",").map { entry ->
        val parts = entry.split(":")
        val indices = parts[0].split(".").map { it.toInt() }
        val fromIndex = indices[0]
        val toIndex = indices[1]

        val direction = if (parts[1].matches(Regex("[RLUD]"))) parts[1] else null
        val angle = parts[1].toFloatOrNull()

        Placement(fromIndex, toIndex, direction, angle)
    }
}

data class Placement(
    val fromIndex: Int,
    val toIndex: Int,
    val direction: String? = null,
    val angle: Float? = null
)

data class LineInfo(
    val fromIndex: Int,
    val toIndex: Int,
    val lineCount: Int = 1
)

data class Line(
    val sx: Float,
    val sy: Float,
    val ex: Float,
    val ey: Float
)

fun parseLinesString(input: String): List<LineInfo> {
    return input.split(",").map { entry ->
        val parts = entry.split(":")
        val indices = parts[0].split(".").map { it.toInt() }
        val fromIndex = indices[0]
        val toIndex = indices[1]
        val lineCount = parts[1].toInt()
        LineInfo(fromIndex, toIndex, lineCount)
    }
}

fun calculateLineCoords(
    sx: Float, sy: Float,
    ex: Float, ey: Float,
    linesOffset: Int,
    linesSpacing: Int,
    lineCount: Int
): List<Line> {
    val dx = (ex - sx)
    val dy = (ey - sy)
    val length = sqrt(dx * dx + dy * dy)
    val k = linesSpacing / length

    val offsetCount = 0.5f * (lineCount - 1)
    val lineOffsetX = k * dy
    val lineOffsetY = k * dx

    val unitDx = dx / length
    val unitDy = dy / length
    var startX = sx + lineOffsetX * offsetCount + linesOffset * unitDx
    var startY = sy - lineOffsetY * offsetCount + linesOffset * unitDy
    var endX = ex + lineOffsetX * offsetCount - linesOffset * unitDx
    var endY = ey - lineOffsetY * offsetCount - linesOffset * unitDy

    val linesList = mutableListOf<Line>()
    for (i in 0 until lineCount) {
        linesList.add(Line(startX, startY, endX, endY))
        startX -= lineOffsetX
        startY += lineOffsetY
        endX -= lineOffsetX
        endY += lineOffsetY
    }

    return linesList
}