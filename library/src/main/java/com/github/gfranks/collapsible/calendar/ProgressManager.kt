package com.github.gfranks.collapsible.calendar

import android.widget.LinearLayout
import com.github.gfranks.collapsible.calendar.viewholder.AbstractViewHolder
import com.github.gfranks.collapsible.calendar.viewholder.SizeViewHolder

internal abstract class ProgressManager(
        protected var mCalendarView: CollapsibleCalendarView,
        protected val activeIndex: Int,
        val mFromMonth: Boolean
) {

    protected var mWeeksView: LinearLayout = mCalendarView.weeksView!!
    protected var mViews: ArrayList<AbstractViewHolder> = ArrayList()
    protected var mCalendarHolder: SizeViewHolder? = null
    protected var mWeeksHolder: SizeViewHolder? = null
    private var mListener: IInitListener? = null
    var isInitialized = false
        set(initialized) {
            field = initialized
            if (mListener != null && initialized) {
                mListener!!.onInit()
            }
        }

    val currentHeight: Int
        get() = mCalendarView.layoutParams.height - mCalendarHolder!!.minHeight

    val startSize: Int
        get() = 0

    val endSize: Int
        get() = mCalendarHolder!!.height

    fun setListener(listener: IInitListener?) {
        mListener = listener
    }

    fun applyDelta(delta: Float) {
        val progress = getProgress(getDeltaInBounds(delta))
        apply(progress)
    }

    fun apply(progress: Float) {
        mCalendarHolder!!.animate(progress)
        mWeeksHolder!!.animate(progress)

        // animate views if necessary
        if (mViews != null) {
            for (view in mViews) {
                view.animate(progress)
            }
        }

        // request layout
        mCalendarView.requestLayout()

    }

    abstract fun finish(expanded: Boolean)

    fun getProgress(distance: Int): Float {
        val height = mCalendarHolder!!.height
        return if (height == 0) {
            1f
        } else Math.max(0f, Math.min(distance * 1f / height, 1f))
    }

    private fun getDeltaInBounds(delta: Float): Int {
        return if (mFromMonth) {
            Math.max((-mCalendarHolder!!.height).toFloat(), Math.min(0f, delta)).toInt() + mCalendarHolder!!.height
        } else {
            Math.max(0f, Math.min(mCalendarHolder!!.height.toFloat(), delta)).toInt()
        }
    }

    interface IInitListener {
        fun onInit()
    }

}
