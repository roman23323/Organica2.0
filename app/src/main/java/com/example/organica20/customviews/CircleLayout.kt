package com.example.organica20.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import com.example.organica20.R
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

class CircleLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr){

    private var angle: Float = 0f
    private var isAngleComputed: Boolean = false
    private var radiusMultiplier: Int = 4
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
        if (angle != 0f) {
            angle = Math.toRadians(angle.toDouble()).toFloat()
            isAngleComputed = true
        }
        radiusMultiplier = typedArray.getInteger(
            R.styleable.CircleLayout_radiusMultiplier,
            4
        )
        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (!isAngleComputed) {
            angle = (2 * Math.PI / (childCount - 1)).toFloat()
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
        val childCount = childCount
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
            Log.d("ON_LAYOUT", "Индекс: $i, (x, y): ($childCenterX, $childCenterY), угол: $angle")

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

        val mainChild = getChildAt(0)
        val mWidth = mainChild.width
        val mHeight = mainChild.height
        val mOffsetRadius = (mWidth.toDouble().div(2).pow(2) + mHeight.toDouble().div(2).pow(2)).pow(0.5).toFloat()

        val mCenterX = mainChild.left + mainChild.width / 2
        val mCenterY = mainChild.top + mainChild.height / 2

        for (i in 1 until childCount) {
            val child = getChildAt(i)
            val cWidth = child.width
            val cHeight = child.height
            val cCenterX = child.left + child.width / 2
            val cCenterY = child.top + child.height / 2
            val cOffsetRadius = (cWidth.toDouble().div(2).pow(2) + cHeight.toDouble().div(2).pow(2)).pow(0.5).toFloat()

            val angle = (i - 1) * angle

            val startX = mCenterX + cos(angle) * mOffsetRadius
            val startY = mCenterY + sin(angle) * mOffsetRadius
            val endX = (cCenterX + cos(2 * Math.PI - (Math.PI - angle)) * cOffsetRadius).toFloat()
            val endY = (cCenterY + sin(2 * Math.PI - (Math.PI - angle)) * cOffsetRadius).toFloat()

            canvas.drawLine(startX, startY, endX, endY, paint)
        }
    }
}