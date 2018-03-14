package com.github.gfranks.collapsible.calendar.viewholder

import android.view.View
import android.view.ViewGroup

class SizeViewHolder(var minHeight: Int, var maxHeight: Int) : AbstractViewHolder() {

    val height: Int
        get() = maxHeight - minHeight

    override fun onFinish(done: Boolean) {
        if (done) {
            onShown()
        } else {
            onHidden()
        }
    }

    fun onShown() {
        view?.apply {
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            visibility = View.VISIBLE
        }
    }

    fun onHidden() {
        view?.apply {
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            visibility = View.GONE
        }
    }

    override fun onAnimate(time: Float) {
        view?.apply {
            visibility = View.VISIBLE
            layoutParams.height = (minHeight + height * time).toInt()
            requestLayout()
        }
    }

}
