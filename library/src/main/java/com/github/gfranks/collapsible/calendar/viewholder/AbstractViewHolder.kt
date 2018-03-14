package com.github.gfranks.collapsible.calendar.viewholder

import android.view.View

abstract class AbstractViewHolder {

    var view: View? = null
    var delay: Float = 0F
    var duration: Float = 0F

    private var mAnimating = false

    protected val end: Float
        get() = delay + duration

    fun animate(time: Float) {
        when {
            shouldAnimate(time) -> {
                mAnimating = true
                onAnimate(getRelativeTime(time))
            }
            mAnimating -> {
                mAnimating = false
                onFinish(Math.round(getRelativeTime(time)) >= 1)
            }
            delay > time -> // FIXME this should only be called once
                onFinish(false)
            time > end -> // FIXME this should only be called once
                onFinish(true)
        }
    }

    abstract fun onFinish(done: Boolean)

    protected fun shouldAnimate(time: Float): Boolean {
        return time in delay..end
    }

    protected abstract fun onAnimate(time: Float)

    protected fun getRelativeTime(time: Float): Float {
        return (time - delay) * 1f / duration
    }

}
