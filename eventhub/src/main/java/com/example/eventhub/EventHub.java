package com.example.eventhub;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by 01547598 on 1/26/2017.
 */

public class EventHub extends AsyncTask<String, Void, Intent> {

    /**
     * HTTPS constants
     */
    private static final String HTTPS = "https://";
    private static final String MESSAGES = "/messages";
    private static final String SERVER = ".servicebus.windows.net/";
    private static final String AUTHORIZATION = "Authorization";
    private static final String CONTENT = "Content-Type";
    private static final String JSON = "application/json";
    public static final String HTTP_CODE = "HttpCode";

    /**
     * JSON constants
     */
    private static final String DEVICE = "device";
    private static final String SENSOR = "sensor";
    private static final String TIME = "time";
    private static final String VALUE = "value";


    private String mNameSpace; //eventhubvtc-ns
    private String mEventHub; //eh-vtc-iao
    private String mHubSasKeyName; //"RootManageSharedAccessKey" "valepoc"
    private String mHubSasKeyValue; //"iXwYAtBfEwW1vf7vr9O9GAoMaoKHjOkeJL3bXDfPSkA=" "1Yqc2kkuVAabR0eRGOO5Via2lArYAXkkRr78MdvMySY="



    public final static List<EventHub> nAsyncTasks = new ArrayList<EventHub>();

    private int responseCode;

    protected OnTaskFinished mCallbacks;

    public EventHub(OnTaskFinished o) {
        this.mCallbacks = o;
    }

    public EventHub setmNameSpace(String mNameSpace) {
        this.mNameSpace = mNameSpace;
        return this;
    }

    public EventHub setmEventHub(String mEventHub) {
        this.mEventHub = mEventHub;
        return this;
    }

    public EventHub setmHubSasKeyName(String mHubSasKeyName) {
        this.mHubSasKeyName = mHubSasKeyName;
        return this;
    }

    public EventHub setmHubSasKeyValue(String mHubSasKeyValue) {
        this.mHubSasKeyValue = mHubSasKeyValue;
        return this;
    }

    public String getmNameSpace() {
        return mNameSpace;
    }

    public String getmEventHub() {
        return mEventHub;
    }

    public String getmHubSasKeyName() {
        return mHubSasKeyName;
    }

    public String getmHubSasKeyValue() {
        return mHubSasKeyValue;
    }

    public void send (String body) {
        this.execute(body);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        EventHub.nAsyncTasks.add(this);
    }

    @Override
    protected Intent doInBackground(String... params) {
        Bundle data = new Bundle();
        try {
            String mURL = HTTPS + mNameSpace + SERVER + mEventHub +MESSAGES;
            String mUrlToken = HTTPS + mNameSpace + SERVER;
            URL url = new URL(mURL);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(15000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod(HTTPMethodEnum.HTTP_POST.toString());
            urlConnection.setRequestProperty(CONTENT, JSON);
            urlConnection.setRequestProperty(AUTHORIZATION, generateSasToken(mUrlToken));

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = null;
            writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(params[0]);
            writer.flush();
            writer.close();

            responseCode = urlConnection.getResponseCode();
            data.putInt(HTTP_CODE, responseCode);
        } catch (Exception e) {
            e.printStackTrace();
            //Toast.makeText(context, "Exception", Toast.LENGTH_LONG).show();
        }

        final Intent intent = new Intent();
        intent.putExtras(data);
        return intent;
    }

    @Override
    protected void onPostExecute(Intent intent) {
        super.onPostExecute(intent);
        mCallbacks.postExecute(intent);
        if (responseCode == HttpURLConnection.HTTP_CREATED) { // success
            //Toast.makeText(context, jsonParam.toString(), Toast.LENGTH_SHORT).show();
        } else {
           // Toast.makeText(context, "Something wrong with Internet connection", Toast.LENGTH_SHORT).show();
        }
        EventHub.nAsyncTasks.remove(this);
    }

    private String generateSasToken(String uri) {
        String targetUri;
        String token = null;
        try {
            targetUri = URLEncoder
                    .encode(uri.toString().toLowerCase(), "UTF-8")
                    .toLowerCase();

            long expiresOnDate = System.currentTimeMillis();
            int expiresInMins = 60; // 1 hour
            expiresOnDate += expiresInMins * 60 * 1000;
            long expires = expiresOnDate / 1000;
            String toSign = targetUri + "\n" + expires;

            // Get an hmac_sha1 key from the raw key bytes
            byte[] keyBytes = mHubSasKeyValue.getBytes("UTF-8");
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA256");

            // Get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);

            // Compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(toSign.getBytes("UTF-8"));

            // Using android.util.Base64 for Android Studio instead of
            // Apache commons codec
            String signature = URLEncoder.encode(
                    Base64.encodeToString(rawHmac, Base64.NO_WRAP).toString(), "UTF-8");

            // Construct authorization string
            token = "SharedAccessSignature sr=" + targetUri + "&sig="
                    + signature + "&se=" + expires + "&skn=" + mHubSasKeyName;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return token;
    }
}
