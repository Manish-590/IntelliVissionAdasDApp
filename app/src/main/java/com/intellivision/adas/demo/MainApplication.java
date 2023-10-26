package com.intellivision.adas.demo;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.intellivision.adas.demo.logger.Category;
import com.intellivision.adas.demo.logger.Logger;
import com.intellivision.adas.demo.logger.LoggerParams;
import com.intellivision.adas.demo.logger.VCLog;
import com.intellivision.adas.demo.utilities.DeviceUtils;

/**
 * The entry point of Application.
 * 
 */
public class MainApplication extends Application {
    public static Context appContext;

    public static boolean isDebugBuild = true;

    public void onCreate() {
        super.onCreate( );
        appContext = this;
        _initalizeLogger( );

        VCLog.info( Category.CAT_GENERAL, "MainApplication: onCreate" );

        /**
         * Initialize uncaught exception handler to catch all the unhandled exceptions and log them
         * into file.
         */
        Thread.setDefaultUncaughtExceptionHandler( new Thread.UncaughtExceptionHandler( ) {
            public void uncaughtException( Thread thread, Throwable ex ) {
                try {
                    String error = Log.getStackTraceString( ex );
                    VCLog.error( Category.CAT_GENERAL, "MainApplication: uncaughtException: thread->" + thread.getName( ) + "Exception was: " + ex.getMessage( ) + " stack trace->" + error );
                    System.exit( 1 );
                } catch ( Exception e ) {
                    VCLog.error( Category.CAT_GENERAL, "MainApplication: Exception->" + e.getMessage( ) );
                }
            }
        } );
    }

    /**
     * Method to initialize logger.
     */
    private void _initalizeLogger() {
        String appName = getString( R.string.app_label );
        String packageName = getPackageName( );
        int applicationVersionCode = 0;
        String applicationVersionName = "";
        try {
            applicationVersionCode = appContext.getPackageManager( ).getPackageInfo( packageName, 0 ).versionCode;
            applicationVersionName = appContext.getPackageManager( ).getPackageInfo( packageName, 0 ).versionName;
        } catch ( Exception e ) {
            e.printStackTrace( );
        }
        String targetSdk = android.os.Build.VERSION.RELEASE;
        DeviceUtils device = new DeviceUtils( );
        String deviceModel = device.getDeviceModel( );
        String deviveManufacturer = device.getDeviceManufacturer( );

        LoggerParams params = new LoggerParams( appName, packageName, "" + applicationVersionCode, applicationVersionName, targetSdk, deviceModel, deviveManufacturer, appName );
        Logger.init( params );
        Logger.removeOlderFiles( );
    }

}
