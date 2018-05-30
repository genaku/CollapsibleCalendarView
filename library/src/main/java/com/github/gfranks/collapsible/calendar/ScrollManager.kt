package com.github.gfranks.collapsible.calendar

import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.widget.Scroller

class ScrollManager(private val mCalendarView: CollapsibleCalendarView) {

    private val mTouchSlop: Int
    private val mMinFlingVelocity: Int
    private val mMaxFlingVelocity: Int
    private val mScroller: Scroller = Scroller(mCalendarView.context)
    private var mDownX: Float = 0.toFloat()
    private var mDragStartX: Float = 0.toFloat()
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
        val action = ev.action

        if (!mCalendarView.isAllowStateChange) {
            return false
        }

        when (action) {
            MotionEvent.ACTION_DOWN -> return onDownEvent(ev)
            MotionEvent.ACTION_MOVE -> {
                mVelocityTracker?.addMovement(ev)
                return checkForSliding(ev)
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                finishMotionEvent()
                return false
            }
        }

        return false
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action

        if (!mCalendarView.isAllowStateChange) {
            return true
        }

        if (action == MotionEvent.ACTION_MOVE) {
            mVelocityTracker?.addMovement(event)
        }

        if (mState == State.DRAGGING) {
            when (action) {
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = calculateXDistanceForDrag(event)
                    mProgressManager?.applyDelta(deltaX.toFloat())
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> finishMotionEvent()
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            checkForSliding(event)
        }

        return true
    }

    /**
     * Triggered
     *
     * @param event Down event
     */
    private fun onDownEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN) {
            throw IllegalStateException("Has to be down event!")
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        } else {
            mVelocityTracker?.clear()
        }

        mDownX = event.x

        if (mScroller.isFinished) {
            return false
        }

        mScroller.forceFinished(true)
        mDragStartX = if (mScroller.finalX == 0) {
            mDownX + mScroller.startX - mScroller.currX
        } else {
            mDownX - mScroller.currX
        }
        mState = State.DRAGGING
        return true
    }

    fun recycle() {
        if (mVelocityTracker != null) {
            mVelocityTracker?.recycle()
            mVelocityTracker = null
        }
    }

    private fun checkForSliding(ev: MotionEvent): Boolean {
        if (mState == State.DRAGGING) {
            return true
        }

        val xDIff = calculateXDistance(ev)

        if (Math.abs(xDIff) > mTouchSlop) {
            mState = State.DRAGGING
            mDragStartX = ev.x

            startSliding()

            return true
        }

        return false
    }

    private fun startSliding() {
        if (mProgressManager == null) {

/*
            val manager = mCalendarView.manager
            val state = manager.state

            val weekOfMonth = manager.weekOfMonth

            if (state === CollapsibleState.WEEK) { // always animate in month view
                manager.toggleView()
                mCalendarView.populateLayout()
            }

            mProgressManager = ProgressManagerImpl(mCalendarView, weekOfMonth,
                    state === CollapsibleState.MONTH)
*/
        }
    }

    private fun finishMotionEvent() {
        if (mProgressManager != null && mProgressManager?.isInitialized == true) {
            startScrollingX()
        }
    }

    private fun startScrollingX() {
        mVelocityTracker?.computeCurrentVelocity(1000, mMaxFlingVelocity.toFloat())
        val velocity = mVelocityTracker?.xVelocity?.toInt() ?: 0

        if (!mScroller.isFinished) {
            mScroller.forceFinished(true)
        }

        val progress = mProgressManager?.currentHeight ?: 0
        val end = if (Math.abs(velocity) > mMinFlingVelocity) {
            if (velocity > 0) {
                (mProgressManager?.endSize ?: 0) - progress
            } else {
                -progress
            }
        } else {
            val endSize = mProgressManager?.endSize ?: 0
            if (endSize / 2 <= progress) {
                endSize - progress
            } else {
                -progress
            }
        }

        mScroller.startScroll(0, progress, 0, end)
        mCalendarView.postInvalidate()

        mState = State.SETTLING
    }

    private fun calculateXDistance(event: MotionEvent): Int {
        return (event.x - mDownX).toInt()
    }

    private fun calculateXDistanceForDrag(event: MotionEvent): Int {
        return (event.x - mDragStartX).toInt()
    }

    fun onDraw() {
        if (!mScroller.isFinished) {
            mScroller.computeScrollOffset()

            val endSize = mProgressManager?.endSize ?: 0
            if (endSize != 0) {
                val position = mScroller.currY * 1f / endSize
                mProgressManager?.apply(position)
            }
            mCalendarView.postInvalidate()
        } else if (mState == State.SETTLING) {
            mState = State.IDLE
            val position = mScroller.currY * 1f / mProgressManager!!.endSize
            mProgressManager?.finish(position > 0)
            mProgressManager = null
        }
    }

    private enum class State {
        IDLE,
        DRAGGING,
        SETTLING
    }

}
