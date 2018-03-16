package com.github.gfranks.collapsible.calendar.model

import org.joda.time.LocalDate

interface IFormatter {
    fun getDayName(date: LocalDate?): String
    fun getHeaderText(type: CalendarUnitType, from: LocalDate?, to: LocalDate?): String
}
