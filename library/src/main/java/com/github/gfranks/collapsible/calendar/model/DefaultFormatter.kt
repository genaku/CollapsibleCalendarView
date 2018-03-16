package com.github.gfranks.collapsible.calendar.model

import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

class DefaultFormatter(
        dayPattern: String = "E",
        weekPattern: String = "MMMM yyyy",
        monthPattern: String = "MMMM yyyy"
) : IFormatter {

    private val dayFormatter: DateTimeFormatter = DateTimeFormat.forPattern(dayPattern)
    private val weekHeaderFormatter: DateTimeFormatter = DateTimeFormat.forPattern(weekPattern)
    private val monthHeaderFormatter: DateTimeFormatter = DateTimeFormat.forPattern(monthPattern)

    override fun getDayName(date: LocalDate?): String = date?.toString(dayFormatter) ?: ""

    override fun getHeaderText(type: CalendarUnitType, from: LocalDate?, to: LocalDate?): String =
            from?.toString(when (type) {
                CalendarUnitType.WEEK -> weekHeaderFormatter
                CalendarUnitType.MONTH -> monthHeaderFormatter
            }) ?: ""

}
