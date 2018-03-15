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

    private fun onShown() {
        view!!.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        view!!.visibility = View.VISIBLE
    }

    private fun onHidden() {
        view!!.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        view!!.visibility = View.GONE
    }

    override fun onAnimate(time: Float) {
        val view = view
        view!!.visibility = View.VISIBLE
        view.layoutParams.height = (minHeight + height * time).toInt()
        view.requestLayout()
    }

}
