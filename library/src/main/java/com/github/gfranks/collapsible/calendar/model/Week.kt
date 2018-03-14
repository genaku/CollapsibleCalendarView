package com.github.gfranks.collapsible.calendar.model

import org.joda.time.LocalDate
import java.util.*

class Week(
        date: LocalDate,
        today: LocalDate,
        minDate: LocalDate?,
        maxDate: LocalDate?
) : RangeUnit(date.withDayOfWeek(1), date.withDayOfWeek(7), today, minDate, maxDate) {

    private val mDays = ArrayList<Day>(7)

    override val type = CalendarUnitType.WEEK

    val days: List<Day>
        get() = mDays

    init {
        build()
    }

    override fun hasNext(): Boolean {
        val maxDate = maxDate
        return maxDate?.isAfter(mDays[6].date) ?: true
    }

    override fun hasPrev(): Boolean {
        val minDate = minDate
        return minDate?.isBefore(mDays[0].date) ?: true
    }

    override fun setPeriod(date: LocalDate): Boolean {
        return if (hasDate(date)) {
            dateFrom = date.withDayOfWeek(1)
            dateTo = date.withDayOfWeek(7)
            build()
            true
        } else {
            false
        }
    }

    override fun next(): Boolean {
        return if (hasNext()) {
            dateFrom = dateFrom.plusWeeks(1)
            dateTo = dateTo.plusWeeks(1)
            build()
            true
        } else {
            false
        }
    }

    override fun prev(): Boolean {
        return if (hasPrev()) {
            dateFrom = dateFrom.minusWeeks(1)
            dateTo = dateTo.minusWeeks(1)
            build()
            true
        } else {
            false
        }
    }

    override fun deselect(date: LocalDate?) {
        if (date != null && dateFrom <= date && dateTo >= date) {
            isSelected = false
            mDays.forEach {
                it.isSelected = false
            }
        }
    }

    override fun select(date: LocalDate?): Boolean {
        return if (date != null && dateFrom <= date && dateTo >= date) {
            isSelected = true
            mDays.forEach {
                it.isSelected = it.date.isEqual(date)
            }
            true
        } else {
            false
        }
    }

    override fun build() {
        mDays.clear()

        var date = dateFrom
        while (date <= dateTo) {
            val day = Day(date, date == today, false, true, true)
            day.isEnabled = isDayEnabled(date)
            mDays.add(day)
            date = date.plusDays(1)
        }
    }

    private fun isDayEnabled(date: LocalDate): Boolean {
        if (minDate != null && date.isBefore(minDate)) {
            return false
        }

        return !(maxDate != null && date.isAfter(maxDate))
    }

    override fun getFirstDateOfCurrentMonth(currentMonth: LocalDate?): LocalDate? {
        currentMonth ?: return null

        val currentYear = currentMonth.year
        val currentMonthOfYear = currentMonth.monthOfYear

        var date = dateFrom
        while (date <= dateTo) {
            val fromYear = date.year
            val fromMonthOfYear = date.monthOfYear

            if (currentYear == fromYear && currentMonthOfYear == fromMonthOfYear) {
                return date
            }
            date = date.plusDays(1)
        }

        return null
    }
}
