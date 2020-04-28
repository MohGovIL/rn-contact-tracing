package com.wix.specialble.listeners;

import android.hardware.SensorEventListener;

public interface ISensorListener extends SensorEventListener {
    float[] getEvents();
    boolean isSensorAvailable();
}
