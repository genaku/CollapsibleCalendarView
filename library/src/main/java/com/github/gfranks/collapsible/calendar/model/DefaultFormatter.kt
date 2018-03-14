package com.github.gfranks.collapsible.calendar.model

import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

class DefaultFormatter @JvmOverloads constructor(
        dayPattern: String = "E",
        weekPattern: String = "MMMM yyyy",
        monthPattern: String = "MMMM yyyy"
) : IFormatter {

    private val mDayFormatter: DateTimeFormatter = DateTimeFormat.forPattern(dayPattern)
    private val mWeekHeaderFormatter: DateTimeFormatter = DateTimeFormat.forPattern(weekPattern)
    private val mMonthHeaderFormatter: DateTimeFormatter = DateTimeFormat.forPattern(monthPattern)

    override fun getDayName(date: LocalDate): String =
            date.toString(mDayFormatter)

    override fun getHeaderText(type: CalendarUnitType, dateFrom: LocalDate, dateTo: LocalDate): String {
        return dateFrom.toString(when (type) {
            CalendarUnitType.WEEK -> mWeekHeaderFormatter
            CalendarUnitType.MONTH -> mMonthHeaderFormatter
        })
    }
}
