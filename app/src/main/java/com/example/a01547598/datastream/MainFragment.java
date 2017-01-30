package com.example.a01547598.datastream;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventhub.EventHub;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


/**
 * Created by 01547598 on 1/26/2017.
 */

public class MainFragment extends Fragment {

    public static final int RequestPermissionCode = 1;

    private static final String NAMESPACE = "eventhubvtc-ns"; //eventhubvtc-ns
    private static final String EVENTHUB = "eh-vtc-iao"; //eh-vtc-iao
    private static final String HUBSASKEYNAME = "RootManageSharedAccessKey"; //"RootManageSharedAccessKey" "valepoc"
    private static final String HUBSASKEYVALUE = "iXwYAtBfEwW1vf7vr9O9GAoMaoKHjOkeJL3bXDfPSkA="; //"iXwYAtBfEwW1vf7vr9O9GAoMaoKHjOkeJL3bXDfPSkA=" "1Yqc2kkuVAabR0eRGOO5Via2lArYAXkkRr78MdvMySY="

    private static final String AUDIO = "audio";

    private TextView textView;
    private TextView deviceValue;
    private TextView timeValue;
    private Switch switchView;
    private Switch switchView2;
    private android.support.v7.widget.CardView cardView1;
    private android.support.v7.widget.CardView cardView2;

    private String AudioSavePathInDevice = null;
    private MediaRecorder mediaRecorder ;

    private int mInterval = 1000; // 1 second by default, can be changed later
    private Handler mHandler;
    private MyRunnable mAudioListener;

    private static final double EMA_FILTER = 0.6;
    private double mEMA = 0.0;

    private boolean isMediaRecorder = false;

    private String android_id;

    public static MainFragment newInstance() {
        MainFragment f = new MainFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.main_fragment, container, false);

        cardView1 = (CardView) rootView.findViewById(R.id.card_view);
        cardView2 = (CardView) rootView.findViewById(R.id.card_view2);
        deviceValue = (TextView) rootView.findViewById(R.id.devicevalue);
        timeValue = (TextView) rootView.findViewById(R.id.timevalue);
        textView = (TextView) rootView.findViewById(R.id.textview);
        switchView = (Switch) rootView.findViewById(R.id.switchAudio);
        switchView2 = (Switch) rootView.findViewById(R.id.switchAudio2);

        android_id = Settings.Secure.getString(getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        prepareAudioListener();

        cardView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!switchView.isChecked()) {
                    switchView.setChecked(true);
                } else {
                    switchView.setChecked(false);
                }
            }
        });

        cardView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!switchView2.isChecked()) {
                    switchView2.setChecked(true);
                } else {
                    switchView2.setChecked(false);
                }
            }
        });

        switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    start();
                }else{
                    stop();
                }
            }
        });

        return rootView;
    }

    private void prepareAudioListener() {
        mHandler = new Handler();
        mAudioListener = new MyRunnable() {
            @Override
            public void doWork() {
                if(mediaRecorder != null) {
                    try {
                        double amp = getAmplitudeEMA3();
                        //double amp = getAmplitudeDB();
                        if (amp != 0) {
                            SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
                            s.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                            String now = s.format(new Date());

                            deviceValue.setText(android_id);
                            timeValue.setText(now.substring(0, 19));
                            textView.setText(String.valueOf(amp));

                            JSONObject jsonParam = new JSONObject();
                            jsonParam.put("device", android_id);
                            jsonParam.put("sensor", AUDIO);
                            jsonParam.put("time", now);
                            jsonParam.put("value", amp);

                            new EventHub(getContext())
                                    .setmNameSpace(NAMESPACE)
                                    .setmEventHub(EVENTHUB)
                                    .setmHubSasKeyName(HUBSASKEYNAME)
                                    .setmHubSasKeyValue(HUBSASKEYVALUE).send(jsonParam.toString());

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        // 100% guarantee that this always happens, even if
                        // your update method throws an exception
                        mHandler.postDelayed(mAudioListener, mInterval);
                    }
                }
            }
        };
    }

    private void start() {
        if(checkPermission()) {
            startRecord();
        }
        else {
            requestPermission();
        }
    }

    public void setAudioSwitchCheck(boolean b) {
        switchView.setChecked(b);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length > 0) {

                    boolean StoragePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean InternetPermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean NetWorkPermission = grantResults[3] == PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission && InternetPermission && NetWorkPermission) {

                        Toast.makeText(getContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(getContext(),"Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }

                break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void startRecord() {
        AudioSavePathInDevice = getContext().getExternalCacheDir().getAbsolutePath() + "/" + "AudioRecording.3gp";
        MediaRecorderReady();
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isMediaRecorder = true;
            mAudioListener.liveRunnable();
            mAudioListener.run();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void stop() {
        if (isMediaRecorder) {
            try {
                mediaRecorder.stop();
                isMediaRecorder = false;
                mediaRecorder = null;
                mAudioListener.killRunnable();
                mHandler.removeCallbacks(mAudioListener);
                textView.setText("0.000");
                timeValue.setText("dd-mm-yyyyThh:mm:ss");
                deviceValue.setText("Device Name");
                File file = new File(getContext().getExternalCacheDir().getAbsolutePath() + "/" + "AudioRecording.3gp");
                file.delete();

                for (EventHub n : EventHub.nAsyncTasks) {
                    if (n.getStatus().equals(AsyncTask.Status.RUNNING)) {
                      n.cancel(true);
                    }
                }
                EventHub.nAsyncTasks.clear();

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void MediaRecorderReady(){

        mediaRecorder=new MediaRecorder();

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);

        mediaRecorder.setOutputFile(AudioSavePathInDevice);

    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE}, RequestPermissionCode);
    }

    private boolean checkPermission() {

        int result = ContextCompat.checkSelfPermission(getContext().getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getContext().getApplicationContext(), RECORD_AUDIO);
        int result2 = ContextCompat.checkSelfPermission(getContext().getApplicationContext(), INTERNET);
        int result3 = ContextCompat.checkSelfPermission(getContext().getApplicationContext(), ACCESS_NETWORK_STATE);

        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED && result3 == PackageManager.PERMISSION_GRANTED;
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    private double getAmplitude() {
        if (mediaRecorder != null)
            return  (mediaRecorder.getMaxAmplitude()/2700.0);
        else
            return 0;

    }

    private double getAmplitudeEMA() {
        double amp = getAmplitude();
        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
        return mEMA;
    }

    private double getAmplitudeEMA3() {
        double amp2 = getAmplitudeEMA();
        return round (amp2, 3);
    }

    private double getAmplitudeDB() {
        double dB =  20 * Math.log10(getAmplitudeEMA() / 32767.0);
        return round (dB, 3);
    }

    @Override
    public void onDestroyView() {
        stop();
        super.onDestroyView();
    }

}
