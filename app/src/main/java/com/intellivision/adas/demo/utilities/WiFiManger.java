package com.intellivision.adas.demo.utilities;

import android.content.Context;
import android.net.wifi.WifiManager;

import com.intellivision.adas.demo.MainApplication;

public class WiFiManger {

    public static boolean isWiFiEnabled() {
        boolean isEnabled = false;
        try {
            WifiManager wifi = (WifiManager) MainApplication.appContext.getSystemService( Context.WIFI_SERVICE );
            isEnabled = wifi.isWifiEnabled( );
        } catch ( Exception e ) {
            e.printStackTrace( );
        }
        return isEnabled;
    }

}
