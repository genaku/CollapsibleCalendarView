package com.github.gfranks.collapsible.calendar.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

class WeekView(context: Context, attrs: AttributeSet) : ViewGroup(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)

        val maxSize = widthSize / 7
        var baseSize = 0

        val cnt = childCount
        for (i in 0 until cnt) {

            val child = getChildAt(i)

            if (child.visibility == View.GONE) {
                continue
            }

            child.measure(
                    View.MeasureSpec.makeMeasureSpec(maxSize, View.MeasureSpec.AT_MOST),
                    View.MeasureSpec.makeMeasureSpec(maxSize, View.MeasureSpec.AT_MOST)
            )

            baseSize = Math.max(baseSize, child.measuredHeight)

        }

        for (i in 0 until cnt) {

            val child = getChildAt(i)

            if (child.visibility == View.GONE) {
                continue
            }

            child.measure(
                    View.MeasureSpec.makeMeasureSpec(baseSize, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(baseSize, View.MeasureSpec.EXACTLY)
            )

        }

        setMeasuredDimension(widthSize, if (layoutParams.height >= 0) layoutParams.height else baseSize + paddingBottom + paddingTop)

    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {

        val cnt = childCount

        val width = measuredWidth
        val part = width / cnt

        for (i in 0 until cnt) {

            val child = getChildAt(i)
            if (child.visibility == View.GONE) {
                continue
            }

            val childWidth = child.measuredWidth

            val x = i * part + (part - childWidth) / 2
            child.layout(x, 0, x + childWidth, child.measuredHeight)

        }

    }
}
