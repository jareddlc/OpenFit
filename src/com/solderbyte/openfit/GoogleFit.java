package com.solderbyte.openfit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataDeleteRequest;
import com.google.android.gms.fitness.request.SessionInsertRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.SessionReadResult;
import com.solderbyte.openfit.util.OpenFitIntent;

public class GoogleFit {
    private static final String LOG_TAG = "OpenFit:GoogleFit";

    private static Context context = null;
    private static GoogleApiClient mClient = null;
    private static ArrayList<Session> sessions = null;
    private static ArrayList<DataSet> sessionsStepsDataSets = null;
    private static ArrayList<DataSet> sessionsDistanceDataSets = null;
    private static ArrayList<DataSet> sessionsCaloriesDataSets = null;
    private static ArrayList<DataSet> sessionsActivitySegmentDataSets = null;
    private static ArrayList<PedometerData> pedometerList = null;

    private static Date lastSession = null;

    public GoogleFit(Context cntxt, GoogleApiClient client) {
        Log.d(LOG_TAG, "Creating Google Fit");
        context = cntxt;
        mClient = client;
    }

    public void syncData() {
        Log.d(LOG_TAG, "syncing data");
        new readDataTask().execute();
    }

    public void setData(ArrayList<PedometerData> pList) {
        Log.d(LOG_TAG, "setData");
        pedometerList = pList;
    }

    public void getData() {
        Log.d(LOG_TAG, "getData");
        new readDataTask().execute();
    }

    public void writeData() {
        Log.d(LOG_TAG, "writeData");
        new writeDataTask().execute();
    }

    public void parseDataSet(DataSet dataSet) {
        for(DataPoint dp : dataSet.getDataPoints()) {
            Date start = new Date(dp.getStartTime(TimeUnit.MILLISECONDS));
            Date end = new Date(dp.getEndTime(TimeUnit.MILLISECONDS));
            Log.d(LOG_TAG, "Type: " + dp.getDataType().getName());
            Log.d(LOG_TAG, "Date: " + start + ":" + end);
            for(Field field : dp.getDataType().getFields()) {
                Log.d(LOG_TAG, "Field: " + field.getName() + " Value: " + dp.getValue(field));
            }
        }
    }

    public void delData() {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -4);
        long startTime = cal.getTimeInMillis();

        DataDeleteRequest delRequest = new DataDeleteRequest.Builder()
        .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
        .deleteAllData()
        .deleteAllSessions()
        .build();

