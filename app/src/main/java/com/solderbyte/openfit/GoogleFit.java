package com.solderbyte.openfit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
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
import com.solderbyte.openfit.util.OpenFitData;
import com.solderbyte.openfit.util.OpenFitIntent;

public class GoogleFit {
    private static final String LOG_TAG = "OpenFit:GoogleFit";

    private static Context context = null;
    private static GoogleApiClient mClient = null;
    private static boolean GAPI_CONNECTED = false;
    private static ArrayList<Session> sessions = null;
    private static ArrayList<DataSet> sessionsStepsDataSets = null;
    private static ArrayList<DataSet> sessionsDistanceDataSets = null;
    private static ArrayList<DataSet> sessionsCaloriesDataSets = null;
    private static ArrayList<DataSet> sessionsActivitySegmentDataSets = null;

    private static ArrayList<Session> sessionsExercise = null;
    private static ArrayList<DataSet> sessionsExerciseDuration = null;
    private static ArrayList<DataSet> sessionsExerciseCalorie = null;
    private static ArrayList<DataSet> sessionsExerciseAvgHeartRate = null;
    private static ArrayList<DataSet> sessionsExerciseMaxHeartRate = null;
    private static ArrayList<DataSet> sessionsExerciseDistance = null;
    private static ArrayList<DataSet> sessionsExerciseAvgSpeed = null;
    private static ArrayList<DataSet> sessionsExerciseMaxSpeed = null;
    private static ArrayList<DataSet> sessionsExerciseSteps = null;

    private static ArrayList<Session> sessionsSleep = null;

    private static ArrayList<PedometerData> pedometerList = null;
    private static ArrayList<ExerciseData> excerciseList = null;
    private static ArrayList<SleepData> sleepList = null;
    private static ArrayList<SleepInfo> sleepInfoList = null;
    private static ArrayList<HeartRateData> heartRateList = null;
    private static ProfileData profileData = null;
    private static Date lastPedometerSession = null;
    private static Date lastRunningSession = null;
    private static Date lastWalkingSession = null;
    private static Date lastSleepSession = null;
    private static Date lastProfileData = null;
    private static Date lastPedometerStartTime = null;

    private static int lastPedometerSteps = 0;
    private static float lastPedometerCals = 0;
    private static float lastPedometerDist = 0;

    public GoogleFit() {}

    public GoogleFit(Context cntxt) {
        Log.d(LOG_TAG, "Creating Google Fit without client");
        context = cntxt;
        this.buildFitnessClient(cntxt);
        this.connectGoogleFit();
    }

    public GoogleFit(Context cntxt, GoogleApiClient client) {
        Log.d(LOG_TAG, "Creating Google Fit");
        context = cntxt;
        mClient = client;
    }

    public void syncData() {
        Log.d(LOG_TAG, "syncing data");
        new readDataTask().execute();
    }

