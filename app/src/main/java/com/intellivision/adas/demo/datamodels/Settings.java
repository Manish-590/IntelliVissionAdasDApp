package com.intellivision.adas.demo.datamodels;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import com.intellivision.adas.demo.MainApplication;
import com.intellivision.adas.demo.R;

import com.intellivision.adas.IVAdasEnumConstants.IVAdasInferenceDevice;

public class Settings {
    public static final int F_HORIZONTAL_FOV = 0;
    public static final int F_VERTICAL_FOV = 1;

    public static final int B_AUTOCALIBRATION = 10;

    public static final int F_HOOD_LEVEL = 11;
    public static final int F_HORIZON_LEVEL = 12;
    public static final int F_HORIZONTAL_PAN = 13;
    public static final int F_CAMERA_MOUNT_HEIGHT = 14;

    public static final int F_AUTO_CALIB_MIN_SPEED = 15;
    public static final int F_LANE_WIDTH = 16;
    public static final int F_MIN_DISTANCE = 17;

    public static final int B_DEVIATION = 20;
    public static final int B_AUTODEVIATION = 21;

    public static final int B_USE_WHEELS_TO_LINE = 30;
    public static final int F_DIST_TO_LEFT_WHEEL = 31;
    public static final int F_DIST_TO_RIGHT_WHEEL = 32;
    public static final int F_DIST_LEFT_THRESHOLD = 33;
    public static final int F_DIST_RIGHT_THRESHOLD = 36;
    public static final int F_LANE_CHANGE_DISTANCE_THRESHOLD = 37;
    public static final int F_MIN_DEVIATION_DURATION = 34;
    public static final int F_MIN_DURATION_BETWEEN_DEVIATION_EVENTS = 38;
    public static final int I_LINE_CLASSIFIER_TYPE = 35;
    public static final int B_SOLID_ONLY = 39;

    public static final int I_VD_TYPE = 60;
    public static final int F_VD_SCORE_THRESHOLD = 61;
    public static final int I_VD_DEVICE = 62;
    public static final int I_VD_NUM_THREADS = 63;
    public static final int F_VD_TL_SCORE_THRESHOLD = 64;
    public static final int I_VD_CPU_USAGE_LEVEL = 65;

    public static final int F_TG_MIN_SPEED = 40;
    public static final int I_TG_SENSITIVITY = 41;
    public static final int I_TG_NIGHT_SENSITIVITY = 44;
    public static final int F_TG_MIN_DURATION = 42;
    public static final int F_TG_MIN_DURATION_BETWEEN_EVENTS = 43;

    public static final int F_FCW2_MIN_SPEED = 50;
    public static final int I_FCW2_SENSITIVITY = 51;
    public static final int I_FCW2_NIGHT_SENSITIVITY = 52;
    public static final int F_FCW2_MIN_DURATION_BETWEEN_EVENTS = 53;
    public static final int I_FCW2_EVENT_TYPE = 54;
    public static final int F_FCW2_MIN_DISTANCE_DECREASE = 56;
    public static final int F_FCW2_MAX_CURRENT_DISTANCE = 57;

    public static final int B_RS_ENABLED = 80;
    public static final int F_RS_MIN_SPEED = 81;
    public static final int F_RS_SCORE_THRESHOLD = 82;
    public static final int F_RS_MIN_DURATION_BETWEEN_EVENTS = 83;

    public static final int B_SL_ENABLED = 90;
    public static final int F_SL_DET_SCORE_THRESHOLD = 91;
    public static final int F_SL_REC_SCORE_THRESHOLD = 92;
    public static final int F_SL_MIN_DURATION_BETWEEN_EVENTS = 93;
    public static final int F_SL_ROI_X = 94;
    public static final int F_SL_ROI_Y = 95;
    public static final int F_SL_ROI_WIDTH = 96;
    public static final int F_SL_ROI_HEIGHT = 97;
    public static final int F_SL_DURATION = 98;

    public static final int B_PCW_ENABLED = 120;
    public static final int F_PCW_MIN_SPEED = 121;
    public static final int F_PCW_MAX_SPEED = 122;
    public static final int F_PCW_ROI_X = 123;
    public static final int F_PCW_ROI_Y = 124;
    public static final int F_PCW_ROI_WIDTH = 125;
    public static final int F_PCW_ROI_HEIGHT = 126;
    public static final int B_PCW_USE_EVENT_ROI = 131;
    public static final int F_PCW_EVENT_ROI_LEFT_OFFSET = 132;
    public static final int F_PCW_EVENT_ROI_RIGHT_OFFSET = 133;
    public static final int F_PCW_MIN_DURATION_BETWEEN_EVENTS = 127;
    public static final int F_PCW_SCORE_THRESHOLD = 128;
    public static final int I_PCW_DEVICE = 129;
    public static final int F_PCW_MAX_FRAME_RATE = 130;

