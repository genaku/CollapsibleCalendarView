package com.github.gfranks.collapsible.calendar.model

import org.joda.time.LocalDate
import java.util.*

class Month(
        date: LocalDate,
        today: LocalDate,
        minDate: LocalDate?,
        maxDate: LocalDate?
) : RangeUnit(date.withDayOfMonth(1), date.withDayOfMonth(date.dayOfMonth().maximumValue), today, minDate, maxDate) {

    private val mWeeks = ArrayList<Week>()
    var selectedIndex = -1
        private set

    override val type = CalendarUnitType.MONTH

    val weeks: List<Week>
        get() = mWeeks

    init {
        build()
    }

    override fun hasNext(): Boolean {
        val maxDate = maxDate ?: return true

        val year = maxDate.year
        val yearTo = dateTo.year

        val month = maxDate.monthOfYear
        val monthTo = dateTo.monthOfYear

        return year > yearTo || year == yearTo && month > monthTo
    }

    override fun hasPrev(): Boolean {
        val minDate = minDate ?: return true

        val from = dateFrom
        val year = minDate.year
        val yearFrom = from.year

        val month = minDate.monthOfYear
        val monthFrom = from.monthOfYear

        return year < yearFrom || year == yearFrom && month < monthFrom
    }

    override fun setPeriod(date: LocalDate): Boolean {
        return if (hasDate(date)) {
            dateFrom = date.withDayOfMonth(1)
            dateTo = dateFrom.withDayOfMonth(dateFrom.dayOfMonth().maximumValue)
            build()
            true
        } else {
            false
        }
    }

    override fun next(): Boolean {
        return if (hasNext()) {
            dateFrom = dateTo.plusDays(1)
            dateTo = dateFrom.withDayOfMonth(dateFrom.dayOfMonth().maximumValue)
            build()
            true
        } else {
            false
        }
    }

    override fun prev(): Boolean {
        return if (hasPrev()) {
            dateFrom = dateFrom.minusDays(1).withDayOfMonth(1)
            dateTo = dateFrom.withDayOfMonth(dateFrom.dayOfMonth().maximumValue)
            build()
            true
        } else {
            false
        }
    }

    override fun deselect(date: LocalDate?) {
        if (date != null && isSelected && isInView(date)) {
            for (week in mWeeks) {
                if (week.isSelected && week.isIn(date)) {
                    selectedIndex = -1
                    isSelected = false
                    week.deselect(date)
                }
            }
        }
    }

    override fun select(date: LocalDate?): Boolean {
        val cnt = mWeeks.size
        for (i in 0 until cnt) {
            val week = mWeeks[i]
            if (week.select(date)) {
                selectedIndex = i
                isSelected = true
                return true
            }
        }
        return false
    }

    override fun build() {
        isSelected = false
        mWeeks.clear()

        var date = dateFrom.withDayOfWeek(1)
        var i = 0
        while (i == 0 || dateTo >= date) {
            mWeeks.add(Week(date, today, minDate, maxDate))
            date = date.plusWeeks(1)
            i++
        }
    }

    override fun getFirstDateOfCurrentMonth(currentMonth: LocalDate?): LocalDate? {
        currentMonth ?: return null

        with(firstEnabled) {
            if (currentMonth.year == year && currentMonth.monthOfYear == monthOfYear) {
                return this
            }
        }

        return null
    }
}
