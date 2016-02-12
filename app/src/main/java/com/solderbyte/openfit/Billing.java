package com.solderbyte.openfit;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;
import com.solderbyte.openfit.util.OpenFitIntent;

import org.json.JSONObject;

import java.util.ArrayList;

public class Billing {
    private static final String LOG_TAG = "OpenFit:Billing";

    public static final int BILLING_REQ = 1001;
    public static final int BILLING_RESPONSE_RESULT_OK = 0;
    public static final int BILLING_RESPONSE_RESULT_USER_CANCELED = 1;
    public static final int BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE = 2;
    public static final int BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE = 3;
    public static final int BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE = 4;
    public static final int BILLING_RESPONSE_RESULT_DEVELOPER_ERROR = 5;
    public static final int BILLING_RESPONSE_RESULT_ERROR = 6;
    public static final int BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7;
    public static final int BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED = 8;

    public static final int IABHELPER_ERROR_BASE = -1000;
    public static final int IABHELPER_REMOTE_EXCEPTION = -1001;
    public static final int IABHELPER_BAD_RESPONSE = -1002;
    public static final int IABHELPER_VERIFICATION_FAILED = -1003;
    public static final int IABHELPER_SEND_INTENT_FAILED = -1004;
    public static final int IABHELPER_USER_CANCELLED = -1005;
    public static final int IABHELPER_UNKNOWN_PURCHASE_RESPONSE = -1006;
    public static final int IABHELPER_MISSING_TOKEN = -1007;
    public static final int IABHELPER_UNKNOWN_ERROR = -1008;
    public static final int IABHELPER_SUBSCRIPTIONS_NOT_AVAILABLE = -1009;
    public static final int IABHELPER_INVALID_CONSUMPTION = -1010;
    public static final int IABHELPER_SUBSCRIPTION_UPDATE_NOT_AVAILABLE = -1011;

    public static final String RESPONSE_CODE = "RESPONSE_CODE";
    public static final String RESPONSE_GET_SKU_DETAILS_LIST = "DETAILS_LIST";
    public static final String RESPONSE_BUY_INTENT = "BUY_INTENT";
    public static final String RESPONSE_INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA";
    public static final String RESPONSE_INAPP_SIGNATURE = "INAPP_DATA_SIGNATURE";
    public static final String RESPONSE_INAPP_ITEM_LIST = "INAPP_PURCHASE_ITEM_LIST";
    public static final String RESPONSE_INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST";
    public static final String RESPONSE_INAPP_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST";
    public static final String INAPP_CONTINUATION_TOKEN = "INAPP_CONTINUATION_TOKEN";

    public static final int PURCHASE_STATE_PURCHASED = 0;
    public static final int PURCHASE_STATE_CANCELLED = 1;
    public static final int PURCHASE_STATE_REFUNDED = 2;

    public static final String ITEM_TYPE_INAPP = "inapp";

    public static final String GET_SKU_DETAILS_ITEM_LIST = "ITEM_ID_LIST";
    public static final String GET_SKU_DETAILS_ITEM_TYPE_LIST = "ITEM_TYPE_LIST";

    public static final String PREMIUM = "premium";


    private static IInAppBillingService billingService = null;
    private static PendingIntent buyIntent = null;
    private static boolean isPremium = false;
    public static String PREMIUM_PRICE = null;
    public static String PREMIUM_SKU = null;
    private static Context context = null;
    private static Activity activity = null;

    public void setContext(Context cntxt) {
        Log.d(LOG_TAG, "Setting context");
        context = cntxt;
    }

    public void setService(IInAppBillingService service) {
        Log.d(LOG_TAG, "Setting service");
        billingService = service;
    }

    public void setActivity(Activity actvty) {
        activity = actvty;
    }

    public Bundle getQueryBundle() {
        ArrayList<String> skuList = new ArrayList<String> ();
        skuList.add(PREMIUM);
        Bundle querySkus = new Bundle();
        querySkus.putStringArrayList(GET_SKU_DETAILS_ITEM_LIST, skuList);

        return querySkus;
    }

    public PendingIntent getBuyIntent() {
        return buyIntent;
    }

    public void getSkuDetails() {
        if(billingService != null && context != null) {
            new getPremiumTask().execute();
        }
        else {
            Log.d(LOG_TAG, "Cannot getSkuDetails. billingService or context is null");
        }
    }

    public void verifyPremium() {
        if(billingService != null && context != null) {
            new verifyPremiumTask().execute();
        }
        else {
            Log.d(LOG_TAG, "Cannot getPurchasedPremium. billingService or context is null");
        }
    }

    public void purchasePremium() {
        if(billingService != null && context != null || activity != null) {
            new purchasePremiumTask().execute();
        }
        else {
            Log.d(LOG_TAG, "Cannot purchasePremium. billingService, context, or activity is null");
        }
    }

