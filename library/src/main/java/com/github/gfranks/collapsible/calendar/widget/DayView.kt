package com.github.gfranks.collapsible.calendar.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import com.github.gfranks.collapsible.calendar.R

class DayView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    var isCurrent: Boolean = false
        set(current) {
            if (isCurrent != current) {
                field = current
                refreshDrawableState()
            }
        }

    private var mHasEvent: Boolean = false

    private var mEventIndicatorColor = Color.DKGRAY
    private lateinit var mDayViewText: TextView
    private lateinit var mEventIndicator: ImageView

    init {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        mDayViewText = findViewById<View>(R.id.day_view_text) as TextView
        mEventIndicator = findViewById<View>(R.id.day_view_indicator) as ImageView

        updateEventIndicatorDrawable()
        mEventIndicator.visibility = View.INVISIBLE
    }

    fun setSelected(selected: Boolean, selectedBackgroundColor: Int, selectedTextColor: Int, normalTextColor: Int) {
        super.setSelected(selected)
        val drawable = background as GradientDrawable
        if (selected) {
            setTextColor(selectedTextColor)
            drawable.setColor(selectedBackgroundColor)
        } else {
            setTextColor(normalTextColor)
            drawable.setColor(Color.TRANSPARENT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            background = drawable
        } else {
            setBackgroundDrawable(drawable)
        }
    }

    fun setText(text: String) {
        mDayViewText.text = text
    }

    fun setText(resId: Int) {
        mDayViewText.setText(resId)
    }

    fun setTextColor(color: Int) {
        mDayViewText.setTextColor(color)
    }

    fun setEventIndicatorColor(color: Int) {
        mEventIndicatorColor = color
        updateEventIndicatorDrawable()
    }

    fun setHasEvent(hasEvent: Boolean) {
        mHasEvent = hasEvent
        getChildAt(1).visibility = if (mHasEvent) View.VISIBLE else View.INVISIBLE
    }

    public override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val state = super.onCreateDrawableState(extraSpace + 1)

        if (isCurrent) {
            View.mergeDrawableStates(state, STATE_CURRENT)
        }

        return state
    }

    private fun updateEventIndicatorDrawable() {
        val drawable = mEventIndicator.drawable ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            context.getDrawable(R.drawable.bg_indicator)
        } else {
            resources.getDrawable(R.drawable.bg_indicator)
        }
        if (drawable is GradientDrawable) {
            drawable.setColor(mEventIndicatorColor)
        }
        mEventIndicator.setImageDrawable(drawable)
    }

    companion object {
        private val STATE_CURRENT = intArrayOf(R.attr.state_current)
    }
}
