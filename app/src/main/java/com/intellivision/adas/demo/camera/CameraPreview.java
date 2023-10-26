package com.intellivision.adas.demo.camera;

import android.content.Context;
import android.os.Build;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.intellivision.adas.demo.datamodels.Settings;
import com.intellivision.adas.demo.utilities.DeviceUtils;

/**
 * A basic camera preview class.<br>
 * Following are the responsibilities of this class;<br>
 * <li>Extends Android's {@link SurfaceView} to render camera frames.</li><br>
 * <li>Handling SurfaceHolder callbacks (surfaceCreated, surfaceChanged and surfaceDestroyed) and
 * taking appropriate action.</li><br>
 * <li>Calling {@link CameraControllerBase} methods to set custom params (autofocusMode,
 * previewSizes, autoExposureLock, displayOrientation etc).</li><br>
 * <li>Providing the display to render camera frames to {@link CameraControllerBase} once the
 * surface is created.</li><br>
 * <li>ReStarting camera preview once the surface is changed.</li><br>
 * <li>Also displays the keypoints where the object is detected while running in debug mode.</li><br>
 * 
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "CameraPreview";
    private SurfaceHolder _surfaceHolder;

    private final CameraControllerBase _cameraController;

    /**
     * Parameterized Constructor.
     * 
     * @param context
     *            the Activity context
     * @param cameraController
     *            the {@link CameraControllerBase} instance
     */
    @SuppressWarnings( "deprecation" )
    public CameraPreview( Context context, CameraControllerBase cameraController ) {
        super( context );

        _cameraController = cameraController;

        /**
         * Add a SurfaceHolder.Callback to get notified when the underlying surface is created,
         * changed and destroyed.
         */
        _surfaceHolder = getHolder( );
        _surfaceHolder.setFixedSize(Settings.getInt(Settings.I_WIDTH), Settings.getInt(Settings.I_HEIGHT));
        _surfaceHolder.addCallback( this );

        if ( DeviceUtils.getDeviceOsVersion( ) < Build.VERSION_CODES.HONEYCOMB ) {
            /**
             * deprecated setting, but required on Android versions prior to 3.0
             */
            _surfaceHolder.setType( SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS );
        }
    }

    @Override
    public void surfaceCreated( SurfaceHolder holder ) {
        /**
         * The Surface has been created, now tell the camera where to draw the preview.
         */
        //_cameraController.setPreviewDisplay( _surfaceHolder );
        //_cameraController.openCamera(holder.getSurface());
    }

    @Override
    public void surfaceDestroyed( SurfaceHolder holder ) {
    }

    @Override
    public void surfaceChanged( SurfaceHolder holder, int format, int w, int h ) {
        /**
         * If your preview can change or rotate, take care of those events here. Make sure to stop
         * the preview before resizing or reformatting it.
         */
        if ( _surfaceHolder.getSurface( ) == null ) {
            /**
             * Preview surface does not exist
             */
            return;
        }
        _cameraController.openCamera(holder.getSurface());

        /**
         * Stop preview before making changes
         */
        //_cameraController.stopPreview( );

        /**
         * Set preview size and make any resize, rotate or reformatting changes here and start
         * preview with new settings
         */
        //_cameraController.setOptimalPreviewSize( );

//        _cameraController.setExposureParams( );
//
//        _cameraController.setAutofocusMode( );
//
//        _cameraController.setDisplayOrientation( );

//        _cameraController.startPreview( );
//
//        final Handler handler = new Handler( );
//        handler.postDelayed( new Runnable( ) {
//            @Override
//            public void run() {
//                /**
//                 * So we get the autofocus when starting up - we do this on a delay, as calling it
//                 * immediately means the autofocus doesn't seem to work properly sometimes (at least
//                 * on Galaxy Nexus)
//                 */
//                //_cameraController.autofocus( );
//            }
//        }, 1000 );
    }

}