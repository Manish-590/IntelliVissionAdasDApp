package com.intellivision.adas.demo.datamodels;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.intellivision.adas.demo.MainApplication;

public class IVLicenseData {

    private static IVLicenseData _instance = null;
    ;

    private static SharedPreferences _sharedPreferences = null;
    private static Editor _sharedPrefEditor = null;
    private final String SHARED_PREFERENCE_NAME = "AdasLicenseData";

    private final String KEY_LICENSE_KEY = "LICENSE_KEY";

    /**
     * Private Constructor
     */
    private IVLicenseData() {

    }

    public static IVLicenseData getInstance() {
        if (_instance == null) {
            _instance = new IVLicenseData();
            _instance._initSharedPreferences();
        }
        return _instance;
    }

    /**
     * This method is used to initialized {@link SharedPreferences} and
     * {@link Editor}
     */
    private void _initSharedPreferences() {
        _sharedPreferences = _getSharedPref();
        _sharedPrefEditor = _getSharedPrefEditor();
    }

    /**
     * Method to get the SharedPreferences.
     *
     * @return the {@link SharedPreferences} object.
     */
    private SharedPreferences _getSharedPref() {
        if (_sharedPreferences == null) {
            _sharedPreferences = MainApplication.appContext
                    .getSharedPreferences(SHARED_PREFERENCE_NAME,
                            Context.MODE_PRIVATE);
        }
        return _sharedPreferences;
    }

    /**
     * Method to get the {@link Editor} for writing values to
     * {@link SharedPreferences}.
     *
     * @return the {@link Editor} object.
     */
    private Editor _getSharedPrefEditor() {
        if (_sharedPrefEditor == null) {
            _sharedPrefEditor = _getSharedPref().edit();
        }
        return _sharedPrefEditor;
    }

    public void setLicenseKey(String key) {
        _getSharedPrefEditor().putString(KEY_LICENSE_KEY, key).commit();
    }

    public String getLicenseKey() {
        return _getSharedPref().getString(KEY_LICENSE_KEY, null);
    }

}
