package com.intellivision.adas.demo.utilities;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import com.intellivision.adas.demo.MainApplication;
import com.intellivision.adas.demo.logger.Category;
import com.intellivision.adas.demo.logger.VCLog;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Utility class to get device parameters
 */
public class DeviceUtils {
    private static final String CPU_DIR = "/sys/devices/system/cpu/";

    /**
     * This method is used to get the Device's OS version
     *
     * @return the Device's OS version
     */
    public static int getDeviceOsVersion() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * Gets the number of cores available on this device, across all processors.
     * Uses/parses the directory "/sys/devices/system/cpu" and no of files in
     * this directory is the actual no of cores. Uses file filters for accurate
     * result.
     *
     * @return the number of available cores, or 1 if unable to get the result
     */
    public static int getNumberOfCores() {
        /**
         * Private FileFilter Class to display only CPU devices in the directory
         * listing
         *
         */
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                /**
                 * Check if filename is "cpu", followed by a single digit number
                 */
                if (Pattern.matches("cpu[0-9]+", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }

        try {
            /**
             * Get directory containing CPU info
             */
            File dir = new File(CPU_DIR);

            /**
             * Add a filer to only list the files starting with "cpu"
             */
            File[] files = dir.listFiles(new CpuFilter());

            /**
             * Return the number of cores (virtual CPU devices)
             */
            return files.length;
        } catch (Exception e) {
            VCLog.error(
                    Category.CAT_OTHER,
                    "DeviceUtils: getnumberOfCores: Exception->"
                            + e.getMessage());
            return 1;
        }
    }

    /**
     * This function checks whether SD card is present or not and whether space
     * is available or not.
     *
     * @return true if SD card is available, false otherwise.
     */
    public static boolean isSdCardPresent() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)
                && !state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            return true;
        }
        return false;
    }

    /**
     * Returns the canonical path of the external media directory. A canonical
     * path is an absolute path with all symbolic links and references to "."
     * and ".." resolved. If the canonical path cannot be resolved, returns the
     * absolute path.<br>
     * Note that the media may not be available. Use
     *
     * @return A String containing the path.
     */
    private static final String _getExternalMediaPath() {
        try {
            return Environment.getExternalStorageDirectory().getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        }
    }

    /**
     * Method to get the device model. e.g. Droid 3
     *
     * @return The model name of the device.
     */
    public String getDeviceModel() {
        return Build.MODEL;
    }

    /**
     * This method is used to get the device manufacturer. e.g. Samsung
     *
     * @return the manufacturer.
     */
    public String getDeviceManufacturer() {
        return Build.MANUFACTURER;
    }

    /**
     * This method is used to get the device sdk version.
     *
     * @return the the device sdk version.
     */
    public String getDeviceSdkVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * This method is used to get the application data directory
     *
     * @return the application data directory
     */
    public static String getAppDataDirectory() {
        try {
            String basePath = MainApplication.appContext.getFilesDir()
                    .getAbsolutePath();
            String appDataDirectoryPath = basePath + File.separator
                    + "DbIndexFiles";
            File file = new File(appDataDirectoryPath);
            if (file != null && !file.exists()) {
                file.mkdirs();
            }
            return appDataDirectoryPath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getDataBaseDirectory() {
        try {
            String basePath = MainApplication.appContext.getFilesDir()
                    .getAbsolutePath();
            String appDataDirectoryPath = basePath + File.separator
                    + "DbIndexFiles" + File.separator + "IVFRDb";
            File file = new File(appDataDirectoryPath);
            if (file != null && !file.exists()) {
                file.mkdirs();
            }
            return appDataDirectoryPath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    public static boolean isPermissionGranted(String permission) {
        PackageManager pm = MainApplication.appContext.getPackageManager();
        int hasPerm = pm.checkPermission(
                permission,
                MainApplication.appContext.getPackageName());
        if (hasPerm != PackageManager.PERMISSION_GRANTED) {
            return false;

        }
        return true;
    }

    public static String[] getAllPermissionsNotEnabled() {
        ArrayList<String> list = new ArrayList<String>();
        if (!isPermissionGranted("android.permission.VIBRATE")) {
            list.add("android.permission.VIBRATE");
        }
        if (!isPermissionGranted("android.permission.CAMERA")) {
            list.add("android.permission.CAMERA");
        }

        if (!isPermissionGranted("android.permission.WRITE_EXTERNAL_STORAGE")) {
            list.add("android.permission.WRITE_EXTERNAL_STORAGE");
        }
        if (!isPermissionGranted("android.permission.READ_EXTERNAL_STORAGE")) {
            list.add("android.permission.READ_EXTERNAL_STORAGE");
        }


        if (!isPermissionGranted("android.permission.INTERNET")) {
            list.add("android.permission.INTERNET");
        }
        if (!isPermissionGranted("android.permission.ACCESS_WIFI_STATE")) {
            list.add("android.permission.ACCESS_WIFI_STATE");
        }


        if (!isPermissionGranted("android.permission.RECORD_AUDIO")) {
            list.add("android.permission.RECORD_AUDIO");
        }

        if (!isPermissionGranted("android.permission.ACCESS_COARSE_LOCATION")) {
            list.add("android.permission.ACCESS_COARSE_LOCATION");
        }
        if (!isPermissionGranted("android.permission.ACCESS_FINE_LOCATION")) {
            list.add("android.permission.ACCESS_FINE_LOCATION");
        }

        if (!isPermissionGranted("android.permission.READ_PHONE_STATE")) {
            list.add("android.permission.READ_PHONE_STATE");
        }

        String[] permissions = list.toArray(new String[list.size()]);

        return permissions;
    }


}
