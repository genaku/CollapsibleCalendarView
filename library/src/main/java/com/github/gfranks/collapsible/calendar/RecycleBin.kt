package com.github.gfranks.collapsible.calendar

import android.view.View
import java.util.*

internal class RecycleBin {

    private val mViews = LinkedList<View>()

    fun recycleView(): View? = mViews.poll()

    fun addView(view: View) {
        mViews.add(view)
    }

}
