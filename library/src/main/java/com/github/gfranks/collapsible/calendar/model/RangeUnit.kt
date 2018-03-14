package com.github.gfranks.collapsible.calendar.model

import org.joda.time.DateTimeConstants
import org.joda.time.Days
import org.joda.time.LocalDate

abstract class RangeUnit protected constructor(
        dateFrom: LocalDate,
        dateTo: LocalDate,
        today: LocalDate,
        val minDate: LocalDate?,
        val maxDate: LocalDate?
) : CalendarUnit(dateFrom, dateTo, today) {

    val firstEnabled: LocalDate
        get() {
            return if (minDate != null && dateFrom.isBefore(minDate)) {
                minDate
            } else {
                dateFrom
            }
        }

    init {
        if (minDate != null && maxDate != null && minDate.isAfter(maxDate)) {
            throw IllegalArgumentException("Min date should be before max date")
        }
    }

    abstract fun getFirstDateOfCurrentMonth(currentMonth: LocalDate?): LocalDate?

    override fun hasDate(date: LocalDate): Boolean {
        var min = true
        var max = true

        val maxDate = maxDate
        if (maxDate != null) {
            max = !date.isAfter(maxDate)
        }

        val minDate = minDate
        if (minDate != null) {
            min = !date.isBefore(minDate)
        }

        return min && max
    }

    /**
     * @param firstDayOfMonth First day of current month in range unit
     * @return Week of month of first enabled date, 0 if no dates are enabled.
     */
    fun getFirstWeek(firstDayOfMonth: LocalDate): Int {
        val date = if (minDate != null && minDate.isAfter(dateFrom)) { // TODO check if same month
            minDate
        } else {
            firstDayOfMonth
        }

        return getWeekInMonth(date)
    }

    fun getWeekInMonth(date: LocalDate?): Int {
        date ?: return 0
        val first = date.withDayOfMonth(1).withDayOfWeek(DateTimeConstants.MONDAY)
        val days = Days.daysBetween(first, date)
        return days.dividedBy(DateTimeConstants.DAYS_PER_WEEK).days
    }
}
