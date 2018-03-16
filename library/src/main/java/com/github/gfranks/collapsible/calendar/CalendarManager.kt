package com.github.gfranks.collapsible.calendar

import com.github.gfranks.collapsible.calendar.model.*
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import java.util.*

internal class CalendarManager(
        selected: LocalDate,
        state: CollapsibleState,
        minDate: LocalDate?,
        maxDate: LocalDate?
) {

    private val mMapEventKeyFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")

    private val mToday: LocalDate = LocalDate.now()

    private lateinit var mUnit: RangeUnit

    private var mFormatter: IFormatter = DefaultFormatter()

    private val mEventsMap = HashMap<String, ArrayList<CollapsibleCalendarEvent>>()

    var state: CollapsibleState
        private set

    lateinit var selectedDay: LocalDate
        private set

    var minDate: LocalDate? = null
    var maxDate: LocalDate? = null

    fun setFormatter(formatter: IFormatter?) {
        mFormatter = formatter ?: DefaultFormatter()
    }

    fun getFormatter(): IFormatter =
            mFormatter

    var activeMonth: LocalDate? = null
        private set(activeMonth) {
            field = activeMonth?.withDayOfMonth(1)
        }

    val headerText: String
        get() = mFormatter.getHeaderText(mUnit.type, mUnit.dateFrom, mUnit.dateTo)

    val units: CalendarUnit?
        get() = mUnit

    // if not in this month first week should be selected
    val weekOfMonth: Int
        get() = if (mUnit.isInView(selectedDay)) {
            when {
                mUnit.isIn(selectedDay) -> mUnit.getWeekInMonth(selectedDay)
                mUnit.dateFrom.isAfter(selectedDay) -> mUnit.getWeekInMonth(mUnit.dateFrom)
                else -> mUnit.getWeekInMonth(mUnit.dateTo)
            }
        } else {
            mUnit.getFirstWeek(mUnit.getFirstDateOfCurrentMonth(activeMonth)!!)
        }

    init {
        this.state = state
        setupManager(selected, minDate, maxDate)
    }

    fun setEvents(events: List<CollapsibleCalendarEvent>) {
        mEventsMap.clear()
        for (i in events.indices) {
            addEvent(events[i])
        }
    }

    fun addEvents(events: List<CollapsibleCalendarEvent>) {
        for (i in events.indices) {
            addEvent(events[i])
        }
    }

    fun addEvent(event: CollapsibleCalendarEvent) {
        val key = mMapEventKeyFormatter.print(event.collapsibleEventLocalDate)
        val events: MutableList<CollapsibleCalendarEvent>
        if (mEventsMap.containsKey(key)) {
            mEventsMap[key]?.add(event)
        } else {
            events = ArrayList()
            events.add(event)
            mEventsMap[key] = events
        }
    }

    fun removeEvent(event: CollapsibleCalendarEvent) {
        val key = mMapEventKeyFormatter.print(event.collapsibleEventLocalDate)
        mEventsMap[key]?.remove(event)
    }

    fun getEventsForDate(date: LocalDate): List<CollapsibleCalendarEvent> {
        val key = mMapEventKeyFormatter.print(date)
        return mEventsMap[key] ?: ArrayList()

    }

    fun selectPeriod(date: LocalDate): Boolean {
        return if (!mUnit.isIn(date) && mUnit.setPeriod(date)) {
            mUnit.select(selectedDay)
            activeMonth = mUnit.dateFrom
            true
        } else {
            false
        }
    }

    fun selectDay(date: LocalDate): Boolean {
        return if (!selectedDay.isEqual(date) && mUnit.hasDate(date)) {
            mUnit.deselect(selectedDay)
            selectedDay = date
            mUnit.select(selectedDay)

            if (state === CollapsibleState.WEEK) {
                activeMonth = date
            }
            true
        } else {
            false
        }
    }

    operator fun hasNext(): Boolean {
        return mUnit.hasNext()
    }

    fun hasPrev(): Boolean {
        return mUnit.hasPrev()
    }

    operator fun next(): Boolean {
        return if (mUnit.next()) {
            mUnit.select(selectedDay)
            activeMonth = mUnit.dateFrom
            true
        } else {
            false
        }
    }

    fun prev(): Boolean {
        return if (mUnit.prev()) {
            mUnit.select(selectedDay)
            activeMonth = mUnit.dateTo
            true
        } else {
            false
        }
    }

    /**
     * @return index of month to focus to
     */
    fun toggleView() {
        if (state === CollapsibleState.MONTH) {
            toggleFromMonth()
        } else {
            toggleFromWeek()
        }
    }

    private fun toggleFromMonth() {
        // if same month as selected
        if (mUnit.isInView(selectedDay)) {
            toggleFromMonth(selectedDay)

            activeMonth = selectedDay
        } else {
            activeMonth = mUnit.dateFrom
            toggleFromMonth(mUnit.getFirstDateOfCurrentMonth(activeMonth))
        }
    }

    fun toggleToWeek(weekInMonth: Int) {
        val date = mUnit.dateFrom.plusDays(weekInMonth * 7)
        toggleFromMonth(date)
    }

    private fun toggleFromMonth(date: LocalDate?) {
        setUnit(Week(date!!, mToday, minDate, maxDate))
        mUnit.select(selectedDay)
        state = CollapsibleState.WEEK
    }

    private fun toggleFromWeek() {
        setUnit(Month(activeMonth!!, mToday, minDate, maxDate))
        mUnit.select(selectedDay)

        state = CollapsibleState.MONTH
    }

    private fun initManager() {
        if (state === CollapsibleState.MONTH) {
            setUnit(Month(selectedDay, mToday, minDate, maxDate))
        } else {
            setUnit(Week(selectedDay, mToday, minDate, maxDate))
        }
        mUnit.select(selectedDay)
    }

    private fun setUnit(unit: RangeUnit?) {
        if (unit != null) {
            mUnit = unit
        }
    }

    private fun setupManager(selectedDate: LocalDate, minDate: LocalDate?, maxDate: LocalDate?) {
        selectedDay = selectedDate
        activeMonth = selectedDate
        this.minDate = minDate
        this.maxDate = maxDate

        initManager()
    }

    fun dayHasEvent(day: Day): Boolean {
        return mEventsMap.containsKey(mMapEventKeyFormatter.print(day.date))
    }

}