    public static final int I_SNG_SENSITIVITY = 70;
    public static final int F_SNG_MIN_DURATION = 71;

    public static final int B_FCW2_OUTPUT = 109;
    public static final int B_TRAFFICLIGHTS_OUTPUT = 110;
    public static final int B_TG_OUTPUT = 104;
    public static final int B_ENABLE_SNG = 107;

    public static final int F_SPEED = 100;
    public static final int B_PLAY_SOUND = 101;
    public static final int I_CUR_SPEED = 102;
    public static final int B_REVERSE_LANDSCAPE = 105;

    public static final int F_VIDEO_FILE_SIZE = 106;

    public static final int F_FONT_SCALE_PERC = 108;

    public static final int I_WIDTH = 111;
    public static final int I_HEIGHT = 112;

    public static final int I_DUMP_GAP = 113;

    public static final float DEFAULT_HORIZONTAL_FOV = 120.0f;
    public static final float DEFAULT_VERTICAL_FOV = 60.0f;

    public static final boolean DEFAULT_SOLID_ONLY = false;

    public static final boolean DEFAULT_FCW2_OUTPUT = false;
    public static final boolean DEFAULT_TG_OUTPUT = false;
    public static final boolean DEFAULT_TRAFFICLIGHTS_OUTPUT = false;

    public static final boolean DEFAULT_PCW_ENABLED = true;

    public static final float DEFAULT_SPEED = 70.0f;
    public static final boolean DEFAULT_PLAY_SOUND = true;
    public static final boolean DEFAULT_REVERSE_LANDSCAPE = false;
    public static final float DEFAULT_VIDEO_FILE_SIZE = 256.0f;
    public static final float DEFAULT_FONT_SCALE_PERC = 100.0f;

    public static final int DEFAULT_WIDTH = 1280;
    public static final int DEFAULT_HEIGHT = 720;

    public static final int DEFAULT_DUMP_GAP = 0;

    public static final int CUR_SPEED_GPS = 0;
    public static final int CUR_SPEED_0 = 1;
    public static final int CUR_SPEED_FROM_SETTINGS = 2;
    public static final int CUR_SPEED_NUM = 3;

    private static final String INTERNAL_STATE_FILENAME = "InternalState";

    private static SharedPreferences sharedPreferences = null;
    private static Editor sharedPrefEditor = null;
    private static final String SHARED_PREFERENCE_NAME = "SettingData";
    private static final String KEY_PREFIX = "KEY_";

    private static final String TAG = "Settings";

    static {
        sharedPreferences = MainApplication.appContext.getSharedPreferences(SHARED_PREFERENCE_NAME,
                Context.MODE_PRIVATE);
        sharedPrefEditor = sharedPreferences.edit();
        initDefaultValues();
    }

    public static boolean isValid(int property) {
        String key = KEY_PREFIX + Integer.toString(property);
        return sharedPreferences.contains(key);
    }

    public static void setBooleanIfVoid(int property, boolean value) {
        String key = KEY_PREFIX + Integer.toString(property);
        if (!sharedPreferences.contains(key)) {
            sharedPrefEditor.putBoolean(key, value).apply();
        }
    }

    public static void setBoolean(int property, boolean value) {
        String key = KEY_PREFIX + Integer.toString(property);
        sharedPrefEditor.putBoolean(key, value).apply();
    }

    public static boolean getBoolean(int property) {
        String key = KEY_PREFIX + Integer.toString(property);
        if (!sharedPreferences.contains(key)) {
            throw new RuntimeException("Property " + Integer.toString(property) + " not found");
        }
        return sharedPreferences.getBoolean(key, false);
    }

    public static void setIntIfVoid(int property, int value) {
        String key = KEY_PREFIX + Integer.toString(property);
        if (!sharedPreferences.contains(key)) {
            sharedPrefEditor.putInt(key, value).apply();
        }
    }

    public static void setInt(int property, int value) {
        String key = KEY_PREFIX + Integer.toString(property);
        sharedPrefEditor.putInt(key, value).apply();
    }

    public static int getInt(int property) {
        String key = KEY_PREFIX + Integer.toString(property);
        if (!sharedPreferences.contains(key)) {
            throw new RuntimeException("Property " + Integer.toString(property) + " not found");
        }
        return sharedPreferences.getInt(key, 0);
    }

