package com.example.organica20.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
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
    var placements: List<Placement> = emptyList()
    var linesInfo: List<LineInfo> = emptyList()
    var placementString: String = ""
        set(value) {
            val parsed = parseElementsString(value)
            placements = parsed.first
            linesInfo = parsed.second
        }
    var additionalLinesString: String = ""
        set(value) {
            linesInfo = parseAdditionalLinesString(value)
        }
    var linesOffset: Int = 0
    var elementsSpacing: Int = 0
    var linesSpacing: Int = 10.dpToPx()
    val mainChildPosition: MainChildPosition = MainChildPosition(0)
    var elementsBounds: Array<IntArray>? = null
    var paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }
    private val density = resources.displayMetrics.density

    init {
        setBackgroundColor(Color.TRANSPARENT)
        context.withStyledAttributes(attrs, R.styleable.PlaceLayout) {
            placementString = getString(R.styleable.PlaceLayout_placement) ?: ""
            additionalLinesString = getString(R.styleable.PlaceLayout_additionalLines) ?: ""
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
            mainChildPosition.position = getInt(
                R.styleable.PlaceLayout_zeroElementPosition,
                mainChildPosition.position
            )
            mainChildPosition.xOffset = getDimensionPixelSize(
                R.styleable.PlaceLayout_zeroElementOffsetX,
                mainChildPosition.xOffset
            )
            mainChildPosition.yOffset = getDimensionPixelSize(
                R.styleable.PlaceLayout_zeroElementOffsetY,
                mainChildPosition.yOffset
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
        }
        val mainChild = getChildAt(0)
        if (elementsBounds == null || elementsBounds!!.size != childCount) {
            elementsBounds = Array(childCount) { IntArray(childCount) { 0 } }
        }
        elementsBounds!![0][0] = mainChild.measuredWidth / 2
        elementsBounds!![0][1] = mainChild.measuredHeight / 2
        elementsBounds!![0][2] = - mainChild.measuredWidth / 2
        elementsBounds!![0][3] = - mainChild.measuredHeight / 2
        for (placement in placements) {
            val fromChild = getChildAt(placement.fromIndex)
            val toChild = getChildAt(placement.toIndex)
            val fromArray = elementsBounds!![placement.fromIndex]
            val toArray = elementsBounds!![placement.toIndex]

            val mRadius = getCircumcircleRadius(fromChild.measuredWidth, fromChild.measuredHeight)
            val sRadius = getCircumcircleRadius(toChild.measuredWidth, toChild.measuredHeight)
            val customSpacing = placement.spacing?.let { (it * density).toInt() }
            val spacing = ((customSpacing ?: elementsSpacing) + (mRadius + sRadius) / 2).toInt()

            val sine = sin(Math.toRadians(placement.angle.toDouble()))
            val cosine = if (placement.angle in 90..270) {
                 - sqrt(1 - sine * sine)
            } else {
                sqrt(1 - sine * sine)
            }
            val yOffset = (spacing * sine).toInt()
            val xOffset = (spacing * cosine).toInt()
            val fromX = (fromArray[0] - fromChild.measuredWidth / 2)
            val fromY = (fromArray[1] - fromChild.measuredHeight / 2)
            var toX = fromX + xOffset
            var toY = fromY + yOffset
            toArray[0] = toX + toChild.measuredWidth / 2
            toArray[1] = toY + toChild.measuredHeight / 2
            toArray[2] = toX - toChild.measuredWidth / 2
            toArray[3] = toY - toChild.measuredHeight / 2
        }
        var left = 0
        var up = 0
        var right = 0
        var down = 0
        for (bound in elementsBounds!!) {
            right = right.coerceAtLeast(bound[0])
            up = up.coerceAtLeast(bound[1])
            left = left.coerceAtMost(bound[2])
            down = down.coerceAtMost(bound[3])
        }
        val evaluatedWidth = right - left + paddingLeft + paddingRight
        val evaluatedHeight = up - down + paddingTop + paddingBottom
        setMeasuredDimension(
            resolveSize(evaluatedWidth, widthMeasureSpec),
            resolveSize(evaluatedHeight, heightMeasureSpec)
        )
    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (isEmpty()) return

        val parentWidth = r - l
        val parentHeight = b - t
        val mainChild = getChildAt(0)
        var (mainChildLeft, mainChildTop) = getMainChildPosition(parentWidth, parentHeight, mainChild.measuredWidth, mainChild.measuredHeight)

        mainChildLeft += mainChildPosition.xOffset
        mainChildTop += mainChildPosition.yOffset

        mainChild.layout(
            mainChildLeft,
            mainChildTop,
            mainChildLeft + mainChild.measuredWidth,
            mainChildTop + mainChild.measuredHeight
        )

        for (placement in placements) {
            val fromChild = getChildAt(placement.fromIndex)
            val toChild = getChildAt(placement.toIndex)
            val fromCenterX = fromChild.left + fromChild.width / 2
            val fromCenterY = fromChild.top + fromChild.height / 2

            var toCenterX: Int
            var toCenterY: Int

            val customSpacing = placement.spacing?.let { (it * resources.displayMetrics.density).toInt() }

            val mRadius = getCircumcircleRadius(fromChild.width, fromChild.height)
            val sRadius = getCircumcircleRadius(toChild.measuredWidth, toChild.measuredHeight)
            val radialSpacing = (customSpacing ?: elementsSpacing) + (mRadius + sRadius) / 2
            toCenterX =
                fromCenterX + (cos(Math.toRadians(placement.angle.toDouble())) * radialSpacing).toInt()
            toCenterY =
                fromCenterY + (sin(Math.toRadians(placement.angle.toDouble())) * radialSpacing).toInt()

            toChild.layout(
                toCenterX - toChild.measuredWidth / 2,
                toCenterY - toChild.measuredHeight / 2,
                toCenterX + toChild.measuredWidth / 2,
                toCenterY + toChild.measuredHeight / 2
            )
        }
    }


    override fun onDraw(canvas: Canvas) {
        for (lineInfo in linesInfo) {
            val fromChild = getChildAt(lineInfo.fromIndex)
            val toChild = getChildAt(lineInfo.toIndex)
            if (fromChild == null || toChild == null) {
                continue
            }
            val mCenterX = (fromChild.left + fromChild.width / 2).toFloat()
            val mCenterY = (fromChild.top + fromChild.height / 2).toFloat()
            val sCenterX = (toChild.left + toChild.width / 2).toFloat()
            val sCenterY = (toChild.top + toChild.height / 2).toFloat()

            val lines = calculateLineCoordinates(
                mCenterX, mCenterY,
                sCenterX, sCenterY,
                fromChild.width, fromChild.height,
                toChild.width, toChild.height,
                lineInfo.lineCount,
                lineInfo.startOffset?.let { (it * resources.displayMetrics.density).toInt() },
                lineInfo.endOffset?.let { (it * resources.displayMetrics.density).toInt() }
            )

            for (line in lines) {
                canvas.drawLine(line.sx, line.sy, line.ex, line.ey, paint)
            }
        }
    }

    private fun calculateLineCoordinates(
        sx: Float, sy: Float,
        ex: Float, ey: Float,
        sw: Int, sh: Int,
        ew: Int, eh: Int,
        lineCount: Int,
        startOffset: Int?,
        endOffset: Int?
    ): List<Line> {
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
        val (xSign, ySign) = when (angleDegrees) {
            in 0.0..90.0 -> Pair(1, 1)
            in 90.0..180.0 -> Pair(-1, 1)
            in 180.0..270.0 -> Pair(-1, -1)
            in 270.0..360.0 -> Pair(1, -1)
            else -> throw IllegalArgumentException("Invalid angle: $angleDegrees")
        }
        if (tg.absoluteValue <= tgStart.absoluteValue) {
            startX = sx + xSign * (sw / 2)
            startY = sy + ySign * (tg.absoluteValue * (sw / 2))
        } else {
            startY = sy + xSign * (sh / 2)
            startX = sx + ySign * (sh / tg.absoluteValue / 2)
        }
        if (tg.absoluteValue <= tgEnd.absoluteValue) {
            endX = ex - xSign * (ew / 2)
            endY = ey - ySign * (tg.absoluteValue * (ew / 2))
        } else {
            endY = ey - xSign * (eh / 2)
            endX = ex - ySign * (eh / tg.absoluteValue / 2)
        }
        dx = endX - startX
        dy = endY - startY

        val length = sqrt(dx * dx + dy * dy)
        val ratioOfSimilarity = linesSpacing / length

        val offsetCount = 0.5f * (lineCount - 1)
        val lineOffsetX = ratioOfSimilarity * dy
        val lineOffsetY = ratioOfSimilarity * dx

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

        val linesList = mutableListOf<Line>()
        repeat(lineCount) {
            linesList.add(Line(startX, startY, endX, endY))
            startX -= lineOffsetX
            startY += lineOffsetY
            endX -= lineOffsetX
            endY += lineOffsetY
        }
        return linesList
    }
    
    private fun getMainChildPosition(parentWidth: Int, parentHeight: Int, mainChildWidth: Int, mainChildHeight: Int): Pair<Int, Int> {
        return when (mainChildPosition.position) {
            0 -> Pair(0 + paddingLeft, 0 + paddingTop) // LeftTop
            1 -> Pair((parentWidth - mainChildWidth) / 2, 0 + paddingTop) // CenterTop
            2 -> Pair(parentWidth - mainChildWidth - paddingRight, 0 + paddingTop) // RightTop
            3 -> Pair(0 + paddingLeft, (parentHeight - mainChildHeight) / 2) // LeftCenter
            4 -> Pair((parentWidth - mainChildWidth) / 2, (parentHeight - mainChildHeight) / 2) // Center
            5 -> Pair(parentWidth - mainChildWidth - paddingRight, (parentHeight - mainChildHeight) / 2) // RightCenter
            6 -> Pair(0 + paddingLeft, parentHeight - mainChildHeight - paddingBottom) // LeftBottom
            7 -> Pair((parentWidth - mainChildWidth) / 2, parentHeight - mainChildHeight - paddingBottom) // CenterBottom
            8 -> Pair(parentWidth - mainChildWidth - paddingRight, parentHeight - mainChildHeight - paddingBottom) // RightBottom
            else -> throw IllegalArgumentException("Invalid main child position argument: expected from 0 to 8, got ${mainChildPosition.position}")
        }
    }

    private fun parseElementsString(elementsString: String): Pair<List<Placement>, List<LineInfo>> {
        if (elementsString.isEmpty()) {
            return Pair(emptyList(), emptyList())
        }
        val placements = mutableListOf<Placement>()
        val lines = mutableListOf<LineInfo>()
        elementsString.split(" ").forEach { entry ->
            val parts = entry.split("_")

            val indices = parts[0].split(".")
            require(indices.size >= 2) { "Invalid placement argument: element index is not specified or is not Integer: $entry" }
            val fromIndex = indices[0].toIntOrNull()
                ?: throw IllegalArgumentException("Invalid fromIndex in placement: $entry")
            val toIndex = indices[1].toIntOrNull()
                ?: throw IllegalArgumentException("Invalid toIndex in placement: $entry")

            val placementInfo = parts.getOrNull(1)?.split(".")
            var angle = placementInfo?.getOrNull(0)?.toIntOrNull()
                ?: throw IllegalArgumentException("Invalid angle in placement: $entry")
            angle = (angle % 360 + 360) % 360
            val spacing = placementInfo.getOrNull(1)?.toIntOrNull()

            val linesInfo = parts.getOrNull(2)?.split(".")?.map { it.toIntOrNull() }
            val lineCount = linesInfo?.getOrNull(0) ?: 1
            val startOffset = linesInfo?.getOrNull(1)
            val endOffset = linesInfo?.getOrNull(2)

            placements.add(Placement(fromIndex, toIndex, angle, spacing))
            lines.add(LineInfo(fromIndex, toIndex, lineCount, startOffset, endOffset))
        }
        return Pair(placements, lines)
    }
    private fun parseAdditionalLinesString(additionalLinesInfo: String): List<LineInfo> {
        val mutableLinesInfo = linesInfo.toMutableList()
        if (additionalLinesInfo.isNotEmpty()) {
            additionalLinesInfo.split(" ").forEach { entry ->
                val parts = entry.split("_")

                val indices = parts[0].split(".")
                require(indices.size >= 2) { "Invalid additionalLines argument: element index is not specified or is not Integer: $entry" }
                val fromIndex = indices[0].toIntOrNull() ?: throw IllegalArgumentException("Invalid fromIndex in additionalLines: $entry")
                val toIndex = indices[1].toIntOrNull() ?: throw IllegalArgumentException("Invalid toIndex in additionalLines: $entry")

                val linesInfo = parts.getOrNull(1)?.split(".")?.map { it.toIntOrNull() }
                val lineCount = linesInfo?.getOrNull(0) ?: 1
                val startOffset = linesInfo?.getOrNull(1)
                val endOffset = linesInfo?.getOrNull(2)

                mutableLinesInfo.add(LineInfo(fromIndex, toIndex, lineCount, startOffset, endOffset))
            }
        }
        return mutableLinesInfo
    }

    fun Int.dpToPx(): Int {
        return (this * density).toInt()
    }
}

data class Placement(
    val fromIndex: Int,
    val toIndex: Int,
    val angle: Int,
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

data class MainChildPosition(
    var position: Int,
    var xOffset: Int = 0,
    var yOffset: Int = 0
) {
    init {
        require(position in 0..8) { "Invalid main child position argument: expected from 0 to 8, got $position" }
    }
}