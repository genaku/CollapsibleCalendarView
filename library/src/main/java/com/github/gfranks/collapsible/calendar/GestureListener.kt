package com.github.gfranks.collapsible.calendar

import android.view.GestureDetector
import android.view.MotionEvent

internal class GestureListener(private val collapsibleCalendarView: CollapsibleCalendarView) : GestureDetector.SimpleOnGestureListener() {

    override fun onDown(e: MotionEvent?): Boolean {
        return true
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        e1 ?: return false
        e2 ?: return false

        try {
            val distanceX = e2.x - e1.x
            val distanceY = e2.y - e1.y
            if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (distanceX > 0) {
                    collapsibleCalendarView.prev()
                } else {
                    collapsibleCalendarView.next()
                }
                return true
            }
            return false
        } catch (t: Throwable) {
            t.printStackTrace()
        }

        return false
    }

    companion object {
        private const val SWIPE_DISTANCE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
    }

}
