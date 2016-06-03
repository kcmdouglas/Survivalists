package com.eyecuelab.survivalists.util;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

/**
 * Created by eyecuelab on 6/2/16.
 */
public class ShakeDetector implements SensorEventListener {
    private static final int MIN_SHAKE_ACCELERATION = 5;
    private static final int MIN_MOVEMENTS = 5;
    private static final int MAX_SHAKE_DURATION = 700;

    //Store gravity and linear acceleration values
    private float[] mGravity = {0.0f,0.0f,0.0f};
    private float[] mLinearAcceleration = {0.0f, 0.0f, 0.0f};

    private static final int X = 0;
    private static final int Y = 1;
    private static final int Z = 2;

    private OnShakeListener mShakeListener;

    long startTime = 0;
    int moveCount = 0;

    public ShakeDetector(OnShakeListener shakeListener) {
        mShakeListener = shakeListener;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        setCurrentAcceleration(event);
        float maxLinearAcceleration = getMaxCurrentLinearAcceleration();

        if(maxLinearAcceleration > MIN_SHAKE_ACCELERATION) {
            long now = System.currentTimeMillis();

            if (startTime == 0) {
                startTime = now;
            }

            long elapsedTime = now - startTime;

            if(elapsedTime > MAX_SHAKE_DURATION) {
                resetShakeDetection();
            } else {
                moveCount++;

                if (moveCount > MIN_MOVEMENTS) {
                    mShakeListener.onShake();
                    resetShakeDetection();
                }
            }
        }

    }

    private void resetShakeDetection() {
        startTime = 0;
        moveCount = 0;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void setCurrentAcceleration(SensorEvent event) {
        final float alpha = 0.8f;

        // Gravity components of x, y, and z acceleration
        mGravity[X] = alpha * mGravity[X] + (1 - alpha) * event.values[X];
        mGravity[Y] = alpha * mGravity[Y] + (1 - alpha) * event.values[Y];
        mGravity[Z] = alpha * mGravity[Z] + (1 - alpha) * event.values[Z];

        // Linear acceleration along the x, y, and z axes (gravity effects removed)
        mLinearAcceleration[X] = event.values[X] - mGravity[X];
        mLinearAcceleration[Y] = event.values[Y] - mGravity[Y];
        mLinearAcceleration[Z] = event.values[Z] - mGravity[Z];
    }

    public float getMaxCurrentLinearAcceleration() {
        float maxLinearAcceleration = mLinearAcceleration[X];

        if(mLinearAcceleration[Y] > maxLinearAcceleration) {
            maxLinearAcceleration = mLinearAcceleration[Y];
        }

        if (mLinearAcceleration[Z] > maxLinearAcceleration) {
            maxLinearAcceleration = mLinearAcceleration[Z];
        }

        return maxLinearAcceleration;
    }

    public interface OnShakeListener {
        public void onShake();
    }
}
