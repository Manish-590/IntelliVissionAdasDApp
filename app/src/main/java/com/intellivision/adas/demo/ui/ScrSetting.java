package com.intellivision.adas.demo.ui;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.intellivision.adas.demo.BuildConfig;
import com.intellivision.adas.IvAdasWrapper;
import com.intellivision.adas.demo.R;
import com.intellivision.adas.demo.datamodels.Settings;

public class ScrSetting extends Activity {

	private static final int INT = 0;
	private static final int FLOAT = 1;

	private class EditTextBinding {
		int resId;
		int property;
		int type;

		EditTextBinding(int resId, int property, int type) {
			this.resId = resId;
			this.property = property;
			this.type = type;
		}
	}

	private class SwitchBinding {
		int resId;
		int property;

		SwitchBinding(int resId, int property) {
			this.resId = resId;
			this.property = property;
		}
	}

	private EditTextBinding[] editTextBindings = {
			new EditTextBinding(R.id.et_horizontalFov, Settings.F_HORIZONTAL_FOV, FLOAT),
			new EditTextBinding(R.id.et_verticalFov, Settings.F_VERTICAL_FOV, FLOAT),
			new EditTextBinding(R.id.et_autoCalibMinSpeed, Settings.F_AUTO_CALIB_MIN_SPEED, FLOAT),
			new EditTextBinding(R.id.et_hoodLevel, Settings.F_HOOD_LEVEL, FLOAT),
			new EditTextBinding(R.id.et_horizonLevel, Settings.F_HORIZON_LEVEL, FLOAT),
			new EditTextBinding(R.id.et_horizontalPan, Settings.F_HORIZONTAL_PAN, FLOAT),
			new EditTextBinding(R.id.et_cameraMountHeight, Settings.F_CAMERA_MOUNT_HEIGHT, FLOAT),
			new EditTextBinding(R.id.et_laneWidth, Settings.F_LANE_WIDTH, FLOAT),
			new EditTextBinding(R.id.et_minDistance, Settings.F_MIN_DISTANCE, FLOAT),

			new EditTextBinding(R.id.et_distToLeftWheel, Settings.F_DIST_TO_LEFT_WHEEL, FLOAT),
			new EditTextBinding(R.id.et_distToRightWheel, Settings.F_DIST_TO_RIGHT_WHEEL, FLOAT),
			new EditTextBinding(R.id.et_distLeftThreshold, Settings.F_DIST_LEFT_THRESHOLD, FLOAT),
			new EditTextBinding(R.id.et_distRightThreshold, Settings.F_DIST_RIGHT_THRESHOLD, FLOAT),
			new EditTextBinding(R.id.et_laneChangeDistanceThreshold, Settings.F_LANE_CHANGE_DISTANCE_THRESHOLD, FLOAT),
			new EditTextBinding(R.id.et_minDeviationDuration, Settings.F_MIN_DEVIATION_DURATION, FLOAT),
			new EditTextBinding(R.id.et_minDurationBetweenDeviationEvents, Settings.F_MIN_DURATION_BETWEEN_DEVIATION_EVENTS, FLOAT),
			new EditTextBinding(R.id.et_lineClassifierType, Settings.I_LINE_CLASSIFIER_TYPE, INT),

			new EditTextBinding(R.id.et_vehicleDetectorType, Settings.I_VD_TYPE, INT),
			new EditTextBinding(R.id.et_vdScoreThreshold, Settings.F_VD_SCORE_THRESHOLD, FLOAT),
			new EditTextBinding(R.id.et_vdTrafficLightScoreThreshold, Settings.F_VD_TL_SCORE_THRESHOLD, FLOAT),
			new EditTextBinding(R.id.et_vdDevice, Settings.I_VD_DEVICE, INT),
			new EditTextBinding(R.id.et_vdNumThreads, Settings.I_VD_NUM_THREADS, INT),
			new EditTextBinding(R.id.et_vdCpuUsageLevel, Settings.I_VD_CPU_USAGE_LEVEL, INT),

			new EditTextBinding(R.id.et_rsMinSpeed, Settings.F_RS_MIN_SPEED, FLOAT),
			new EditTextBinding(R.id.et_rsMinDurationBetweenEvents, Settings.F_RS_MIN_DURATION_BETWEEN_EVENTS, FLOAT),
			new EditTextBinding(R.id.et_rsScoreThreshold, Settings.F_RS_SCORE_THRESHOLD, FLOAT),

			new EditTextBinding(R.id.et_tgMinSpeed, Settings.F_TG_MIN_SPEED, FLOAT),
			new EditTextBinding(R.id.et_tgSensitivity, Settings.I_TG_SENSITIVITY, INT),
			new EditTextBinding(R.id.et_tgNightSensitivity, Settings.I_TG_NIGHT_SENSITIVITY, INT),
			new EditTextBinding(R.id.et_tgMinDuration, Settings.F_TG_MIN_DURATION, FLOAT),
			new EditTextBinding(R.id.et_tgMinDurationBetweenEvents, Settings.F_TG_MIN_DURATION_BETWEEN_EVENTS, FLOAT),

			new EditTextBinding(R.id.et_sngSensitivity, Settings.I_SNG_SENSITIVITY, INT),
			new EditTextBinding(R.id.et_sngMinDuration, Settings.F_SNG_MIN_DURATION, FLOAT),

			new EditTextBinding(R.id.et_fcw2MinSpeed, Settings.F_FCW2_MIN_SPEED, FLOAT),
			new EditTextBinding(R.id.et_fcw2EventType, Settings.I_FCW2_EVENT_TYPE, INT),
			new EditTextBinding(R.id.et_fcw2Sensitivity, Settings.I_FCW2_SENSITIVITY, INT),
			new EditTextBinding(R.id.et_fcw2NightSensitivity, Settings.I_FCW2_NIGHT_SENSITIVITY, INT),
			new EditTextBinding(R.id.et_fcw2MinDurationBetweenEvents, Settings.F_FCW2_MIN_DURATION_BETWEEN_EVENTS, FLOAT),
			new EditTextBinding(R.id.et_fcw2MinDistanceDecrease, Settings.F_FCW2_MIN_DISTANCE_DECREASE, FLOAT),
			new EditTextBinding(R.id.et_fcw2MaxCurrentDistance, Settings.F_FCW2_MAX_CURRENT_DISTANCE, FLOAT),

			new EditTextBinding(R.id.et_slMinDurationBetweenEvents, Settings.F_SL_MIN_DURATION_BETWEEN_EVENTS, FLOAT),
			new EditTextBinding(R.id.et_slDuration, Settings.F_SL_DURATION, FLOAT),
			new EditTextBinding(R.id.et_slDetScoreThreshold, Settings.F_SL_DET_SCORE_THRESHOLD, FLOAT),
			new EditTextBinding(R.id.et_slRecScoreThreshold, Settings.F_SL_REC_SCORE_THRESHOLD, FLOAT),
			new EditTextBinding(R.id.et_slRoiX, Settings.F_SL_ROI_X, FLOAT),
			new EditTextBinding(R.id.et_slRoiY, Settings.F_SL_ROI_Y, FLOAT),
			new EditTextBinding(R.id.et_slRoiWidth, Settings.F_SL_ROI_WIDTH, FLOAT),
			new EditTextBinding(R.id.et_slRoiHeight, Settings.F_SL_ROI_HEIGHT, FLOAT),

			new EditTextBinding(R.id.et_pcwMinSpeed, Settings.F_PCW_MIN_SPEED, FLOAT),
			new EditTextBinding(R.id.et_pcwMaxSpeed, Settings.F_PCW_MAX_SPEED, FLOAT),
			new EditTextBinding(R.id.et_pcwRoiX, Settings.F_PCW_ROI_X, FLOAT),
			new EditTextBinding(R.id.et_pcwRoiY, Settings.F_PCW_ROI_Y, FLOAT),
			new EditTextBinding(R.id.et_pcwRoiWidth, Settings.F_PCW_ROI_WIDTH, FLOAT),
			new EditTextBinding(R.id.et_pcwRoiHeight, Settings.F_PCW_ROI_HEIGHT, FLOAT),
			new EditTextBinding(R.id.et_pcwEventRoiLeftOffset, Settings.F_PCW_EVENT_ROI_LEFT_OFFSET, FLOAT),
			new EditTextBinding(R.id.et_pcwEventRoiRightOffset, Settings.F_PCW_EVENT_ROI_RIGHT_OFFSET, FLOAT),
			new EditTextBinding(R.id.et_pcwMinDurationBetweenEvents, Settings.F_PCW_MIN_DURATION_BETWEEN_EVENTS, FLOAT),
			new EditTextBinding(R.id.et_pcwScoreThreshold, Settings.F_PCW_SCORE_THRESHOLD, FLOAT),
			new EditTextBinding(R.id.et_pcwDevice, Settings.I_PCW_DEVICE, INT),
			new EditTextBinding(R.id.et_pcwMaxFrameRate, Settings.F_PCW_MAX_FRAME_RATE, FLOAT),

			new EditTextBinding(R.id.et_speed, Settings.F_SPEED, FLOAT),

			new EditTextBinding(R.id.et_width, Settings.I_WIDTH, INT),
			new EditTextBinding(R.id.et_height, Settings.I_HEIGHT, INT),

			new EditTextBinding(R.id.et_videoFileSize, Settings.F_VIDEO_FILE_SIZE, FLOAT),
			new EditTextBinding(R.id.et_fontScalePerc, Settings.F_FONT_SCALE_PERC, FLOAT),

			new EditTextBinding(R.id.et_dumpGap, Settings.I_DUMP_GAP, INT)
	};

