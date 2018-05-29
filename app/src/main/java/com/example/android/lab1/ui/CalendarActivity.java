package com.example.android.lab1.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.exceptions.OutOfDateRangeException;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.example.android.lab1.R;
import com.example.android.lab1.ui.homepage.HomePageActivity;
import com.example.android.lab1.utils.Utilities;

import java.util.Calendar;
import java.util.Date;

public class CalendarActivity extends AppCompatActivity {
    public static final String FIRST_DATE = "FIRST_DATE";
    public static final String LAST_DATE = "LAST_DATE";
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

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(v.getContext());
                    LayoutInflater inflater = (LayoutInflater) v.getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                    // inflate the custom dialog view
                    final View mDialogView = inflater.inflate(R.layout.dialog_layout, null);
                    // set the View for the AlertDialog
                    alertDialogBuilder.setView(mDialogView);

                    AppCompatButton btnCancel  = mDialogView.findViewById(R.id.cancel_button_dialog);
                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    });
                    AppCompatButton btnConfirm  = mDialogView.findViewById(R.id.confirm_button_dialog);
                    btnConfirm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Snackbar mySnackbar = Snackbar.make(v.getRootView().findViewById(R.id.chat_layout_container),
                                    "Prestito iniziato con successo", Snackbar.LENGTH_LONG).setDuration(4000);
                            mySnackbar.show();

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent result = new Intent(getApplicationContext(), HomePageActivity.class);
                                    result.putExtra(FIRST_DATE, mCalendar.getSelectedDates().get(0));
                                    if (mCalendar.getSelectedDates().size() > 1)
                                        result.putExtra(LAST_DATE, mCalendar.getSelectedDates().get(mCalendar.getSelectedDates().size() - 1));

                                    CalendarActivity.this.finish();
                                }
                            }, 4000);
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }

            }
        });
    }
}
