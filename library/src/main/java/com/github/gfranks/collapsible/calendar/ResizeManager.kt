package com.github.gfranks.collapsible.calendar

import android.support.v4.view.MotionEventCompat
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.widget.Scroller
import com.github.gfranks.collapsible.calendar.model.CollapsibleState

internal class ResizeManager(private val mCalendarView: CollapsibleCalendarView) {

    private val mTouchSlop: Int
    private val mMinFlingVelocity: Int
    private val mMaxFlingVelocity: Int
    private val mScroller: Scroller = Scroller(mCalendarView.context)
    private var mDownY: Float = 0.toFloat()
    private var mDragStartY: Float = 0.toFloat()
    private var mState = State.IDLE
    private var mVelocityTracker: VelocityTracker? = null
    private var mProgressManager: ProgressManager? = null

    init {
        val viewConfig = ViewConfiguration.get(mCalendarView.context)
        mTouchSlop = viewConfig.scaledTouchSlop
        mMinFlingVelocity = viewConfig.scaledMinimumFlingVelocity
        mMaxFlingVelocity = viewConfig.scaledMaximumFlingVelocity
    }

    fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val action = MotionEventCompat.getActionMasked(ev)

        if (!mCalendarView.isAllowStateChange) {
            return false
        }

        when (action) {
            MotionEvent.ACTION_DOWN -> return onDownEvent(ev)
            MotionEvent.ACTION_MOVE -> {
                mVelocityTracker!!.addMovement(ev)
                return checkForResizing(ev)
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                finishMotionEvent()
                return false
            }
        }

        return false
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        val action = MotionEventCompat.getActionMasked(event)

        if (!mCalendarView.isAllowStateChange) {
            return true
        }

        if (action == MotionEvent.ACTION_MOVE) {
            mVelocityTracker!!.addMovement(event)
        }

        if (mState == State.DRAGGING) {
            when (action) {
                MotionEvent.ACTION_MOVE -> {
                    val deltaY = calculateDistanceForDrag(event)
                    mProgressManager!!.applyDelta(deltaY.toFloat())
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> finishMotionEvent()
            }

        } else if (action == MotionEvent.ACTION_MOVE) {
            checkForResizing(event)
        }

        return true
    }

    /**
     * Triggered
     *
     * @param event Down event
     */
    private fun onDownEvent(event: MotionEvent): Boolean {
        if (MotionEventCompat.getActionMasked(event) != MotionEvent.ACTION_DOWN) {
            throw IllegalStateException("Has to be down event!")
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        } else {
            mVelocityTracker!!.clear()
        }

        mDownY = event.y

        if (!mScroller.isFinished) {
            mScroller.forceFinished(true)
            if (mScroller.finalY == 0) {
                mDragStartY = mDownY + mScroller.startY - mScroller.currY
            } else {
                mDragStartY = mDownY - mScroller.currY
            }
            mState = State.DRAGGING
            return true
        } else {
            return false
        }

    }

    fun recycle() {
        if (mVelocityTracker != null) {
            mVelocityTracker!!.recycle()
            mVelocityTracker = null
        }
    }

    fun checkForResizing(ev: MotionEvent): Boolean {
        if (mState == State.DRAGGING) {
            return true
        }

        val yDIff = calculateDistance(ev)

        if (Math.abs(yDIff) > mTouchSlop) {
            mState = State.DRAGGING
            mDragStartY = ev.y

            startResizing()

            return true
        }

        return false
    }

    private fun startResizing() {
        if (mProgressManager == null) {

            val manager = mCalendarView.manager
            val state = manager.state

            val weekOfMonth = manager.weekOfMonth

            if (state === CollapsibleState.WEEK) { // always animate in month view
                manager.toggleView()
                mCalendarView.populateLayout()
            }

            mProgressManager = ProgressManagerImpl(mCalendarView, weekOfMonth,
                    state === CollapsibleState.MONTH)
        }
    }

    private fun finishMotionEvent() {
        if (mProgressManager != null && mProgressManager!!.isInitialized) {
            startScrolling()
        }
    }

    private fun startScrolling() {
        mVelocityTracker!!.computeCurrentVelocity(1000, mMaxFlingVelocity.toFloat())
        val velocity = mVelocityTracker!!.yVelocity.toInt()

        if (!mScroller.isFinished) {
            mScroller.forceFinished(true)
        }

        val progress = mProgressManager!!.currentHeight
        val end: Int
        if (Math.abs(velocity) > mMinFlingVelocity) {

            if (velocity > 0) {
                end = mProgressManager!!.endSize - progress
            } else {
                end = -progress
            }

        } else {

            val endSize = mProgressManager!!.endSize
            if (endSize / 2 <= progress) {
                end = endSize - progress
            } else {
                end = -progress
            }

        }

        mScroller.startScroll(0, progress, 0, end)
        mCalendarView.postInvalidate()

        mState = State.SETTLING

    }

    private fun calculateDistance(event: MotionEvent): Int {
        return (event.y - mDownY).toInt()
    }

    private fun calculateDistanceForDrag(event: MotionEvent): Int {
        return (event.y - mDragStartY).toInt()
    }

    fun onDraw() {
        if (!mScroller.isFinished) {
            mScroller.computeScrollOffset()

            val endSize = mProgressManager!!.endSize
            if (endSize != 0) {
                val position = mScroller.currY * 1f / endSize
                mProgressManager!!.apply(position)
            }
            mCalendarView.postInvalidate()
        } else if (mState == State.SETTLING) {
            mState = State.IDLE
            val position = mScroller.currY * 1f / mProgressManager!!.endSize
            mProgressManager!!.finish(position > 0)
            mProgressManager = null
        }
    }

    fun toggle() {
        if (mProgressManager == null) {
            startResizing()
        }

        if (!mScroller.isFinished) {
            mScroller.forceFinished(true)
        }

        if (mProgressManager?.isInitialized == false) {
            mProgressManager?.setListener(object : ProgressManager.IInitListener {
                override fun onInit() {
                    testFinish()
                    mProgressManager?.setListener(null)
                }
            })
        } else {
            testFinish()
        }
    }

    private fun testFinish() {
        if (!mScroller.isFinished) {
            mScroller.forceFinished(true)
        }

        val progress = mProgressManager!!.currentHeight
        var end = 0
        val endSize = mProgressManager!!.endSize
        if (endSize / 2 > progress) {
            end += endSize
        }
        end -= progress

        mScroller.startScroll(0, progress, 0, end)
        mCalendarView.postInvalidate()

        mState = State.SETTLING
    }

    private enum class State {
        IDLE,
        DRAGGING,
        SETTLING
    }
}
