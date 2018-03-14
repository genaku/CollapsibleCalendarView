package com.github.gfranks.collapsible.calendar.model

import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

data class Day(
        val date: LocalDate,
        val isToday: Boolean,
        var isSelected: Boolean = false,
        var isEnabled: Boolean = true,
        var isCurrent: Boolean = true
) {
    val text: String
        get() = date.toString(mFormatter)

    companion object {
        private val mFormatter = DateTimeFormat.forPattern("d")
    }
}