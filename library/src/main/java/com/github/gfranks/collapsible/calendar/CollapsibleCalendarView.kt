package com.github.gfranks.collapsible.calendar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.github.gfranks.collapsible.calendar.model.*
import com.github.gfranks.collapsible.calendar.widget.DayView
import com.github.gfranks.collapsible.calendar.widget.WeekView
import org.joda.time.DateTimeConstants
import org.joda.time.LocalDate

class CollapsibleCalendarView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = R.attr.calendarViewStyle
) : LinearLayout(context, attrs, defStyle), View.OnClickListener, View.OnTouchListener {

    private val mInflater: LayoutInflater
    private val mRecycleBin = RecycleBin()
    private val mGestureDetector: GestureDetector
    private var mTitleView: TextView? = null
    private var mSelectionTitleView: TextView? = null
    private var mPrev: ImageButton? = null
    private var mNext: ImageButton? = null
    private var mArrowColor = -1
    private var mPrevArrowRes: Drawable? = null
    private var mNextArrowRes: Drawable? = null
    private var mHeaderTextColor = Color.DKGRAY
    private var mHeaderBold = false
    private var mWeekDayTextColor = Color.DKGRAY
    private var mWeekBold = false
    private var mDayTextColor = Color.DKGRAY
    private var mEventIndicatorColor = Color.RED
    private var mSelectedDayTextColor = Color.WHITE
    private var mSelectedDayBackgroundColor = Color.DKGRAY
    private var mSmallHeader = false
    private var mNoHeader = false
    private var mShowInactiveDays = true
    private var mDisableSwipe = true
    private var mListener: ICollapsibleCalendarListener? = null
    private var mHeader: LinearLayout? = null
    private val mResizeManager: ResizeManager
    private var mInitialized: Boolean = false

    internal var manager: CalendarManager
    internal var weeksView: LinearLayout? = null
        private set

    var isAllowStateChange = true

    var minDate: LocalDate?
        get() = manager.minDate
        set(minDate) {
            manager.minDate = minDate
        }

    var maxDate: LocalDate?
        get() = manager.maxDate
        set(maxDate) {
            manager.maxDate = maxDate
        }

    val state: CollapsibleState
        get() = manager.state

    val headerText: String
        get() = manager.headerText

    val selectedDate: LocalDate
        get() = manager.selectedDay

    private fun getCachedView(): View {
        val view = mRecycleBin.recycleView()
                ?: mInflater.inflate(R.layout.week_layout, this, false)
        view.visibility = View.VISIBLE
        return view
    }

    init {
        var startingState = CollapsibleState.MONTH
        if (attrs != null) {
            val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.CollapsibleCalendarView, 0, 0)
            try {
                startingState = CollapsibleState.values()[typedArray.getInt(R.styleable.CollapsibleCalendarView_ccv_state, startingState.ordinal)]
                mArrowColor = typedArray.getColor(R.styleable.CollapsibleCalendarView_ccv_arrowColor, mArrowColor)
                mPrevArrowRes = typedArray.getDrawable(R.styleable.CollapsibleCalendarView_ccv_prevArrowSrc)
                mNextArrowRes = typedArray.getDrawable(R.styleable.CollapsibleCalendarView_ccv_nextArrowSrc)
                mHeaderTextColor = typedArray.getColor(R.styleable.CollapsibleCalendarView_ccv_headerTextColor, mHeaderTextColor)
                mHeaderBold = typedArray.getBoolean(R.styleable.CollapsibleCalendarView_ccv_boldHeaderText, mHeaderBold)
                mWeekDayTextColor = typedArray.getColor(R.styleable.CollapsibleCalendarView_ccv_weekDayTextColor, mWeekDayTextColor)
                mWeekBold = typedArray.getBoolean(R.styleable.CollapsibleCalendarView_ccv_boldWeekDayText, mWeekBold)
                mDayTextColor = typedArray.getColor(R.styleable.CollapsibleCalendarView_ccv_dayTextColor, mDayTextColor)
                mEventIndicatorColor = typedArray.getColor(R.styleable.CollapsibleCalendarView_ccv_eventIndicatorColor, mEventIndicatorColor)
                mSelectedDayTextColor = typedArray.getColor(R.styleable.CollapsibleCalendarView_ccv_selectedDayTextColor, mSelectedDayTextColor)
                mSelectedDayBackgroundColor = typedArray.getColor(R.styleable.CollapsibleCalendarView_ccv_selectedDayBackgroundColor, mSelectedDayBackgroundColor)
                mSmallHeader = typedArray.getBoolean(R.styleable.CollapsibleCalendarView_ccv_smallHeader, mSmallHeader)
                mNoHeader = typedArray.getBoolean(R.styleable.CollapsibleCalendarView_ccv_noHeader, mNoHeader)
                mShowInactiveDays = typedArray.getBoolean(R.styleable.CollapsibleCalendarView_ccv_showInactiveDays, mShowInactiveDays)
                isAllowStateChange = typedArray.getBoolean(R.styleable.CollapsibleCalendarView_ccv_allowStateChange, isAllowStateChange)
                mDisableSwipe = typedArray.getBoolean(R.styleable.CollapsibleCalendarView_ccv_disableSwipe, mDisableSwipe)
            } finally {
                typedArray.recycle()
            }
        }

        //        mManager = new CalendarManager(LocalDate.now(), startingState, LocalDate.now(), LocalDate.now().plusYears(1));
        manager = CalendarManager(LocalDate.now(), startingState, null, null)
        mInflater = LayoutInflater.from(context)
        mResizeManager = ResizeManager(this)
        View.inflate(context, R.layout.calendar_layout, this)
        orientation = LinearLayout.VERTICAL

        mGestureDetector = GestureDetector(context, GestureListener(this))
        setOnTouchListener(this)
    }

    override fun dispatchDraw(canvas: Canvas) {
        mResizeManager.onDraw()
        super.dispatchDraw(canvas)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return mResizeManager.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        return mResizeManager.onTouchEvent(event)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return mDisableSwipe || mGestureDetector.onTouchEvent(event)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        mTitleView = findViewById(R.id.title)
        if (mHeaderBold) {
            mTitleView?.setTypeface(null, Typeface.BOLD)
        }
        mPrev = findViewById(R.id.prev)
        mNext = findViewById(R.id.next)
        weeksView = findViewById(R.id.weeks)

        mHeader = findViewById(R.id.header)
        if (mNoHeader) {
            mHeader?.visibility = View.GONE
        }
        mSelectionTitleView = findViewById(R.id.selection_title)
        if (mHeaderBold) {
            mSelectionTitleView?.setTypeface(null, Typeface.BOLD)
        }

        mPrev?.setOnClickListener(this)
        mNext?.setOnClickListener(this)
        mTitleView?.setOnClickListener(this)
        mSelectionTitleView?.setOnClickListener(this)

        setTitleColor(mHeaderTextColor)
        setSmallHeader(mSmallHeader)

        if (mArrowColor != -1) {
            setArrowColor(mArrowColor)
        }

        mPrevArrowRes?.apply {
            setPrevArrowImageDrawable(this)
        }

        mNextArrowRes?.apply {
            setNextArrowImageDrawable(this)
        }

        populateLayout()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mResizeManager.recycle()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.prev -> prev()
            R.id.next -> next()
            R.id.title, R.id.selection_title -> mListener?.onHeaderClick()
        }
    }

    fun setEvents(events: List<CollapsibleCalendarEvent>) {
        manager.setEvents(events)
        populateLayout()
        mListener?.onDateSelected(selectedDate, manager.getEventsForDate(selectedDate))
    }

    fun addEvents(events: List<CollapsibleCalendarEvent>) {
        manager.addEvents(events)
        populateLayout()
        mListener?.onDateSelected(selectedDate, manager.getEventsForDate(selectedDate))
    }

    fun addEvent(event: CollapsibleCalendarEvent) {
        manager.addEvent(event)
        populateLayout()
        mListener?.onDateSelected(selectedDate, manager.getEventsForDate(selectedDate))
    }

    fun removeEvent(event: CollapsibleCalendarEvent) {
        manager.removeEvent(event)
        populateLayout()
        mListener?.onDateSelected(selectedDate, manager.getEventsForDate(selectedDate))
    }

    fun getEventsForDate(date: LocalDate): List<CollapsibleCalendarEvent> {
        return manager.getEventsForDate(date)
    }

    fun setTitle(text: String?) {
        if (mNoHeader) {
            return
        }

        if (text?.isEmpty() == true) {
            mHeader?.visibility = View.VISIBLE
            mSelectionTitleView?.visibility = View.GONE
        } else {
            mHeader?.visibility = View.GONE
            mSelectionTitleView?.visibility = View.VISIBLE
            mSelectionTitleView?.text = text
        }
    }

    fun setTitleColor(color: Int) {
        mHeaderTextColor = color
        mTitleView?.setTextColor(mHeaderTextColor)
        mSelectionTitleView?.setTextColor(mHeaderTextColor)
    }

    fun setBoldHeaderText(headerBold: Boolean) {
        mHeaderBold = headerBold
        populateLayout()
    }

    fun setArrowColor(color: Int) {
        mArrowColor = color
        mPrev?.setColorFilter(mArrowColor, PorterDuff.Mode.SRC_IN)
        mNext?.setColorFilter(mArrowColor, PorterDuff.Mode.SRC_IN)
    }

    fun setPrevArrowImageResource(resId: Int) {
        mPrev?.setImageResource(resId)
    }

    fun setPrevArrowImageDrawable(drawable: Drawable) {
        mPrev?.setImageDrawable(drawable)
    }

    fun setNextArrowImageResource(resId: Int) {
        mNext?.setImageResource(resId)
    }

    fun setNextArrowImageDrawable(drawable: Drawable) {
        mNext?.setImageDrawable(drawable)
    }

    fun setWeekDayTextColor(color: Int) {
        mWeekDayTextColor = color
        populateLayout()
    }

    fun setBoldWeeDayText(weekBold: Boolean) {
        mWeekBold = weekBold
        populateLayout()
    }

    fun setDayTextColor(color: Int) {
        mDayTextColor = color
        populateLayout()
    }

    fun setEventIndicatorColor(color: Int) {
        mEventIndicatorColor = color
        populateLayout()
    }

    fun setSelectedDayTextColor(color: Int) {
        mSelectedDayTextColor = color
        populateLayout()
    }

    fun setSelectedDayBackgroundColor(color: Int) {
        mSelectedDayBackgroundColor = color
        populateLayout()
    }

    fun setSmallHeader(smallHeader: Boolean) {
        mSmallHeader = smallHeader
        val textSize = if (mSmallHeader) 14f else 20f
        mTitleView?.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
        mSelectionTitleView?.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
    }

    fun setShowInactiveDays(showInactiveDays: Boolean) {
        mShowInactiveDays = showInactiveDays
        populateLayout()
    }

    fun disableSwipe(disableSwipe: Boolean) {
        mDisableSwipe = disableSwipe
    }

    fun setFormatter(formatter: IFormatter) {
        manager.setFormatter(formatter)
    }

    fun setListener(listener: ICollapsibleCalendarListener) {
        mListener = listener
    }

    operator fun next() {
        if (manager.next()) {
            populateLayout()
            mListener?.onMonthChanged(manager.activeMonth)
        }
    }

    fun prev() {
        if (manager.prev()) {
            populateLayout()
            mListener?.onMonthChanged(manager.activeMonth)
        }
    }

    fun selectDate(date: LocalDate) {
        val period = manager.selectPeriod(date)
        val day = manager.selectDay(date)

        if (period || day) {
            populateLayout()
        }

        if (day) {
            mListener?.onDateSelected(date, manager.getEventsForDate(date))
        }
    }

    fun toggle() {
        mResizeManager.toggle()
    }

    fun populateLayout() {
        populateDays()

        mPrev?.isEnabled = manager.hasPrev()
        mNext?.isEnabled = manager.hasNext()

        mTitleView?.text = manager.headerText

        if (manager.state == CollapsibleState.MONTH) {
            populateMonthLayout(manager.units as Month?)
        } else {
            populateWeekLayout(manager.units as Week?)
        }
    }

    private fun populateMonthLayout(month: Month?) {
        month ?: return

        val weeks = month.weeks
        val cnt = weeks.size
        for (i in 0 until cnt) {
            populateWeekLayout(weeks[i], getWeekView(i))
        }

        weeksView?.childCount?.apply {
            if (cnt < this) {
                for (i in cnt until this) {
                    cacheView(i)
                }
            }
        }
    }

    private fun populateWeekLayout(week: Week?) {
        week ?: return

        populateWeekLayout(week, getWeekView(0))

        weeksView?.childCount?.apply {
            if (this > 1) {
                for (i in this - 1 downTo 1) {
                    cacheView(i)
                }
            }
        }
    }

    private fun populateWeekLayout(week: Week, weekView: WeekView) {
        val days = week.days
        for (i in 0..6) {
            val day = days[i]
            val dayView = weekView.getChildAt(i) as DayView

            dayView.setText(day.text)
            if (day.date.getValue(1) != manager.activeMonth?.getValue(1) && state === CollapsibleState.MONTH) {
                if (mShowInactiveDays) {
                    dayView.alpha = 0.5f
                    dayView.visibility = View.VISIBLE
                } else {
                    dayView.visibility = View.INVISIBLE
                }
            } else {
                dayView.alpha = 1f
                dayView.visibility = View.VISIBLE
            }
            dayView.setSelected(day.isSelected, mSelectedDayBackgroundColor, mSelectedDayTextColor, mDayTextColor)
            dayView.isCurrent = day.isCurrent
            dayView.setHasEvent(manager.dayHasEvent(day))
            dayView.setEventIndicatorColor(mEventIndicatorColor)

            val enabled = day.isEnabled
            dayView.isEnabled = enabled

            if (enabled && (state === CollapsibleState.WEEK || day.date.getValue(1) == manager.activeMonth?.getValue(1))) {
                dayView.setOnClickListener {
                    val date = day.date
                    if (manager.selectDay(date)) {
                        populateLayout()
                        mListener?.onDateSelected(date, manager.getEventsForDate(date))
                    }
                }
            } else {
                dayView.setOnClickListener(null)
            }
        }
    }

    private fun populateDays() {
        if (mInitialized) {
            return
        }
        val formatter = manager.getFormatter()

        val layout = findViewById<LinearLayout>(R.id.days)

        var date = LocalDate.now().withDayOfWeek(DateTimeConstants.MONDAY)
        for (i in 0..6) {
            val textView = layout.getChildAt(i) as TextView
            textView.text = formatter.getDayName(date)
            textView.setTextColor(mWeekDayTextColor)
            if (mWeekBold) {
                textView.setTypeface(null, Typeface.BOLD)
            }
            date = date.plusDays(1)
        }

        mInitialized = true
    }

    private fun getWeekView(index: Int): WeekView {
        weeksView?.childCount?.apply {
            if (this < index + 1) {
                for (i in this until index + 1) {
                    weeksView?.addView(getCachedView())
                }
            }
        }
        return weeksView?.getChildAt(index) as WeekView
    }

    private fun cacheView(index: Int) {
        weeksView?.getChildAt(index)?.apply {
            weeksView?.removeViewAt(index)
            mRecycleBin.addView(this)
        }
    }

    interface ICollapsibleCalendarListener {
        fun onDateSelected(date: LocalDate, events: List<CollapsibleCalendarEvent>)
        fun onMonthChanged(date: LocalDate?)
        fun onHeaderClick()
    }

}
