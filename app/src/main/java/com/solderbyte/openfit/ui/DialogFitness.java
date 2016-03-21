package com.solderbyte.openfit.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.solderbyte.openfit.ExerciseData;
import com.solderbyte.openfit.HeartRateData;
import com.solderbyte.openfit.PedometerData;
import com.solderbyte.openfit.PedometerTotal;
import com.solderbyte.openfit.ProfileData;
import com.solderbyte.openfit.R;
import com.solderbyte.openfit.SleepData;
import com.solderbyte.openfit.util.OpenFitData;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;

public class DialogFitness extends DialogFragment {
    private static final String LOG_TAG = "OpenFit:DialogFitness";

    private AlertDialog dialog = null;
    private Date trialDate = null;
    private ListAdapter adapter;
    private Context context;

    public DialogFitness() {}

    public DialogFitness(Context cntxt, ArrayList<PedometerData> pedometerDailyList, ArrayList<PedometerData> pedometerList, PedometerTotal pedometerTotal, ArrayList<ExerciseData> exerciseDataList, ArrayList<SleepData> sleepList, ArrayList<HeartRateData> heartRateList, ProfileData profileData) {
        context = cntxt;
        buildAdapter(pedometerDailyList, pedometerList, pedometerTotal, exerciseDataList, sleepList, heartRateList, profileData);
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

    public void buildAdapter(ArrayList<PedometerData> pedometerDailyList, ArrayList<PedometerData> pedometerList, PedometerTotal pedometerTotal, ArrayList<ExerciseData> exerciseDataList, ArrayList<SleepData> sleepList, ArrayList<HeartRateData> heartRateList, ProfileData profileData) {
        ArrayList<String> items = new ArrayList<String>();
        ArrayList<String> subitems = new ArrayList<String>();
        ArrayList<Drawable> iDraw = new ArrayList<Drawable>();
        Calendar cal = Calendar.getInstance();

        // profile data
        if(profileData != null) {
            String item = "Profile";
            String subitem = "Gender: " + OpenFitData.getGender(profileData.getGender()) + "\n";
            subitem += "Age: " + profileData.getAge() + " years\n";
            subitem += "Height: " + String.format(Locale.getDefault(), "%.2f", profileData.getHeight()) + "cm\n";
            subitem += "Weight: " + String.format(Locale.getDefault(), "%.2f", profileData.getWeight()) + "kg";
            Drawable icon = context.getResources().getDrawable(R.drawable.open_stand);
            icon.setBounds(0, 0, 144, 144);
            items.add(item);
            subitems.add(subitem);
            iDraw.add(icon);
        }

        // pedometer total
        if(pedometerTotal != null) {
            String steps = Integer.toString(pedometerTotal.getSteps());
            String distance = String.format(Locale.getDefault(), "%.2f", pedometerTotal.getDistance());
            String calories = String.format(Locale.getDefault(), "%.2f", pedometerTotal.getCalories());
            String item = "Pedometer Total";
            items.add(item);
            String subitem = "Total steps: " + steps + "\n";
            subitem += "Distance: " + distance + "m\n";
            subitem += "Calories: " + calories + "kcal";
            subitems.add(subitem);

            Drawable icon = context.getResources().getDrawable(R.drawable.open_walk);
            icon.setBounds(0, 0, 144, 144);
            iDraw.add(icon);
        }

        // pedometer list
        for(int i = pedometerDailyList.size() - 1; i >= 0; i--) {
            Date date = new Date(pedometerDailyList.get(i).getTimeStamp());
            cal.setTime(date);
            cal.set(Calendar.DAY_OF_MONTH, (cal.get(Calendar.DAY_OF_MONTH) - 1));
            String month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
            String day = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
            String year = Integer.toString(cal.get(Calendar.YEAR));

            String item = "Steps";
            items.add(item);

            String steps = Integer.toString(pedometerDailyList.get(i).getSteps());
            String distance = String.format(Locale.getDefault(), "%.2f", pedometerDailyList.get(i).getDistance());
            String calories = String.format(Locale.getDefault(), "%.2f", pedometerDailyList.get(i).getCalories());

            String subitem = month + " " + day + ", " + year + "\n";
            subitem += "Steps: " + steps + "\n";
            subitem += "Distance: " + distance + "m\n";
            subitem += "Calories: " + calories + "kcal";
            subitems.add(subitem);

            Drawable icon = context.getResources().getDrawable(R.drawable.open_walk);
            icon.setBounds(0, 0, 144, 144);
            iDraw.add(icon);
        }

        // exercise list
        for(int i = exerciseDataList.size() - 1; i >= 0; i--) {
            Date date = new Date(exerciseDataList.get(i).getTimeStamp());
            cal.setTime(date);
            cal.set(Calendar.DAY_OF_MONTH, (cal.get(Calendar.DAY_OF_MONTH)));
            String month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
            String day = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
            String year = Integer.toString(cal.get(Calendar.YEAR));
            String hour = Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
            String min = String.format("%02d", cal.get(Calendar.MINUTE));

            Drawable icon = null;
            String item = null;

            if(exerciseDataList.get(i).getExerciseType() == OpenFitData.WALK) {
                item = "Exercise: Walking";
                icon = context.getResources().getDrawable(R.drawable.open_walk);
            }
            else if(exerciseDataList.get(i).getExerciseType() == OpenFitData.RUN) {
                item = "Exercise: Running";
                icon = context.getResources().getDrawable(R.drawable.open_run);
            }
            items.add(item);

            int h = (int)exerciseDataList.get(i).getDuration() / 3600;
            int m = (int)(exerciseDataList.get(i).getDuration() % 3600) / 60;
            int s = (int)exerciseDataList.get(i).getDuration() % 60;
            String duration = Integer.toString(h) + "h " + Integer.toString(m) + "m " + Integer.toString(s) + "s";
            String calories = String.format(Locale.getDefault(), "%.2f", exerciseDataList.get(i).getCalories());

            String heartRate = Integer.toString(exerciseDataList.get(i).getAvgHeartRate());
            String distance = String.format(Locale.getDefault(), "%.2f", exerciseDataList.get(i).getDistance());
            String avgSpeed = String.format(Locale.getDefault(), "%.2f", exerciseDataList.get(i).getAvgSpeed()*3.6); // 3.6 to km/h
            String maxSpeed = String.format(Locale.getDefault(), "%.2f", exerciseDataList.get(i).getMaxSpeed()*3.6); // 3.6 to km/h
            String maxHeartRate = Integer.toString(exerciseDataList.get(i).getMaxHeartRate());

            String subitem = month + " " + day + ", " + year + "\n";
            subitem += "Time: " + hour + ":" +  min + "\n";
            subitem += "Duration: " + duration + "\n";
            subitem += "Calories: " + calories + "kcal\n";
            subitem += "Distance: " + distance + "m\n";
            subitem += "Avg HR: " + heartRate + "bpm\n";
            subitem += "Max HR: " + maxHeartRate + "bpm\n";
            subitem += "Avg speed: " + avgSpeed + "km/h\n";
            subitem += "Max speed: " + maxSpeed + "km/h";
            subitems.add(subitem);

            icon.setBounds(0, 0, 144, 144);
            iDraw.add(icon);
        }

        // sleep list
        for(int i = sleepList.size() - 1; i >= 0 && i > sleepList.size() - 4; i--) {
            Date dateFrom = new Date(sleepList.get(i).getStartTimeStamp());
            cal.setTime(dateFrom);
            cal.set(Calendar.DAY_OF_MONTH, (cal.get(Calendar.DAY_OF_MONTH)));
            String monthF = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
            String dayF = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
            String yearF = Integer.toString(cal.get(Calendar.YEAR));
            String hourF = Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
            String minF = String.format("%02d", cal.get(Calendar.MINUTE));

            Date dateTo = new Date(sleepList.get(i).getEndTimeStamp());
            cal.setTime(dateTo);
            cal.set(Calendar.DAY_OF_MONTH, (cal.get(Calendar.DAY_OF_MONTH)));
            String monthT = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
            String dayT = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
            String yearT = Integer.toString(cal.get(Calendar.YEAR));
            String hourT = Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
            String minT = String.format("%02d", cal.get(Calendar.MINUTE));

            String item = "Sleep";
            items.add(item);

            String efficiency = String.format("%.2f", sleepList.get(i).getEfficiency());

            String subitem = "Start: " + monthF + " " + dayF + ", " + yearF + ", " + hourF + ":" +  minF + "\n";
            subitem += "Stop: " + monthT + " " + dayT + ", " + yearT + ", " + hourT + ":" +  minT + "\n";
            subitem += "Efficiency: " + efficiency + "%";
            subitems.add(subitem);

            Drawable icon = context.getResources().getDrawable(R.drawable.open_sleep);
            icon.setBounds(0, 0, 144, 144);
            iDraw.add(icon);
        }

        // heartrate list
        for(int i = heartRateList.size() - 1; i >= 0 && i > heartRateList.size() - 4; i--) {
            Date date = new Date(heartRateList.get(i).getTimeStamp());
            cal.setTime(date);
            cal.set(Calendar.DAY_OF_MONTH, (cal.get(Calendar.DAY_OF_MONTH)));
            String month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
            String day = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
            String year = Integer.toString(cal.get(Calendar.YEAR));
            String hour = Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
            String min = String.format("%02d", cal.get(Calendar.MINUTE));

            String item = "Heart Rate";
            items.add(item);

            String heartRate = Integer.toString(heartRateList.get(i).getHeartRate());

            String subitem = month + " " + day + ", " + year + "\n";
            subitem += "Time: " + hour + ":" +  min + "\n";
            subitem += "Heart Rate: " + heartRate + "bpm";
            subitems.add(subitem);

            Drawable icon = context.getResources().getDrawable(R.drawable.open_heart);
            icon.setBounds(0, 0, 144, 144);
            iDraw.add(icon);
        }

        // no fitness data
        if(items.size() <= 0) {
            String item = "No fitness data found";
            String subitem = "Enable pedometer on Gear Fit";
            items.add(item);
            subitems.add(subitem);

            Drawable icon = context.getResources().getDrawable(R.drawable.open_info);
            icon.setBounds(0, 0, 144, 144);
            iDraw.add(icon);
        }

        adapter = new ArrayAdapterFitness(context, items, subitems, iDraw);
    }
}
