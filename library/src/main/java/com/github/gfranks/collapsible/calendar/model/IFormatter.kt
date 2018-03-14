package com.github.gfranks.collapsible.calendar.model

import org.joda.time.LocalDate

interface IFormatter {
    fun getDayName(date: LocalDate): String
    fun getHeaderText(type: CalendarUnitType, dateFrom: LocalDate, dateTo: LocalDate): String
}
