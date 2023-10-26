package com.intellivision.adas.demo.camera;

import java.util.Arrays;

import android.util.Log;
import android.view.Surface;
import android.util.Base64;
import android.text.TextUtils;
import android.widget.TextView;

import com.intellivision.adas.IVAdasEnumConstants;
import com.intellivision.adas.IVAdasErrorCode;
import com.intellivision.adas.IvAdasWrapper;
import com.intellivision.adas.datamodels.IVAdasPcwOutput;
import com.intellivision.adas.datamodels.IVAdasPcwOutputEvent;
import com.intellivision.adas.datamodels.IVAdasSpeedLimitOutput;
import com.intellivision.adas.datamodels.IVAdasSpeedLimitOutputEvent;
import com.intellivision.adas.demo.R;
import com.intellivision.adas.datamodels.IVAdasAuxData;
import com.intellivision.adas.datamodels.IVAdasDashboardInfo;
import com.intellivision.adas.datamodels.IVAdasTailgatingOutput;
import com.intellivision.adas.datamodels.IVAdasTailgatingOutputEvent;
import com.intellivision.adas.datamodels.IVAdasFcw2Output;
import com.intellivision.adas.datamodels.IVAdasFcw2OutputEvent;
import com.intellivision.adas.datamodels.IVAdasTrafficLightsOutput;
import com.intellivision.adas.datamodels.IVAdasTrafficLightsOutputEvent;
import com.intellivision.adas.datamodels.IVAdasRollingStopOutput;
import com.intellivision.adas.datamodels.IVAdasRollingStopOutputEvent;
import com.intellivision.adas.demo.datamodels.Settings;
import com.intellivision.adas.datamodels.IVAdasFcwCertOutput;
import com.intellivision.adas.datamodels.IVAdasFcwOutput;
import com.intellivision.adas.datamodels.IVAdasFcwOutputEvent;
import com.intellivision.adas.datamodels.IVAdasGpsInfo;
import com.intellivision.adas.datamodels.IVAdasLdwOutput;
import com.intellivision.adas.datamodels.IVAdasLdwOutputEvent;
import com.intellivision.adas.datamodels.IVAdasSceneStatus;
import com.intellivision.adas.datamodels.IVAdasSnGOutputEvent;
import com.intellivision.adas.datamodels.IVAdasVideoData;
import com.intellivision.adas.demo.notification.AppEvents;
import com.intellivision.adas.demo.notification.AppNotifiers;
import com.intellivision.adas.demo.ui.ScrMain;
import com.sct.eventnotification.EventNotifier;
import com.sct.eventnotification.EventState;
import com.sct.eventnotification.IEventListener;
import com.sct.eventnotification.ListenerPriority;
import com.sct.eventnotification.NotifierFactory;

import java.util.Locale;

/**
 * This is an abstract class grouping the common camera related functionality
 * for different android version. Therefore the class which extends this is
 * responsible for providing the definitions of all abstract methods using
 * Android's Camera APIs.
 *
 * @author Sayyad.abid
 */
public abstract class CameraControllerBase implements IEventListener {

    public ScrMain activity = null;

    public abstract void setActivity(ScrMain act);

    public abstract void setRecorderSurface(Surface surface);

    public abstract boolean findCamera(boolean frontCamera, int width, int height);

    public abstract void openCamera(Surface previewSurface);

    /**
     * Method to close the camera. Must be called when the camera is no more in
     * use.
     */
    public abstract void closeCamera();

    public abstract void startPreview();

    public abstract void startRecording();

    /**
     * Method to set the display orientation.
     */
    public abstract void setDisplayOrientation();

    /**
     * Method to start the autofocus cycle
     */
    public abstract void autofocus();

    /**
     * boolean flag indicating whether calibration detected and valid
     */
    protected boolean calibrationDetected = false;

    /*
     * long indicating timestamp getting previous value of internal state
     */
    protected long prevGetInternalStateTime = 0;

    /**
     * long indicating short time interval between checking whether calibration valid
     * It needs for immediately save internal state when we get valid calibration firstly
     */
    protected static final long GET_INTERNAL_STATE_TIMEOUT_SHORT = 1*1000; //1 second

    /**
     * long indicating long time interval between getting adas engine internal state. Using when
     * calibration detected
     */
    protected static final long GET_INTERNAL_STATE_TIMEOUT_LONG = 30 * 1000; //30 seconds

    /**
     * long indicating current time interval between
     * It's equal GET_INTERNAL_STATE_TIMEOUT_SHORT when calibration is not valid
     * otherwise GET_INTERNAL_STATE_TIMEOUT_LONG
    */
    protected static long state_time_out = GET_INTERNAL_STATE_TIMEOUT_SHORT;