    public int getResponseCodeFromBundle(Bundle b) {
        Object o = b.get(RESPONSE_CODE);
        if(o == null) {
            Log.d(LOG_TAG, "Bundle with null response code, assuming OK (known issue)");
            return BILLING_RESPONSE_RESULT_OK;
        }
        else if(o instanceof Integer) {
            return ((Integer)o).intValue();
        }
        else if(o instanceof Long) {
            return (int)((Long)o).longValue();
        }
        else {
            Log.d(LOG_TAG, "Unexpected type for bundle response code" + o.getClass().getName());
            throw new RuntimeException("Unexpected type for bundle response code: " + o.getClass().getName());
        }
    }

    private class getPremiumTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            Log.d(LOG_TAG, "getPremiumTask:");
            Bundle skuDetails;

            ArrayList<String> skuList = new ArrayList<String> ();
            skuList.add(PREMIUM);
            Bundle querySkus = new Bundle();
            querySkus.putStringArrayList(GET_SKU_DETAILS_ITEM_LIST, skuList);

            try {
                skuDetails = billingService.getSkuDetails(3, context.getPackageName(), ITEM_TYPE_INAPP, querySkus);

                int response = skuDetails.getInt(RESPONSE_CODE);
                if(response == BILLING_RESPONSE_RESULT_OK) {
                    ArrayList<String> responseList = skuDetails.getStringArrayList(RESPONSE_GET_SKU_DETAILS_LIST);

                    for(String thisResponse : responseList) {
                        JSONObject object = new JSONObject(thisResponse);
                        PREMIUM_SKU = object.getString("productId");
                        PREMIUM_PRICE = object.getString("price");
                        Log.d(LOG_TAG, "ProductId: " + PREMIUM_SKU);
                    }
                }
            }
            catch(Exception e) {
                Log.e(LOG_TAG, "getPremiumTask failed: " + e.getMessage());
            }
            return null;
        }
    }

    private class purchasePremiumTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            Log.d(LOG_TAG, "purchasePremiumTask: " + PREMIUM_SKU);
            Bundle buyIntentBundle;
            try {
                buyIntentBundle = billingService.getBuyIntent(3, context.getPackageName(), PREMIUM_SKU, ITEM_TYPE_INAPP, null);
                Log.d(LOG_TAG, "buyIntentBundle: " + buyIntentBundle);
                buyIntent = buyIntentBundle.getParcelable(RESPONSE_BUY_INTENT);
                activity.startIntentSenderForResult(buyIntent.getIntentSender(), BILLING_REQ, new Intent(), 0, 0, 0);
            }
            catch(Exception e) {
                Log.e(LOG_TAG, "purchasePremiumTask failed: " + e.getMessage());
            }
            return null;
        }
    }

    private class verifyPremiumTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            Log.d(LOG_TAG, "verifyPremiumTask: ");
            Bundle ownedItems;
            try {
                ownedItems = billingService.getPurchases(3, context.getPackageName(), ITEM_TYPE_INAPP, null);

                int response = ownedItems.getInt(RESPONSE_CODE);
                if(response == BILLING_RESPONSE_RESULT_OK) {
                    ArrayList<String>  purchaseDataList = ownedItems.getStringArrayList(RESPONSE_INAPP_PURCHASE_DATA_LIST);
                    ArrayList<String>  signatureList = ownedItems.getStringArrayList(RESPONSE_INAPP_SIGNATURE_LIST);
                    String continuationToken = ownedItems.getString(INAPP_CONTINUATION_TOKEN);

                    for(int i = 0; i < purchaseDataList.size(); ++i) {
                        String purchaseData = purchaseDataList.get(i);
                        //String signature = signatureList.get(i);

                        JSONObject object = new JSONObject(purchaseData);
                        String packageName = object.getString("packageName");
                        String sku = object.getString("productId");
                        String purchaseTime = object.getString("purchaseTime");
                        String purchaseState = object.getString("purchaseState");

                        Intent msg = new Intent(OpenFitIntent.INTENT_BILLING);
                        msg.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.INTENT_BILLING_VERIFIED);

                        if(sku.equals(PREMIUM_SKU) && packageName.equals(context.getPackageName())) {
                            isPremium = true;
                            Log.d(LOG_TAG, "Premium purchase found");
                            Log.d(LOG_TAG, "state: " + purchaseState);
                            msg.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, true);
                        }
                        else {
                            isPremium = false;
                            msg.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, false);
                            Log.d(LOG_TAG, "Premium purchase not found");
                        }
                        Log.d(LOG_TAG, "Sending cotext");
                        context.sendBroadcast(msg);

                    }
                }

            }
            catch(Exception e) {
                Log.e(LOG_TAG, "verifyPremiumTask failed: " + e.getMessage());
            }
            return null;
        }
    }
}
