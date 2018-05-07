package com.example.android.lab1.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.exceptions.OutOfDateRangeException;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.example.android.lab1.R;

import java.util.Calendar;
import java.util.Date;

public class CalendarActivity extends AppCompatActivity {
    public static final String FIRST_DATE = "FIRST_DATE";
    public static final String LAST_DATE = "LAST_DATE";
    private CalendarView mCalendar; // https://github.com/Applandeo/Material-Calendar-View
    private Toolbar mToolbar;
    private AppCompatButton mConfirmButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        mCalendar = findViewById(R.id.calendar_view);
        mToolbar = findViewById(R.id.toolbar);
        mConfirmButton = findViewById(R.id.confirm_date);

        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        mToolbar.setTitle(R.string.choose_initial_date);
        setSupportActionBar(mToolbar);

        Date d = new Date();
        d.setTime(Calendar.getInstance().getTime().getTime());
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        mCalendar.setMinimumDate(c);
        mCalendar.showCurrentMonthPage();
        try {
            mCalendar.setDate(c);
        } catch (OutOfDateRangeException e) {
            e.printStackTrace();
        }

        mCalendar.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                mToolbar.setTitle(R.string.choose_end_date);
            }
        });

        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCalendar.getSelectedDates().size() > 0) {
                    Intent result = new Intent();
                    result.putExtra(FIRST_DATE, mCalendar.getSelectedDates().get(0));
                    if (mCalendar.getSelectedDates().size() > 1)
                        result.putExtra(LAST_DATE, mCalendar.getSelectedDates().get(mCalendar.getSelectedDates().size() - 1));
                    setResult(RESULT_OK, result);
                } else
                    setResult(RESULT_CANCELED);
                finish();
            }
        });

    }
}
