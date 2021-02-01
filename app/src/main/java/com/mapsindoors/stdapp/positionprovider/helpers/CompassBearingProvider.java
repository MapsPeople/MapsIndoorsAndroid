package com.mapsindoors.stdapp.positionprovider.helpers;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.Surface;
import android.view.WindowManager;

import com.mapsindoors.stdapp.listeners.OnBearingChangedListener;

import static android.content.Context.SENSOR_SERVICE;

public class CompassBearingProvider implements SensorEventListener {
    static final String TAG = CompassBearingProvider.class.getSimpleName();

    static final boolean USE_NEW_IMPLEMENTATION = false;

    static final int SENSOR_ACCELEROMETER_SAMPLING_RATE = USE_NEW_IMPLEMENTATION ? SensorManager.SENSOR_DELAY_UI : SensorManager.SENSOR_DELAY_NORMAL;
    static final int SENSOR_MAGNETIC_FIELD_SAMPLING_RATE = USE_NEW_IMPLEMENTATION ? SensorManager.SENSOR_DELAY_UI : SensorManager.SENSOR_DELAY_NORMAL;

    private Context mContext;
    private SensorManager mSensorManager;
    private final Sensor mAccelerometer, mMagnetSensor;
    private final float[] mLastAccelerometer, mLastMagnetSensor, mR, mRAdjusted, mOrientation;
    private boolean mLastAccelerometerSet, mLastMagnetSensorSet;

    @Nullable
    private OnBearingChangedListener mOnBearingChangedListener;


    public CompassBearingProvider(@NonNull Context context) {
        mContext = context;

        mLastAccelerometer = new float[3];
        mLastMagnetSensor = new float[3];
        mOrientation = new float[3];
        mR = new float[9];
        mRAdjusted = new float[9];
        mLastAccelerometerSet = mLastMagnetSensorSet = false;

        mSensorManager = (SensorManager) mContext.getSystemService(SENSOR_SERVICE);

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    public void setBearingChangedListener(@Nullable OnBearingChangedListener onBearingChangedListener) {
        mOnBearingChangedListener = onBearingChangedListener;
    }

    public void start() {
        if (mSensorManager != null) {
            if (mAccelerometer != null) {
                mSensorManager.registerListener(this, mAccelerometer, SENSOR_ACCELEROMETER_SAMPLING_RATE);
            }
            if (mMagnetSensor != null) {
                mSensorManager.registerListener(this, mMagnetSensor, SENSOR_MAGNETIC_FIELD_SAMPLING_RATE);
            }
        }
    }

    public void stop() {
        if (mSensorManager != null) {
            if (mAccelerometer != null) {
                mSensorManager.unregisterListener(this, mAccelerometer);
            }

            if (mMagnetSensor != null) {
                mSensorManager.unregisterListener(this, mMagnetSensor);
            }
        }
    }


    //region IMPLEMENTS SensorEventListener

    @Override
    public void onSensorChanged(@Nullable SensorEvent event) {
        if (USE_NEW_IMPLEMENTATION) {
            onSensorChangedNewImpl(event);
        } else {
            onSensorChangedOldImpl(event);
        }
    }

    void onSensorChangedOldImpl(@Nullable SensorEvent event) {
        if (event == null) {
            return;
        }

        float[] smoothed;

        if (event.sensor == mAccelerometer) {
            smoothed = LowPassFilter.filter(event.values, mLastAccelerometer);
            mLastAccelerometer[0] = smoothed[0];
            mLastAccelerometer[1] = smoothed[1];
            mLastAccelerometer[2] = smoothed[2];

            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetSensor) {

            smoothed = LowPassFilter.filter(event.values, mLastMagnetSensor);
            mLastMagnetSensor[0] = smoothed[0];
            mLastMagnetSensor[1] = smoothed[1];
            mLastMagnetSensor[2] = smoothed[2];

            mLastMagnetSensorSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetSensorSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetSensor);
            SensorManager.getOrientation(mR, mOrientation);
            float azimuthInRadians = mOrientation[0];
            float azimuthInDegrees = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;

            if (mOnBearingChangedListener != null) {
                mOnBearingChangedListener.onBearingChanged(azimuthInDegrees);
            }
        }
    }

    static final boolean APPLY_LOW_PASS_FILTER = false;

