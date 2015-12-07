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
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataDeleteRequest;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.SessionInsertRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.solderbyte.openfit.util.OpenFitIntent;

public class GoogleFit {
    private static final String LOG_TAG = "OpenFit:GoogleFit";

    private static Context context = null;
    private static GoogleApiClient mClient = null;
    private static DataReadRequest readRequest = null;
    private static DataDeleteRequest delRequest = null;
    private static DataSet dataSet = null;
    private static DataSet activitySegmentDataSet = null;
    private static ArrayList<Session> sessions = null;
    private static ArrayList<DataSet> sessionsDataSets = null;
    private static ArrayList<DataSet> sessionsActivitySegmentDataSets = null;
    private static ArrayList<PedometerData> pedometerList = null;

    public GoogleFit(Context cntxt, GoogleApiClient client) {
        Log.d(LOG_TAG, "Creating Google Fit");
        context = cntxt;
        mClient = client;
        buildReadRequest();
    }

    public void buildReadRequest() {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -3);
        long startTime = cal.getTimeInMillis();

        readRequest = new DataReadRequest.Builder()
        .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
        .bucketByTime(1, TimeUnit.DAYS)
        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
        .build();
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
        Log.d(LOG_TAG, "getData");
        new writeDataTask().execute();
    }
    
    public void delData() {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -4);
        long startTime = cal.getTimeInMillis();

        delRequest = new DataDeleteRequest.Builder()
        .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
        //.addDataType(DataType.TYPE_STEP_COUNT_DELTA)
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
            DataReadResult dataReadResult = Fitness.HistoryApi.readData(mClient, readRequest).await(1, TimeUnit.MINUTES);
            Log.d(LOG_TAG, "dataReadResult" + dataReadResult);
            
            if(dataReadResult.getBuckets().size() > 0) {
                Log.d(LOG_TAG, "Number of returned buckets of DataSets is: " + dataReadResult.getBuckets().size());
                for(Bucket bucket : dataReadResult.getBuckets()) {
                    List<DataSet> dataSets = bucket.getDataSets();
                    for(DataSet dataSet : dataSets) {
                        parseDataSet(dataSet);
                    }
                }
            }
            else if(dataReadResult.getDataSets().size() > 0) {
                Log.d(LOG_TAG, "Number of returned DataSets is: " + dataReadResult.getDataSets().size());
                for(DataSet dataSet : dataReadResult.getDataSets()) {
                    parseDataSet(dataSet);
                }
            }
            return null;
        }
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

    private class writeDataTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            Log.d(LOG_TAG, "writeDataTask");
            if(pedometerList == null) {
                Log.d(LOG_TAG, "pedometerList not set");
                return null;
            }

            DataSource dataSource = new DataSource.Builder()
            .setAppPackageName("com.solderbyte.openfit")
            .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .setName("Open Fit - step count")
            .setType(DataSource.TYPE_RAW)
            .build();
            
            DataSource activitySegmentDataSource = new DataSource.Builder()
            .setAppPackageName("com.solderbyte.openfit")
            .setDataType(DataType.TYPE_ACTIVITY_SEGMENT)
            .setName("Open Fit - activity segments")
            .setType(DataSource.TYPE_RAW)
            .build();

            dataSet = DataSet.create(dataSource);
            activitySegmentDataSet = DataSet.create(activitySegmentDataSource);
            sessions = new ArrayList<Session>();

            sessionsDataSets = new ArrayList<DataSet>();
            sessionsActivitySegmentDataSets = new ArrayList<DataSet>();
            DataSet dSet = DataSet.create(dataSource);
            DataSet wSet = DataSet.create(activitySegmentDataSource);
            Calendar cal = Calendar.getInstance();

            Date startDate = null;
            Date startOfDay = null;
            Date endOfDay = null;
            int currentDay = 0;
            for(int i = 0; i < pedometerList.size(); i++) {
                int steps = pedometerList.get(i).getSteps();
                Date endDate = new Date(pedometerList.get(i).getTimeStamp());
                cal.setTime(endDate);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                cal.set(Calendar.MILLISECOND, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                startOfDay = cal.getTime();

                cal.set(Calendar.MILLISECOND, 0);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.HOUR_OF_DAY, 23);
                endOfDay = cal.getTime();

                if(day != currentDay) {
                    currentDay = day;

                    if(i == 0) {
                        // create data points
                        startDate = startOfDay;
                        DataPoint dataPoint = dSet.createDataPoint().setTimeInterval(startDate.getTime(), endDate.getTime(), TimeUnit.MILLISECONDS);
                        dataPoint.getValue(Field.FIELD_STEPS).setInt(steps);
                        dSet.add(dataPoint);

                        DataPoint wDataPoint = wSet.createDataPoint().setTimeInterval(startDate.getTime(), endDate.getTime(), TimeUnit.MILLISECONDS);
                        wDataPoint.getValue(Field.FIELD_ACTIVITY).setActivity(FitnessActivities.WALKING);
                        wSet.add(wDataPoint);
                    }
                    else {
                        // add data set to array
                        sessionsDataSets.add(dSet);
                        sessionsActivitySegmentDataSets.add(wSet);
                        // reset data sets
                        dSet = DataSet.create(dataSource);
                        wSet = DataSet.create(activitySegmentDataSource);

                        // check for out of bounds
                        Date prevDate = startDate;
                        if(startDate != null && startDate.getTime() < startOfDay.getTime()) {
                            startDate = startOfDay;
                        }
                        else if(startDate != null && startDate.getTime() > endOfDay.getTime()) {
                            startDate = endOfDay;
                        }
                        if(endDate.getTime() < startOfDay.getTime()) {
                            endDate = startOfDay;
                        }
                        else if(endDate.getTime() > endOfDay.getTime()) {
                            endDate = endOfDay;
                        }

                        // create data points
                        DataPoint dataPoint = dSet.createDataPoint().setTimeInterval(startDate.getTime(), endDate.getTime(), TimeUnit.MILLISECONDS);
                        dataPoint.getValue(Field.FIELD_STEPS).setInt(steps);
                        dSet.add(dataPoint);

                        DataPoint wDataPoint = wSet.createDataPoint().setTimeInterval(startDate.getTime(), endDate.getTime(), TimeUnit.MILLISECONDS);
                        wDataPoint.getValue(Field.FIELD_ACTIVITY).setActivity(FitnessActivities.WALKING);
                        wSet.add(wDataPoint);

                        // set previous day
                        cal.setTime(prevDate);
                        cal.set(Calendar.MILLISECOND, 0);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.HOUR_OF_DAY, 0);
                        startOfDay = cal.getTime();
                        String month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
                        String date = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
                        String year = Integer.toString(cal.get(Calendar.YEAR));

                        // create session
                        Session session = new Session.Builder()
                        .setName("Open Fit Pedometer")
                        .setDescription("Open Fit Pedometer - " + month + " " + date + ", " + year)
                        .setActivity(FitnessActivities.WALKING)
                        .setStartTime(startOfDay.getTime(), TimeUnit.MILLISECONDS)
                        .setEndTime(endOfDay.getTime(), TimeUnit.MILLISECONDS)
                        .build();
                        sessions.add(session);
                    }
                }
                else {
                    // create data points
                    DataPoint dataPoint = dSet.createDataPoint().setTimeInterval(startDate.getTime(), endDate.getTime(), TimeUnit.MILLISECONDS);
                    dataPoint.getValue(Field.FIELD_STEPS).setInt(steps);
                    dSet.add(dataPoint);

                    DataPoint wDataPoint = wSet.createDataPoint().setTimeInterval(startDate.getTime(), endDate.getTime(), TimeUnit.MILLISECONDS);
                    wDataPoint.getValue(Field.FIELD_ACTIVITY).setActivity(FitnessActivities.WALKING);
                    wSet.add(wDataPoint);
                }
                startDate = endDate;
                /* WORKING
                DataPoint dataPoint = dataSet.createDataPoint().setTimeInterval(startDate.getTime(), endDate.getTime(), TimeUnit.MILLISECONDS);
                dataPoint.getValue(Field.FIELD_STEPS).setInt(steps);
                dataSet.add(dataPoint);

                DataPoint wDataPoint = activitySegmentDataSet.createDataPoint().setTimeInterval(startDate.getTime(), endDate.getTime(), TimeUnit.MILLISECONDS);
                wDataPoint.getValue(Field.FIELD_ACTIVITY).setActivity(FitnessActivities.WALKING);
                activitySegmentDataSet.add(wDataPoint);*/

                //startDate = endDate;
            }
            /* WORKING
            Log.d(LOG_TAG, "Inserting the dataset in the History API");
            com.google.android.gms.common.api.Status insertStatus = Fitness.HistoryApi.insertData(mClient, dataSet).await(1, TimeUnit.MINUTES);

            Intent msg = new Intent(OpenFitIntent.INTENT_GOOGLE_FIT);
            msg.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.INTENT_GOOGLE_FIT_SYNC_STATUS);
            if(!insertStatus.isSuccess()) {
                msg.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, false);
                Log.i(LOG_TAG, "There was a problem inserting the dataset: " + insertStatus);
                return null;
            }
            else {
                Log.d(LOG_TAG, "Data insert was successful! ");
                msg.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, true);
            }*/

            boolean success = false;
            for(int j = 0; j < sessions.size(); j++) {
                SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
                .setSession(sessions.get(j))
                .addDataSet(sessionsDataSets.get(j))
                .addDataSet(sessionsActivitySegmentDataSets.get(j))
                .build();

                Log.d(LOG_TAG, "Inserting the session in the History API " + sessions.get(j).getDescription());
                com.google.android.gms.common.api.Status insertStatus = Fitness.SessionsApi.insertSession(mClient, insertRequest).await(1, TimeUnit.MINUTES);

                if(!insertStatus.isSuccess()) {
                    success = false;
                    Log.i(LOG_TAG, "There was a problem inserting the session: " + insertStatus);
                    return null;
                }
                else {
                    success = true;
                }
            }
            
            Intent msg = new Intent(OpenFitIntent.INTENT_GOOGLE_FIT);
            msg.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.INTENT_GOOGLE_FIT_SYNC_STATUS);
            if(!success) {
                msg.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, false);
                Log.i(LOG_TAG, "There was a problem inserting the dataset: ");
                return null;
            }
            else {
                Log.d(LOG_TAG, "Data insert was successful! ");
                msg.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, true);
            }
            context.sendBroadcast(msg);

            return null;
        }
    }
}
