package com.websocketim.activity;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.websocketim.R;

import java.io.IOException;

import static android.content.ContentValues.TAG;

public class SensorActivity extends Activity implements SensorEventListener{

    private AudioManager audioManager;
    private SensorManager sensorManager;
    private Sensor sensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        init();

    }

    private void init() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        MediaPlayer mPlayer = new MediaPlayer();
        try {
            mPlayer.reset();
            mPlayer.setDataSource("/storage/emulated/0/Music鹿晗-追梦赤子心.mp3");
            mPlayer.prepare();
            mPlayer.start();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float range = event.values[0];
        
        if (range == sensor.getMaximumRange()) {
            Log.d(TAG, "onSensorChanged: 正常模式 ");
            Toast.makeText(this, "正常模式", Toast.LENGTH_SHORT).show();
            audioManager.setMode(AudioManager.MODE_NORMAL);
        }else {
            Log.d(TAG, "onSensorChanged: 听筒模式 ");
            Toast.makeText(this, "听筒模式", Toast.LENGTH_SHORT).show();
            audioManager.setMode(AudioManager.MODE_IN_CALL);
        }
        Log.d(TAG, "onSensorChanged: "+sensor.getMaximumRange()+" "+sensor.getMaxDelay()+"  "+sensor.getMinDelay());

        float[] values = event.values;
        StringBuilder sb = new StringBuilder();
        sb.append("\nX方向上的加速度：");
        sb.append(values[0]);
        sb.append("\nY方向上的加速度：");
        sb.append(values[1]);
        sb.append("\nZ方向上的加速度：");
        sb.append(values[2]);
        Log.d(TAG, "onSensorChanged: "+sb.toString());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.d(TAG, "onAccuracyChanged: ");

    }
}