    public void setData(ArrayList<PedometerData> pList, ArrayList<ExerciseData> eList, ArrayList<SleepData> srList, ArrayList<SleepInfo> siList, ArrayList<HeartRateData> hList, ProfileData pData) {
        Log.d(LOG_TAG, "setData");
        pedometerList = pList;
        excerciseList = eList;
        sleepList = srList;
        sleepInfoList = siList;
        heartRateList = hList;
        profileData = pData;
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

    private void buildFitnessClient(Context cntxt) {
        Log.d(LOG_TAG, "buildFitnessClient");
        mClient = new GoogleApiClient.Builder(cntxt)
        .addApi(Fitness.CONFIG_API)
        .addApi(Fitness.HISTORY_API)
        .addApi(Fitness.SESSIONS_API)
        .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
        .addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
        .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                Log.d(LOG_TAG, "Google Fit connected");
                GAPI_CONNECTED = true;

                Intent msg = new Intent(OpenFitIntent.INTENT_GOOGLE_FIT);
                msg.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.INTENT_GOOGLE_FIT);
                msg.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, true);
                //sendBroadcast(msg);
                //gFit = new GoogleFit(getBaseContext(), mClient);
            }

            @Override
            public void onConnectionSuspended(int i) {
                GAPI_CONNECTED = false;
                Intent msg = new Intent(OpenFitIntent.INTENT_GOOGLE_FIT);
                msg.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.INTENT_GOOGLE_FIT);
                msg.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, false);
                //sendBroadcast(msg);

                if(i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                    Log.d(LOG_TAG, "Google Fit connection lost. Network Lost");
                }
                else if(i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                    Log.d(LOG_TAG, "Google Fit connection lost. Service Disconnected");
                }
            }
        }).addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult result) {
                Log.d(LOG_TAG, "Google Fit connection failed. Cause: " + result.toString());
                GAPI_CONNECTED = false;
                Intent msg = new Intent(OpenFitIntent.INTENT_GOOGLE_FIT);
                msg.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.INTENT_GOOGLE_FIT);
                msg.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, false);
                //sendBroadcast(msg);
            }
        }).build();
    }

    public void connectGoogleFit() {
        if(mClient == null) {
            Log.d(LOG_TAG, "GoogleFit is null");
            return;
        }
        if(!mClient.isConnecting() && !mClient.isConnected()) {
            Log.d(LOG_TAG, "Connecting to GoogleFit");
            mClient.connect();
        }
        else {
            Log.d(LOG_TAG, "GoogleFit already connected: ");
        }
    }

    public void disconnectGoogleFit() {
        if(mClient.isConnected()) {
            Log.d(LOG_TAG, "Disconnecting to GoogleFit");
            PendingResult<Status> pendingResult = Fitness.ConfigApi.disableFit(mClient);

            pendingResult.setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if(status.isSuccess()) {
                        //GFIT_CONNECTED = false;
                        Log.d(LOG_TAG, "Google Fit disabled");
                        Intent msg = new Intent(OpenFitIntent.INTENT_GOOGLE_FIT);
                        msg.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.INTENT_GOOGLE_FIT);
                        msg.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, false);
                        //getActivity().sendBroadcast(msg);

                        mClient.disconnect();
                    } else {
                        Log.e(LOG_TAG, "Google Fit wasn't disabled " + status);
                    }
                }
            });
        }
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
            lastPedometerSteps = 0;
            lastPedometerCals = 0;
            lastPedometerDist = 0;

            SessionReadRequest readRequest = new SessionReadRequest.Builder()
            .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
            .read(DataType.TYPE_ACTIVITY_SEGMENT)
            .build();

            SessionReadResult sessionReadResult = Fitness.SessionsApi.readSession(mClient, readRequest).await(1, TimeUnit.MINUTES);

            Log.d(LOG_TAG, "Session read was successful. Number of returned sessions is: " + sessionReadResult.getSessions().size());
            for(Session session : sessionReadResult.getSessions()) {
                Date start = new Date(session.getStartTime(TimeUnit.MILLISECONDS));
                Date end = new Date(session.getEndTime(TimeUnit.MILLISECONDS));
                // Log.d(LOG_TAG, "Description: " + session.getDescription());
                // Log.d(LOG_TAG, "start: " + start + ", end: " + end);
                // Log.d(LOG_TAG, "Activity" + session.getActivity());

                List<DataSet> dataSets = sessionReadResult.getDataSet(session);
                for(DataSet dataSet : dataSets) {
                    for(DataPoint dp : dataSet.getDataPoints()) {
                        if(session.getActivity() == FitnessActivities.WALKING) {
                            long l = dp.getStartTime(TimeUnit.MILLISECONDS);
                            if (l % 60000 == 0) {
                                lastPedometerStartTime = new Date(l);
                            }
                            lastPedometerSession = new Date(dp.getEndTime(TimeUnit.MILLISECONDS));
                        }
                        else if(session.getActivity() == FitnessActivities.WALKING_FITNESS) {
                            lastWalkingSession = new Date(dp.getEndTime(TimeUnit.MILLISECONDS));
                        }
                        else if(session.getActivity() == FitnessActivities.RUNNING) {
                            lastRunningSession = new Date(dp.getEndTime(TimeUnit.MILLISECONDS));
                        }
                        else if(session.getActivity() == FitnessActivities.SLEEP) {
                            lastSleepSession = new Date(dp.getStartTime(TimeUnit.MILLISECONDS));
                        }
                    }
                }
            }

            if (lastPedometerSession != null) {
                if (lastWalkingSession != null && lastPedometerSession.getTime() < lastWalkingSession.getTime()) {
                    lastPedometerSession = lastWalkingSession;
                }
                if (lastRunningSession != null && lastPedometerSession.getTime() < lastRunningSession.getTime()) {
                    lastPedometerSession = lastRunningSession;
                }
            }

            if (lastPedometerStartTime != null) {
                SessionReadRequest readRequestPedometerSteps = new SessionReadRequest.Builder()
                        .setTimeInterval(lastPedometerStartTime.getTime(), lastPedometerSession.getTime(), TimeUnit.MILLISECONDS)
                        .read(DataType.TYPE_STEP_COUNT_DELTA)
                        .build();

                SessionReadResult sessionReadResultPedometerSteps = Fitness.SessionsApi.readSession(mClient, readRequestPedometerSteps).await(1, TimeUnit.MINUTES);

                for(Session session : sessionReadResultPedometerSteps.getSessions()) {
                    List<DataSet> dataSets = sessionReadResultPedometerSteps.getDataSet(session);
                    for(DataSet dataSet : dataSets) {
                        for (DataPoint dp : dataSet.getDataPoints()) {
                            lastPedometerSteps += dp.getValue(Field.FIELD_STEPS).asInt();
                        }
                    }
                }

                SessionReadRequest readRequestPedometerCals = new SessionReadRequest.Builder()
                        .setTimeInterval(lastPedometerStartTime.getTime(), lastPedometerSession.getTime(), TimeUnit.MILLISECONDS)
                        .read(DataType.TYPE_CALORIES_EXPENDED)
                        .build();

                SessionReadResult sessionReadResultPedometerCals = Fitness.SessionsApi.readSession(mClient, readRequestPedometerCals).await(1, TimeUnit.MINUTES);

                for(Session session : sessionReadResultPedometerCals.getSessions()) {
                    List<DataSet> dataSets = sessionReadResultPedometerCals.getDataSet(session);
                    for(DataSet dataSet : dataSets) {
                        for (DataPoint dp : dataSet.getDataPoints()) {
                            lastPedometerCals += dp.getValue(Field.FIELD_CALORIES).asFloat();
                        }
                    }
                }

                SessionReadRequest readRequestPedometerDist = new SessionReadRequest.Builder()
                        .setTimeInterval(lastPedometerStartTime.getTime(), lastPedometerSession.getTime(), TimeUnit.MILLISECONDS)
                        .read(DataType.TYPE_DISTANCE_DELTA)
                        .build();

                SessionReadResult sessionReadResultPedometerDist = Fitness.SessionsApi.readSession(mClient, readRequestPedometerDist).await(1, TimeUnit.MINUTES);

                for(Session session : sessionReadResultPedometerDist.getSessions()) {
                    List<DataSet> dataSets = sessionReadResultPedometerDist.getDataSet(session);
                    for(DataSet dataSet : dataSets) {
                        for (DataPoint dp : dataSet.getDataPoints()) {
                            lastPedometerDist += dp.getValue(Field.FIELD_DISTANCE).asFloat();
                        }
                    }
                }
            }

            Log.d(LOG_TAG, "Last pedo from: " + lastPedometerStartTime + " to: " + lastPedometerSession + " steps: " + lastPedometerSteps + " cals: " + lastPedometerCals + " dist: " + lastPedometerDist);
            Log.d(LOG_TAG, "Last walking: " + lastWalkingSession);
            Log.d(LOG_TAG, "Last running: " + lastRunningSession);
            writeData();
            return null;
        }
    }

    private class writeDataTask extends AsyncTask<Void, Void, Void> {

        private boolean insertPedometerData () {
            boolean success = false;
            if(pedometerList != null) {
                Log.d(LOG_TAG, "write pedometerList");

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
                    String month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
                    String date = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
                    String year = Integer.toString(cal.get(Calendar.YEAR));
                    Date endDate = new Date(pedometerList.get(i).getTimeStampEnd());

                    if(lastPedometerSession != null && lastPedometerSession.getTime() > startDate.getTime()) {
                        Log.d(LOG_TAG, "Pedometer data already Synced: " + startDate + " --- " + endDate + " --- "+steps);
                        continue;
                    }
                    if(steps == 0) {
                        continue;
                    }

                    Log.d(LOG_TAG, "Syncing: " + startDate + " --- " + endDate + " steps: " + steps);

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

                if(sessions.size() == 0) {
                    success = true;
                    Log.d(LOG_TAG, "Pedometer data alredy synced");
                }

                for(int j = 0; j < sessions.size(); j++) {
                    SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
                    .setSession(sessions.get(j))
                    .addDataSet(sessionsStepsDataSets.get(j))
                    .addDataSet(sessionsDistanceDataSets.get(j))
                    .addDataSet(sessionsCaloriesDataSets.get(j))
                    .addDataSet(sessionsActivitySegmentDataSets.get(j))
                    .build();

                    com.google.android.gms.common.api.Status insertStatus = Fitness.SessionsApi.insertSession(mClient, insertRequest).await(1, TimeUnit.MINUTES);

                    if(!insertStatus.isSuccess()) {
                        success = false;
                        Log.d(LOG_TAG, "Pedometer data: There was a problem inserting the session: " + insertStatus);
                    }
                    else {
                        success = true;
                        Log.d(LOG_TAG, "Pedometer data inserted: " + insertStatus);
                    }
                }
            }
            return success;
        }

        private boolean insertExerciseData () {
            boolean success = false;
            if(excerciseList != null) {
                Log.d(LOG_TAG, "write exerciseList");

                DataSource caloriesDataSource = new DataSource.Builder()
                .setAppPackageName("com.solderbyte.openfit")
                .setDataType(DataType.TYPE_CALORIES_EXPENDED)
                .setName("Open Fit - exercise")
                .setType(DataSource.TYPE_RAW)
                .build();

                DataSource avgHeartRateDataSource = new DataSource.Builder()
                .setAppPackageName("com.solderbyte.openfit")
                .setDataType(DataType.TYPE_HEART_RATE_BPM)
                .setName("Open Fit - exercise")
                .setType(DataSource.TYPE_RAW)
                .build();

                DataSource maxHeartRateDataSource = new DataSource.Builder()
                .setAppPackageName("com.solderbyte.openfit")
                .setDataType(DataType.TYPE_HEART_RATE_BPM)
                .setName("Open Fit - exercise")
                .setType(DataSource.TYPE_RAW)
                .build();

                DataSource avgSpeedDataSource = new DataSource.Builder()
                .setAppPackageName("com.solderbyte.openfit")
                .setDataType(DataType.TYPE_SPEED)
                .setName("Open Fit - exercise")
                .setType(DataSource.TYPE_RAW)
                .build();

                DataSource maxSpeedDataSource = new DataSource.Builder()
                .setAppPackageName("com.solderbyte.openfit")
                .setDataType(DataType.TYPE_SPEED)
                .setName("Open Fit - exercise")
                .setType(DataSource.TYPE_RAW)
                .build();

                DataSource distanceDataSource = new DataSource.Builder()
                .setAppPackageName("com.solderbyte.openfit")
                .setDataType(DataType.TYPE_DISTANCE_DELTA)
                .setName("Open Fit - exercise")
                .setType(DataSource.TYPE_RAW)
                .build();

                DataSource stepsDataSource = new DataSource.Builder()
                .setAppPackageName("com.solderbyte.openfit")
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setName("Open Fit - exercise")
                .setType(DataSource.TYPE_RAW)
                .build();

                DataSource activitySegmentDataSource = new DataSource.Builder()
                .setAppPackageName("com.solderbyte.openfit")
                .setDataType(DataType.TYPE_ACTIVITY_SEGMENT)
                .setName("Open Fit - activity segment")
                .setType(DataSource.TYPE_RAW)
                .build();

                sessionsExercise = new ArrayList<Session>();
                sessionsExerciseDistance = new ArrayList<DataSet>();
                sessionsExerciseCalorie = new ArrayList<DataSet>();
                sessionsExerciseAvgHeartRate = new ArrayList<DataSet>();
                sessionsExerciseAvgSpeed = new ArrayList<DataSet>();
                sessionsExerciseSteps = new ArrayList<DataSet>();
                sessionsActivitySegmentDataSets = new ArrayList<DataSet>();

                for(int i = 0; i < excerciseList.size(); i++) {
                    long exTimeStamp = excerciseList.get(i).getTimeStamp();
                    long exTimeStampEnd = excerciseList.get(i).getTimeStampEnd();
                    float calories = excerciseList.get(i).getCalories();
                    int avgHeartrate = excerciseList.get(i).getAvgHeartRate();
                    float distance = excerciseList.get(i).getDistance();
                    int type = excerciseList.get(i).getExerciseType();
                    float avgSpeed = excerciseList.get(i).getAvgSpeed();
                    float maxSpeed = excerciseList.get(i).getMaxSpeed();
                    int maxHeartrate = excerciseList.get(i).getMaxHeartRate();

                    Calendar cal = Calendar.getInstance();
                    Date startDate = new Date(exTimeStamp);
                    cal.setTime(startDate);
                    Date endDate = new Date(exTimeStampEnd);
                    String month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
                    String date = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
                    String year = Integer.toString(cal.get(Calendar.YEAR));

                    // get steps from pedometer for this exercise
                    int j = 0;
                    ArrayList<PedometerData> updatedPedometer = new ArrayList<PedometerData>();
                    long steps = 0;
                    while(true) {
                        if(j >= pedometerList.size()) {
                            break;
                        }
                        if(j < pedometerList.size() - 1 && pedometerList.get(j).getTimeStamp() < exTimeStamp && pedometerList.get(j + 1).getTimeStamp() > exTimeStamp) {
                            int firstj = j;
                            long st = 0;
                            float dst = 0;
                            float cls = 0;
                            int lastj = j;
                            while(j < pedometerList.size() - 1 && pedometerList.get(j).getTimeStampEnd() < exTimeStampEnd) {
                                st += pedometerList.get(j).getSteps();
                                dst += pedometerList.get(j).getDistance();
                                cls += pedometerList.get(j).getCalories();
                                j++;
                                lastj = j;
                            }
                            st += pedometerList.get(j).getSteps();
                            dst += pedometerList.get(j).getDistance();
                            cls += pedometerList.get(j).getCalories();

                            long lastTimeStamp = lastj < pedometerList.size() ? pedometerList.get(lastj).getTimeStampEnd() : pedometerList.get(lastj - 1).getTimeStampEnd();
                            double stepsPerSec = (double) st / (lastTimeStamp - pedometerList.get(firstj).getTimeStamp());
                            steps = Math.abs(Math.round(stepsPerSec * (exTimeStampEnd - exTimeStamp)));

                            long startTimeStamp = pedometerList.get(firstj).getTimeStamp();
                            long endTimeStamp = exTimeStamp;
                            st = Math.abs(Math.round(stepsPerSec * (endTimeStamp - startTimeStamp)));

                            float div = steps + st;
                            float newDst = div > 0 ? st * dst / div : 0;
                            float newCls = div > 0 ? st * cls / div : 0;
                            PedometerData pd = new PedometerData(startTimeStamp, (int) st, newDst, newCls);
                            pd.setTimeStampEnd(endTimeStamp);
                            updatedPedometer.add(pd);

                            startTimeStamp = exTimeStampEnd;
                            endTimeStamp = lastTimeStamp;
                            st = Math.abs(Math.round(stepsPerSec * (endTimeStamp - startTimeStamp)));
                            div = steps + st;
                            newDst = div > 0 ? st * dst / div : 0;
                            newCls = div > 0 ? st * cls / div : 0;
                            pd = new PedometerData(startTimeStamp, (int) st, newDst, newCls);
                            pd.setTimeStampEnd(endTimeStamp);
                            updatedPedometer.add(pd);
                        }
                        else if(j == pedometerList.size() - 1 && pedometerList.get(j).getTimeStamp() < exTimeStamp) {
                            float stepsPerSec = (float) pedometerList.get(j).getSteps() / (exTimeStampEnd - pedometerList.get(j).getTimeStamp());
                            steps = Math.abs(Math.round(stepsPerSec * (exTimeStampEnd - exTimeStamp)));

                            long startTimeStamp = pedometerList.get(j).getTimeStamp();
                            long endTimeStamp = exTimeStamp;
                            int st = Math.abs(Math.round(stepsPerSec * (endTimeStamp - startTimeStamp)));

                            float newDst = st * pedometerList.get(j).getDistance() / (steps + st);
                            float newCls = st * pedometerList.get(j).getCalories() / (steps + st);
                            PedometerData pd = new PedometerData(startTimeStamp, st, newDst, newCls);
                            pd.setTimeStampEnd(endTimeStamp);
                            updatedPedometer.add(pd);
                        }
                        else {
                            updatedPedometer.add(pedometerList.get(j));
                        }
                        j++;
                    }

                    pedometerList = updatedPedometer;

                    if(lastPedometerSession != null && lastPedometerSession.getTime() >= endDate.getTime()) {
                        // Log.d(LOG_TAG, "Pedometer data already Synced");
                        continue;
                    }

                    // create data points
                    DataSet dCalories = DataSet.create(caloriesDataSource);
                    DataPoint pCalories = dCalories.createDataPoint().setTimeInterval(startDate.getTime(), endDate.getTime(), TimeUnit.MILLISECONDS);
                    pCalories.getValue(Field.FIELD_CALORIES).setFloat(calories);
                    dCalories.add(pCalories);

                    DataSet dDistance = DataSet.create(distanceDataSource);
                    DataPoint pDistance = dDistance.createDataPoint().setTimeInterval(startDate.getTime(), endDate.getTime(), TimeUnit.MILLISECONDS);
                    pDistance.getValue(Field.FIELD_DISTANCE).setFloat(distance);
                    dDistance.add(pDistance);

                    DataSet dHeartRateAVG = DataSet.create(avgHeartRateDataSource);
                    DataPoint pHeartRateAVG = dHeartRateAVG.createDataPoint().setTimeInterval(startDate.getTime(), endDate.getTime(), TimeUnit.MILLISECONDS);
                    pHeartRateAVG.getValue(Field.FIELD_BPM).setFloat((float) avgHeartrate);
                    dHeartRateAVG.add(pHeartRateAVG);

                    DataSet dSpeedAVG = DataSet.create(avgSpeedDataSource);
                    DataPoint pSpeedAVG = dSpeedAVG.createDataPoint().setTimeInterval(startDate.getTime(), endDate.getTime(), TimeUnit.MILLISECONDS);
                    pSpeedAVG.getValue(Field.FIELD_SPEED).setFloat(avgSpeed);
                    dSpeedAVG.add(pSpeedAVG);

                    DataSet dSteps = DataSet.create(stepsDataSource);
                    DataPoint pSteps = dSteps.createDataPoint().setTimeInterval(startDate.getTime(), endDate.getTime(), TimeUnit.MILLISECONDS);
                    pSteps.getValue(Field.FIELD_STEPS).setInt((int)steps);
                    dSteps.add(pSteps);

                    String act = FitnessActivities.WALKING_FITNESS;
                    if(type == OpenFitData.RUN) {
                        act = FitnessActivities.RUNNING;
                    }

                    DataSet dActivitySegmentDataSet = DataSet.create(activitySegmentDataSource);
                    DataPoint pActivitySegment = dActivitySegmentDataSet.createDataPoint().setTimeInterval(startDate.getTime(), endDate.getTime(), TimeUnit.MILLISECONDS);
                    pActivitySegment.getValue(Field.FIELD_ACTIVITY).setActivity(act);
                    dActivitySegmentDataSet.add(pActivitySegment);

                    Session session = new Session.Builder()
                    .setName("Open Fit Exercise - " + month + " " + date + ", " + year)
                    .setDescription("Open Fit exercise data gathered from Samsung Gear Fit")
                    .setActivity(act)
                    .setStartTime(startDate.getTime(), TimeUnit.MILLISECONDS)
                    .setEndTime(endDate.getTime(), TimeUnit.MILLISECONDS)
                    .build();

                    sessionsExerciseDistance.add(dDistance);
                    sessionsExerciseCalorie.add(dCalories);
                    sessionsExerciseAvgHeartRate.add(dHeartRateAVG);
                    sessionsExerciseAvgSpeed.add(dSpeedAVG);
                    sessionsExerciseSteps.add(dSteps);
                    sessionsActivitySegmentDataSets.add(dActivitySegmentDataSet);
                    sessionsExercise.add(session);
                }

                if(sessionsExercise.size() == 0) {
                    success = true;
                }


                Log.d(LOG_TAG, "Exercise data size: " + sessionsExercise.size());

                for(int j = 0; j < sessionsExercise.size(); j++) {
                    SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
                    .setSession(sessionsExercise.get(j))
                    .addDataSet(sessionsExerciseDistance.get(j))
                    .addDataSet(sessionsExerciseCalorie.get(j))
                    .addDataSet(sessionsExerciseAvgHeartRate.get(j))
                    .addDataSet(sessionsExerciseAvgSpeed.get(j))
                    .addDataSet(sessionsExerciseSteps.get(j))
                    .addDataSet(sessionsActivitySegmentDataSets.get(j))
                    .build();

                    com.google.android.gms.common.api.Status insertStatus = Fitness.SessionsApi.insertSession(mClient, insertRequest).await(1, TimeUnit.MINUTES);

                    if(!insertStatus.isSuccess()) {
                        success = false;
                        Log.d(LOG_TAG, "Exercise data: There was a problem inserting the session: " + insertStatus);
                    }
                    else {
                        success = true;
                        Log.d(LOG_TAG, "Exercise data inserted: " + insertStatus);
                    }
                }
            }
            return success;
        }

        private boolean insertSleepData () {
            boolean success = false;
            if(sleepList != null) {
                Log.d(LOG_TAG, "write sleepList");

                DataSource activitySegmentDataSource = new DataSource.Builder()
                .setAppPackageName("com.solderbyte.openfit")
                .setDataType(DataType.TYPE_ACTIVITY_SEGMENT)
                .setName("Open Fit - activity segment")
                .setType(DataSource.TYPE_RAW)
                .build();

                sessionsSleep = new ArrayList<Session>();
                sessionsActivitySegmentDataSets = new ArrayList<DataSet>();

                for(int i = 0; i < sleepList.size(); i++) {
                    int index = sleepList.get(i).getIndex() - 1;
                    long timestampStart = sleepList.get(i).getStartTimeStamp();
                    long timestampEnd = sleepList.get(i).getEndTimeStamp();
                    float efficiency = sleepList.get(i).getEfficiency();
                    int len = sleepList.get(i).getLen();

                    Calendar cal = Calendar.getInstance();
                    Date startDate = new Date(timestampStart);
                    cal.setTime(startDate);
                    Date endDate = new Date(timestampEnd);
                    String month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
                    String date = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
                    String year = Integer.toString(cal.get(Calendar.YEAR));

                    if(lastSleepSession != null && lastSleepSession.getTime() >= startDate.getTime()) {
                        //Log.d(LOG_TAG, "Sleep info already Synced");
                        continue;
                    }
                    if(sleepInfoList.get(0).getIndex() > index + 1) {
                        continue;
                    }
                    index = index - sleepInfoList.get(0).getIndex() + 1;

                    // 0 - 29 SLEEP_AWAKE
                    // 30 - 79 SLEEP_LIGHT
                    // 80 - 100 SLEEP_DEEP
                    // Log.d(LOG_TAG, "START - END: " + startDate + "   " + endDate);
                    DataSet dActivitySegmentDataSet = DataSet.create(activitySegmentDataSource);
                    for(int j = index; j < index + len; j++) {
                        // create sessions
                        int sleepType = sleepInfoList.get(j).getStatus();
                        long infoFrom = sleepInfoList.get(j).getTimeStamp();
                        long infoTo = 0;

                        if(j < index + len - 1) {
                            infoTo = sleepInfoList.get(j + 1).getTimeStamp();
                        }
                        else {
                            infoTo = timestampEnd;
                        }

                        String sleepAct = FitnessActivities.SLEEP_AWAKE;

                        if(sleepType >= 30 && sleepType < 80) {
                            sleepAct = FitnessActivities.SLEEP_LIGHT;
                        }
                        else if(sleepType >= 80) {
                            sleepAct = FitnessActivities.SLEEP_DEEP;
                        }
                        Date t = new Date(infoFrom);
                        Date tt = new Date(infoTo);
                        DataPoint pActivitySegment = dActivitySegmentDataSet.createDataPoint().setTimeInterval(infoFrom, infoTo, TimeUnit.MILLISECONDS);
                        pActivitySegment.getValue(Field.FIELD_ACTIVITY).setActivity(sleepAct);
                        dActivitySegmentDataSet.add(pActivitySegment);

                    }

                    // create sessions
                    Session sessionSleep = new Session.Builder()
                    .setName("Open Fit Sleep info - " + month + " " + date + ", " + year)
                    .setDescription("Open Fit sleep data gathered from Samsung Gear Fit")
                    .setActivity(FitnessActivities.SLEEP)
                    .setStartTime(startDate.getTime(), TimeUnit.MILLISECONDS)
                    .setEndTime(endDate.getTime(), TimeUnit.MILLISECONDS)
                    .build();

                    sessionsActivitySegmentDataSets.add(dActivitySegmentDataSet);
                    sessionsSleep.add(sessionSleep);
                }

                if(sessionsSleep.size() == 0) {
                    success = true;
                    Log.d(LOG_TAG, "Sleep data alredy synced");
                }

                for(int j = 0; j < sessionsSleep.size(); j++) {
                    SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
                    .setSession(sessionsSleep.get(j))
                    .addDataSet(sessionsActivitySegmentDataSets.get(j))
                    .build();

                    com.google.android.gms.common.api.Status insertStatus = Fitness.SessionsApi.insertSession(mClient, insertRequest).await(1, TimeUnit.MINUTES);

                    if(!insertStatus.isSuccess()) {
                        success = false;
                        Log.d(LOG_TAG, "Sleep data: There was a problem inserting the session: " + insertStatus);
                    }
                    else {
                        success = true;
                        Log.d(LOG_TAG, "Sleep data inserted: " + insertStatus);
                    }
                }
            }
            return success;
        }

        private boolean insertProfileData () {
            boolean success = false;
            if(profileData != null) {
                Log.d(LOG_TAG, "write userData");

                DataSource heightDataSource = new DataSource.Builder()
                .setAppPackageName("com.solderbyte.openfit")
                .setDataType(DataType.TYPE_HEIGHT)
                .setName("Open Fit - profile data")
                .setType(DataSource.TYPE_RAW)
                .build();

                DataSource weightDataSource = new DataSource.Builder()
                .setAppPackageName("com.solderbyte.openfit")
                .setDataType(DataType.TYPE_WEIGHT)
                .setName("Open Fit - profile data")
                .setType(DataSource.TYPE_RAW)
                .build();

                Calendar cal = Calendar.getInstance();
                Date startDate = new Date(profileData.getTimeStamp());
                cal.setTime(startDate);
                cal.add(Calendar.SECOND, 1);
                Date endDate = new Date(cal.getTimeInMillis());

                DataSet dHeight = DataSet.create(heightDataSource);
                DataPoint pHeight = dHeight.createDataPoint().setTimeInterval(startDate.getTime(), endDate.getTime(), TimeUnit.MILLISECONDS);
                pHeight = pHeight.setFloatValues(profileData.getHeight() / 100);
                dHeight.add(pHeight);

                DataSet dWeight = DataSet.create(weightDataSource);
                DataPoint pWeight = dWeight.createDataPoint().setTimeInterval(startDate.getTime(), endDate.getTime(), TimeUnit.MILLISECONDS);
                pWeight = pWeight.setFloatValues(profileData.getWeight());
                dWeight.add(pWeight);

                com.google.android.gms.common.api.Status insertStatusH = Fitness.HistoryApi.insertData(mClient, dHeight).await(1, TimeUnit.MINUTES);
                com.google.android.gms.common.api.Status insertStatusW = Fitness.HistoryApi.insertData(mClient, dWeight).await(1, TimeUnit.MINUTES);

                if(!insertStatusH.isSuccess() || !insertStatusW.isSuccess()) {
                    success = false;
                    Log.d(LOG_TAG, "Profile data: There was a problem inserting the session");
                }
                else {
                    success = true;
                    Log.d(LOG_TAG, "Profile data inserted: " + insertStatusW);
                }

            }
            return success;
        }

        private boolean insertHeartRate () {
            boolean success = true;
            if(heartRateList != null) {
                Log.d(LOG_TAG, "write heartRate");

                DataSource bpmDataSource = new DataSource.Builder()
                .setAppPackageName("com.solderbyte.openfit")
                .setDataType(DataType.TYPE_HEART_RATE_BPM)
                .setName("Open Fit - heartrate data")
                .setType(DataSource.TYPE_RAW)
                .build();

                for(int i = 0; i < heartRateList.size(); i++) {
                    long timeStamp = heartRateList.get(i).getTimeStamp();
                    float bpm = (float) heartRateList.get(i).getHeartRate();
                    Calendar cal = Calendar.getInstance();
                    Date startDate = new Date(timeStamp);
                    cal.setTime(startDate);
                    cal.add(Calendar.SECOND, 1);
                    Date endDate = new Date(cal.getTimeInMillis());

                    DataSet dBPM = DataSet.create(bpmDataSource);
                    DataPoint pBPM = dBPM.createDataPoint().setTimeInterval(startDate.getTime(), endDate.getTime(), TimeUnit.MILLISECONDS);
                    pBPM = pBPM.setFloatValues(bpm);
                    dBPM.add(pBPM);

                    com.google.android.gms.common.api.Status insertStatusBPM = Fitness.HistoryApi.insertData(mClient, dBPM).await(1, TimeUnit.MINUTES);
                    if(!insertStatusBPM.isSuccess()) {
                        success &= false;
                        Log.d(LOG_TAG, "Profile data: There was a problem inserting the session");
                    }
                    else {
                        success &= true;
                        Log.d(LOG_TAG, "Profile data inserted: " + insertStatusBPM);
                    }
                }
            }
            return success;
        }

        private void updateLastPedo () {
            if (lastPedometerStartTime != null) {
                PedometerData p = pedometerList.get(pedometerList.size() - 1);
                Log.d(LOG_TAG, "Original: " + new Date(p.getTimeStamp()) + " to: " + new Date(p.getTimeStampEnd()) + " steps: " + p.getSteps() + " cals: " + p.getCalories() + " dist: " + p.getDistance());
                if (lastPedometerStartTime.getTime() == p.getTimeStamp() && p.getTimeStamp() < lastPedometerSession.getTime() && p.getTimeStampEnd() > lastPedometerSession.getTime()) {
                    pedometerList.remove(pedometerList.size() - 1);
                    PedometerData p1 = new PedometerData(lastPedometerStartTime.getTime(), lastPedometerSteps, lastPedometerDist, lastPedometerCals);
                    p1.setTimeStampEnd(lastPedometerSession.getTime());
                    PedometerData p2 = new PedometerData(lastPedometerSession.getTime(), p.getSteps() - lastPedometerSteps, p.getDistance() - lastPedometerDist, p.getCalories() - lastPedometerCals);
                    p2.setTimeStampEnd(p.getTimeStampEnd());
                    Log.d(LOG_TAG, "Update1: " + new Date(p1.getTimeStamp()) + " to: " + new Date(p1.getTimeStampEnd()) + " steps: " + p1.getSteps() + " cals: " + p1.getCalories() + " dist: " + p1.getDistance());
                    Log.d(LOG_TAG, "Update2: " + new Date(p2.getTimeStamp()) + " to: " + new Date(p2.getTimeStampEnd()) + " steps: " + p2.getSteps() + " cals: " + p2.getCalories() + " dist: " + p2.getDistance());
                    pedometerList.add(p1);
                    pedometerList.add(p2);
                }
            }
        }

        protected Void doInBackground(Void... params) {
            Log.d(LOG_TAG, "writeDataTask");

            updateLastPedo();

            boolean successExercise = insertExerciseData();
            boolean successPedometer = insertPedometerData();
            boolean successSleep = insertSleepData();
            boolean successProfileData = insertProfileData();;
            boolean successHeartRate = insertHeartRate();

            Intent msg = new Intent(OpenFitIntent.INTENT_GOOGLE_FIT);
            msg.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.INTENT_GOOGLE_FIT_SYNC_STATUS);

            if(!successExercise) {
                msg.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, false);
                Log.d(LOG_TAG, "There was a problem inserting excercise data");
            }
            else if(!successPedometer) {
                msg.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, false);
                Log.d(LOG_TAG, "There was a problem inserting pedometer data");
            }
            else if(!successSleep) {
                msg.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, false);
                Log.d(LOG_TAG, "There was a problem inserting sleep data");
            }
            else if(!successProfileData) {
                msg.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, false);
                Log.d(LOG_TAG, "There was a problem inserting profile data");
            }
            else if(!successHeartRate) {
                msg.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, false);
                Log.d(LOG_TAG, "There was a problem inserting heartrate data");
            }
            else {
                Log.d(LOG_TAG, "Data insert was successful! ");
                msg.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, true);
            }
            Log.d(LOG_TAG, "Sending context");
            context.sendBroadcast(msg);

            return null;
        }
    }
}