        Fitness.HistoryApi.deleteData(mClient, delRequest)
        .setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if(status.isSuccess()) {
                    Log.d(LOG_TAG, "Successfully deleted sessions");
                }
                else {
                    Log.d(LOG_TAG, "Failed to delete sessions");
                }
            }
        });
    }

    private class readDataTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            Log.d(LOG_TAG, "readDataTask");
            Calendar cal = Calendar.getInstance();
            Date now = new Date();
            cal.setTime(now);
            long endTime = cal.getTimeInMillis();
            cal.add(Calendar.DAY_OF_MONTH, -3);
            long startTime = cal.getTimeInMillis();

            SessionReadRequest readRequest = new SessionReadRequest.Builder()
            .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
            .read(DataType.TYPE_ACTIVITY_SEGMENT)
            .build();

            SessionReadResult sessionReadResult = Fitness.SessionsApi.readSession(mClient, readRequest).await(1, TimeUnit.MINUTES);

            Log.i(LOG_TAG, "Session read was successful. Number of returned sessions is: " + sessionReadResult.getSessions().size());
            for(Session session : sessionReadResult.getSessions()) {
                //Date start = new Date(session.getStartTime(TimeUnit.MILLISECONDS));
                //Date end = new Date(session.getEndTime(TimeUnit.MILLISECONDS));
                //Log.d(LOG_TAG, "Description: " + session.getDescription());
                //Log.d(LOG_TAG, "start: " + start + ", end: " + end);

                List<DataSet> dataSets = sessionReadResult.getDataSet(session);
                for(DataSet dataSet : dataSets) {
                    for(DataPoint dp : dataSet.getDataPoints()) {
                        lastSession = new Date(dp.getStartTime(TimeUnit.MILLISECONDS));
                    }
                }
            }

            writeData();
            return null;
        }
    }

    private class writeDataTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            Log.d(LOG_TAG, "writeDataTask");
            if(pedometerList == null) {
                Log.d(LOG_TAG, "pedometerList not set");
                return null;
            }

            DataSource stepsDataSource = new DataSource.Builder()
            .setAppPackageName("com.solderbyte.openfit")
            .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .setName("Open Fit - step count")
            .setType(DataSource.TYPE_RAW)
            .build();

            DataSource distanceDataSource = new DataSource.Builder()
            .setAppPackageName("com.solderbyte.openfit")
            .setDataType(DataType.TYPE_DISTANCE_DELTA)
            .setName("Open Fit - step count")
            .setType(DataSource.TYPE_RAW)
            .build();

            DataSource caloriesDataSource = new DataSource.Builder()
            .setAppPackageName("com.solderbyte.openfit")
            .setDataType(DataType.TYPE_CALORIES_EXPENDED)
            .setName("Open Fit - step count")
            .setType(DataSource.TYPE_RAW)
            .build();

            DataSource activitySegmentDataSource = new DataSource.Builder()
            .setAppPackageName("com.solderbyte.openfit")
            .setDataType(DataType.TYPE_ACTIVITY_SEGMENT)
            .setName("Open Fit - activity segment")
            .setType(DataSource.TYPE_RAW)
            .build();

            sessions = new ArrayList<Session>();
            sessionsStepsDataSets = new ArrayList<DataSet>();
            sessionsDistanceDataSets = new ArrayList<DataSet>();
            sessionsCaloriesDataSets = new ArrayList<DataSet>();
            sessionsActivitySegmentDataSets = new ArrayList<DataSet>();

            for(int i = 0; i < pedometerList.size(); i++) {
                int steps = pedometerList.get(i).getSteps();
                float cals = pedometerList.get(i).getCalories();
                float dist = pedometerList.get(i).getDistance();

                Calendar cal = Calendar.getInstance();
                Date startDate = new Date(pedometerList.get(i).getTimeStamp());
                cal.setTime(startDate);
                cal.add(Calendar.MINUTE, 10);
                Date endDate = cal.getTime();
                String month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
                String date = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
                String year = Integer.toString(cal.get(Calendar.YEAR));


                if(lastSession != null && lastSession.getTime() >= startDate.getTime()) {
                    Log.i(LOG_TAG, "Data already Synced");
                    continue;
                }

                // create data points
                DataSet dSteps = DataSet.create(stepsDataSource);
                DataPoint pSteps = dSteps.createDataPoint().setTimeInterval(startDate.getTime(), endDate.getTime(), TimeUnit.MILLISECONDS);
                pSteps.getValue(Field.FIELD_STEPS).setInt(steps);
                dSteps.add(pSteps);

                DataSet dDistance = DataSet.create(distanceDataSource);
                DataPoint pDistance = dDistance.createDataPoint().setTimeInterval(startDate.getTime(), endDate.getTime(), TimeUnit.MILLISECONDS);
                pDistance.getValue(Field.FIELD_DISTANCE).setFloat(dist);
                dDistance.add(pDistance);

                DataSet dCalories = DataSet.create(caloriesDataSource);
                DataPoint pCalories = dCalories.createDataPoint().setTimeInterval(startDate.getTime(), endDate.getTime(), TimeUnit.MILLISECONDS);
                pCalories.getValue(Field.FIELD_CALORIES).setFloat(cals);
                dCalories.add(pCalories);

                DataSet dActivitySegmentDataSet = DataSet.create(activitySegmentDataSource);
                DataPoint pActivitySegment = dActivitySegmentDataSet.createDataPoint().setTimeInterval(startDate.getTime(), endDate.getTime(), TimeUnit.MILLISECONDS);
                pActivitySegment.getValue(Field.FIELD_ACTIVITY).setActivity(FitnessActivities.WALKING);
                dActivitySegmentDataSet.add(pActivitySegment);

                Session session = new Session.Builder()
                .setName("Open Fit Pedometer - " + month + " " + date + ", " + year)
                .setDescription("Open Fit pedometer data gathered from Samsung Gear Fit")
                .setActivity(FitnessActivities.WALKING)
                .setStartTime(startDate.getTime(), TimeUnit.MILLISECONDS)
                .setEndTime(endDate.getTime(), TimeUnit.MILLISECONDS)
                .build();

                sessionsStepsDataSets.add(dSteps);
                sessionsDistanceDataSets.add(dDistance);
                sessionsCaloriesDataSets.add(dCalories);
                sessionsActivitySegmentDataSets.add(dActivitySegmentDataSet);
                sessions.add(session);
            }

            boolean success = false;
            if(sessions.size() == 0) {
                success = true;
            }

            for(int j = 0; j < sessions.size(); j++) {
                SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
                .setSession(sessions.get(j))
                .addDataSet(sessionsStepsDataSets.get(j))
                .addDataSet(sessionsDistanceDataSets.get(j))
                .addDataSet(sessionsCaloriesDataSets.get(j))
                .addDataSet(sessionsActivitySegmentDataSets.get(j))
                .build();

                //Log.d(LOG_TAG, "Inserting the session in the History API " + sessions.get(j).getDescription());
                com.google.android.gms.common.api.Status insertStatus = Fitness.SessionsApi.insertSession(mClient, insertRequest).await(1, TimeUnit.MINUTES);

                if(!insertStatus.isSuccess()) {
                    success = false;
                    Log.i(LOG_TAG, "There was a problem inserting the session: " + insertStatus);
                }
                else {
                    success = true;
                }
            }

            Intent msg = new Intent(OpenFitIntent.INTENT_GOOGLE_FIT);
            msg.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.INTENT_GOOGLE_FIT_SYNC_STATUS);
            if(!success) {
                msg.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, false);
                Log.d(LOG_TAG, "There was a problem inserting the dataset: ");
            }
            else {
                Log.d(LOG_TAG, "Data insert was successful! ");
                msg.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, true);
            }
            Log.i(LOG_TAG, "Sending cotext");
            context.sendBroadcast(msg);

            return null;
        }
    }
}
