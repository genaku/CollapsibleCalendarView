package com.github.gfranks.collapsible.calendar.sample

import com.github.gfranks.collapsible.calendar.model.CollapsibleCalendarEvent

import org.joda.time.DateTime
import org.joda.time.LocalDate

class Event(val title: String, private val mDate: Long) : CollapsibleCalendarEvent() {

    val listCellTime: DateTime
        get() = DateTime(mDate)

    override val collapsibleEventLocalDate: LocalDate
        get() = LocalDate(mDate)
}