	private SwitchBinding[] switchBindings = {
			new SwitchBinding(R.id.sw_autocalibration, Settings.B_AUTOCALIBRATION),
			new SwitchBinding(R.id.sw_useWheelsToLine, Settings.B_USE_WHEELS_TO_LINE),
			new SwitchBinding(R.id.sw_deviation, Settings.B_DEVIATION),
			new SwitchBinding(R.id.sw_autodeviation, Settings.B_AUTODEVIATION),
			new SwitchBinding(R.id.sw_solidOnly, Settings.B_SOLID_ONLY),
			new SwitchBinding(R.id.sw_enableRs, Settings.B_RS_ENABLED),
			new SwitchBinding(R.id.sw_fcw2Output, Settings.B_FCW2_OUTPUT),
			new SwitchBinding(R.id.sw_trafficLightsOutput, Settings.B_TRAFFICLIGHTS_OUTPUT),
			new SwitchBinding(R.id.sw_tgOutput, Settings.B_TG_OUTPUT),
			new SwitchBinding(R.id.sw_enableSng, Settings.B_ENABLE_SNG),
			new SwitchBinding(R.id.sw_enableSpeedLimit, Settings.B_SL_ENABLED),
			new SwitchBinding(R.id.sw_enablePcw, Settings.B_PCW_ENABLED),
			new SwitchBinding(R.id.sw_pcwUseEventRoi, Settings.B_PCW_USE_EVENT_ROI),
			new SwitchBinding(R.id.sw_playSound, Settings.B_PLAY_SOUND),
			new SwitchBinding(R.id.sw_reverseLandscape, Settings.B_REVERSE_LANDSCAPE)
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.settings);
		String libVersion = IvAdasWrapper.getInstance().getVersionInfo();
        String version = "App " + BuildConfig.VERSION_NAME + " " + libVersion;
        TextView tvVersion = findViewById(R.id.tv_ver);
        tvVersion.setText(version);

