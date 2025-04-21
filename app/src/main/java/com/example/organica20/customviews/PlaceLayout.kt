package com.example.organica20.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import com.example.organica20.R
import com.example.organica20.utils.getCircumcircleRadius
import kotlin.math.absoluteValue
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.withSign
import androidx.core.content.withStyledAttributes
import androidx.core.view.isEmpty

class PlaceLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    lateinit var placementString: String
    lateinit var placements: List<Placement>
    lateinit var additionalLinesString: String
    lateinit var linesInfo: List<LineInfo>
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
        context.withStyledAttributes(attrs, R.styleable.PlaceLayout) {
            placementString = getString(
                R.styleable.PlaceLayout_placement
            ) ?: ""
            additionalLinesString = getString(
                R.styleable.PlaceLayout_additionalLines
            ) ?: ""
            val (parsedPlacements, parsedLinesInfo) = parseElementsString(
                placementString,
                additionalLinesString
            )
            placements = parsedPlacements
            linesInfo = parsedLinesInfo
            linesOffset = getDimensionPixelSize(
                R.styleable.PlaceLayout_linesOffset,
                linesOffset
            )
            elementsSpacing = getDimensionPixelSize(
                R.styleable.PlaceLayout_elementsSpacing,
                elementsSpacing
            )
            linesSpacing = getDimensionPixelSize(
                R.styleable.PlaceLayout_linesSpacing,
                linesSpacing
            )
            zeroElementPosition = getInt(
                R.styleable.PlaceLayout_zeroElementPosition,
                zeroElementPosition
            )
            zeroElementOffsetX = getDimensionPixelSize(
                R.styleable.PlaceLayout_zeroElementOffsetX,
                0
            )
            zeroElementOffsetY = getDimensionPixelSize(
                R.styleable.PlaceLayout_zeroElementOffsetY,
                0
            )
        }
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
        }
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (isEmpty()) return

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

        for (placement in placements) {
            val customSpacing = placement.spacing?.let { (it * resources.displayMetrics.density).toInt() }

            val mainChild = getChildAt(placement.fromIndex)
            val sideChild = getChildAt(placement.toIndex)
            val mCenterX = mainChild.left + mainChild.width / 2
            val mCenterY = mainChild.top + mainChild.height / 2

            var sCenterX = mCenterX
            var sCenterY = mCenterY

            if (placement.direction != null) {
                when (placement.direction) {
                    "R" -> sCenterX += (2 * (customSpacing ?: elementsSpacing) )
                    "L" -> sCenterX -= (2 * (customSpacing ?: elementsSpacing) )
                    "U" -> sCenterY -= (2 * (customSpacing ?: elementsSpacing) )
                    "D" -> sCenterY += (2 * (customSpacing ?: elementsSpacing) )
                }
            }
            if (placement.angle != null) {
                val mRadius = getCircumcircleRadius(mainChild.width, mainChild.height)
                val sRadius = getCircumcircleRadius(sideChild.measuredWidth, sideChild.measuredHeight)
                val radialSpacing = ( (customSpacing ?: elementsSpacing) + (mRadius + sRadius) / 2)
                sCenterX = mCenterX + (cos(Math.toRadians(placement.angle.toDouble())) * radialSpacing).toInt()
                sCenterY = mCenterY + (sin(Math.toRadians(placement.angle.toDouble())) * radialSpacing).toInt()
            }
            sideChild.layout(sCenterX - sideChild.measuredWidth / 2, sCenterY - sideChild.measuredHeight / 2,
                sCenterX + sideChild.measuredWidth / 2, sCenterY + sideChild.measuredHeight / 2)
        }
    }


    override fun onDraw(canvas: Canvas) {
        for (lineInfo in linesInfo) {
            val mainChild = getChildAt(lineInfo.fromIndex)
            val sideChild = getChildAt(lineInfo.toIndex)
            if (mainChild == null || sideChild == null) {
                continue
            }
            val mCenterX = (mainChild.left + mainChild.width / 2).toFloat()
            val mCenterY = (mainChild.top + mainChild.height / 2).toFloat()
            val sCenterX = (sideChild.left + sideChild.width / 2).toFloat()
            val sCenterY = (sideChild.top + sideChild.height / 2).toFloat()

            val lines = calculateLineCoords(
                mCenterX, mCenterY,
                sCenterX, sCenterY,
                mainChild.width, mainChild.height,
                sideChild.width, sideChild.height,
                lineInfo.lineCount,
                lineInfo.startOffset?.let { (it * resources.displayMetrics.density).toInt() },
                lineInfo.endOffset?.let { (it * resources.displayMetrics.density).toInt() }
            )

            for (line in lines) {
                canvas.drawLine(line.sx, line.sy, line.ex, line.ey, paint)
            }
        }
    }

    fun calculateLineCoords(
        sx: Float, sy: Float,
        ex: Float, ey: Float,
        sw: Int, sh: Int,
        ew: Int, eh: Int,
        lineCount: Int,
        startOffset: Int?,
        endOffset: Int?
    ): List<Line> {
        Log.d("LINES", "($sx, $sy), ($ex, $ey), $sw, $sh")
        var startX: Float
        var startY: Float
        var endX: Float
        var endY: Float
        var dx = ex - sx
        var dy = ey - sy
        val tg = dy / dx
        val tgStart = (sh / 2f) / (sw / 2f)
        val tgEnd = (eh / 2f) / (ew / 2f)
        val angleDegrees = when {
            tg == Float.POSITIVE_INFINITY -> 90f
            tg == Float.NEGATIVE_INFINITY -> 270f
            tg == 0.0f && 1.0f.withSign(tg) < 0 -> 180f
            tg == 0.0f -> 0f
            else -> {
                val priorAngle = ((Math.toDegrees(atan(tg).toDouble()) % 360 + 360) % 360).toFloat()
                if (dx < 0) (180f + priorAngle) % 360
                else priorAngle
            }
        }
        when (angleDegrees) {
            in 0.0..90.0 -> {
                if (tg.absoluteValue <= tgStart.absoluteValue) {
                    startX = sx + sw / 2
                    startY = sy + tg.absoluteValue * (sw / 2)
                } else {
                    startY = sy + sh / 2
                    startX = sx + sh / tg.absoluteValue / 2
                }
            }

            in 90.0..180.0 -> {
                if (tg.absoluteValue <= tgStart.absoluteValue) {
                    startX = sx - sw / 2
                    startY = sy + tg.absoluteValue * sw / 2
                } else {
                    startY = sy + sh / 2
                    startX = sx - sh / tg.absoluteValue / 2
                }
            }

            in 180.0..270.0 -> {
                if (tg.absoluteValue <= tgStart.absoluteValue) {
                    startX = sx - sw / 2
                    startY = sy - tg.absoluteValue * sw / 2
                } else {
                    startY = sy - sh / 2
                    startX = sx - sh / tg.absoluteValue / 2
                }
            }

            in 270.0..360.0 -> {
                if (tg.absoluteValue <= tgStart.absoluteValue) {
                    startX = sx + sw / 2
                    startY = sy - tg.absoluteValue * sw / 2
                } else {
                    startY = sy - sh / 2
                    startX = sx + sh / tg.absoluteValue / 2
                }
            }
            else -> {
                throw IllegalArgumentException("Invalid angle: $angleDegrees")
            }
        }
        when (angleDegrees) {
            in 0.0..90.0 -> {
                if (tg.absoluteValue <= tgEnd.absoluteValue) {
                    endX = ex - ew / 2
                    endY = ey - tg.absoluteValue * (ew / 2)
                } else {
                    endY = ey - eh / 2
                    endX = ex - eh / tg.absoluteValue / 2
                }
            }

            in 90.0..180.0 -> {
                if (tg.absoluteValue <= tgEnd.absoluteValue) {
                    endX = ex + ew / 2
                    endY = ey - tg.absoluteValue * (ew / 2)
                } else {
                    endY = ey - eh / 2
                    endX = ex + eh / tg.absoluteValue / 2
                }
            }

            in 180.0..270.0 -> {
                if (tg.absoluteValue <= tgEnd.absoluteValue) {
                    endX = ex + ew / 2
                    endY = ey + tg.absoluteValue * (ew / 2)
                } else {
                    endY = ey + eh / 2
                    endX = ex + eh / tg.absoluteValue / 2
                }
            }

            in 270.0..360.0 -> {
                if (tg.absoluteValue <= tgEnd.absoluteValue) {
                    endX = ex - ew / 2
                    endY = ey + tg.absoluteValue * (ew / 2)
                } else {
                    endY = ey + eh / 2
                    endX = ex - eh / tg.absoluteValue / 2
                }
            }
            else -> {
                throw IllegalArgumentException("Invalid angle: $angleDegrees")
            }
        }
        dx = endX - startX
        dy = endY - startY

        val length = sqrt(dx * dx + dy * dy)
        val k = linesSpacing / length

        val offsetCount = 0.5f * (lineCount - 1)
        val lineOffsetX = k * dy
        val lineOffsetY = k * dx

        val unitDx = dx / length
        val unitDy = dy / length
        startX += (startOffset ?: linesOffset) * unitDx
        startY += (startOffset ?: linesOffset) * unitDy
        endX -= (endOffset ?: linesOffset) * unitDx
        endY -= (endOffset ?: linesOffset) * unitDy
        startX += (lineOffsetX * offsetCount)
        startY -= (lineOffsetY * offsetCount)
        endX += (lineOffsetX * offsetCount)
        endY -= (lineOffsetY * offsetCount)
        Log.d("LINES", "($startX, $startY), ($endX, $endY)")
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

    fun parseElementsString(input: String, additionalLinesInfo: String = ""): Pair<List<Placement>, List<LineInfo>> {
        if (input.isEmpty()) {
            return Pair(emptyList(), emptyList())
        }
        val placements = mutableListOf<Placement>()
        val lines = mutableListOf<LineInfo>()
        input.split(",").map { entry ->
            val parts = entry.split("_")

            val indices = parts[0].split(".").map { it.toInt() }
            val fromIndex = indices[0]
            val toIndex = indices[1]

            val placementInfo = parts[1].split(".")
            val direction = if (placementInfo[0].matches(Regex("[RLUD]"))) placementInfo[0] else null
            val angle = placementInfo[0].toFloatOrNull()
            val spacing = placementInfo.getOrNull(1)?.toIntOrNull()

            val linesInfo = (parts.getOrNull(2) ?: "1").split(".").map { it.toInt() }
            val lineCount = linesInfo[0]
            val startOffset = linesInfo.getOrNull(1)
            val endOffset = linesInfo.getOrNull(2)

            placements.add(Placement(fromIndex, toIndex, direction, angle, spacing))
            lines.add(LineInfo(fromIndex, toIndex, lineCount, startOffset, endOffset))
        }
        if (additionalLinesInfo.isNotEmpty()) {
            additionalLinesInfo.split(",").map { entry ->
                Log.d("ADDITIONALLINES", entry)
                val parts = entry.split("_")
                Log.d("ADDITIONALLINES", "$parts")
                val indices = parts[0].split(".").map { it.toInt() }
                val fromIndex = indices[0]
                val toIndex = indices[1]

                val linesInfo = (parts.getOrNull(1) ?: "1").split(".").map { it.toInt() }
                val lineCount = linesInfo[0]
                val startOffset = linesInfo.getOrNull(1)
                val endOffset = linesInfo.getOrNull(2)

                lines.add(LineInfo(fromIndex, toIndex, lineCount, startOffset, endOffset))
            }
        }
        return Pair(placements, lines)
    }
}

data class Placement(
    val fromIndex: Int,
    val toIndex: Int,
    val direction: String? = null,
    val angle: Float? = null,
    val spacing: Int?
)

data class LineInfo(
    val fromIndex: Int,
    val toIndex: Int,
    val lineCount: Int = 1,
    val startOffset: Int? = null,
    val endOffset: Int? = null,
)

data class Line(
    val sx: Float,
    val sy: Float,
    val ex: Float,
    val ey: Float
)