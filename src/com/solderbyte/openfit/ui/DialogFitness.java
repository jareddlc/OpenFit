package com.solderbyte.openfit.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.solderbyte.openfit.PedometerData;
import com.solderbyte.openfit.PedometerTotal;
import com.solderbyte.openfit.R;

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

    private ListAdapter adapter;
    private Context context;
    public DialogFitness(Context cntxt, ArrayList<PedometerData> pedometerDailyList, ArrayList<PedometerData> pedometerList, PedometerTotal pedometerTotal) {
        context = cntxt;
        buildAdapter(pedometerDailyList, pedometerList, pedometerTotal);
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
        builder.setPositiveButton(R.string.dialog_close_fitness,  new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int index) {}
        });

        return builder.create();
    }
    
    public void buildAdapter(ArrayList<PedometerData> pedometerDailyList, ArrayList<PedometerData> pedometerList, PedometerTotal pedometerTotal) {
        ArrayList<String> items = new ArrayList<String>();
        ArrayList<String> subitems = new ArrayList<String>();
        ArrayList<Drawable> iDraw = new ArrayList<Drawable>();
        Calendar cal = Calendar.getInstance();

        for(int i = 0; i < pedometerDailyList.size(); i++) {
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

            String subitem = "Steps: " + steps + ", " + distance + "m, " + calories + "cal";
            subitems.add(subitem);
        }
        Drawable icon = context.getResources().getDrawable(R.drawable.open_walk);
        icon.setBounds(0, 0, 144, 144);
        iDraw.add(icon);
        adapter = new ArrayAdapterFitness(context, items, subitems, iDraw);
    }
}
