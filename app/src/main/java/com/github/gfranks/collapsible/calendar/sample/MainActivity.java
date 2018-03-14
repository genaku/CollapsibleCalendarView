package com.github.gfranks.collapsible.calendar.sample;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.github.gfranks.collapsible.calendar.CollapsibleCalendarView;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements CollapsibleCalendarView.Listener<Event> {

    private CollapsibleCalendarView mCalendarView;
    private ListView mListView;
    private EventListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCalendarView = findViewById(R.id.calendar);
        mListView = findViewById(R.id.calendar_event_list);

        mCalendarView.setListener(this);
        mCalendarView.addEvents(getEvents());
        mCalendarView.setMinDate(new LocalDate(2010, 1, 1));
        mCalendarView.setMaxDate(new LocalDate(2020, 1, 1));
        Log.d("TAG", "min date" + mCalendarView.getMinDate().toString());
        Log.d("TAG", "max date" + mCalendarView.getMaxDate().toString());
    }

    private List<Event> getEvents() {
        List<Event> events = new ArrayList<>();
        for (int i=-20; i<80; i++) {
            events.add(new Event("Event " + (i+1), System.currentTimeMillis() + (21600000 * i)));
        }
        return events;
    }

    @Override
    public void onDateSelected(LocalDate date, List<Event> events) {
        if (mAdapter == null || mListView.getAdapter() == null) {
            mAdapter = new EventListAdapter(this, events);
            mListView.setAdapter(mAdapter);
        } else {
            mAdapter.setEvents(events);
        }
    }

    @Override
    public void onMonthChanged(LocalDate date) {
    }

    @Override
    public void onHeaderClick() {
        mCalendarView.toggle();
    }

    private class EventListAdapter extends ArrayAdapter<String> {

        public final DateTimeFormatter mTimeFormat = DateTimeFormat.forPattern(" h:mm a");
        private List<Event> mEvents;

        public EventListAdapter(Context context, List<Event> events) {
            super(context, android.R.layout.simple_list_item_1);
            mEvents = events;
        }

        public void setEvents(List<Event> events) {
            mEvents = events;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mEvents.size();
        }

        @Override
        public String getItem(int position) {
            Event event = mEvents.get(position);
            return mTimeFormat.print(event.getListCellTime()) + " - " + event.getTitle();
        }
    }
}
