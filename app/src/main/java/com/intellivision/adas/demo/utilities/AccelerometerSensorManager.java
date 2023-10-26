package com.intellivision.adas.demo.utilities;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.intellivision.adas.demo.MainApplication;
import com.intellivision.adas.demo.notification.AppEvents;
import com.intellivision.adas.demo.notification.AppNotifiers;
import com.sct.eventnotification.EventNotifier;
import com.sct.eventnotification.NotifierFactory;

/**
 * TThis is a singleton class and is responsible for implementing Android's SensorEventListener to
 * capture accelerometer sensor related events. Once the change is detected, notifies events to the
 * application. In order to avoid frequent changes, it maintains a last event notified timeStamp. If
 * the difference is < 1sec, it does not notify event to application.
 * 
 */
public class AccelerometerSensorManager implements SensorEventListener {
    private static AccelerometerSensorManager _instance;

    private SensorManager _sensorManager;
    private Sensor _accelerometerSensor;
    private boolean _initialized = false;
    private float _lastX = 0;
    private float _lastY = 0;
    private float _lastZ = 0;

    private float ACCELEROMETER_THRESHOLD_X = 0.25f;
    private float ACCELEROMETER_THRESHOLD_Y = 0.25f;
    private float ACCELEROMETER_THRESHOLD_Z = 0.35f;

    private long _lastEventNotifiedTimestamp;

    private final long NOTIFY_EVENT_TIMEOUT = 1000;

    /**
     * Private Constructor
     */
    private AccelerometerSensorManager() {
        _lastEventNotifiedTimestamp = System.currentTimeMillis( );
    }

    /**
     * Method to get the initialized object of type {@link AccelerometerSensorManager}
     * 
     * @return the initialized object of type {@link AccelerometerSensorManager}
     */
    public static AccelerometerSensorManager getInstance() {
        if ( _instance == null ) {
            _instance = new AccelerometerSensorManager( );
        }
        return _instance;
    }

    /**
     * Method to start receiving accelerometer sensor changed events
     */
    public void start() {
       // VCLog.debug( Category.CAT_OTHER, "AccelerometerSensorManager: start" );
        _sensorManager = (SensorManager) MainApplication.appContext.getSystemService( Context.SENSOR_SERVICE );
        _accelerometerSensor = _sensorManager.getDefaultSensor( Sensor.TYPE_ACCELEROMETER );
        _sensorManager.registerListener( this, _accelerometerSensor, SensorManager.SENSOR_DELAY_UI );
    }

    /**
     * Method to stop receiving accelerometer sensor changed events
     */
    public void stop() {
     //   VCLog.debug( Category.CAT_OTHER, "AccelerometerSensorManager: stop" );
        _sensorManager.unregisterListener( AccelerometerSensorManager.this, _accelerometerSensor );
    }

    /**
     * This method is mainly used for autofocus to happen when the user moves the device.
     */
    @Override
    public void onSensorChanged( SensorEvent event ) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        if ( !_initialized ) {
            _initialized = true;
            _lastX = x;
            _lastY = y;
            _lastZ = z;
        }
        float deltaX = Math.abs( _lastX - x );
        float deltaY = Math.abs( _lastY - y );
        float deltaZ = Math.abs( _lastZ - z );
        _lastX = x;
        _lastY = y;
        _lastZ = z;

        boolean notifyEvent = false;
        if ( deltaX > ACCELEROMETER_THRESHOLD_X ) {
            /**
             * Notify sensor changed event to application
             */
            notifyEvent = true;

        } else if ( deltaY > ACCELEROMETER_THRESHOLD_Y ) {
            /**
             * Notify sensor changed event to application
             */
            notifyEvent = true;
        } else if ( deltaZ > ACCELEROMETER_THRESHOLD_Z ) {
            /**
             * Notify sensor changed event to application
             */
            notifyEvent = true;
        }

        long currentTimestamp = System.currentTimeMillis( );
        if ( notifyEvent && currentTimestamp - _lastEventNotifiedTimestamp > NOTIFY_EVENT_TIMEOUT ) {
            /**
             * Notify events to application only if the difference is > 1 sec
             */
         //   VCLog.debug( Category.CAT_CONTROLLER, "AccelerometerSensorManager: onSensorChanged: notifying event" );
            _lastEventNotifiedTimestamp = currentTimestamp;
            EventNotifier notifier = NotifierFactory.getInstance( ).getNotifier( AppNotifiers.ACCELEROMETER_SENSOR_EVENT_NOTIFIER );
            notifier.eventNotify( AppEvents.ACCELEROMETER_SENSOR_CHANGED, 1 );
        }
    }

    @Override
    public void onAccuracyChanged( Sensor sensor, int accuracy ) {
     //   VCLog.debug( Category.CAT_CONTROLLER, "AccelerometerSensorManager: onAccuracyChanged: accuracy->" + accuracy );
    }
}
