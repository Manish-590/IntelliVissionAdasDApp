package com.intellivision.adas.demo.camera;

import android.media.CamcorderProfile;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.view.Surface;

import com.intellivision.adas.demo.R;
import com.intellivision.adas.demo.datamodels.Settings;
import com.intellivision.adas.demo.gpsinfo.GpsInfo;
import com.intellivision.adas.demo.logger.Category;
import com.intellivision.adas.demo.logger.VCLog;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VideoRecorder{
    /**
     * Directory name for storing video records
     */
    public static String VIDEO_DIR = "";

    /**
     * Prefix for storing video records
     */
    public static final String FNAME_PREFIX = "";

    /**
     * Max video record size.
     * If max file size is reached recording to new file is started.
     */
    public static int MAX_FILE_SIZE = 110 * 1024 * 1024;

    /**
     * Max number files to store.
     * Last RECORDS_NUMBER records are stored.
     * Previous records are removed by application.
     */
    public static final int RECORDS_NUMBER = 10;

    private MediaRecorder _recorder;
    private Surface surface;
    private CameraControllerBase _cameraController;
    boolean _recording = false;

    private String [] _filenames = new String[RECORDS_NUMBER];
    private int _filenamesSize  = 0;
    private int _currentFilenamesIndex = 0;

    private final GpsInfo _gpsInfo;

    public VideoRecorder(CameraControllerBase cameraController, GpsInfo gpsInfo) {
        if (cameraController == null) {
            VCLog.error(
                    Category.CAT_RECORDER,
                    "null cameraController pointer");
        }

        if (cameraController != null && cameraController.activity != null) {
            VIDEO_DIR = cameraController.activity.getResources().getString(R.string.app_label);
        }

        _gpsInfo = gpsInfo;

        _cameraController = cameraController;
        _recording = false;

        surface = createSurface();
        cameraController.setRecorderSurface(surface);

        _recorder = new MediaRecorder();

        MediaRecorder.OnInfoListener listener = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // On Android versions prior to 8.0 _recorder.setNextOutputFile() is not supported.
            // As a workaround recording is reset and started again to a new file.
            listener = new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mr, int what, int extra) {
                    if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
                        try {
                            // Stop current recording
                            _recorder.reset();
                            _recording = false;
                            if (_gpsInfo != null) {
                                _gpsInfo._recording = false;
                            }

                            // Start new recording
                            setParams();
                            _recorder.prepare();
                            _recorder.start();
                            _recording = true;
                            if (_gpsInfo != null) {
                                _gpsInfo._recording = true;
                            }
                        } catch (Exception e) {
                            VCLog.error(
                                    Category.CAT_RECORDER,
                                    "MediaRecorder.OnInfoListener.onInfo(): Exception->"
                                            + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            };
        } else {
            listener = new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mr, int what, int extra) {
                    if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_APPROACHING) {
                        String mediaFilePath = getNextFileName();
                        String nmeaFilePath = mediaFilePath.replace(".mp4", ".nmea");
                        File mediaFile = new File(mediaFilePath);

                        //Log.e("RecordActivity",mediaFilePath);
                        try {
                            RandomAccessFile f = new RandomAccessFile(mediaFile, "rw");
                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    _recorder.setNextOutputFile(f.getFD());
                                }
                                if (_gpsInfo != null) {
                                    _gpsInfo.nmeaNewFilename = nmeaFilePath;
                                }

                            } finally {
                                f.close();
                            }
                        } catch (Exception e) {
                            VCLog.error(
                                    Category.CAT_RECORDER,
                                    "MediaRecorder.OnInfoListener.onInfo(): Exception->"
                                            + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            };
        }
        _recorder.setOnInfoListener(listener);
    }

    private Surface createSurface() {
        surface = MediaCodec.createPersistentInputSurface();
        // Create, prepare and release recorder to initialize surface.
        _recorder = new MediaRecorder();
        String mediaFileName = setParams();
        try {
            _recorder.prepare();
        } catch(Exception e) {
            VCLog.error(Category.CAT_RECORDER,
                "VideoRecorder.createSurface(): Exception->" + e.getMessage());
            e.printStackTrace();
        }
        _recorder.release();
        _recorder = null;

        // Delete empty media file.
        File f = new File(mediaFileName);
        if(f.exists()) {
            f.delete();
        }
        _filenamesSize = 0;
        _currentFilenamesIndex = 0;

        return surface;
    }

    private String getNextFileName()  {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), VIDEO_DIR);
        if (!mediaStorageDir.exists()){
            mediaStorageDir.mkdirs();
        }
        Date date= new java.util.Date();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(date.getTime());
        String mediaFilePath = mediaStorageDir.getPath() + File.separator +
                FNAME_PREFIX + timeStamp + ".mp4";

        VCLog.info(Category.CAT_RECORDER, "start record video to file " + mediaFilePath);

        if(_filenamesSize == RECORDS_NUMBER)
        {
            String fnameToRemove = _filenames[_currentFilenamesIndex];
            File f = new File(fnameToRemove);
            if(f.exists()) {
                f.delete();
                VCLog.info(Category.CAT_RECORDER, "File " + fnameToRemove + " is removed");
            }
            else
            {
                VCLog.info(Category.CAT_RECORDER, "File " + fnameToRemove + " doesn't exist");
            }
            fnameToRemove = fnameToRemove.replace(".mp4", ".nmea");
            f= new File(fnameToRemove);
            if(f.exists()) {
                f.delete();
                VCLog.info(Category.CAT_RECORDER, "File " + fnameToRemove + " is removed");
            }
            else
            {
                VCLog.info(Category.CAT_RECORDER, "File " + fnameToRemove + " doesn't exist");
            }
        }
        else
        {
            _filenamesSize++;
        }
        _filenames[_currentFilenamesIndex] = mediaFilePath;
        _currentFilenamesIndex++;
        _currentFilenamesIndex = _currentFilenamesIndex % RECORDS_NUMBER;

        return mediaFilePath;
    }

    private String setParams() {
        _recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        _recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);

        _recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
        int origWidth = Settings.getInt(Settings.I_WIDTH);
        int origHeight = Settings.getInt(Settings.I_HEIGHT);
        _recorder.setVideoSize(origWidth, origHeight);

        int maxFileSize = MAX_FILE_SIZE;
        maxFileSize = (int)(Settings.getFloat(Settings.F_VIDEO_FILE_SIZE) * 1024 * 1024);
        _recorder.setMaxFileSize(maxFileSize);

        String mediaFilePath = getNextFileName();
        String nmeaFilePath = mediaFilePath.replace(".mp4", ".nmea");
        _recorder.setOutputFile(mediaFilePath);
        _recorder.setInputSurface(surface);

        if (_gpsInfo != null) {
            _gpsInfo.nmeaNewFilename = nmeaFilePath;
        }

        return mediaFilePath;
    }

    public int start() {
        if(!_recording) {
            try {
                setParams();
                _cameraController.startRecording();
                _recorder.prepare();
                _recorder.start();
                _recording = true;
                if(_gpsInfo != null)
                {
                    _gpsInfo._recording = true;
                }
                return 1;
            } catch (Exception e) {
                // Camera is not available (in use or does not exist)
                VCLog.error(
                        Category.CAT_RECORDER,
                        "start(): Exception->"
                                + e.getMessage());
                e.printStackTrace();
            }
        }
        return -1;
    }

    public int stop()
    {
        if(_recording) {
            _recorder.stop();
            _recording = false;
            if(_gpsInfo != null)
            {
                _gpsInfo._recording = false;
            }
            _cameraController.startPreview();
            return 1;
        }
        return -1;
    }

    public int release() {
        if (_recording) {
            stop();
        }
        if (_recorder != null) {
            _recorder.reset();
            _recorder.release();
            _recorder = null;
        }
        return 1;
    }
}
