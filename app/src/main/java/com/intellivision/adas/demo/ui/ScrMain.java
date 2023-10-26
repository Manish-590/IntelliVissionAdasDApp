package com.intellivision.adas.demo.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.intellivision.adas.IVAdasEnumConstants.IVAdasDepartureDirection;
import com.intellivision.adas.IVAdasErrorCode;
import com.intellivision.adas.IvAdasWrapper;
import com.intellivision.adas.datamodels.IVAdasPcwOutput;
import com.intellivision.adas.datamodels.IVAdasPcwOutputEvent;
import com.intellivision.adas.datamodels.IVAdasSpeedLimitOutput;
import com.intellivision.adas.datamodels.IVAdasSpeedLimitOutputEvent;
import com.intellivision.adas.demo.MainApplication;
import com.intellivision.adas.demo.R;
import com.intellivision.adas.demo.camera.CameraController;
import com.intellivision.adas.demo.camera.CameraControllerBase;
import com.intellivision.adas.demo.camera.CameraPreview;
import com.intellivision.adas.demo.camera.VideoRecorder;
import com.intellivision.adas.datamodels.IVAdasTailgatingOutput;
import com.intellivision.adas.datamodels.IVAdasTailgatingOutputEvent;
import com.intellivision.adas.datamodels.IVAdasFcw2Output;
import com.intellivision.adas.datamodels.IVAdasFcw2OutputEvent;
import com.intellivision.adas.datamodels.IVAdasTrafficLightsOutput;
import com.intellivision.adas.datamodels.IVAdasTrafficLightsOutputEvent;
import com.intellivision.adas.datamodels.IVAdasRollingStopOutput;
import com.intellivision.adas.datamodels.IVAdasRollingStopOutputEvent;
import com.intellivision.adas.demo.datamodels.Settings;
import com.intellivision.adas.datamodels.IVAdasFcwAdvancedParams;
import com.intellivision.adas.datamodels.IVAdasFcwOutput;
import com.intellivision.adas.datamodels.IVAdasFcwCertOutput;
import com.intellivision.adas.datamodels.IVAdasFcwOutputEvent;
import com.intellivision.adas.datamodels.IVAdasFcwParams;
import com.intellivision.adas.datamodels.IVAdasLdwOutput;
import com.intellivision.adas.datamodels.IVAdasLdwOutputEvent;
import com.intellivision.adas.datamodels.IVAdasSceneParams;
import com.intellivision.adas.datamodels.IVAdasSceneStatus;
import com.intellivision.adas.datamodels.IVAdasSnGOutputEvent;
import com.intellivision.adas.demo.datamodels.IVLicenseData;
import com.intellivision.adas.demo.gpsinfo.GpsInfo;
import com.intellivision.adas.demo.logger.Category;
import com.intellivision.adas.demo.logger.VCLog;
import com.intellivision.adas.demo.ui.controller.MainController;
import com.intellivision.adas.demo.utilities.AccelerometerSensorManager;
import com.intellivision.adas.demo.utilities.AudioPlayer;
import com.intellivision.adas.demo.utilities.DialogHelper;
import com.intellivision.adas.demo.utilities.ObjectUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Locale;

public class ScrMain extends Activity {

    private boolean cameraFound;
    private CameraPreview _cameraPreview;
    private CameraControllerBase _cameraController;
    private VideoRecorder _videoRecorder;
    protected MainController controller;
    private OverlayView ivOverlay;
    private GpsInfo gpsInfo;
	/**
	 *  Previous detected FCW event.
	 */
    private IVAdasFcwOutputEvent previousFCWEvent = null;

	/**
	 * Previous detected LDW event
	 */
    private IVAdasLdwOutputEvent previousLdwEvent = null;

    /**
     *  Previous detected Tailgating event.
     */
    private IVAdasTailgatingOutputEvent previousTgEvent = null;

    /**
     *  Previous detected FCW2 event.
     */
    private IVAdasFcw2OutputEvent previousFcw2Event = null;

    /**
     *  Previous detected TrafficLights event.
     */
    private IVAdasTrafficLightsOutputEvent previousTrafficLightsEvent = null;

    /**
     *  Previous detected RollingStop event.
     */
    private IVAdasRollingStopOutputEvent previousRollingStopEvent = null;

    private IVAdasSpeedLimitOutputEvent previousSpeedLimitEvent = null;

    private IVAdasPcwOutputEvent previousPcwEvent = null;

    /**
	 * Timestamp of previous detected FCW event
	 */
	private long previousEventTimeFcw = 0;

	/**
	 * Timestamp of previous detected LDW event
	 */
	private long previousEventTimeLdw = 0;

