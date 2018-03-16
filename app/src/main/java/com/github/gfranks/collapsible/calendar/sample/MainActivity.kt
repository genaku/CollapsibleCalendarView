package com.github.gfranks.collapsible.calendar.sample

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import com.github.gfranks.collapsible.calendar.CollapsibleCalendarView
import com.github.gfranks.collapsible.calendar.model.CollapsibleCalendarEvent
import kotlinx.android.synthetic.main.activity_main.*
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.util.*

class MainActivity : Activity(), CollapsibleCalendarView.ICollapsibleCalendarListener {

    private var mAdapter: EventListAdapter? = null

    private val events = ArrayList<Event>()

    init {
        for (i in -20..79) {
            events.add(Event("Event " + (i + 1), System.currentTimeMillis() + 21600000 * i))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        calendar.apply {
            setListener(this@MainActivity)
            addEvents(events)
            minDate = LocalDate(2010, 1, 1)
            maxDate = LocalDate(2020, 1, 1)
        }
    }

    override fun onDateSelected(date: LocalDate, events: List<CollapsibleCalendarEvent>) {
        if (mAdapter == null || calendar_event_list.adapter == null) {
            mAdapter = EventListAdapter(this, ArrayList(events))
            calendar_event_list.adapter = mAdapter
        } else {
            mAdapter?.setEvents(events)
        }
    }

    override fun onMonthChanged(date: LocalDate?) {}

    override fun onHeaderClick() {
        calendar.toggle()
    }

    private inner class EventListAdapter(context: Context, private val mEvents: ArrayList<CollapsibleCalendarEvent>) : ArrayAdapter<String>(context, android.R.layout.simple_list_item_1) {

        val mTimeFormat: DateTimeFormatter? = DateTimeFormat.forPattern(" h:mm a")

        fun setEvents(events: List<CollapsibleCalendarEvent>) {
            mEvents.clear()
            mEvents.addAll(events)
            notifyDataSetChanged()
        }

        override fun getCount(): Int = mEvents.size

        override fun getItem(position: Int): String? {
            val event = mEvents[position] as Event
            return mTimeFormat?.print(event.listCellTime) + " - " + event.title
        }
    }
}
