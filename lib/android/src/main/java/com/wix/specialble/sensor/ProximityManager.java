package com.wix.specialble.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.util.Log;

import com.wix.specialble.listeners.ISensorListener;

public class ProximityManager implements ISensorListener {

    private static final String TAG = ProximityManager.class.getSimpleName();
    private boolean mIsSensorAvailable;
    private SensorManager mSensorManager;
    private Sensor mProximity;
    private SensorEvent mSensorEvent;

    public ProximityManager(SensorManager sensorManager) {
        mSensorManager = sensorManager;
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (mProximity != null) {
            mIsSensorAvailable = true;
        }
    }

    public void registerListener() {
        if (isSensorAvailable()) {
            mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void unregisterListener() {
        try {
            mSensorManager.unregisterListener(this);
        } catch (Exception e) {
            Log.e(TAG, "unregisterListener: ", e);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            mSensorEvent = event;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public boolean isSensorAvailable() {
        return mIsSensorAvailable;
    }

    @Override
    public float[] getEvents() {
        return mSensorEvent == null ? new float[1] : mSensorEvent.values;
    }
}