    /**
     * Timestamp of previous detected Tailgating event
     */
    private long previousEventTimeTg = 0;

    /**
     * Timestamp of previous detected FCW2 event
     */
    private long previousEventTimeFcw2 = 0;

    /**
     * Timestamp of previous detected TrafficLights event
     */
    private long previousEventTimeTrafficLights = 0;

    /**
     * Timestamp of previous detected Rolling Stop event
     */
    private long previousEventTimeRollingStop = 0;

    private long previousEventTimeSpeedLimit = 0;

    private long previousEventTimePcw = 0;

	/**
	 * Time interwal for drawing detected LDW event
	 */
	public static final long EVENT_TIME_OUT_LDW = 2000;

	/**
	 * Time interval for drawing detected FCW event
	 */
	public static final long EVENT_TIME_OUT_FCW = 2500;

    /**
     * Time interval for drawing detected Tailgating event
     */
    public static final long EVENT_TIME_OUT_TG = 2000;

    /**
     * Time interval for drawing detected FCW2 event
     */
    public static final long EVENT_TIME_OUT_FCW2 = 2000;

    /**
     * Time interval for drawing detected TrafficLights event
     */
    public static final long EVENT_TIME_OUT_TRAFFICLIGHTS = 2500;

    public static final long EVENT_TIME_OUT_ROLLING_STOP = 2000;
    public static final long EVENT_TIME_OUT_SPEED_LIMIT = 2000;
    public static final long EVENT_TIME_OUT_PCW = 2000;

    public float speed = 0;
    public float bearing = -1;
    public boolean gpsActive = false;  // G.RMC message contains active data

    private TextView tvProcFps;
    private TextView tvGpuUsage;
    private TextView tvCalibration;
    private ImageView ivDeleteCalibration;

    private MediaPlayer ldwPlayer;
    private MediaPlayer fcw2Player;
    private MediaPlayer trafficLightsPlayer;
    private MediaPlayer stopSignPlayer;
    private MediaPlayer rollingStopPlayer;
    private MediaPlayer sngPlayer;
    private MediaPlayer tgPlayer;
    private MediaPlayer pcwPlayer;

