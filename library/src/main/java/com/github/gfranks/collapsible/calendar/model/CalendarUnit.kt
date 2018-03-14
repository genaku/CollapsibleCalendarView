package com.github.gfranks.collapsible.calendar.model

import org.joda.time.DateTimeConstants
import org.joda.time.LocalDate

abstract class CalendarUnit protected constructor(var dateFrom: LocalDate, var dateTo: LocalDate, val today: LocalDate) {

    var isSelected: Boolean = false

    abstract val type: CalendarUnitType

    abstract fun hasDate(date: LocalDate): Boolean

    abstract operator fun hasNext(): Boolean
    abstract operator fun next(): Boolean

    abstract fun hasPrev(): Boolean
    abstract fun prev(): Boolean

    abstract fun setPeriod(date: LocalDate): Boolean

    abstract fun deselect(date: LocalDate?)
    abstract fun select(date: LocalDate?): Boolean

    abstract fun build()

    fun isIn(date: LocalDate): Boolean {
        return !(dateFrom.isAfter(date) || dateTo.isBefore(date))
    }

    fun isInView(date: LocalDate): Boolean {
        return !(dateFrom.withDayOfWeek(DateTimeConstants.MONDAY).isAfter(date) ||
                dateTo.withDayOfWeek(DateTimeConstants.SUNDAY).isBefore(date))
    }

    override fun equals(other: Any?): Boolean {
        other ?: return false
        if (this === other) return true
        if (other !is CalendarUnit) return false

        if (isSelected != other.isSelected) return false
        if (dateFrom != other.dateFrom) return false
        if (dateTo != other.dateTo) return false
        return today == other.today

    }

    override fun hashCode(): Int {
        var result = today.hashCode()
        result = 31 * result + dateFrom.hashCode()
        result = 31 * result + dateTo.hashCode()
        result = 31 * result + if (isSelected) 1 else 0
        return result
    }

}
