package com.intellivision.adas.demo.ui.controller;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.intellivision.adas.IVAdasEnumConstants.IVAdasLdwType;
import com.intellivision.adas.IVAdasEnumConstants.IVAdasFrameFormat;
import com.intellivision.adas.IVAdasEnumConstants.IVCpuUsageLevel;
import com.intellivision.adas.IVAdasErrorCode;
import com.intellivision.adas.IvAdasWrapper;
import com.intellivision.adas.datamodels.IVAdasPcwParams;
import com.intellivision.adas.datamodels.IVAdasSpeedLimitParams;
import com.intellivision.adas.demo.R;
import com.intellivision.adas.datamodels.IVAdasFcw2Params;
import com.intellivision.adas.datamodels.IVAdasTailgatingParams;
import com.intellivision.adas.datamodels.IVAdasVehicleDetectorParams;
import com.intellivision.adas.datamodels.IVAdasRollingStopParams;
import com.intellivision.adas.demo.datamodels.Settings;
import com.intellivision.adas.datamodels.IVAdasFcwAdvancedParams;
import com.intellivision.adas.datamodels.IVAdasFcwParams;
import com.intellivision.adas.datamodels.IVAdasLdwAdvancedParams;
import com.intellivision.adas.datamodels.IVAdasLdwParams;
import com.intellivision.adas.datamodels.IVAdasSceneParams;
import com.intellivision.adas.datamodels.IVAdasSnGParams;
import com.intellivision.adas.demo.datamodels.IVLicenseData;
import com.intellivision.adas.demo.ui.ScrMain;
import com.intellivision.adas.demo.utilities.ObjectUtils;

import java.io.File;