    private static int SPEED_LIMIT_UNKNOWN = 0;
    private HashMap<Integer, MediaPlayer> speedLimitPlayers = new HashMap<>();
    private HashMap<Integer, MediaPlayer> violationPlayers = new HashMap<>();
    private MediaPlayer curSpeedLimitPlayer = null;
    private MediaPlayer curViolationPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createPlayers();
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
        controller = new MainController(this);
        _initUI();
        _initCameraController();
//        boolean hasCamera = _cameraController.hasCamera();
//        if (!hasCamera) {
//            Toast.makeText(this, "Device does not support Camera",
//                    Toast.LENGTH_LONG).show();
//            return;
//        }
        gpsInfo = new GpsInfo(this);
    }

    private void _initCameraController() {
        _cameraController = new CameraController();
        _cameraController.setActivity(this);
        _cameraController.registerListener();
    }

    private void _initUI() {
        ivOverlay = (OverlayView) findViewById(R.id.iv_overlay);
        ivOverlay.setWillNotDraw(false);
        FrameLayout flCameraPreview = (FrameLayout) findViewById(R.id.camera_preview);
        flCameraPreview.setOnKeyListener(controller);
        flCameraPreview.setFocusableInTouchMode(true);
        flCameraPreview.requestFocus();
        ImageView ivSetting = (ImageView) findViewById(R.id.iv_settings);
        ivSetting.setOnClickListener(controller);

        Button bnSpeed = (Button) findViewById(R.id.bn_speed);
        bnSpeed.setOnClickListener(controller);

        controller.setSpeedButtonText();

        ToggleButton tgRecord = (ToggleButton) findViewById(R.id.tg_record);
        tgRecord.setChecked(false);
        tgRecord.setOnCheckedChangeListener(controller);

        tvProcFps = (TextView) findViewById(R.id.tv_processingFps);
        tvGpuUsage = (TextView) findViewById(R.id.tv_gpuUsage);
        tvCalibration = findViewById(R.id.tv_calibration);
        ivDeleteCalibration = findViewById(R.id.iv_delete_calibration);
        ivDeleteCalibration.setOnClickListener(controller);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Settings.getBoolean(Settings.B_REVERSE_LANDSCAPE)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        int width = Settings.getInt(Settings.I_WIDTH);
        int height = Settings.getInt(Settings.I_HEIGHT);
        cameraFound =_cameraController.findCamera(false, width, height);
        if (!cameraFound) {
            Toast.makeText(this, "Failed to find proper camera for " +
                    Integer.toString(width) + "x" + Integer.toString(height),
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Resolution: " +
                    Integer.toString(width) + "x" + Integer.toString(height),
                    Toast.LENGTH_LONG).show();
        }

        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.removeAllViews();

        if (cameraFound) {
            _videoRecorder = new VideoRecorder(_cameraController, gpsInfo);

            // Create our Preview view and set it as the content of our activity.
            _cameraPreview = new CameraPreview(this, _cameraController);
            preview.addView(_cameraPreview);

            AccelerometerSensorManager.getInstance().start();

            //controller.setSpeedButtonText();

            controller.initializeNativeEngine(width, height);

            boolean autoCalibration = Settings.getBoolean(Settings.B_AUTOCALIBRATION);
            if (autoCalibration) {
                ivDeleteCalibration.setVisibility(View.VISIBLE);
            } else {
                ivDeleteCalibration.setVisibility(View.INVISIBLE);
            }

            gpsInfo.startRequestData();

            //testParams();
        }
    }

    public void showProcFps(final float procFPS) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                tvProcFps.setText(String.format("%.2f", procFPS));

                String gpuBusyPerc = "N/A";
                try {
                    File gpuBusyPercFile = new File("/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage");
                    BufferedReader br = new BufferedReader(new FileReader(gpuBusyPercFile));
                    gpuBusyPerc = br.readLine();
                    br.close();

                    // Convert '87 %' to '87'
                    int len = gpuBusyPerc.length();
                    int st = 0;
                    while ((st < len) && (gpuBusyPerc.charAt(st) <= ' ')) {
                        st++;
                    }
                    int end = st;
                    while ((end < len) && (gpuBusyPerc.charAt(end) >= '0' && gpuBusyPerc.charAt(end) <= '9')) {
                        end++;
                    }
                    gpuBusyPerc = gpuBusyPerc.substring(st, end) + "%";
                } catch (Exception e) {
                    // GPU usage is not available on this device.
                    //Log.e("ScrMain", e.toString());
                }
                tvGpuUsage.setText(gpuBusyPerc);
            }
        });

    }

    public void showCalibration(final boolean autoCalibration, final IVAdasSceneStatus sceneStatus,
                                final boolean autoDeviation, final IVAdasLdwOutput ldwOutput) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean isHorizonCalibration = true; // show horizon calibration (true) or
                                                     // deviation calibration (false)
                boolean isDevCalibrationInProcess = // true if deviation calibration is in process
                        autoDeviation && ldwOutput.getAutocalibPercentDone() < 100.f;
                TextView tvCalibrationLabel = findViewById(R.id.tv_calibrationLabel);
                tvCalibrationLabel.setText(R.string.calibration_label);
                if (autoCalibration) {
                    if (sceneStatus.getIsCalibrationDetected() != 0) {
                        if(isDevCalibrationInProcess) {
                            isHorizonCalibration = false;
                        }
                        else {
                            tvCalibration.setText(R.string.calibration_auto);
                        }
                    } else {
                        float percent = sceneStatus.getAutocalibPercentDone();
                        tvCalibration.setText(String.format(Locale.getDefault(), "%.0f%%", percent));
                    }
                } else {
                    if(isDevCalibrationInProcess) {
                        isHorizonCalibration = false;
                    }
                    else {
                        tvCalibration.setText(R.string.calibration_manual);
                    }
                }
                if(!isHorizonCalibration){
                    float percent = ldwOutput.getAutocalibPercentDone();
                    tvCalibrationLabel.setText(R.string.calibDeviation_label);
                    tvCalibration.setText(String.format(Locale.getDefault(), "%.0f%%", percent));
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            ToggleButton tgRecord = (ToggleButton) findViewById(R.id.tg_record);
            tgRecord.setChecked(false);

            if (_videoRecorder != null) {
                _videoRecorder.release();
                _videoRecorder = null;
            }

            _cameraController.closeCamera();
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.removeAllViews();
            if (_cameraPreview != null) {
                _cameraPreview.getHolder().removeCallback(_cameraPreview);
            }
            if (cameraFound) {
                AccelerometerSensorManager.getInstance().stop();
                //_cameraController.closeCamera();
                //_cameraController.unregisterListener();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cameraFound) {
            gpsInfo.stopRequestData();
        }
    }

    public void showConfirmExitDialog() {
        OnClickListener dialogButtonClickListener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        ScrMain.this.finish();
                        break;
                }
            }
        };
        DialogHelper.showDialog(ScrMain.this, R.string.dlg_title_confirmation,
                R.string.msg_exit, R.string.dlg_btn_no, -1,
                R.string.dlg_btn_yes, dialogButtonClickListener);
    }

    public void showErrorDialog(final int errorCode) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                CameraControllerBase.stopPrecessing = true;
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        ScrMain.this);
                builder.setTitle("Error!");
                String errMsg = IVAdasErrorCode.getErrorString(errorCode);
                builder.setMessage(errMsg)
                        .setCancelable(false)
                        .setPositiveButton("Close",
                                new OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.dismiss();
                                        CameraControllerBase.stopPrecessing = false;
                                        if (errorCode == IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                                            IVLicenseData.getInstance()
                                                    .setLicenseKey(null);
                                            finish();
                                        }
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();

            }
        });
    }

    public void drawOutput(final IVAdasLdwOutput ldwOutput,
                           final IVAdasFcwOutput fcwOutput,
                           final IVAdasFcwCertOutput fcwCertOutput,
                           final IVAdasTailgatingOutput tgOutput,
                           final IVAdasFcw2Output fcw2Output,
                           final IVAdasTrafficLightsOutput trafficLightsOutput,
                           final IVAdasRollingStopOutput rollingStopOutput,
                           final IVAdasSpeedLimitOutput speedLimitOutput,
                           final IVAdasPcwOutput pcwOutput,
                           final IVAdasLdwOutputEvent ldwEvent,
                           final IVAdasFcwOutputEvent fcwEvent,
                           final IVAdasSnGOutputEvent sngEvent,
                           final IVAdasTailgatingOutputEvent tgEvent,
                           final IVAdasFcw2OutputEvent fcw2Event,
                           final IVAdasTrafficLightsOutputEvent trafficLightsEvent,
                           final IVAdasRollingStopOutputEvent rollingStopEvent,
                           final IVAdasSpeedLimitOutputEvent speedLimitEvent,
                           final IVAdasPcwOutputEvent pcwEvent,
                           final IVAdasSceneStatus sceneStatus) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
					//Final LDW and FCW events for drawing
					IVAdasLdwOutputEvent dLdwEvent = ldwEvent;
					IVAdasFcwOutputEvent dFcwEvent = fcwEvent;
                    IVAdasTailgatingOutputEvent dTgEvent = tgEvent;
                    IVAdasFcw2OutputEvent dFcw2Event = fcw2Event;
                    IVAdasTrafficLightsOutputEvent dTrafficLightsEvent = trafficLightsEvent;
                    IVAdasRollingStopOutputEvent dRollingStopEvent = rollingStopEvent;
                    IVAdasSpeedLimitOutputEvent dSpeedLimitEvent = speedLimitEvent;
                    IVAdasPcwOutputEvent dPcwEvent = pcwEvent;

                    boolean isStopSignPresent = rollingStopOutput.objectsCount > 0;
                    boolean rollingStopSoundEvent = rollingStopEvent.isPresent == 1;
                    boolean trafficLightsSoundEvent = trafficLightsEvent.isPresent == 1;

					if (ldwEvent.getIsPresent() != 0) {
						//detected new LDW event. Will draw it.
						previousLdwEvent = ldwEvent;
						previousEventTimeLdw = System.currentTimeMillis();
						Log.d("EventLDW", "Departure " +
                                ((ldwEvent.getDepartureDirection() == IVAdasDepartureDirection.IVDDTOWARDSLEFT) ?
                                        "left" : "right"));
					}
					else {
						//There is no new LDW event
						if (previousLdwEvent != null) {
							if (System.currentTimeMillis() - previousEventTimeLdw <= EVENT_TIME_OUT_LDW) {
								//Previous LDW event is still actual. Will draw it.
								dLdwEvent = previousLdwEvent;
							}
							else {
								previousLdwEvent = null;
							}
						}
					}

					if (fcwEvent.getIsPresent() != 0) {
						//detected new FCW event. Will draw it.
						previousFCWEvent = fcwEvent;
						previousEventTimeFcw = System.currentTimeMillis();
						Log.d("EventFCW", "Frontal collision warning");
					}
					else {
						if (previousFCWEvent != null) {
							//There is no new FCW event
							if (System.currentTimeMillis() - previousEventTimeFcw <= EVENT_TIME_OUT_FCW) {
								//Previous FCW event is still actual. Will draw it.
								dFcwEvent = previousFCWEvent;
							}
							else {
								previousFCWEvent = null;
							}
						}
					}

                    if (tgEvent.isPresent != 0) {
                        //detected new Tailgating event. Will draw it.
                        previousTgEvent = tgEvent;
                        previousEventTimeTg = System.currentTimeMillis();
                        Log.d("EventTailgating", "Tailgating warning");
                    }
                    else {
                        //There is no new Tailgating event
                        if (previousTgEvent != null) {
                            if (System.currentTimeMillis() - previousEventTimeTg <= EVENT_TIME_OUT_TG) {
                                //Previous Tailgating event is still actual. Will draw it.
                                dTgEvent = previousTgEvent;
                            }
                            else {
                                previousTgEvent = null;
                            }
                        }
                    }

                    if (fcw2Event.isPresent != 0) {
                        //detected new FCW event. Will draw it.
                        previousFcw2Event = fcw2Event;
                        previousEventTimeFcw2 = System.currentTimeMillis();
                        Log.d("EventFCW2", "Frontal collision warning");
                    }
                    else {
                        if (previousFcw2Event != null) {
                            //There is no new FCW event
                            if (System.currentTimeMillis() - previousEventTimeFcw2 <= EVENT_TIME_OUT_FCW2) {
                                //Previous FCW event is still actual. Will draw it.
                                dFcw2Event = previousFcw2Event;
                            }
                            else {
                                previousFcw2Event = null;
                            }
                        }
                    }

                    if (trafficLightsEvent.isPresent != 0) {
                        if (System.currentTimeMillis() - previousEventTimeTrafficLights <= EVENT_TIME_OUT_TRAFFICLIGHTS){
                            //Previous TrafficLights event is still actual. Will must ignore new event for sounds.
                            trafficLightsSoundEvent = false;
                        }
                        else{
                            //Previous TrafficLights event was not actual. Will update and draw it.
                            previousTrafficLightsEvent = trafficLightsEvent;
                            previousEventTimeTrafficLights = System.currentTimeMillis();
                            Log.d("JNI", "TrafficLights warning");
                        }
                    }
                    else {
                        if (previousTrafficLightsEvent != null) {
                            //There is no new TrafficLights event
                            //Previous TrafficLights event is still actual. Will draw it.
                            if (System.currentTimeMillis() - previousEventTimeTrafficLights <= EVENT_TIME_OUT_TRAFFICLIGHTS)
                                dTrafficLightsEvent = previousTrafficLightsEvent;
                            else {
                                previousTrafficLightsEvent = null;
                            }
                        }
                    }

                    // Detected new Rolling Stop event.
                    if (rollingStopEvent.isPresent != 0) {
                        // Detected new event. Will draw it.
                        previousRollingStopEvent = rollingStopEvent;
                        previousEventTimeRollingStop = System.currentTimeMillis();
                        Log.d("EventRollingStop", "Rolling Stop. Min Speed " + rollingStopEvent.minSpeed);
                    }
                    else {
                        if (previousRollingStopEvent != null) {
                            //There is no new event
                            if (System.currentTimeMillis() - previousEventTimeRollingStop <= EVENT_TIME_OUT_ROLLING_STOP) {
                                //Previous event is still actual. Will draw it.
                                dRollingStopEvent = previousRollingStopEvent;
                            } else {
                                previousRollingStopEvent = null;
                            }
                        }
                    }

                    // Detected new Speed Limit event.
                    if (speedLimitEvent.isPresent != 0) {
                        // Detected new event. Will draw it.
                        previousSpeedLimitEvent = speedLimitEvent;
                        previousEventTimeSpeedLimit = System.currentTimeMillis();
                        Log.d("EventSpeedLimit", "Speed Limit violation. Limit " +
                                Integer.toString(speedLimitEvent.speedLimitMph) +
                                " Current " +
                                Float.toString(speedLimitEvent.speedMph));
                    }
                    else {
                        if (previousSpeedLimitEvent != null) {
                            //There is no new event
                            if (System.currentTimeMillis() - previousEventTimeSpeedLimit <= EVENT_TIME_OUT_SPEED_LIMIT) {
                                //Previous event is still actual. Will draw it.
                                dSpeedLimitEvent = previousSpeedLimitEvent;
                            } else {
                                previousSpeedLimitEvent = null;
                            }
                        }
                    }

                    // Detected new PCW event.
                    if (pcwEvent.isPresent != 0) {
                        // Detected new event. Will draw it.
                        previousPcwEvent = pcwEvent;
                        previousEventTimePcw = System.currentTimeMillis();
                        Log.d("EventPCW", "Pedestrian collision warning");
                    }
                    else {
                        if (previousPcwEvent != null) {
                            //There is no new event
                            if (System.currentTimeMillis() - previousEventTimePcw <= EVENT_TIME_OUT_PCW) {
                                //Previous event is still actual. Will draw it.
                                dPcwEvent = previousPcwEvent;
                            } else {
                                previousPcwEvent = null;
                            }
                        }
                    }

                    // Draw road lane, and events
                    ivOverlay.setOutput(ldwOutput, fcwOutput, fcwCertOutput, tgOutput, fcw2Output,
                            trafficLightsOutput, rollingStopOutput, speedLimitOutput,
                            pcwOutput,
                            dLdwEvent, dFcwEvent, sngEvent, dTgEvent, dFcw2Event,
                            dTrafficLightsEvent, dRollingStopEvent, dSpeedLimitEvent,
                            dPcwEvent);
                    ivOverlay.setHorizonValues(sceneStatus.getHoodLevel(),
                            sceneStatus.getHorizonLevel(),
                            sceneStatus.getHorizontalPan());
                    ivOverlay.invalidate();

                    playSound(speedLimitOutput,
                            ldwEvent, dFcwEvent, sngEvent, tgEvent, fcw2Event,
                            trafficLightsSoundEvent, isStopSignPresent, rollingStopEvent,
                            speedLimitEvent, pcwEvent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void startSettingScreen() {
        Intent intent = new Intent(this, ScrSetting.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyPlayers();
        AudioPlayer.getInstance().stop();
        IvAdasWrapper.getInstance().release();
    }

    private void createPlayers() {
        ldwPlayer = MediaPlayer.create(this, R.raw.bldw);
        ldwPlayer.setLooping(false);
        fcw2Player = MediaPlayer.create(this, R.raw.bfcw);
        fcw2Player.setLooping(false);
        trafficLightsPlayer = MediaPlayer.create(this, R.raw.btl);
        trafficLightsPlayer.setLooping(false);
        stopSignPlayer = MediaPlayer.create(this, R.raw.bstopsign);
        stopSignPlayer.setLooping(false);
        rollingStopPlayer = MediaPlayer.create(this, R.raw.brollingstop);
        rollingStopPlayer.setLooping(false);
        sngPlayer = MediaPlayer.create(this, R.raw.bsng);
        sngPlayer.setLooping(true);
        tgPlayer = MediaPlayer.create(this, R.raw.btg);
        tgPlayer.setLooping(false);

        int speedValuesMph[] = {SPEED_LIMIT_UNKNOWN, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70};
        int speedLimitResId[] = {R.raw.speed_limit_unknown, R.raw.speed_limit_10, R.raw.speed_limit_15,
                R.raw.speed_limit_20, R.raw.speed_limit_25, R.raw.speed_limit_30, R.raw.speed_limit_35,
                R.raw.speed_limit_40, R.raw.speed_limit_45, R.raw.speed_limit_50, R.raw.speed_limit_55,
                R.raw.speed_limit_60, R.raw.speed_limit_65, R.raw.speed_limit_70};
        int violationResId[] = {R.raw.violation, R.raw.violation_10, R.raw.violation_15,
                R.raw.violation_20, R.raw.violation_25, R.raw.violation_30, R.raw.violation_35,
                R.raw.violation_40, R.raw.violation_45, R.raw.violation_50, R.raw.violation_55,
                R.raw.violation_60, R.raw.violation_65, R.raw.violation_70};
        for (int i = 0; i < speedValuesMph.length; i++) {
            MediaPlayer p = MediaPlayer.create(this, speedLimitResId[i]);
            p.setLooping(false);
            speedLimitPlayers.put(speedValuesMph[i], p);
            p = MediaPlayer.create(this, violationResId[i]);
            p.setLooping(false);
            violationPlayers.put(speedValuesMph[i], p);
        }

        pcwPlayer = MediaPlayer.create(this, R.raw.bpedestrian);
        pcwPlayer.setLooping(false);
    }

    private void destroyPlayers() {
        if (ldwPlayer != null) {
            ldwPlayer.stop();
            ldwPlayer.release();
            ldwPlayer = null;
        }
        if (fcw2Player != null) {
            fcw2Player.stop();
            fcw2Player.release();
            fcw2Player = null;
        }
        if (trafficLightsPlayer != null) {
            trafficLightsPlayer.stop();
            trafficLightsPlayer.release();
            trafficLightsPlayer = null;
        }
        if (stopSignPlayer != null) {
            stopSignPlayer.stop();
            stopSignPlayer.release();
            stopSignPlayer = null;
        }
        if (rollingStopPlayer != null) {
            rollingStopPlayer.stop();
            rollingStopPlayer.release();
            rollingStopPlayer = null;
        }
        if (sngPlayer != null) {
            sngPlayer.stop();
            sngPlayer.release();
            sngPlayer = null;
        }
        if (tgPlayer != null) {
            tgPlayer.stop();
            tgPlayer.release();
            tgPlayer = null;
        }
        curSpeedLimitPlayer = null;
        curViolationPlayer = null;
        for(int key: speedLimitPlayers.keySet()) {
            MediaPlayer p = speedLimitPlayers.get(key);
            p.stop();
            p.release();
            speedLimitPlayers.put(key, null);
        }
        speedLimitPlayers.clear();
        for(int key: violationPlayers.keySet()) {
            MediaPlayer p = violationPlayers.get(key);
            p.stop();
            p.release();
            violationPlayers.put(key, null);
        }
        violationPlayers.clear();
        if (pcwPlayer != null) {
            pcwPlayer.stop();
            pcwPlayer.release();
            pcwPlayer = null;
        }
    }

    private void playSound(IVAdasSpeedLimitOutput slOutput,
                           IVAdasLdwOutputEvent ldwEvent,
                           IVAdasFcwOutputEvent fcwEvent,
                           IVAdasSnGOutputEvent sngEvent,
                           IVAdasTailgatingOutputEvent tgEvent,
                           IVAdasFcw2OutputEvent fcw2Event,
                           boolean trafficLightsEvent,
                           boolean isStopSignPresent,
                           IVAdasRollingStopOutputEvent rsEvent,
                           IVAdasSpeedLimitOutputEvent slEvent,
                           IVAdasPcwOutputEvent pcwEvent) {
        boolean play = Settings.getBoolean(Settings.B_PLAY_SOUND);
        if (ldwPlayer != null) {
            boolean playLdw = play && (ldwEvent.getIsPresent() != 0);
            if (playLdw) {
                if (!ldwPlayer.isPlaying()) {
                    ldwPlayer.start();
                }
            } else {
                if (ldwPlayer.isPlaying()) {
                    ldwPlayer.pause();
                }
            }
        }
        if (fcw2Player != null) {
            boolean playFcw2 = play && (fcw2Event.isPresent != 0) && Settings.getBoolean(Settings.B_FCW2_OUTPUT);
            if (playFcw2) {
                if (!fcw2Player.isPlaying()) {
                    fcw2Player.start();
                }
            }
        }
        if (trafficLightsPlayer != null) {
            boolean playTrafficLights = play && trafficLightsEvent && Settings.getBoolean(Settings.B_TRAFFICLIGHTS_OUTPUT);
            if (playTrafficLights) {
                if (!trafficLightsPlayer.isPlaying()) {
                    trafficLightsPlayer.start();
                }
            }
        }
        if (sngPlayer != null) {
            boolean playSng = play && (sngEvent.getIsPresent() != 0);
            if (playSng) {
                if (!sngPlayer.isPlaying()) {
                    sngPlayer.start();
                }
            } else {
                if (sngPlayer.isPlaying()) {
                    sngPlayer.pause();
                }
            }
        }
        if (tgPlayer != null) {
            boolean playTg = play && (tgEvent.isPresent != 0) && Settings.getBoolean(Settings.B_TG_OUTPUT);
            if (playTg) {
                if (!tgPlayer.isPlaying()) {
                    tgPlayer.start();
                }
            }
        }
        if (stopSignPlayer != null) {
            boolean playSs = play && isStopSignPresent;
            if (playSs) {
                if (!stopSignPlayer.isPlaying()) {
                    stopSignPlayer.start();
                }
            }
        }
        if (rollingStopPlayer != null) {
            boolean playRs = play && (rsEvent.isPresent != 0);
            if (playRs) {
                if (!rollingStopPlayer.isPlaying()) {
                    rollingStopPlayer.start();
                }
            }
        }
        boolean playSlv = play && (slEvent.isPresent != 0);
        if (playSlv) {
            if (curViolationPlayer == null || !curViolationPlayer.isPlaying()) {
                curViolationPlayer = violationPlayers.get(slEvent.speedLimitMph);
                if (curViolationPlayer == null) {
                    curViolationPlayer = violationPlayers.get(SPEED_LIMIT_UNKNOWN);
                }
                if (curViolationPlayer != null) {
                    // Stop current Speed Limit player if any
                    if (curSpeedLimitPlayer != null && curSpeedLimitPlayer.isPlaying()) {
                        curSpeedLimitPlayer.stop();
                        curSpeedLimitPlayer = null;
                    }
                    curViolationPlayer.start();
                }
            }
        }
        boolean playSl = play && slOutput != null && slOutput.speedLimitSigns != null && slOutput.speedLimitSigns.length > 0;
        if (playSl) {
            // Violation audio has priority over Speed Limit one.
            if (curViolationPlayer == null || !curViolationPlayer.isPlaying()) {
                if (curSpeedLimitPlayer == null || !curSpeedLimitPlayer.isPlaying()) {
                    curSpeedLimitPlayer = speedLimitPlayers.get(slOutput.speedLimitSigns[0].speedLimitMph);
                    if (curSpeedLimitPlayer == null) {
                        curSpeedLimitPlayer = speedLimitPlayers.get(SPEED_LIMIT_UNKNOWN);
                    }
                    if (curSpeedLimitPlayer != null) {
                        curSpeedLimitPlayer.start();
                    }
                }
            }
        }
        if (pcwPlayer != null) {
            boolean playPcw = play && (pcwEvent.isPresent != 0);
            if (playPcw) {
                if (!pcwPlayer.isPlaying()) {
                    pcwPlayer.start();
                }
            }
        }
    }

    private void testParams() {
        int retVal = 0;
        IVAdasSceneParams params = new IVAdasSceneParams();
        VCLog.error(Category.CAT_GUI, "Testing Scene Params");
        IvAdasWrapper.getInstance().getSceneParams(params);
        ObjectUtils.displayObject(params);
        params.setHorizonLevel(20);
        params.setHoodLevel(25);
        params.setCameraMountHeight(1.5f);
        params.setHorizontalPan(4);
        params.setAutoCalibCpuReduction(2);
        params.setLaneWidthMeters(3);
        retVal = IvAdasWrapper.getInstance().setSceneParams(params);
        VCLog.error(Category.CAT_GUI, "Testing setSceneParams retVal-> " + retVal);
        IVAdasSceneParams ivAdasSceneParamsNew = new IVAdasSceneParams();
        IvAdasWrapper.getInstance().getSceneParams(ivAdasSceneParamsNew);
        ObjectUtils.displayObject(ivAdasSceneParamsNew);

        VCLog.error(Category.CAT_GUI, "Testing IVAdasFcw Params");
        IVAdasFcwParams fcwParam = new IVAdasFcwParams();
        IvAdasWrapper.getInstance().getFcwParams(fcwParam);
        ObjectUtils.displayObject(fcwParam);

        fcwParam.setEnableFcwLogging(1);
        fcwParam.setEnableUFcw(0);
        fcwParam.setUFcwMinSpeed(25);
        retVal = IvAdasWrapper.getInstance().setFcwParams(fcwParam);
        VCLog.error(Category.CAT_GUI, "Testing setFcwParams retVal-> " + retVal);
        IVAdasFcwParams fcwParamNew = new IVAdasFcwParams();
        IvAdasWrapper.getInstance().getFcwParams(fcwParamNew);
        ObjectUtils.displayObject(fcwParamNew);

        VCLog.error(Category.CAT_GUI, "Testing IVAdasFcwAdvanced Params");
        IVAdasFcwAdvancedParams fcwAdvanceParams = new IVAdasFcwAdvancedParams();
        IvAdasWrapper.getInstance().getFcwAdvancedParams(
                fcwAdvanceParams);
        ObjectUtils.displayObject(fcwAdvanceParams);
        fcwAdvanceParams.setSafeDistanceTtc(0.6f);
        fcwAdvanceParams.setSecondAlarmTtcFactor(0.7f);
        fcwAdvanceParams.setReappAlarmSafeDistanceTtc(0.3f);
        fcwAdvanceParams.setFastappAlarmFirstSafeDistTtc(0.86f);
        fcwAdvanceParams.setFastappAlarmReappSafeDistTtc(0.86f);
        fcwAdvanceParams.setMaxTimeToSecondAlarm(5000);
        fcwAdvanceParams.setVehicleMovingAwayFactor(3);
        fcwAdvanceParams.setVehicleMovingAwayMaxSpeed(100);
        fcwAdvanceParams.setGpsDelaySpeedFactor(0.5f);
        fcwAdvanceParams.setEnableUnreliableTtcAlarm(1);
        retVal = IvAdasWrapper.getInstance().setFcwAdvancedParams(
                fcwAdvanceParams);
        VCLog.error(Category.CAT_GUI, "Testing setFcwAdvancedParams retVal-> " + retVal);
        IVAdasFcwAdvancedParams fcwAdvanceParamsNew = new IVAdasFcwAdvancedParams();
        IvAdasWrapper.getInstance().getFcwAdvancedParams(
                fcwAdvanceParamsNew);
        ObjectUtils.displayObject(fcwAdvanceParamsNew);
    }

    public void startRecording() {
        _videoRecorder.start();
    }

    public void stopRecording() {
        _videoRecorder.stop();
    }
}
