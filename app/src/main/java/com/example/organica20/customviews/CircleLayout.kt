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
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class CircleLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr){

    private var angle: Float = 0f
    private var radiusMultiplier: Int = 4
    private var linesPlacement: String = ""
    private var lineOffset: Float = 1f
    private var isAngleComputed: Boolean = false
    private val paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    init {
        setBackgroundColor(Color.TRANSPARENT)
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleLayout)
        angle = typedArray.getFloat(
            R.styleable.CircleLayout_angle,
            0f
        )
        radiusMultiplier = typedArray.getInteger(
            R.styleable.CircleLayout_radiusMultiplier,
            4
        )
        val linePlacementArgument = typedArray.getString(
            R.styleable.CircleLayout_linesPlacement
        )
        if (linePlacementArgument != null) {
            linesPlacement = linePlacementArgument
        }
        lineOffset = typedArray.getFloat(
            R.styleable.CircleLayout_lineOffset,
            1f
        )
        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (!isAngleComputed) {
            angle = if (angle == 0f) {
                (2 * Math.PI / (childCount - 1)).toFloat()
            } else {
                Math.toRadians(angle.toDouble()).toFloat()
            }
            isAngleComputed = true
        }

        var desiredSize = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            desiredSize = max(desiredSize, max(child.measuredWidth, child.measuredHeight))
        }

        desiredSize *= radiusMultiplier

        val measuredWidth = when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize((widthMeasureSpec))
            MeasureSpec.AT_MOST -> min(MeasureSpec.getSize((widthMeasureSpec)), desiredSize)
            else -> desiredSize
        }

        val measuredHeight = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize((heightMeasureSpec))
            MeasureSpec.AT_MOST -> min(MeasureSpec.getSize((heightMeasureSpec)), desiredSize)
            else -> desiredSize
        }

        val size = max(measuredWidth, measuredHeight)

        setMeasuredDimension(size, size)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (childCount == 0) return

        val centerX = (r - l) / 2
        val centerY = (b - t) / 2
        val radius = centerX.coerceAtMost(centerY).toFloat()

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight
            val angle = (i - 1) * angle

            val childCenterX = if (i != 0) {
                centerX + (cos(angle) * (radius - childWidth / 2)).toInt()
            } else {
                centerX
            }
            val childCenterY = if (i != 0) {
                centerY + (sin(angle) * (radius - childHeight / 2)).toInt()
            } else {
                centerY
            }

            child.layout(
                childCenterX - childWidth / 2,
                childCenterY - childHeight / 2,
                childCenterX + childWidth / 2,
                childCenterY + childHeight / 2
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (childCount == 0) return

        val mElement = getChildAt(0)
        val mCircumcircleRadius: Float = getCircumcircleRadius(mElement.width, mElement.height)
        val mCenterX: Int = mElement.left + mElement.width / 2
        val mCenterY: Int = mElement.top + mElement.height / 2

        for (i in 1 until childCount) {
            val lineCount = if (linesPlacement.length > (i - 1)) {
                linesPlacement[i - 1].digitToInt()
            } else {
                1
            }
            val iAngle = (i - 1) * angle
            val cosIAngle = cos(iAngle)
            val sinIAngle = sin(iAngle)

            val offsetCount = 0.5f * (lineCount - 1)
            val lineOffsetX = lineOffset * cos(Math.PI / 2 + iAngle).toFloat()
            val lineOffsetY = lineOffset * sin(Math.PI / 2 + iAngle).toFloat()

            val child = getChildAt(i)
            val cCenterX = child.left + child.width / 2
            val cCenterY = child.top + child.height / 2
            val cCircumcircleRadius = getCircumcircleRadius(child.width, child.height)

            var startX = mCenterX + cosIAngle * mCircumcircleRadius - offsetCount * lineOffsetX
            var startY = mCenterY + sinIAngle * mCircumcircleRadius - offsetCount * lineOffsetY
            var endX = cCenterX - cosIAngle * cCircumcircleRadius - offsetCount * lineOffsetX
            var endY = cCenterY - sinIAngle * cCircumcircleRadius - offsetCount * lineOffsetY

            for (j in 0 until lineCount) {
                canvas.drawLine(startX, startY, endX, endY, paint)
                startX += lineOffsetX
                startY += lineOffsetY
                endX += lineOffsetX
                endY += lineOffsetY
            }
        }
    }
}