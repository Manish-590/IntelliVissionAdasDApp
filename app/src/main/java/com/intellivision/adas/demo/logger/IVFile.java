package com.intellivision.adas.demo.logger;

import java.io.File;

import android.os.Environment;
import android.text.TextUtils;

import com.intellivision.adas.demo.MainApplication;

/**
 * This is base class for all file operations.
 * 
 */
public class IVFile {
    private static String BASE_FOLDER_ON_SD_CARD;

    /**
     * Directory name for storing all files of application logger
     */
    public static final String LOGGER = "Logger";

    public static final String PREVIEW_IMAGES = "PreviewImages";

    public static void setBaseFolderNameOnSdCard( String baseFolderName ) {
        BASE_FOLDER_ON_SD_CARD = baseFolderName;
    }

    /**
     * This function creates platform specific absolute path. If path doesn't exists then creates
     * directory with that path.
     * 
     * @param directoryName
     *            Folder name where file need to be stored
     * @return Absolute file path which is platform specific, null if error occurs.
     * */
    public static String getSdCardFilePath( String directoryName ) {

        String filePath = null;
        try {

            if ( TextUtils.isEmpty( BASE_FOLDER_ON_SD_CARD ) ) {
                return null;
            }

            File sdCard = Environment.getExternalStorageDirectory( );
            String basePath = sdCard.getAbsolutePath( ) + File.separator + BASE_FOLDER_ON_SD_CARD + File.separator;

            File basedir = new File( basePath );
            if ( !basedir.isDirectory( ) ) {
                basedir.mkdir( );
            }

            filePath = basePath + directoryName;
            File dir = new File( filePath );
            if ( !dir.isDirectory( ) ) {
                dir.mkdir( );
            }

        } catch ( Exception e ) {
            e.printStackTrace( );
            return null;
        }
        return filePath;
    }

    /**
     * This function creates platform specific absolute path. If path doesn't exists then creates
     * directory with that path.
     * 
     * @param directoryName
     *            Folder name where file need to be stored
     * @return Absolute file path which is platform specific, null if error occurs.
     * */
    public static String getFilePath( String directoryName ) {

        String filePath = null;
        try {

            String basePath = "/data/data/" + MainApplication.appContext.getPackageName( );

            filePath = basePath + File.separator + directoryName;
            File dir = new File( filePath );
            if ( !dir.isDirectory( ) ) {
                dir.mkdir( );
            }

        } catch ( Exception e ) {
            e.printStackTrace( );
            return null;
        }

        return filePath;
    }
}