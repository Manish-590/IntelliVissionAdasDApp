package com.intellivision.adas.demo.logger;

import android.util.Log;

/**
 * This is a utility class to display logs on console as well as write logs to file. Application can
 * log INFO/DEBUG/ERROR type of logs using this class. If console logs are disabled then logs will
 * not be printed on console. If SDcard logs are disabled then logs will not be written to file.
 * 
 */
public class VCLog {
    /**
     * Log level INFO
     * 
     * @param category
     *            the log category
     * @param message
     *            the log message
     */
    public static void info( String category, String message ) {
        if ( !Logger.disableConsoleLogs ) {
            Log.i( Logger.logTag, message );
        }
        if ( !Logger.disableSDCardLogs ) {
            Logger.addEntry( category, "I", message );
        }
    }

    /**
     * Level DEBUG
     * 
     * @param category
     *            the log category
     * @param message
     *            the log message
     */
    public static void debug( String category, String message ) {
        if ( !Logger.disableConsoleLogs ) {
            Log.d( Logger.logTag, message );
        }
        if ( !Logger.disableSDCardLogs ) {
            Logger.addEntry( category, "D", message );
        }
    }

    /**
     * Level ERROR
     * 
     * @param category
     *            the log category
     * @param message
     *            the log message
     */
    public static void error( String category, String message ) {
        if ( !Logger.disableConsoleLogs ) {
            Log.e( Logger.logTag, message );
        }
        if ( !Logger.disableSDCardLogs ) {
            Logger.addEntry( category, "E", message );
        }
    }
}