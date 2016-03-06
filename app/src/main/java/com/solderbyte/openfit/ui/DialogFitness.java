package com.solderbyte.openfit.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.solderbyte.openfit.ExerciseData;
import com.solderbyte.openfit.HeartRateResultRecord;
import com.solderbyte.openfit.PedometerData;
import com.solderbyte.openfit.PedometerTotal;
import com.solderbyte.openfit.ProfileData;
import com.solderbyte.openfit.R;
import com.solderbyte.openfit.SleepResultRecord;
import com.solderbyte.openfit.util.OpenFitData;
import com.solderbyte.openfit.util.OpenFitIntent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListAdapter;

public class DialogFitness extends DialogFragment {
    private static final String LOG_TAG = "OpenFit:DialogFitness";

    private AlertDialog dialog = null;
    private Date trialDate = null;
    private ListAdapter adapter;
    private Context context;

    public DialogFitness() {}

    public DialogFitness(Context cntxt, ArrayList<PedometerData> pedometerDailyList, ArrayList<PedometerData> pedometerList, PedometerTotal pedometerTotal,
                         ArrayList<ExerciseData> exerciseDataList, ArrayList<SleepResultRecord> sleepResultRecordList, ArrayList<HeartRateResultRecord> heartRateResultRecordList, ProfileData profileData) {
        context = cntxt;
        buildAdapter(pedometerDailyList, pedometerList, pedometerTotal, exerciseDataList, sleepResultRecordList, heartRateResultRecordList, profileData);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_title_fitness);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int index) {
                Log.d(LOG_TAG, "Clicked: " + index);
            }
        });
        builder.setNegativeButton(R.string.dialog_close_fitness,  new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int index) {}
        });
        /*builder.setPositiveButton(R.string.dialog_sync_fitness,  new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int index) {
                Log.d(LOG_TAG, "Sync clicked");
                Intent msg = new Intent(OpenFitIntent.INTENT_GOOGLE_FIT);
                msg.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.INTENT_GOOGLE_FIT_SYNC);
                msg.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, true);
                getActivity().sendBroadcast(msg);
            }
        });*/
        dialog = builder.create();

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "Sync onstart");
        if(dialog != null) {
            /*Button syncButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
            if(syncButton != null) {
                Date now = new Date();
                Log.d(LOG_TAG, "trail: " + trialDate + " current: " + now);
                if(trialDate.getTime() < now.getTime()) {
                    syncButton.setEnabled(false);
                }
            }*/
        }
    }

    public void buildAdapter(ArrayList<PedometerData> pedometerDailyList, ArrayList<PedometerData> pedometerList, PedometerTotal pedometerTotal,
                             ArrayList<ExerciseData> exerciseDataList, ArrayList<SleepResultRecord> sleepResultRecordList,ArrayList<HeartRateResultRecord> heartRateResultRecordList, ProfileData profileData) {
        ArrayList<String> items = new ArrayList<String>();
        ArrayList<String> subitems = new ArrayList<String>();
        ArrayList<Drawable> iDraw = new ArrayList<Drawable>();
        Calendar cal = Calendar.getInstance();

        if(profileData != null) {
            String item = OpenFitData.getGender(profileData.getGender()) + ", " + profileData.getAge() + " years";
            String subitem = String.format(Locale.getDefault(), "%.2f", profileData.getHeight()) + "cm, " + String.format(Locale.getDefault(), "%.2f", profileData.getWeight()) + "kg";
            Drawable icon = context.getDrawable(R.drawable.open_stand);
            icon.setBounds(0, 0, 144, 144);
            items.add(item);
            subitems.add(subitem);
            iDraw.add(icon);
        }

        if(pedometerTotal != null) {
            String steps = Integer.toString(pedometerTotal.getSteps());
            String distance = String.format(Locale.getDefault(), "%.2f", pedometerTotal.getDistance());
            String calories = String.format(Locale.getDefault(), "%.2f", pedometerTotal.getCalories());
            String item = "Total steps: " + steps;
            items.add(item);
            String subitem = distance + "m, " + calories + "kcal";
            subitems.add(subitem);

            Drawable icon = context.getDrawable(R.drawable.open_walk);
            icon.setBounds(0, 0, 144, 144);
            iDraw.add(icon);
        }

        for(int i = pedometerDailyList.size() - 1; i >= 0 && i > pedometerDailyList.size() - 4; i--) {
            Date date = new Date(pedometerDailyList.get(i).getTimeStamp());
            cal.setTime(date);
            cal.set(Calendar.DAY_OF_MONTH, (cal.get(Calendar.DAY_OF_MONTH) - 1));
            String month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
            String day = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
            String year = Integer.toString(cal.get(Calendar.YEAR));

            String item = month + " " + day + ", " + year;
            items.add(item);

            String steps = Integer.toString(pedometerDailyList.get(i).getSteps());
            String distance = String.format(Locale.getDefault(), "%.2f", pedometerDailyList.get(i).getDistance());
            String calories = String.format(Locale.getDefault(), "%.2f", pedometerDailyList.get(i).getCalories());

            String subitem = "Steps: " + steps + ", " + distance + "m, " + calories + "kcal";
            subitems.add(subitem);

            Drawable icon = context.getDrawable(R.drawable.open_walk);
            icon.setBounds(0, 0, 144, 144);
            iDraw.add(icon);
        }

        int walking = 0;
        int running = 0;
        for(int i = exerciseDataList.size() - 1; i >= 0; i--) {
            if (walking >= 3 && exerciseDataList.get(i).getExerciseType() == OpenFitData.WALK) {
                continue;
            }
            if (running >= 3 && exerciseDataList.get(i).getExerciseType() == OpenFitData.RUN) {
                continue;
            }
            Date date = new Date(exerciseDataList.get(i).getTimeStamp());
            cal.setTime(date);
            cal.set(Calendar.DAY_OF_MONTH, (cal.get(Calendar.DAY_OF_MONTH)));
            String month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
            String day = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
            String year = Integer.toString(cal.get(Calendar.YEAR));
            String hour = Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
            String min = String.format("%02d", cal.get(Calendar.MINUTE));

            String item = month + " " + day + ", " + year + ", " + hour + ":" +  min;
            items.add(item);

            int h = (int)exerciseDataList.get(i).getDuration() / 3600;
            int m = (int)(exerciseDataList.get(i).getDuration() % 3600) / 60;
            int s = (int)exerciseDataList.get(i).getDuration() % 60;
            String duration = Integer.toString(h) + "h" + Integer.toString(m) + "m" + Integer.toString(s) + "s";
            String calories = String.format(Locale.getDefault(), "%.2f", exerciseDataList.get(i).getCalories());

            String subitem = "Duration: " + duration + "\nCalories: " + calories + "kcal\n";
            String heartRate = Integer.toString(exerciseDataList.get(i).getAvgHeartRate());
            String distance = String.format(Locale.getDefault(), "%.2f", exerciseDataList.get(i).getDistance());
            String avgSpeed = String.format(Locale.getDefault(), "%.2f", exerciseDataList.get(i).getAvgSpeed()*3.6); // 3.6 to km/h
            String maxSpeed = String.format(Locale.getDefault(), "%.2f", exerciseDataList.get(i).getMaxSpeed()*3.6); // 3.6 to km/h
            String maxHeartRate = Integer.toString(exerciseDataList.get(i).getMaxHeartRate());
            subitem += "Distance: " + distance + "m\n";
            subitem += "Avg HR: " + heartRate + "bpm\nMax HR: " + maxHeartRate + "bpm\n";
            subitem += "Avg speed: " + avgSpeed + "km/h\nMax speed: " + maxSpeed + "km/h";
            subitems.add(subitem);

            Drawable icon = null;
            if (exerciseDataList.get(i).getExerciseType() == OpenFitData.WALK) {
                icon = context.getDrawable(R.drawable.open_walk);
                walking += 1;
            }
            else if (exerciseDataList.get(i).getExerciseType() == OpenFitData.RUN) {
                icon = context.getDrawable(R.drawable.open_run);
                running += 1;
            }
            icon.setBounds(0, 0, 144, 144);
            iDraw.add(icon);
        }

        for(int i = sleepResultRecordList.size() - 1; i >= 0 && i > sleepResultRecordList.size() - 4; i--) {
            Date dateFrom = new Date(sleepResultRecordList.get(i).getStartTimeStamp());
            cal.setTime(dateFrom);
            cal.set(Calendar.DAY_OF_MONTH, (cal.get(Calendar.DAY_OF_MONTH)));
            String month1 = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
            String day1 = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
            String year1 = Integer.toString(cal.get(Calendar.YEAR));
            String hour1 = Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
            String min1 = String.format("%02d", cal.get(Calendar.MINUTE));

            Date dateTo = new Date(sleepResultRecordList.get(i).getEndTimeStamp());
            cal.setTime(dateTo);
            cal.set(Calendar.DAY_OF_MONTH, (cal.get(Calendar.DAY_OF_MONTH)));
            String month2 = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
            String day2 = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
            String year2 = Integer.toString(cal.get(Calendar.YEAR));
            String hour2 = Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
            String min2 = String.format("%02d", cal.get(Calendar.MINUTE));

            String item = "Start of sleep: " + month1 + " " + day1 + ", " + year1 + ", " + hour1 + ":" +  min1 + "\n";
            item += "End of sleep: " + month2 + " " + day2 + ", " + year2 + ", " + hour2 + ":" +  min2;
            items.add(item);

            String efficiency = String.format("%.2f", sleepResultRecordList.get(i).getEfficiency());

            String subitem = "Efficiency: " + efficiency + "%";
            subitems.add(subitem);

            Drawable icon = context.getDrawable(R.drawable.open_sleep);
            icon.setBounds(0, 0, 144, 144);
            iDraw.add(icon);
        }

        for(int i = heartRateResultRecordList.size() - 1; i >= 0 && i > heartRateResultRecordList.size() - 4; i--) {
            Date date = new Date(heartRateResultRecordList.get(i).getTimeStamp());
            cal.setTime(date);
            cal.set(Calendar.DAY_OF_MONTH, (cal.get(Calendar.DAY_OF_MONTH)));
            String month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
            String day = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
            String year = Integer.toString(cal.get(Calendar.YEAR));
            String hour = Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
            String min = String.format("%02d", cal.get(Calendar.MINUTE));

            String item = month + " " + day + ", " + year + ", " + hour + ":" +  min;
            items.add(item);

            String heartRate = Integer.toString(heartRateResultRecordList.get(i).getHeartRate());

            String subitem = "Heartrate: " + heartRate + "bpm";
            subitems.add(subitem);

            Drawable icon = context.getDrawable(R.drawable.open_heart);
            icon.setBounds(0, 0, 144, 144);
            iDraw.add(icon);
        }

        if(items.size() <= 0) {
            String item = "No fitness data found";
            String subitem = "Enable pedometer on Gear Fit";
            items.add(item);
            subitems.add(subitem);

            Drawable icon = context.getDrawable(R.drawable.open_info);
            icon.setBounds(0, 0, 144, 144);
            iDraw.add(icon);
        }

        adapter = new ArrayAdapterFitness(context, items, subitems, iDraw);
    }
}
