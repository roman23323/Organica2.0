package com.example.organica20.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class CircleLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr){
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var size = min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            size = size.coerceAtMost(3 * child.measuredWidth)
        }
        setMeasuredDimension(size, size)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val centerX = (r - l) / 2
        val centerY = (b - t) / 2
        val radius = centerX.coerceAtMost(centerY).toFloat()

        val childCount = childCount
        if (childCount == 0) return

        val angleStep = (2 * Math.PI / childCount).toFloat()

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight

            val angle = i * angleStep

            val childCenterX = centerX + (cos(angle) * (radius - childWidth / 2)).toInt()
            val childCenterY = centerY + (sin(angle) * (radius - childHeight / 2)).toInt()

            child.layout(
                childCenterX - childWidth / 2,
                childCenterY - childHeight / 2,
                childCenterX + childWidth / 2,
                childCenterY + childHeight / 2
            )
        }
    }
}