		for (EditTextBinding editTextBinding: editTextBindings) {
			EditText editText = findViewById(editTextBinding.resId);
			String str = "";
			if (editTextBinding.type == INT) {
				if (Settings.isValid(editTextBinding.property)) {
					int value = Settings.getInt(editTextBinding.property);
					str = String.valueOf(value);
				}
			} else if (editTextBinding.type == FLOAT) {
				if (Settings.isValid(editTextBinding.property)) {
					float value = Settings.getFloat(editTextBinding.property);
					str = String.valueOf(value);
				}
			} else {
				throw new RuntimeException("Unsupported type " + Integer.toString((editTextBinding.type)));
			}
			editText.setText(str);
		}

		for (SwitchBinding switchBinding: switchBindings) {
			if (Settings.isValid(switchBinding.property)) {
				Switch sw = findViewById(switchBinding.resId);
				boolean value = Settings.getBoolean(switchBinding.property);
				sw.setChecked(value);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (Settings.getBoolean(Settings.B_REVERSE_LANDSCAPE)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
	}

	@Override
	public void onPause() {
		for (EditTextBinding editTextBinding: editTextBindings) {
			EditText editText = findViewById(editTextBinding.resId);
			String str = editText.getText().toString();
			if (editTextBinding.type == INT) {
				if (Settings.isValid(editTextBinding.property)) {
					int value = Integer.parseInt(str);
					Settings.setInt(editTextBinding.property, value);
				}
			} else if (editTextBinding.type == FLOAT) {
				if (Settings.isValid(editTextBinding.property)) {
					float value = Float.parseFloat(str);
					Settings.setFloat(editTextBinding.property, value);
				}
			} else {
				throw new RuntimeException("Unsupported type " + Integer.toString((editTextBinding.type)));
			}
		}

		for (SwitchBinding switchBinding: switchBindings) {
			if (Settings.isValid(switchBinding.property)) {
				Switch sw = findViewById(switchBinding.resId);
				Settings.setBoolean(switchBinding.property, sw.isChecked());
			}
		}

		Toast.makeText(this, "Settings are updated", Toast.LENGTH_LONG).show();

		super.onPause();
	}
}