    /**
     * float indicating got bearing from gps receiver.
     * Must be from 0 to 360. 0 is true north.
     */
    protected static float gpsBearing = 0;

    /**
     * Boolean indicating whether autofocus is in progress
     */
    protected boolean autofocusing = false;

    /**
     * Boolean indicating whether the camera is focused
     */
    protected boolean isFocused = false;

    /**
     * The last focused timestamp. Used to avoid very frequent autofocus
     * attempts
     */
    protected long lastFocusedTimestamp;

    protected final long CANCEL_AUTOFOCUS_TIMEOUT = 3 * 1000;

    /**
     * This buffer is used while adding object in training mode to hold preview
     * image data.
     */
    protected byte[] previewBuffer;

    protected static final String TAG = "CameraControllerBase";

//    private Timer _cancelAutofocusTimer;
//    private CancelAutoFocusTask _cancelAutofocusTask;
    private static final int UPDATE_PERIOD = 30;
    private int numProcTime = 0;
    private long sumProcTimeMs = 0;

    public static boolean stopPrecessing = false;

//    /**
//     * This class extends {@link TimerTask} and is used to cancel the pending
//     * autofocus cycle.
//     *
//     * @author Sayyad.abid
//     */
//    private class CancelAutoFocusTask extends TimerTask {
//        @Override
//        public void run() {
//            autofocusing = false;
//        }
//    }
//
//    /**
//     * This method is used to schedule a timer to cancel pending autofocus
//     * cycle.
//     */
//    protected void startCancelAutofocusTimer() {
//        cancelAutofocusTimer();
//        try {
//            _cancelAutofocusTimer = new Timer();
//            _cancelAutofocusTask = new CancelAutoFocusTask();
//            _cancelAutofocusTimer.schedule(_cancelAutofocusTask,
//                    CANCEL_AUTOFOCUS_TIMEOUT);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * This method is used to cancel the previously set timer.
//     */
//    protected void cancelAutofocusTimer() {
//        if (_cancelAutofocusTimer != null) {
//            try {
//                _cancelAutofocusTimer.cancel();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            _cancelAutofocusTimer = null;
//        }
//
//        if (_cancelAutofocusTask != null) {
//            try {
//                _cancelAutofocusTask.cancel();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            _cancelAutofocusTask = null;
//        }
//    }

    @Override
    public void registerListener() {
        EventNotifier notifier = NotifierFactory.getInstance().getNotifier(
                AppNotifiers.ACCELEROMETER_SENSOR_EVENT_NOTIFIER);
        notifier.registerListener(CameraControllerBase.this,
                ListenerPriority.PRIORITY_HIGH);
    }

    @Override
    public void unregisterListener() {
        EventNotifier notifier = NotifierFactory.getInstance().getNotifier(
                AppNotifiers.ACCELEROMETER_SENSOR_EVENT_NOTIFIER);
        notifier.unRegisterListener(CameraControllerBase.this);
    }

