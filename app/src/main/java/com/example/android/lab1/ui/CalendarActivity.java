package com.example.android.lab1.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.exceptions.OutOfDateRangeException;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.example.android.lab1.R;
import com.example.android.lab1.utils.Utilities;

import java.util.Calendar;
import java.util.Date;

public class CalendarActivity extends AppCompatActivity {
    public static final String FIRST_DATE = "FIRST_DATE";
    public static final String LAST_DATE = "LAST_DATE";

    public static final int CHOOSE_DATE = 5;
    private CalendarView mCalendar; // https://github.com/Applandeo/Material-Calendar-View
    private Toolbar mToolbar;
    private AppCompatButton mConfirmButton;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        mCalendar = findViewById(R.id.calendar_view);
        mToolbar = findViewById(R.id.toolbar_calendar);
        mConfirmButton = findViewById(R.id.confirm_date);

        Utilities.setupStatusBarColor(this);

        mToolbar.setTitle(R.string.choose_initial_date);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

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
            public void onClick(final View v) {
                if (mCalendar.getSelectedDates().size() > 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setTitle(getResources().getString(R.string.confirm_loan_title));
                    builder.setMessage(getResources().getString(R.string.confirm_loan_text));
                    builder.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final Intent result = new Intent();
                            long lastDate;
                            long firstDate = mCalendar.getSelectedDates().get(0).getTimeInMillis();
                            result.putExtra(FIRST_DATE, firstDate);
                            if (mCalendar.getSelectedDates().size() <= 1) {
                                Toast.makeText(v.getContext(), getResources().getString(R.string.select_start_end_date), Toast.LENGTH_SHORT).show();
                                return;
                            }
                                lastDate = mCalendar.getSelectedDates().get(mCalendar.getSelectedDates().size() - 1).getTimeInMillis();
                                result.putExtra(LAST_DATE, lastDate);

                            setResult(RESULT_OK, result);

                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(v.getContext());
                            LayoutInflater inflater = (LayoutInflater) v.getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                            // inflate the custom dialog view
                            final View mDialogView = inflater.inflate(R.layout.dialog_layout_confirmation, null);
                            // set the View for the AlertDialog
                            alertDialogBuilder.setView(mDialogView);
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();

                            new CountDownTimer(2600, 1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                }

                                @Override
                                public void onFinish() {
                                    CalendarActivity.this.finish();
                                }
                            }.start();
                        }
                    });
                    builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                }

            }
        });
    }
}
