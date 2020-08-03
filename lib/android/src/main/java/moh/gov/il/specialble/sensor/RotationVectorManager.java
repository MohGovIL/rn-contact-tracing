package moh.gov.il.specialble.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.util.Log;

import moh.gov.il.specialble.listeners.ISensorListener;

public class RotationVectorManager implements ISensorListener {

    private static final String TAG = RotationVectorManager.class.getSimpleName();
    private SensorManager mSensorManager;
    private Sensor mRotationVector;
    private SensorEvent mSensorEvent;
    private boolean mIsSensorAvailable;

    public RotationVectorManager(SensorManager sensorManager) {
        mSensorManager = sensorManager;
        mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        if (mRotationVector != null) {
            mIsSensorAvailable = true;
        }
    }

    public void registerListener() {
        if (mIsSensorAvailable) {
            mSensorManager.registerListener(this, mRotationVector, SensorManager.SENSOR_DELAY_NORMAL);
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
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
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

    public float[] getEvents() {
        return mSensorEvent == null ? new float[4] : mSensorEvent.values;
    }
}