public class MainController implements OnKeyListener, OnClickListener,
        OnCheckedChangeListener {
    private final ScrMain _screen;
    private static final String TAG = "MainController";

    public MainController(ScrMain screen) {
        _screen = screen;
    }

    public void initializeNativeEngine(int imgWidth, int imgHeight) {
        String license = "key";  // Fill with license key obtained from IntelliVision.

        // Get license key from engine settings.
        license = IVLicenseData.getInstance().getLicenseKey();

        // License key should be set before initializing ADAS engine.
        int returnval = IvAdasWrapper.getInstance().setLicense(license);
        if (returnval != IVAdasErrorCode.IV_ADAS_OK) {
            // License key is invalid. Contact IntelliVision for license key.
            Log.e(TAG, "setLicense() error code " + returnval);
            _screen.showErrorDialog(returnval);
            return;
        }

        // Set resolution which will be passed to processFrame().
        // Engine supports any resolution with height from 240 to 720.
        // Here it is 720p.
        int width = 1280;
        int height = 720;

        // Set original capture resolution to appropriate value.
        // Here it is 720p.
        int origWidth = 1280;  // Set to appropriate value.
        int origHeight = 720;  // Set to appropriate value.

        // Set angle of view values, corresponding to the camera lens.
        float horizontalAngleOfViewDegree = 112.0f;  // Set to appropriate value.
        float verticalAngleOfViewDegrees = 65.0f;  // Set to appropriate value.

        // Default camera mount height is 1.2m (sedan); change if higher.
        float cameraMountHeight = 1.2f;
        // Default lane width is 3.7 meters; change if different.
        float laneWidth = 3.7f;
        // Default min distance is 4.5 meters; change if needed.
        float minDistance = 4.5f;

        width = imgWidth;
        height = imgHeight;

        origWidth = Settings.getInt(Settings.I_WIDTH);
        origHeight = Settings.getInt(Settings.I_HEIGHT);

        // Get angles of view from engine settings.
        horizontalAngleOfViewDegree = Settings.getFloat(Settings.F_HORIZONTAL_FOV);
        verticalAngleOfViewDegrees = Settings.getFloat(Settings.F_VERTICAL_FOV);

        // Frame format should be IVFFNV21.
        int frameFormat = IVAdasFrameFormat.IVFFNV21;

        // ADAS needs speed and bearing info.
        int gpsAvailable = 1;

        // Initialize ADAS engine.
        returnval = IvAdasWrapper.getInstance().init(width, height,
                origWidth, origHeight,
                frameFormat,
                horizontalAngleOfViewDegree, verticalAngleOfViewDegrees,
                gpsAvailable,
                _screen);
        if (returnval != IVAdasErrorCode.IV_ADAS_OK) {
            // Initialization error.
            Log.e(TAG, "init() error code " + returnval);
            _screen.showErrorDialog(returnval);
            return;
        }

        // Check ADAS engine version.
        String version = IvAdasWrapper.getInstance().getVersionInfo();
        Log.d(TAG, "ADAS version: " + version);

        initSettingsIfVoid();

        // Scene parameters.
        // Get current scene parameters.
        IVAdasSceneParams sceneParams = new IVAdasSceneParams();
        IvAdasWrapper.getInstance().getSceneParams(sceneParams);

        cameraMountHeight = Settings.getFloat(Settings.F_CAMERA_MOUNT_HEIGHT);
        laneWidth = Settings.getFloat(Settings.F_LANE_WIDTH);
        minDistance = Settings.getFloat(Settings.F_MIN_DISTANCE);

        sceneParams.setCameraMountHeight(cameraMountHeight);
        sceneParams.setLaneWidthMeters(laneWidth);
        sceneParams.setMinDistance(minDistance);

        File sdCard = Environment.getExternalStorageDirectory( );
        File logDir = new File(sdCard.getAbsolutePath( ) + File.separator +
                _screen.getResources().getString(R.string.app_label));
        if ( !logDir.isDirectory( ) ) {
            logDir.mkdir( );
        }
        File logFileName = new File(logDir.getAbsolutePath() + File.separator + "log.txt");
        if (logFileName.length() > 1*1024*1024) {
            logFileName.delete();
        }
        sceneParams.setLogFileName(logFileName.getAbsolutePath());

        // Default calibration is automatic, enable manual if required.
        // If manual is enabled, then hoodLevel, horizonLevel, horizonPan need to be set.
        int autoCalib = 1;

        // Get auto-calibration from engine settings.
        autoCalib = Settings.getBoolean(Settings.B_AUTOCALIBRATION) ? 1 : 0;

        if (autoCalib != 0) {
            // If auto-calibration is enabled no need to set horizon and hood settings.
            sceneParams.setAutomaticCalibration(autoCalib);
            // Auto-calibration will start immediately
            sceneParams.setAutoCalibrationActive(1);
            // 1 - the fastest auto-calibration, high level of CPU load;
            // 4 - the slowest auto-calibration, low level of CPU load. (Supports 1, 2, 3, 4).
            sceneParams.setAutoCalibCpuReduction(1);
            // High cpu load.
            sceneParams.setCpuUsageLevel(IVCpuUsageLevel.IVCULHIGH);

            float autoCalibMinSpeed = Settings.getFloat(Settings.F_AUTO_CALIB_MIN_SPEED);
            sceneParams.setAutoCalibMinSpeed(autoCalibMinSpeed);
        } else {
            // Auto-calibration is disabled.
            // Set appropriate horizon and hood levels.
            float horizonLevel = 50.0f;
            float horizonPan = 50.0f;
            float hoodLevel = 90.0f;

            // Get horizon and hood levels from settings.
            horizonLevel = Settings.getFloat(Settings.F_HORIZON_LEVEL);
            horizonPan = Settings.getFloat(Settings.F_HORIZONTAL_PAN);
            hoodLevel = Settings.getFloat(Settings.F_HOOD_LEVEL);
            sceneParams.setAutomaticCalibration(0);
            sceneParams.setAutoCalibrationActive(0);
            sceneParams.setHorizonLevel(horizonLevel);
            sceneParams.setHorizontalPan(horizonPan);
            sceneParams.setHoodLevel(hoodLevel);
        }
        ObjectUtils.displayObject(sceneParams);  // Dump parameters.
        returnval = IvAdasWrapper.getInstance().setSceneParams(sceneParams);
        if (returnval != IVAdasErrorCode.IV_ADAS_OK) {
            // Scene parameters error.
            Log.e(TAG, "setSceneParams() error code " + returnval);
            _screen.showErrorDialog(returnval);
            return;
        }

        // Get, modify and set LDW parameters.
        IVAdasLdwParams ldwParams = new IVAdasLdwParams();
        returnval = IvAdasWrapper.getInstance().getLdwParams(ldwParams);
        boolean deviation = Settings.getBoolean(Settings.B_DEVIATION);
        if (returnval != IVAdasErrorCode.IV_ADAS_OK) {
            Log.e(TAG, "getLdwParams() error code " + returnval);
            if (returnval != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                _screen.showErrorDialog(returnval);
                return;
            }
        } else {
            // Modify parameters.
            // ldwParams.setMinLdwsSpeedKmph(30.0f);
            // ...
            ldwParams.setLdwType(deviation ?
                    IVAdasLdwType.IVLTDEVIATION : IVAdasLdwType.IVLTDEPARTURE);

            ObjectUtils.displayObject(ldwParams);  // Dump parameters.
            returnval = IvAdasWrapper.getInstance().setLdwParams(ldwParams);
            if (returnval != IVAdasErrorCode.IV_ADAS_OK) {
                Log.e(TAG, "setLdwParams() error code " + returnval);
                if (returnval != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                    _screen.showErrorDialog(returnval);
                    return;
                }
            }
        }

        // Get, modify and set LDW advanced parameters.
        IVAdasLdwAdvancedParams ldwAdvancedParams = new IVAdasLdwAdvancedParams();
        returnval = IvAdasWrapper.getInstance().getLdwAdvancedParams(ldwAdvancedParams);
        int autoDeviation = Settings.getBoolean(Settings.B_AUTODEVIATION) ? 1 : 0;
        if (returnval != IVAdasErrorCode.IV_ADAS_OK) {
            Log.e(TAG, "getLdwAdvancedParams() error code " + returnval);
            if (returnval != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                _screen.showErrorDialog(returnval);
                return;
            }
        } else {
            // Modify parameters.
            // ldwAdvancedParams.setMaxFrameRate(30);
            // ...
            ldwAdvancedParams.setAutocalibrateDeviation(autoDeviation);
            ldwAdvancedParams.setUseWheelsToLine(Settings.getBoolean(Settings.B_USE_WHEELS_TO_LINE) ? 1 : 0);
            ldwAdvancedParams.setDistanceFromCameraToLeftWheel(Settings.getFloat(Settings.F_DIST_TO_LEFT_WHEEL));
            ldwAdvancedParams.setDistanceFromCameraToRightWheel(Settings.getFloat(Settings.F_DIST_TO_RIGHT_WHEEL));
            ldwAdvancedParams.setWheelsToLineDistanceLeftThreshold(Settings.getFloat(Settings.F_DIST_LEFT_THRESHOLD));
            ldwAdvancedParams.setWheelsToLineDistanceRightThreshold(Settings.getFloat(Settings.F_DIST_RIGHT_THRESHOLD));
            ldwAdvancedParams.setLaneChangeDistanceThreshold(Settings.getFloat(Settings.F_LANE_CHANGE_DISTANCE_THRESHOLD));
            ldwAdvancedParams.setMinDeviationDuration(Settings.getFloat(Settings.F_MIN_DEVIATION_DURATION));
            ldwAdvancedParams.setMinDurationBetweenDeviationEvents(Settings.getFloat(Settings.F_MIN_DURATION_BETWEEN_DEVIATION_EVENTS));
            ldwAdvancedParams.setLineClassifierType(Settings.getInt(Settings.I_LINE_CLASSIFIER_TYPE));

            ObjectUtils.displayObject(ldwAdvancedParams);  // Dump parameters.
            returnval = IvAdasWrapper.getInstance().setLdwAdvancedParams(ldwAdvancedParams);
            if (returnval != IVAdasErrorCode.IV_ADAS_OK) {
                Log.e(TAG, "setLdwAdvancedParams() error code " + returnval);
                if (returnval != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                    _screen.showErrorDialog(returnval);
                    return;
                }
            }
        }

        // Get auto-calibration from engine settings.
        if (autoCalib != 0 || (deviation && autoDeviation != 0)) {
            // Load and apply internal auto-calibration state *after* setting scene parameters.
            // Load from SD card any saved internal state.
            String state = Settings.loadInternalState();
            if (!TextUtils.isEmpty(state)) {
                // Internal state string is not NULL, apply it.
                Log.d(TAG, "Loaded internal state" + state);
                byte[] internalState = Base64.decode(state.getBytes(), Base64.DEFAULT);
                IvAdasWrapper.getInstance().setInternalState(internalState, internalState.length, 0);
            }
            // If internal state was not loaded or was loaded incorrectly then auto-calibration
            // would start from 0%.
        }

        // Get, modify and set FCW parameters.
        IVAdasFcwParams fcwParams = new IVAdasFcwParams();
        returnval = IvAdasWrapper.getInstance().getFcwParams(fcwParams);
        if (returnval != IVAdasErrorCode.IV_ADAS_OK) {
            Log.e(TAG, "getFcwParams() error code " + returnval);
            if (returnval != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                _screen.showErrorDialog(returnval);
                return;
            }
        } else {
            // Modify parameters.
            // fcwParams.setIsEnabled(1);
            // fcwParams.setEnableUFcw(1);  // Urban FCW is disabled by default. Uncomment to enable it.
            // fcwParams.setMaxProcFps(10);
            // ...
            ObjectUtils.displayObject(fcwParams);  // Dump parameters.
            returnval = IvAdasWrapper.getInstance().setFcwParams(fcwParams);
            if (returnval != IVAdasErrorCode.IV_ADAS_OK) {
                Log.e(TAG, "setFcwParams() error code " + returnval);
                if (returnval != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                    _screen.showErrorDialog(returnval);
                    return;
                }
            }
        }

        // Get, modify and set FCW advanced parameters.
        IVAdasFcwAdvancedParams fcwAdvancedParams = new IVAdasFcwAdvancedParams();
        returnval = IvAdasWrapper.getInstance().getFcwAdvancedParams(fcwAdvancedParams);
        if (returnval != IVAdasErrorCode.IV_ADAS_OK) {
            Log.e(TAG, "getFcwAdvancedParams() error code " + returnval);
            if (returnval != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                _screen.showErrorDialog(returnval);
                return;
            }
        } else {
            // Modify parameters.
            // fcwAdvancedParams.setFastappAlarmFirstSafeDistTtc(0.9f);
            // ...
            ObjectUtils.displayObject(fcwAdvancedParams);  // Dump parameters.
            returnval = IvAdasWrapper.getInstance().setFcwAdvancedParams(fcwAdvancedParams);
            if (returnval != IVAdasErrorCode.IV_ADAS_OK) {
                Log.e(TAG, "setFcwAdvancedParams() error code " + returnval);
                if (returnval != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                    _screen.showErrorDialog(returnval);
                    return;
                }
            }
        }

        // Get, modify and set SnG parameters.
        IVAdasSnGParams sngParams = new IVAdasSnGParams();
        returnval = IvAdasWrapper.getInstance().getSnGParams(sngParams);
        if (returnval != IVAdasErrorCode.IV_ADAS_OK) {
            Log.e(TAG, "getSnGParams() error code " + returnval);
            if (returnval != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                _screen.showErrorDialog(returnval);
                return;
            }
        } else {
            // Modify parameters.
            //
            // SnG is disabled by default.
            // SnG needs GPS data for operation.
            // So b_gpsAvailable parameter of init() must be 1 and
            // valid GPS data must be passed to processFrame().
            //
            // sngParams.isEnabled = 1;  // Set to 1 to enable SnG.
            // ...
            sngParams.isEnabled = Settings.getBoolean(Settings.B_ENABLE_SNG) ? 1 : 0;
            sngParams.minDuration = Settings.getFloat(Settings.F_SNG_MIN_DURATION);
            sngParams.sensitivity = Settings.getInt(Settings.I_SNG_SENSITIVITY);
            ObjectUtils.displayObject(sngParams);  // Dump parameters.
            returnval = IvAdasWrapper.getInstance().setSnGParams(sngParams);
            if (returnval != IVAdasErrorCode.IV_ADAS_OK) {
                Log.e(TAG, "setSnGParams() error code " + returnval);
                if (returnval != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                    _screen.showErrorDialog(returnval);
                    return;
                }
            }
        }

        // Get, modify and set Vehicle Detector parameters.
        IVAdasVehicleDetectorParams vdParams = new IVAdasVehicleDetectorParams();
        returnval = IvAdasWrapper.getInstance().getVehicleDetectorParams(vdParams);
        if (returnval != IVAdasErrorCode.IV_ADAS_OK) {
            Log.e(TAG, "getVehicleDetectorParams() error code " + returnval);
            if (returnval != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                _screen.showErrorDialog(returnval);
                return;
            }
        } else {
            // Modify parameters.
            vdParams.vdType = Settings.getInt(Settings.I_VD_TYPE);
            vdParams.scoreThreshold = Settings.getFloat(Settings.F_VD_SCORE_THRESHOLD);
            vdParams.trafficLightsScoreThreshold = Settings.getFloat(Settings.F_VD_TL_SCORE_THRESHOLD);
            vdParams.device = Settings.getInt(Settings.I_VD_DEVICE);
            vdParams.numThreads = Settings.getInt(Settings.I_VD_NUM_THREADS);
            vdParams.cpuUsageLevel = Settings.getInt(Settings.I_VD_CPU_USAGE_LEVEL);
            ObjectUtils.displayObject(vdParams);  // Dump parameters.
            returnval = IvAdasWrapper.getInstance().setVehicleDetectorParams(vdParams);
            if (returnval != IVAdasErrorCode.IV_ADAS_OK) {
                Log.e(TAG, "setVehicleDetectorParams() error code " + returnval);
                if (returnval != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                    _screen.showErrorDialog(returnval);
                    return;
                }
            }
        }

        // Get, modify and set Tailgating parameters.
        IVAdasTailgatingParams tgParams = new IVAdasTailgatingParams();
        returnval = IvAdasWrapper.getInstance().getTailgatingParams(tgParams);
        if (returnval != IVAdasErrorCode.IV_ADAS_OK) {
            Log.e(TAG, "getTailgatingParams() error code " + returnval);
            if (returnval != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                _screen.showErrorDialog(returnval);
                return;
            }
        } else {
            // Modify parameters.
            //
            // Tailgating is enabled by default.
            tgParams.minSpeed = Settings.getFloat(Settings.F_TG_MIN_SPEED);
            tgParams.sensitivity = Settings.getInt(Settings.I_TG_SENSITIVITY);
            tgParams.nightSensitivity = Settings.getInt(Settings.I_TG_NIGHT_SENSITIVITY);
            tgParams.minDuration = Settings.getFloat(Settings.F_TG_MIN_DURATION);
            tgParams.minDurationBetweenEvents = Settings.getFloat(Settings.F_TG_MIN_DURATION_BETWEEN_EVENTS);
            ObjectUtils.displayObject(tgParams);  // Dump parameters.
            returnval = IvAdasWrapper.getInstance().setTailgatingParams(tgParams);
            if (returnval != IVAdasErrorCode.IV_ADAS_OK) {
                Log.e(TAG, "setTailgatingParams() error code " + returnval);
                if (returnval != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                    _screen.showErrorDialog(returnval);
                    return;
                }
            }
        }

        // Get, modify and set FCW2 parameters.
        IVAdasFcw2Params fcw2Params = new IVAdasFcw2Params();
        returnval = IvAdasWrapper.getInstance().getFcw2Params(fcw2Params);
        if (returnval != IVAdasErrorCode.IV_ADAS_OK) {
            Log.e(TAG, "getFcw2Params() error code " + returnval);
            if (returnval != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                _screen.showErrorDialog(returnval);
                return;
            }
        } else {
            // Modify parameters.
            //
            // FCW2 is enabled by default.
            fcw2Params.minSpeed = Settings.getFloat(Settings.F_FCW2_MIN_SPEED);
            fcw2Params.eventType = Settings.getInt(Settings.I_FCW2_EVENT_TYPE);
            fcw2Params.sensitivity = Settings.getInt(Settings.I_FCW2_SENSITIVITY);
            fcw2Params.nightSensitivity = Settings.getInt(Settings.I_FCW2_NIGHT_SENSITIVITY);
            fcw2Params.minDurationBetweenEvents = Settings.getFloat(Settings.F_FCW2_MIN_DURATION_BETWEEN_EVENTS);
            fcw2Params.minDistanceDecrease = Settings.getFloat(Settings.F_FCW2_MIN_DISTANCE_DECREASE);
            fcw2Params.maxCurrentDistance = Settings.getFloat(Settings.F_FCW2_MAX_CURRENT_DISTANCE);
            ObjectUtils.displayObject(fcw2Params);  // Dump parameters.
            returnval = IvAdasWrapper.getInstance().setFcw2Params(fcw2Params);
            if (returnval != IVAdasErrorCode.IV_ADAS_OK) {
                Log.e(TAG, "setFcw2Params() error code " + returnval);
                if (returnval != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                    _screen.showErrorDialog(returnval);
                    return;
                }
            }
        }

        // Get, modify and set Rolling Stop Detector parameters.
        IVAdasRollingStopParams rsParams = new IVAdasRollingStopParams();
        returnval = IvAdasWrapper.getInstance().getRollingStopParams(rsParams);
        if (returnval != IVAdasErrorCode.IV_ADAS_OK) {
            Log.e(TAG, "getRollingStopParams() error code " + returnval);
            if (returnval != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                _screen.showErrorDialog(returnval);
                return;
            }
        } else {
            // Modify parameters.
            rsParams.isEnabled = Settings.getBoolean(Settings.B_RS_ENABLED) ? 1 : 0;
            rsParams.minSpeed = Settings.getFloat(Settings.F_RS_MIN_SPEED);
            rsParams.minDurationBetweenEvents = Settings.getFloat(Settings.F_RS_MIN_DURATION_BETWEEN_EVENTS);
            rsParams.scoreThreshold = Settings.getFloat(Settings.F_RS_SCORE_THRESHOLD);

            ObjectUtils.displayObject(rsParams);  // Dump parameters.
            returnval = IvAdasWrapper.getInstance().setRollingStopParams(rsParams);
            if (returnval != IVAdasErrorCode.IV_ADAS_OK) {
                Log.e(TAG, "setRollingStopParams() error code " + returnval);
                if (returnval != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                    _screen.showErrorDialog(returnval);
                    return;
                }
            }
        }

        // Get, modify and set Speed Limit detector parameters.
        IVAdasSpeedLimitParams slParams = new IVAdasSpeedLimitParams();
        returnval = IvAdasWrapper.getInstance().getSpeedLimitParams(slParams);
        if (returnval != IVAdasErrorCode.IV_ADAS_OK) {
            Log.e(TAG, "getSpeedLimitParams() error code " + returnval);
            if (returnval != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                _screen.showErrorDialog(returnval);
                return;
            }
        } else {
            // Modify parameters.
            slParams.isEnabled = Settings.getBoolean(Settings.B_SL_ENABLED) ? 1 : 0;
            slParams.speedLimitDuration = Settings.getFloat(Settings.F_SL_DURATION);
            slParams.minDurationBetweenEvents = Settings.getFloat(Settings.F_SL_MIN_DURATION_BETWEEN_EVENTS);
            slParams.detectionScoreThreshold = Settings.getFloat(Settings.F_SL_DET_SCORE_THRESHOLD);
            slParams.recognitionScoreThreshold = Settings.getFloat(Settings.F_SL_REC_SCORE_THRESHOLD);
            slParams.roi.x = Settings.getFloat(Settings.F_SL_ROI_X);
            slParams.roi.y = Settings.getFloat(Settings.F_SL_ROI_Y);
            slParams.roi.width = Settings.getFloat(Settings.F_SL_ROI_WIDTH);
            slParams.roi.height = Settings.getFloat(Settings.F_SL_ROI_HEIGHT);

            ObjectUtils.displayObject(slParams);  // Dump parameters.
            returnval = IvAdasWrapper.getInstance().setSpeedLimitParams(slParams);
            if (returnval != IVAdasErrorCode.IV_ADAS_OK) {
                Log.e(TAG, "setSpeedLimitParams() error code " + returnval);
                if (returnval != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                    _screen.showErrorDialog(returnval);
                    return;
                }
            }
        }

        // Get, modify and set PCW parameters.
        IVAdasPcwParams pcwParams = new IVAdasPcwParams();
        returnval = IvAdasWrapper.getInstance().getPcwParams(pcwParams);
        if (returnval != IVAdasErrorCode.IV_ADAS_OK) {
            Log.e(TAG, "getPcwParams() error code " + returnval);
            if (returnval != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                _screen.showErrorDialog(returnval);
                return;
            }
        } else {
            // Modify parameters.
            pcwParams.isEnabled = Settings.getBoolean(Settings.B_PCW_ENABLED) ? 1 : 0;
            pcwParams.minSpeed = Settings.getFloat(Settings.F_PCW_MIN_SPEED);
            pcwParams.maxSpeed = Settings.getFloat(Settings.F_PCW_MAX_SPEED);
            pcwParams.roi.x = Settings.getFloat(Settings.F_PCW_ROI_X);
            pcwParams.roi.y = Settings.getFloat(Settings.F_PCW_ROI_Y);
            pcwParams.roi.width = Settings.getFloat(Settings.F_PCW_ROI_WIDTH);
            pcwParams.roi.height = Settings.getFloat(Settings.F_PCW_ROI_HEIGHT);
            pcwParams.useEventRoi = Settings.getBoolean(Settings.B_PCW_USE_EVENT_ROI) ? 1 : 0;
            pcwParams.eventRoiLeftOffset = Settings.getFloat(Settings.F_PCW_EVENT_ROI_LEFT_OFFSET);
            pcwParams.eventRoiRightOffset = Settings.getFloat(Settings.F_PCW_EVENT_ROI_RIGHT_OFFSET);
            pcwParams.minDurationBetweenEvents = Settings.getFloat(Settings.F_PCW_MIN_DURATION_BETWEEN_EVENTS);
            pcwParams.scoreThreshold = Settings.getFloat(Settings.F_PCW_SCORE_THRESHOLD);
            pcwParams.device = Settings.getInt(Settings.I_PCW_DEVICE);
            pcwParams.maxFrameRate = Settings.getFloat(Settings.F_PCW_MAX_FRAME_RATE);

            ObjectUtils.displayObject(pcwParams);  // Dump parameters.
            returnval = IvAdasWrapper.getInstance().setPcwParams(pcwParams);
            if (returnval != IVAdasErrorCode.IV_ADAS_OK) {
                Log.e(TAG, "setPcwParams() error code " + returnval);
                if (returnval != IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                    _screen.showErrorDialog(returnval);
                    return;
                }
            }
        }

        String dir = null;
        int classMask = 0;
        int minDurationBetweenImagesMs = Settings.getInt(Settings.I_DUMP_GAP);
        if (minDurationBetweenImagesMs > 0) {
            dir = Settings.getDumpPath().toString();
            classMask = 208;
        }
        returnval = IvAdasWrapper.getInstance().setLogDir(dir, classMask, minDurationBetweenImagesMs);
        if (returnval != IVAdasErrorCode.IV_ADAS_OK) {
            Log.e(TAG, "setLogDir() error code " + returnval);
        }
    }

    private void initSettingsIfVoid() {
        // Init settings from engine if settings are void.
        IVAdasSceneParams sceneParams = new IVAdasSceneParams();
        if (IvAdasWrapper.getInstance().getSceneParams(sceneParams) == IVAdasErrorCode.IV_ADAS_OK) {
            Settings.setBooleanIfVoid(Settings.B_AUTOCALIBRATION,
                    (sceneParams.getAutomaticCalibration() != 0));
            Settings.setFloatIfVoid(Settings.F_AUTO_CALIB_MIN_SPEED, sceneParams.getAutoCalibMinSpeed());
            Settings.setFloatIfVoid(Settings.F_HOOD_LEVEL, sceneParams.getHoodLevel());
            Settings.setFloatIfVoid(Settings.F_HORIZON_LEVEL, sceneParams.getHorizonLevel());
            Settings.setFloatIfVoid(Settings.F_HORIZONTAL_PAN, sceneParams.getHorizontalPan());
            Settings.setFloatIfVoid(Settings.F_CAMERA_MOUNT_HEIGHT, sceneParams.getCameraMountHeight());
            Settings.setFloatIfVoid(Settings.F_LANE_WIDTH, sceneParams.getLaneWidthMeters());
            Settings.setFloatIfVoid(Settings.F_MIN_DISTANCE, sceneParams.getMinDistance());
        }

        IVAdasLdwParams ldwParams = new IVAdasLdwParams();
        if (IvAdasWrapper.getInstance().getLdwParams(ldwParams) == IVAdasErrorCode.IV_ADAS_OK) {
            boolean deviation = ldwParams.getLdwType() == IVAdasLdwType.IVLTDEVIATION;
            Settings.setBooleanIfVoid(Settings.B_DEVIATION, deviation);
        }

        IVAdasLdwAdvancedParams ldwAdvancedParams = new IVAdasLdwAdvancedParams();
        if (IvAdasWrapper.getInstance().getLdwAdvancedParams(ldwAdvancedParams) == IVAdasErrorCode.IV_ADAS_OK) {
            Settings.setBooleanIfVoid(Settings.B_AUTODEVIATION,
                    (ldwAdvancedParams.getAutocalibrateDeviation() != 0));
            Settings.setBooleanIfVoid(Settings.B_USE_WHEELS_TO_LINE,
                    (ldwAdvancedParams.getUseWheelsToLine() != 0));
            Settings.setFloatIfVoid(Settings.F_DIST_TO_LEFT_WHEEL,
                    ldwAdvancedParams.getDistanceFromCameraToLeftWheel());
            Settings.setFloatIfVoid(Settings.F_DIST_TO_RIGHT_WHEEL,
                    ldwAdvancedParams.getDistanceFromCameraToRightWheel());
            Settings.setFloatIfVoid(Settings.F_DIST_LEFT_THRESHOLD,
                    ldwAdvancedParams.getWheelsToLineDistanceLeftThreshold());
            Settings.setFloatIfVoid(Settings.F_DIST_RIGHT_THRESHOLD,
                    ldwAdvancedParams.getWheelsToLineDistanceRightThreshold());
            Settings.setFloatIfVoid(Settings.F_LANE_CHANGE_DISTANCE_THRESHOLD,
                    ldwAdvancedParams.getLaneChangeDistanceThreshold());
            Settings.setFloatIfVoid(Settings.F_MIN_DEVIATION_DURATION,
                    ldwAdvancedParams.getMinDeviationDuration());
            Settings.setFloatIfVoid(Settings.F_MIN_DURATION_BETWEEN_DEVIATION_EVENTS,
                    ldwAdvancedParams.getMinDurationBetweenDeviationEvents());
            Settings.setIntIfVoid(Settings.I_LINE_CLASSIFIER_TYPE,
                    ldwAdvancedParams.getLineClassifierType());
        }

        IVAdasVehicleDetectorParams vdParams = new IVAdasVehicleDetectorParams();
        if (IvAdasWrapper.getInstance().getVehicleDetectorParams(vdParams) == IVAdasErrorCode.IV_ADAS_OK) {
            Settings.setIntIfVoid(Settings.I_VD_TYPE, vdParams.vdType);
            Settings.setFloatIfVoid(Settings.F_VD_SCORE_THRESHOLD, vdParams.scoreThreshold);
            Settings.setFloatIfVoid(Settings.F_VD_TL_SCORE_THRESHOLD, vdParams.trafficLightsScoreThreshold);
            Settings.setIntIfVoid(Settings.I_VD_DEVICE, vdParams.device);
            Settings.setIntIfVoid(Settings.I_VD_NUM_THREADS, vdParams.numThreads);
            Settings.setIntIfVoid(Settings.I_VD_CPU_USAGE_LEVEL, vdParams.cpuUsageLevel);
        }

        IVAdasRollingStopParams rsParams = new IVAdasRollingStopParams();
        if (IvAdasWrapper.getInstance().getRollingStopParams(rsParams) == IVAdasErrorCode.IV_ADAS_OK) {
            Settings.setBooleanIfVoid(Settings.B_RS_ENABLED, (rsParams.isEnabled != 0));
            Settings.setFloatIfVoid(Settings.F_RS_MIN_SPEED, rsParams.minSpeed);
            Settings.setFloatIfVoid(Settings.F_RS_MIN_DURATION_BETWEEN_EVENTS, rsParams.minDurationBetweenEvents);
            Settings.setFloatIfVoid(Settings.F_RS_SCORE_THRESHOLD, rsParams.scoreThreshold);
        }

        IVAdasTailgatingParams tgParams = new IVAdasTailgatingParams();
        if (IvAdasWrapper.getInstance().getTailgatingParams(tgParams) == IVAdasErrorCode.IV_ADAS_OK) {
            Settings.setFloatIfVoid(Settings.F_TG_MIN_SPEED, tgParams.minSpeed);
            Settings.setIntIfVoid(Settings.I_TG_SENSITIVITY, tgParams.sensitivity);
            Settings.setIntIfVoid(Settings.I_TG_NIGHT_SENSITIVITY, tgParams.nightSensitivity);
            Settings.setFloatIfVoid(Settings.F_TG_MIN_DURATION, tgParams.minDuration);
            Settings.setFloatIfVoid(Settings.F_TG_MIN_DURATION_BETWEEN_EVENTS, tgParams.minDurationBetweenEvents);
        }

        IVAdasFcw2Params fcw2Params = new IVAdasFcw2Params();
        if (IvAdasWrapper.getInstance().getFcw2Params(fcw2Params) == IVAdasErrorCode.IV_ADAS_OK) {
            Settings.setFloatIfVoid(Settings.F_FCW2_MIN_SPEED, fcw2Params.minSpeed);
            Settings.setIntIfVoid(Settings.I_FCW2_EVENT_TYPE, fcw2Params.eventType);
            Settings.setIntIfVoid(Settings.I_FCW2_SENSITIVITY, fcw2Params.sensitivity);
            Settings.setIntIfVoid(Settings.I_FCW2_NIGHT_SENSITIVITY, fcw2Params.nightSensitivity);
            Settings.setFloatIfVoid(Settings.F_FCW2_MIN_DURATION_BETWEEN_EVENTS, fcw2Params.minDurationBetweenEvents);
            Settings.setFloatIfVoid(Settings.F_FCW2_MIN_DISTANCE_DECREASE, fcw2Params.minDistanceDecrease);
            Settings.setFloatIfVoid(Settings.F_FCW2_MAX_CURRENT_DISTANCE, fcw2Params.maxCurrentDistance);
        }

        IVAdasSnGParams sngParams = new IVAdasSnGParams();
        if (IvAdasWrapper.getInstance().getSnGParams(sngParams) == IVAdasErrorCode.IV_ADAS_OK) {
            Settings.setBooleanIfVoid(Settings.B_ENABLE_SNG, (sngParams.isEnabled != 0));
            Settings.setFloatIfVoid(Settings.F_SNG_MIN_DURATION, sngParams.minDuration);
            Settings.setIntIfVoid(Settings.I_SNG_SENSITIVITY, sngParams.sensitivity);
        }

        IVAdasSpeedLimitParams slParams = new IVAdasSpeedLimitParams();
        if (IvAdasWrapper.getInstance().getSpeedLimitParams(slParams) == IVAdasErrorCode.IV_ADAS_OK) {
            Settings.setBooleanIfVoid(Settings.B_SL_ENABLED, slParams.isEnabled != 0);
            Settings.setFloatIfVoid(Settings.F_SL_DURATION, slParams.speedLimitDuration);
            Settings.setFloatIfVoid(Settings.F_SL_MIN_DURATION_BETWEEN_EVENTS, slParams.minDurationBetweenEvents);
            Settings.setFloatIfVoid(Settings.F_SL_DET_SCORE_THRESHOLD, slParams.detectionScoreThreshold);
            Settings.setFloatIfVoid(Settings.F_SL_REC_SCORE_THRESHOLD, slParams.recognitionScoreThreshold);
            Settings.setFloatIfVoid(Settings.F_SL_ROI_X, slParams.roi.x);
            Settings.setFloatIfVoid(Settings.F_SL_ROI_Y, slParams.roi.y);
            Settings.setFloatIfVoid(Settings.F_SL_ROI_WIDTH, slParams.roi.width);
            Settings.setFloatIfVoid(Settings.F_SL_ROI_HEIGHT, slParams.roi.height);
        }

        IVAdasPcwParams pcwParams = new IVAdasPcwParams();
        if (IvAdasWrapper.getInstance().getPcwParams(pcwParams) == IVAdasErrorCode.IV_ADAS_OK) {
            Settings.setBooleanIfVoid(Settings.B_PCW_ENABLED, pcwParams.isEnabled != 0);
            Settings.setFloatIfVoid(Settings.F_PCW_MIN_SPEED, pcwParams.minSpeed);
            Settings.setFloatIfVoid(Settings.F_PCW_MAX_SPEED, pcwParams.maxSpeed);
            Settings.setFloatIfVoid(Settings.F_PCW_ROI_X, pcwParams.roi.x);
            Settings.setFloatIfVoid(Settings.F_PCW_ROI_Y, pcwParams.roi.y);
            Settings.setFloatIfVoid(Settings.F_PCW_ROI_WIDTH, pcwParams.roi.width);
            Settings.setFloatIfVoid(Settings.F_PCW_ROI_HEIGHT, pcwParams.roi.height);
            Settings.setBooleanIfVoid(Settings.B_PCW_USE_EVENT_ROI, pcwParams.useEventRoi != 0);
            Settings.setFloatIfVoid(Settings.F_PCW_EVENT_ROI_LEFT_OFFSET, pcwParams.eventRoiLeftOffset);
            Settings.setFloatIfVoid(Settings.F_PCW_EVENT_ROI_RIGHT_OFFSET, pcwParams.eventRoiRightOffset);
            Settings.setFloatIfVoid(Settings.F_PCW_MIN_DURATION_BETWEEN_EVENTS, pcwParams.minDurationBetweenEvents);
            Settings.setFloatIfVoid(Settings.F_PCW_SCORE_THRESHOLD, pcwParams.scoreThreshold);
            Settings.setIntIfVoid(Settings.I_PCW_DEVICE, pcwParams.device);
            Settings.setFloatIfVoid(Settings.F_PCW_MAX_FRAME_RATE, pcwParams.maxFrameRate);
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                _screen.showConfirmExitDialog();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_settings:
                _screen.startSettingScreen();
                break;
            case R.id.iv_delete_calibration:
                Settings.deleteInternalState();
                _screen.recreate();
                break;
            case R.id.bn_speed:
                int curSpeed = Settings.getInt(Settings.I_CUR_SPEED);
                curSpeed = (curSpeed + 1) % Settings.CUR_SPEED_NUM;
                Settings.setInt(Settings.I_CUR_SPEED, curSpeed);
                setSpeedButtonText();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton view, boolean isChecked) {
        switch (view.getId()) {
            case R.id.tg_record:
                boolean is_record = isChecked;
                if(is_record) {
                    view.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_stop, 0, 0);
                    _screen.startRecording();
                }
                else {
                    view.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_record, 0, 0);
                    _screen.stopRecording();
                }
                break;
        }
    }

    public void setSpeedButtonText() {
        int curSpeed = Settings.getInt(Settings.I_CUR_SPEED);
        String text = "";
        if (curSpeed == Settings.CUR_SPEED_GPS) {
            text = "GPS";
        } else if (curSpeed == Settings.CUR_SPEED_0) {
            text = "0 km/h";
        } else if (curSpeed == Settings.CUR_SPEED_FROM_SETTINGS) {
            float speed = Settings.getFloat(Settings.F_SPEED);
            text = Integer.toString((int) (speed + 0.5f)) + " km/h";
        }
        Button bnSpeed = (Button)_screen.findViewById(R.id.bn_speed);
        bnSpeed.setText(text);
    }
}
