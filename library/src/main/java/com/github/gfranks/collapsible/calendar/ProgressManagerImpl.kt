package com.github.gfranks.collapsible.calendar

import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver

import com.github.gfranks.collapsible.calendar.viewholder.AbstractViewHolder
import com.github.gfranks.collapsible.calendar.viewholder.SizeViewHolder
import com.github.gfranks.collapsible.calendar.viewholder.StubViewHolder

internal class ProgressManagerImpl(
        calendarView: CollapsibleCalendarView,
        activeWeek: Int,
        fromMonth: Boolean
) : ProgressManager(calendarView, activeWeek, fromMonth) {

    init {
        if (!fromMonth) {
            initMonthView()
        } else {
            initWeekView()
        }
    }

    override fun finish(expanded: Boolean) {
        mCalendarView.post({
            // to prevent flickering
            mCalendarView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            mWeeksView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

            for (view in mViews) {
                view.onFinish(true)
            }

            if (!expanded) {
                val manager = mCalendarView.manager
                if (mFromMonth) {
                    manager.toggleView()
                } else {
                    manager.toggleToWeek(activeIndex)
                }
                mCalendarView.populateLayout()
            }
        })
    }

    private fun initMonthView() {
        mCalendarHolder = SizeViewHolder(mCalendarView.height, 0)
        mCalendarHolder!!.view = mCalendarView
        mCalendarHolder!!.delay = 0f
        mCalendarHolder!!.duration = 1f

        mWeeksHolder = SizeViewHolder(mWeeksView.height, 0)
        mWeeksHolder!!.view = mWeeksView
        mWeeksHolder!!.delay = 0f
        mWeeksHolder!!.duration = 1f

        mCalendarView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                mCalendarView.viewTreeObserver.removeOnPreDrawListener(this)

                mCalendarHolder!!.maxHeight = mCalendarView.height
                mWeeksHolder!!.maxHeight = mWeeksView.height

                mCalendarView.layoutParams.height = mCalendarHolder!!.minHeight
                mWeeksView.layoutParams.height = mCalendarHolder!!.minHeight

                initializeChildren()

                isInitialized = true
                apply(0f)

                return false
            }
        })
    }

    private fun initWeekView() {

        mCalendarHolder = SizeViewHolder(0, mCalendarView.height)
        mCalendarHolder!!.view = mCalendarView
        mCalendarHolder!!.delay = 0f
        mCalendarHolder!!.duration = 1f

        mWeeksHolder = SizeViewHolder(0, mWeeksView.height)
        mWeeksHolder!!.view = mWeeksView
        mWeeksHolder!!.delay = 0f
        mWeeksHolder!!.duration = 1f

        initializeChildren()

        mCalendarView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                mCalendarView.viewTreeObserver.removeOnPreDrawListener(this)

                mCalendarHolder!!.minHeight = mCalendarView.height
                mWeeksHolder!!.minHeight = mWeeksView.height

                mCalendarView.layoutParams.height = mCalendarHolder!!.maxHeight
                mWeeksView.layoutParams.height = mCalendarHolder!!.maxHeight

                isInitialized = true
                apply(1f)

                return false
            }
        })
    }

    private fun initializeChildren() {

        val childCount = mWeeksView.childCount

        // FIXME do not assume that all views are the same height
        mViews.clear()
        for (i in 0 until childCount) {

            val view = mWeeksView.getChildAt(i)

            val activeIndex = activeIndex

            val holder: AbstractViewHolder
            if (i == activeIndex) {
                holder = StubViewHolder()
            } else {
                val tmpHolder = SizeViewHolder(0, view.height)

                val duration = mWeeksHolder!!.maxHeight - view.height

                if (i < activeIndex) {
                    tmpHolder.delay = view.top * 1.0f / duration
                } else {
                    tmpHolder.delay = (view.top - view.height) * 1.0f / duration
                }
                tmpHolder.duration = view.height * 1.0f / duration

                holder = tmpHolder

                view.visibility = View.GONE
            }

            holder.view = view

            mViews.add(i, holder)
        }
    }
}
