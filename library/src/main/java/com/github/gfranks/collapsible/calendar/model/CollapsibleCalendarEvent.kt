package com.github.gfranks.collapsible.calendar.model

import org.joda.time.LocalDate

abstract class CollapsibleCalendarEvent {

    abstract val collapsibleEventLocalDate: LocalDate

    override fun equals(other: Any?): Boolean {
        return if (other is CollapsibleCalendarEvent) {
            collapsibleEventLocalDate == other.collapsibleEventLocalDate
        } else super.equals(other)
    }

    override fun hashCode(): Int {
        return collapsibleEventLocalDate.hashCode()
    }
}