    void onSensorChangedNewImpl(@Nullable SensorEvent event) {
        if (event == null) {
            return;
        }

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER: {
                final float[] sensorValues;

                if (APPLY_LOW_PASS_FILTER) {
                    // Filtered
                    sensorValues = LowPassFilter.filter(event.values, mLastAccelerometer);
                } else {
                    // Non-filtered
                    sensorValues = event.values;
                }

                System.arraycopy(sensorValues,
                        0, mLastAccelerometer,
                        0, mLastAccelerometer.length);

                mLastAccelerometerSet = true;
                break;
            }
            case Sensor.TYPE_MAGNETIC_FIELD: {
                final float[] sensorValues;

                if (APPLY_LOW_PASS_FILTER) {
                    // Filtered
                    sensorValues = LowPassFilter.filter(event.values, mLastMagnetSensor);
                } else {
                    // Non-filtered
                    sensorValues = event.values;
                }

                System.arraycopy(sensorValues,
                        0, mLastMagnetSensor,
                        0, mLastMagnetSensor.length);

                mLastMagnetSensorSet = true;
                break;
            }
        }

        if (mLastAccelerometerSet && mLastMagnetSensorSet) {
            final boolean resultIsValid = SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetSensor);
            if (!resultIsValid) {
                return;
            }

            switch (getDisplayRotation()) {
                case Surface.ROTATION_0:
                    System.arraycopy(mR,
                            0, mRAdjusted,
                            0, mRAdjusted.length);
                    break;
                case Surface.ROTATION_90:
                    SensorManager.remapCoordinateSystem(mR, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, mRAdjusted);
                    break;
                case Surface.ROTATION_180:
                    SensorManager.remapCoordinateSystem(mR, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y, mRAdjusted);
                    break;
                case Surface.ROTATION_270:
                    SensorManager.remapCoordinateSystem(mR, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X, mRAdjusted);
                    break;
            }

            SensorManager.getOrientation(mRAdjusted, mOrientation);

            final float azimuthInRadians = mOrientation[0];
            final float azimuthInDegrees = (float) Math.toDegrees(azimuthInRadians);

            if (mOnBearingChangedListener != null) {
                mOnBearingChangedListener.onBearingChanged(azimuthInDegrees);
            }
        }
    }

    @Override
    public void onAccuracyChanged(@Nullable Sensor sensor, int accuracy) {
        // =======================================================================================
        // TODO: TO BE IMPLEMENTED
        // =======================================================================================
        //
        //if( sensor == null ) {
        //    return;
        //}
        //
        //final int sensorType = sensor.getType();
        //
        //final boolean isAccelerometer = sensorType == Sensor.TYPE_ACCELEROMETER;
        //final boolean isMagneticField = sensorType == Sensor.TYPE_MAGNETIC_FIELD;
        //
        //if( isAccelerometer || isMagneticField ) {
        //    switch( accuracy ) {
        //        case SensorManager.SENSOR_STATUS_UNRELIABLE:
        //            if( BuildConfig.DEBUG ) {
        //                dbglog.LogI( TAG, "1234567 - is Accelerometer: " + isAccelerometer + " | accuracy: SENSOR_STATUS_UNRELIABLE" );
        //            }
        //            break;
        //        case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
        //            if( BuildConfig.DEBUG ) {
        //                dbglog.LogI( TAG, "1234567 - is Accelerometer: " + isAccelerometer + " | accuracy: SENSOR_STATUS_ACCURACY_LOW" );
        //            }
        //            break;
        //        case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
        //            if( BuildConfig.DEBUG ) {
        //                dbglog.LogI( TAG, "1234567 - is Accelerometer: " + isAccelerometer + " | accuracy: SENSOR_STATUS_ACCURACY_MEDIUM" );
        //            }
        //            break;
        //        case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
        //            if( BuildConfig.DEBUG ) {
        //                dbglog.LogI( TAG, "1234567 - is Accelerometer: " + isAccelerometer + " | accuracy: SENSOR_STATUS_ACCURACY_HIGH" );
        //            }
        //            break;
        //    }
        //}
    }
    //endregion

    final int getDisplayRotation() {
        final WindowManager wm;
        if (mContext instanceof Activity) {
            wm = ((Activity) mContext).getWindowManager();
        } else {
            wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        }

        return (wm != null)
                ? wm.getDefaultDisplay().getRotation()
                : Surface.ROTATION_0;
    }


    /**
     * Call this from your activity's {@link Activity#onDestroy() onDestroy} method, etc.
     */
    public void terminate() {
        stop();
        mSensorManager = null;
        mContext = null;
    }
}
