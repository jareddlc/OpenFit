package com.solderbyte.openfit;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

public class HttpClient {
    private static final String LOG_TAG = "OpenFit:Http";

    boolean isConnected = false;
    private URL url = null;
    private HttpURLConnection urlConnection = null;

    public HttpClient(Context context) {
        ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()) {
            Log.d(LOG_TAG, "Network connection available");
            isConnected = true;
        }
        else {
            isConnected = false;
            Log.d(LOG_TAG, "Network connection Not available");
        }
    }
    
    public String get(String u, AsyncResponse asyncResponse) {
        if(isConnected) {
            try {
                url = new URL(u);
                new getTask(asyncResponse).execute("tempURL");
            }
            catch(MalformedURLException e) {
                e.printStackTrace();
            }
            return u;
        }
        else {
            return null;
        }
    }

    public interface AsyncResponse {
        void callback(JSONObject response);
    }

    public class getTask extends AsyncTask<String, String, String> {
        public AsyncResponse delegate = null;

        public getTask(AsyncResponse asyncResponse) {
            delegate = asyncResponse;
        }

        @Override
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();

            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setUseCaches(false);
                urlConnection.setAllowUserInteraction(false);
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(5000);
                urlConnection.connect();
                int status = urlConnection.getResponseCode();
                Log.d(LOG_TAG, "Http response is: " + status);
                InputStream inStream = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader buffer = new BufferedReader(new InputStreamReader(inStream));

                String data;
                while((data = buffer.readLine()) != null) {
                    result.append(data);
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            finally {
                urlConnection.disconnect();
            }

            return result.toString();
        }

        @Override
        protected void onPostExecute(String res) {
            JSONObject response = null;
            try {
                response = new JSONObject(res.toString());
            }
            catch (JSONException e) {
                Log.e(LOG_TAG, "JSON Parse error" + e);
                e.printStackTrace();
            }
            delegate.callback(response);
        }

    }
}