    public static void setFloatIfVoid(int property, float value) {
        String key = KEY_PREFIX + Integer.toString(property);
        if (!sharedPreferences.contains(key)) {
            sharedPrefEditor.putFloat(key, value).apply();
        }
    }

    public static void setFloat(int property, float value) {
        String key = KEY_PREFIX + Integer.toString(property);
        sharedPrefEditor.putFloat(key, value).apply();
    }

    public static float getFloat(int property) {
        String key = KEY_PREFIX + Integer.toString(property);
        if (!sharedPreferences.contains(key)) {
            throw new RuntimeException("Property " + Integer.toString(property) + " not found");
        }
        return sharedPreferences.getFloat(key, 0.0f);
    }

    public static File getDataPath() {
        File sdCard = Environment.getExternalStorageDirectory();
        File dataPath = new File(sdCard, MainApplication.appContext.getString(R.string.app_label));
        return dataPath;
    }

    public static File getDumpPath() {
        File dataPath = getDataPath();
        File dumpPath = new File(dataPath, "dump");
        return dumpPath;
    }

    private static void initDefaultValues() {
        // Init default values which are not stored in the engine.
        Settings.setFloatIfVoid(Settings.F_HORIZONTAL_FOV, DEFAULT_HORIZONTAL_FOV);
        Settings.setFloatIfVoid(Settings.F_VERTICAL_FOV, DEFAULT_VERTICAL_FOV);
        Settings.setBooleanIfVoid(Settings.B_SOLID_ONLY, DEFAULT_SOLID_ONLY);
        Settings.setBooleanIfVoid(Settings.B_FCW2_OUTPUT, DEFAULT_FCW2_OUTPUT);
        Settings.setBooleanIfVoid(Settings.B_TRAFFICLIGHTS_OUTPUT, DEFAULT_TRAFFICLIGHTS_OUTPUT);
        Settings.setBooleanIfVoid(Settings.B_TG_OUTPUT, DEFAULT_TG_OUTPUT);
        Settings.setFloatIfVoid(Settings.F_SPEED, DEFAULT_SPEED);
        Settings.setIntIfVoid(Settings.I_CUR_SPEED, Settings.CUR_SPEED_GPS);
        Settings.setBooleanIfVoid(Settings.B_PLAY_SOUND, DEFAULT_PLAY_SOUND);
        Settings.setBooleanIfVoid(Settings.B_REVERSE_LANDSCAPE, DEFAULT_REVERSE_LANDSCAPE);
        Settings.setFloatIfVoid(Settings.F_VIDEO_FILE_SIZE, DEFAULT_VIDEO_FILE_SIZE);
        Settings.setFloatIfVoid(Settings.F_FONT_SCALE_PERC, DEFAULT_FONT_SCALE_PERC);
        Settings.setIntIfVoid(Settings.I_WIDTH, DEFAULT_WIDTH);
        Settings.setIntIfVoid(Settings.I_HEIGHT, DEFAULT_HEIGHT);
        Settings.setIntIfVoid(Settings.I_DUMP_GAP, DEFAULT_DUMP_GAP);
        // Override engine default values if necessary.
        Settings.setBooleanIfVoid(Settings.B_PCW_ENABLED, DEFAULT_PCW_ENABLED);
    }

    private static File getInternalStatePath() {
        if (isExternalStorageWritable()) {
            Log.d(TAG, "SD card is available");
            File sdCard = Environment.getExternalStorageDirectory();
            File intStPath = new File(sdCard.toString() + File.separator +
                    MainApplication.appContext.getString(R.string.app_label));
            return new File(intStPath, INTERNAL_STATE_FILENAME);
        } else {
            Log.w(TAG, "!!!SD card is NOT available");
            return null;
        }
    }

    public static void saveInternalState(String state) {
        File intStFile = getInternalStatePath();
        if (intStFile != null) {
            try {
                FileWriter fw = new FileWriter(intStFile);
                PrintWriter pw = new PrintWriter(fw);

                pw.println(state);

                pw.close();
                fw.close();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    public static String loadInternalState() {
        String state = null;
        File intStFile = getInternalStatePath();
        if (intStFile != null) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(intStFile));
                state = br.readLine();
                if (state != null) {
                    String s = null;
                    while ((s = br.readLine()) != null) {
                        state += "\n" + s;
                    }
                }

                br.close();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
        return state;
    }

    public static void deleteInternalState() {
        File intStFile = getInternalStatePath();
        if (intStFile != null) {
            try {
                intStFile.delete();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}