    public void processPreviewFrame(IVAdasVideoData videoData) {
        if (autofocusing) {
            /**
             * Do not processed unfocused frames. Autofocusing flag is only
             * applicable for devices running Android version < 5.0
             */
            return;
        }
        try {
            // Information from board system is not available.
            // Using -1 for left and right turn signal as unknown.
            IVAdasDashboardInfo dashInfo = new IVAdasDashboardInfo(-1, -1);

            // GPS info.
            float gpsSpeed = 50.0f;  // Set current speed in km/h from GPS.
            float gpsBearing = 0.0f;  // Set current bearing (0..360) from GPS. Set to -1 if bearing is unknown.

            int curSpeed = Settings.getInt(Settings.I_CUR_SPEED);
            if (curSpeed == Settings.CUR_SPEED_GPS) {
                gpsSpeed = activity.speed;
                gpsBearing = activity.bearing;
            } else if (curSpeed == Settings.CUR_SPEED_0) {
                gpsSpeed = 0;
                gpsBearing = 0;
            } else if (curSpeed == Settings.CUR_SPEED_FROM_SETTINGS) {
                gpsSpeed = Settings.getFloat(Settings.F_SPEED);
                gpsBearing = 0;
            }

            IVAdasGpsInfo gpsInfo = new IVAdasGpsInfo(gpsSpeed, gpsBearing);

            // Auxiliary data for ADAS engine.
            IVAdasAuxData auxData = new IVAdasAuxData(dashInfo, gpsInfo);

            if (auxData != null && auxData.getGpsInfo() != null) {
                String speedBearing = String.format(Locale.getDefault(), "%1$.1f | %2$.1f %3$s",
                        auxData.getGpsInfo().getSpeed(),
                        auxData.getGpsInfo().getBearing(),
                        activity.gpsActive ? "A" : "");
                TextView tv_gps = (TextView)activity.findViewById(R.id.tv_gps);
                tv_gps.setText(speedBearing);
            }

            long procStart = System.currentTimeMillis();

            // Before calling of processFrame() the engine must be initialized and
            // valid license key must be set.
            int returnVal = IvAdasWrapper.getInstance().processFrame(videoData, auxData);
            if (returnVal != IVAdasErrorCode.IV_ADAS_OK) {
                Log.e(TAG, "processFrame() error code " + returnVal);
                activity.showErrorDialog(returnVal);
                return;
            }
            long procEnd = System.currentTimeMillis();
            sumProcTimeMs += procEnd - procStart;
            numProcTime++;

            // Create output objects.
            IVAdasLdwOutputEvent ldwEvent = new IVAdasLdwOutputEvent();
            IVAdasFcwOutputEvent fcwEvent = new IVAdasFcwOutputEvent();
            IVAdasSnGOutputEvent sngEvent = new IVAdasSnGOutputEvent();
            IVAdasTailgatingOutputEvent tgEvent = new IVAdasTailgatingOutputEvent();
            IVAdasFcw2OutputEvent fcw2Event = new IVAdasFcw2OutputEvent();
            IVAdasLdwOutput ldwOutput = new IVAdasLdwOutput();
            IVAdasFcwOutput fcwOutput = new IVAdasFcwOutput();
            IVAdasFcwCertOutput fcwCertOutput = new IVAdasFcwCertOutput();
            IVAdasTailgatingOutput tgOutput = new IVAdasTailgatingOutput();
            IVAdasFcw2Output fcw2Output = new IVAdasFcw2Output();
            IVAdasTrafficLightsOutput trafficLightsOutput = new IVAdasTrafficLightsOutput();
            IVAdasTrafficLightsOutputEvent trafficLightsEvent = new IVAdasTrafficLightsOutputEvent();
            IVAdasRollingStopOutput rollingStopOutput = new IVAdasRollingStopOutput();
            IVAdasRollingStopOutputEvent rollingStopEvent = new IVAdasRollingStopOutputEvent();
            IVAdasSpeedLimitOutput speedLimitOutput = new IVAdasSpeedLimitOutput();
            IVAdasSpeedLimitOutputEvent speedLimitEvent = new IVAdasSpeedLimitOutputEvent();
            IVAdasPcwOutput pcwOutput = new IVAdasPcwOutput();
            IVAdasPcwOutputEvent pcwEvent = new IVAdasPcwOutputEvent();
            IVAdasSceneStatus sceneStatus = new IVAdasSceneStatus();

            // Get events.
            returnVal = IvAdasWrapper.getInstance().getLdwOutputEvent(ldwEvent);
            if (returnVal != IVAdasErrorCode.IV_ADAS_OK) {
                Log.e(TAG, "getLdwOutputEvent() error code " + returnVal);
                if (returnVal != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                    activity.showErrorDialog(returnVal);
                    return;
                }
            }
            if (Settings.getBoolean(Settings.B_SOLID_ONLY)) {
                // Reset LDW event if Solid Only and event is triggered by non-solid line.
                if (ldwEvent != null && ldwEvent.getIsPresent() != 0 &&
                    ldwEvent.getLaneMarkLineSolidity() != IVAdasEnumConstants.IVLineSolidity.IVLSSOLID) {
                    ldwEvent.setIsPresent(0);
                }
            }
            returnVal = IvAdasWrapper.getInstance().getFcwOutputEvent(fcwEvent);
            if (returnVal != IVAdasErrorCode.IV_ADAS_OK) {
                Log.e(TAG, "getFcwOutputEvent() error code " + returnVal);
                if (returnVal != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                    activity.showErrorDialog(returnVal);
                    return;
                }
            }
            returnVal = IvAdasWrapper.getInstance().getSnGOutputEvent(sngEvent);
            if (returnVal != IVAdasErrorCode.IV_ADAS_OK) {
                Log.e(TAG, "getSnGOutputEvent() error code " + returnVal);
                if (returnVal != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                    activity.showErrorDialog(returnVal);
                    return;
                }
            }
            returnVal = IvAdasWrapper.getInstance().getTailgatingOutputEvent(tgEvent);
            if (returnVal != IVAdasErrorCode.IV_ADAS_OK) {
                Log.e(TAG, "getTailgatingOutputEvent() error code " + returnVal);
                if (returnVal != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                    activity.showErrorDialog(returnVal);
                    return;
                }
            }
            returnVal = IvAdasWrapper.getInstance().getFcw2OutputEvent(fcw2Event);
            if (returnVal != IVAdasErrorCode.IV_ADAS_OK) {
                Log.e(TAG, "getFcw2OutputEvent() error code " + returnVal);
                if (returnVal != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                    activity.showErrorDialog(returnVal);
                    return;
                }
            }
            returnVal = IvAdasWrapper.getInstance().getTrafficLightsOutputEvent(trafficLightsEvent);
            if (returnVal != IVAdasErrorCode.IV_ADAS_OK) {
                Log.e(TAG, "getTrafficLightsOutputEvent() error code " + returnVal);
                if (returnVal != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                    activity.showErrorDialog(returnVal);
                    return;
                }
            }
            returnVal = IvAdasWrapper.getInstance().getRollingStopOutputEvent(rollingStopEvent);
            if (returnVal != IVAdasErrorCode.IV_ADAS_OK) {
                Log.e(TAG, "getRollingStopOutputEvent() error code " + returnVal);
                if (returnVal != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                    activity.showErrorDialog(returnVal);
                    return;
                }
            }
            returnVal = IvAdasWrapper.getInstance().getSpeedLimitOutputEvent(speedLimitEvent);
            if (returnVal != IVAdasErrorCode.IV_ADAS_OK) {
                Log.e(TAG, "getSpeedLimitOutputEvent() error code " + returnVal);
                if (returnVal != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                    activity.showErrorDialog(returnVal);
                    return;
                }
            }
            returnVal = IvAdasWrapper.getInstance().getPcwOutputEvent(pcwEvent);
            if (returnVal != IVAdasErrorCode.IV_ADAS_OK) {
                Log.e(TAG, "getPcwOutputEvent() error code " + returnVal);
                if (returnVal != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                    activity.showErrorDialog(returnVal);
                    return;
                }
            }

            // If an event is detected on current frame then ldw/fcw/sngEvent.getIsPresent() != 0.
            // Otherwise event fields contain invalid values.

            // Get scene status which contains calibration state.
            returnVal = IvAdasWrapper.getInstance().getSceneStatus(sceneStatus);
            if (returnVal != IVAdasErrorCode.IV_ADAS_OK) {
                Log.e(TAG, "getSceneStatus() error code " + returnVal);
                activity.showErrorDialog(returnVal);
                return;
            }
            // Get LDW output which contains detected lines.
            returnVal = IvAdasWrapper.getInstance().getLdwOutput(ldwOutput);
            if (returnVal != IVAdasErrorCode.IV_ADAS_OK) {
                Log.e(TAG, "getLdwOutput() error code " + returnVal);
                if (returnVal != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                    activity.showErrorDialog(returnVal);
                    return;
                }
            }
            // Get FCW output which contains information about detected vehicle.
            returnVal = IvAdasWrapper.getInstance().getFcwOutput(fcwOutput, fcwCertOutput);
            if (returnVal != IVAdasErrorCode.IV_ADAS_OK) {
                Log.e(TAG, "getFcwOutput() error code " + returnVal);
                if (returnVal != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                    activity.showErrorDialog(returnVal);
                    return;
                }
            }
            // Get Tailgating output.
            returnVal = IvAdasWrapper.getInstance().getTailgatingOutput(tgOutput);
            if (returnVal != IVAdasErrorCode.IV_ADAS_OK) {
                Log.e(TAG, "getTailgatingOutput() error code " + returnVal);
                if (returnVal != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                    activity.showErrorDialog(returnVal);
                    return;
                }
            }
            // Get FCW2 output.
            returnVal = IvAdasWrapper.getInstance().getFcw2Output(fcw2Output);
            if (returnVal != IVAdasErrorCode.IV_ADAS_OK) {
                Log.e(TAG, "getFcw2Output() error code " + returnVal);
                if (returnVal != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                    activity.showErrorDialog(returnVal);
                    return;
                }
            }
            // Get TrafficLights output.
            returnVal = IvAdasWrapper.getInstance().getTrafficLightsOutput(trafficLightsOutput);
            if (returnVal != IVAdasErrorCode.IV_ADAS_OK) {
                Log.e(TAG, "getTrafficLightsOutput() error code " + returnVal);
                if (returnVal != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                    activity.showErrorDialog(returnVal);
                    return;
                }
            }
            // Get Rolling Stop output.
            returnVal = IvAdasWrapper.getInstance().getRollingStopOutput(rollingStopOutput);
            if (returnVal != IVAdasErrorCode.IV_ADAS_OK) {
                Log.e(TAG, "getRollingStopOutput() error code " + returnVal);
                if (returnVal != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                    activity.showErrorDialog(returnVal);
                    return;
                }
            }
            // Get Speed Limit output.
            returnVal = IvAdasWrapper.getInstance().getSpeedLimitOutput(speedLimitOutput);
            if (returnVal != IVAdasErrorCode.IV_ADAS_OK) {
                Log.e(TAG, "getSpeedLimitOutput() error code " + returnVal);
                if (returnVal != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                    activity.showErrorDialog(returnVal);
                    return;
                }
            }
            // Get PCW output.
            returnVal = IvAdasWrapper.getInstance().getPcwOutput(pcwOutput);
            if (returnVal != IVAdasErrorCode.IV_ADAS_OK) {
                Log.e(TAG, "getPcwOutput() error code " + returnVal);
                if (returnVal != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                    activity.showErrorDialog(returnVal);
                    return;
                }
            }

            activity.drawOutput(ldwOutput, fcwOutput, fcwCertOutput, tgOutput, fcw2Output,
                    trafficLightsOutput, rollingStopOutput, speedLimitOutput, pcwOutput,
                    ldwEvent, fcwEvent, sngEvent, tgEvent, fcw2Event, trafficLightsEvent,
                    rollingStopEvent, speedLimitEvent, pcwEvent,
                    sceneStatus);

            boolean autoCalibration = Settings.getBoolean(Settings.B_AUTOCALIBRATION);
            boolean autoDeviation = Settings.getBoolean(Settings.B_DEVIATION) && Settings.getBoolean(Settings.B_AUTODEVIATION);
            activity.showCalibration(autoCalibration, sceneStatus, autoDeviation, ldwOutput);

            if (numProcTime >= UPDATE_PERIOD) {
                float avgProcTimeMs = ((float)sumProcTimeMs) / numProcTime;
                float fps = 0;
                if (avgProcTimeMs > 0) {
                    fps = 1000.f / avgProcTimeMs;
                }
                activity.showProcFps(fps);
                sumProcTimeMs = 0;
                numProcTime = 0;
            }

            long currTime = System.currentTimeMillis();
            if ((currTime -  prevGetInternalStateTime) > state_time_out)
            {
                Log.d(TAG, "Timeout interval elapsed");
                if (sceneStatus.getIsCalibrationDetected() == 1) {
                    //Valid calibration

                    if (!calibrationDetected) {
                        calibrationDetected = true;
                        //set new timeout for observing of internal state
                        state_time_out = GET_INTERNAL_STATE_TIMEOUT_LONG;
                        Log.d(TAG, "Autocalibration detected");
                    }

                    byte[] currentState = IvAdasWrapper.getInstance().getInternalState();

                    //load from SD card any saved internal state
                    String loadedStateString = Settings.loadInternalState();
                    if (TextUtils.isEmpty(loadedStateString)) {
                        //Loaded internal state string is empty.
                        Log.d(TAG, "Loaded internal state is empty. Saving current state");
                        String currentStateString = Base64.encodeToString(currentState, Base64.DEFAULT);
                        Settings.saveInternalState(currentStateString);
                    }
                    else {
                        //Loaded from SD card internal state string is NOT empty. Convert it to binary and
                        // compare it with current internal state
                        byte[] loadedState = Base64.decode(loadedStateString.getBytes(), Base64.DEFAULT);
                        if (Arrays.equals(currentState, loadedState)) {
                            //doing nothing
                            Log.d(TAG, "Current state and loaded state are equal");
                        }
                        else {
                            //Save current state
                            String currentStateString = Base64.encodeToString(currentState, Base64.DEFAULT);
                            Settings.saveInternalState(currentStateString);

                            Log.d(TAG, "Current state and loaded state are NOT equal");
                            Log.d(TAG, "Current state string:" + currentStateString + " loaded:" + loadedStateString);
                        }
                    }
                }
                else {
                    //autocalibration not valid. Show autocalibration progress
                    Log.d(TAG, "====Autocalibration " + String.valueOf(sceneStatus.getAutocalibPercentDone()) + "% done");
                }
                prevGetInternalStateTime = currTime;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int eventNotify(int eventTyepe, Object eventObject) {
        int eventState = EventState.EVENT_IGNORED;
        switch (eventTyepe) {
            case AppEvents.ACCELEROMETER_SENSOR_CHANGED:
                if (!autofocusing) {
                    autofocus();
                }
                eventState = EventState.EVENT_CONSUMED;
                break;
        }
        return eventState;
    }

}
