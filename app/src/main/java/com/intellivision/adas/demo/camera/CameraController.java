package com.intellivision.adas.demo.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.intellivision.adas.IVAdasEnumConstants.IVAdasFrameFormat;
import com.intellivision.adas.IVAdasErrorCode;
import com.intellivision.adas.IvAdasWrapper;
import com.intellivision.adas.datamodels.IVAdasImage;
import com.intellivision.adas.datamodels.IVAdasVideoData;
import com.intellivision.adas.demo.datamodels.Settings;
import com.intellivision.adas.demo.logger.Category;
import com.intellivision.adas.demo.logger.VCLog;
import com.intellivision.adas.demo.ui.ScrMain;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * Camera controller class for devices running Android <= 4.x.x
 *
 * @author Sayyad.abid
 */
@SuppressWarnings("deprecation")
public class CameraController extends CameraControllerBase
        implements ImageReader.OnImageAvailableListener {
    private int width;
    private int height;
    private int format = ImageFormat.YUV_420_888;
    private String formatStr = "ImageFormat.YUV_420_888";
    private static final int MAX_IMAGES = 2;

    private CameraManager cameraManager;
    private CameraDevice camera;
    private String cameraId;
    private CameraCharacteristics cameraCharacteristics;

    private ImageReader imageReader;
    private Surface previewSurface;
    private Surface imageSurface;
    private Surface recorderSurface;
    private CameraCaptureSession session;
    CaptureRequest previewRequest;
    CaptureRequest recordRequest;

    ByteBuffer nv21;

    private SurfaceHolder _surfaceHolder;
    private long time = 0;
    /**
     * Boolean indicating whether the app is running in continuous autofocus
     * mode. In which case, the sensor events are simply ignored.
     */
    private boolean _isContinuousAutofocusModeRunning = false;

    /**
     * This flag is required for devices on which Continous_Picture_Mode is
     * supported (found in supported mode list) but is not actually functional.
     * (Samsung galaxy nexus)
     */
    private boolean _doNotUseContinuosPictureMode;

//    private Timer _checkContinuosPictureModeSupportedTimer;
//    private CheckContinuosPictureModeSupportedTask _checkContinuosPictureModeSupportedTask;
//    private final long CHECK_CONTINUOS_PICTURE_MODE_SUPPORTED_TIMEOUT = 5 * 1000;

//    private AutoFocusMoveCallback _autofocusMoveCallback;

    private long startTime = 0;

    @Override
    public void setRecorderSurface(Surface surface) {
        recorderSurface = surface;
    }

    @Override
    public boolean findCamera(boolean frontCamera, int width, int height) {
        cameraId = null;
        cameraCharacteristics = null;
        try {
            Context context = activity.getApplicationContext();
            cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            String[] cameraIds = cameraManager.getCameraIdList();

            int lensFacing = frontCamera ? CameraCharacteristics.LENS_FACING_FRONT :
                    CameraCharacteristics.LENS_FACING_BACK;

            for (String id : cameraIds) {
                CameraCharacteristics cc = cameraManager.getCameraCharacteristics(id);
                if (cc.get(CameraCharacteristics.LENS_FACING) != lensFacing) {
                    Log.d(TAG, "Camera " + id + " is not facing " + (frontCamera ? "Front" : "Back"));
                    continue;
                }
                StreamConfigurationMap scm = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                int[] formats = scm.getOutputFormats();
                Size[] sizes = scm.getOutputSizes(ImageReader.class);

                Integer[] formatsI = new Integer[formats.length];
                for (int i = 0; i < formats.length; i++) {
                    formatsI[i] = formats[i];
                }

                StringBuilder resolutions = new StringBuilder();
                for (Size size : sizes) {
                    resolutions.append(Integer.toString(size.getWidth()) + "x" +
                            Integer.toString(size.getHeight()) + " ");
                }
                Log.d(TAG, "Camera " + id + " resolutions: " + resolutions);

                if (!Arrays.asList(formatsI).contains(format)) {
                    Log.d(TAG, "Camera " + id + " does not support " + formatStr);
                    continue;
                }

                if (!Arrays.asList(sizes).contains(new Size(width, height))) {
                    Log.d(TAG, "Camera " + id + " does not support resolution " +
                            Integer.toString(width) + "x" + Integer.toString(height));
                    continue;
                }

                cameraCharacteristics = cc;
                cameraId = id;
                break;
            }
            if (cameraId == null) {
                Log.d(TAG, "No supported camera found");
            }
            else {
                this.width = width;
                this.height = height;
                Log.d(TAG, "Found camera " + cameraId +
                        " resolution " + Integer.toString(width) + "x" + Integer.toString(height));
            }
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            VCLog.error(Category.CAT_GUI, "CameraController: openCamera: Exception->" + e.getMessage());
            e.printStackTrace();
        }
        return cameraId != null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void openCamera(Surface previewSurface) {
        try {
            closeCamera();
            this.previewSurface = previewSurface;
            if (cameraId != null) {
                cameraManager.openCamera(cameraId, new CameraDeviceStateCallback(), null);
            }
        } catch (Exception e) {
            VCLog.error(Category.CAT_GUI, "CameraController: openCamera: Exception->" + e.getMessage());
            e.printStackTrace();
            closeApp();
        }
        Log.d(TAG, "openCamera");
    }

    @Override
    public void closeCamera() {
        if (session != null) {
            session.close();
            session = null;
        }
        if (camera != null) {
            camera.close();
            camera = null;
        }
        if (imageSurface != null) {
            imageSurface.release();
            imageSurface = null;
        }
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
        if (previewSurface != null) {
            previewSurface.release();
            previewSurface = null;
        }
        Log.d(TAG, "closeCamera");
    }

    private void createCaptureSession(CameraDevice camera) {
        try {
            this.camera = camera;
            imageReader = ImageReader.newInstance(width, height, format, MAX_IMAGES);
            imageReader.setOnImageAvailableListener(this, null);
            imageSurface = imageReader.getSurface();
            List<Surface> outputs = Arrays.asList(previewSurface, recorderSurface, imageSurface);
            camera.createCaptureSession(outputs, new CaptureSessionStateCallback(), null);
        } catch (Exception e) {
            VCLog.error(Category.CAT_GUI, "CameraController: createCaptureSession: Exception->"
                + e.getMessage());
            e.printStackTrace();
            closeApp();
        }
    }

    private void startCaptureSession(CaptureRequest request) {
        try {
            session.setRepeatingRequest(request, null, null);
        } catch (Exception e) {
            VCLog.error(Category.CAT_GUI, "CameraController: startCaptureSession: Exception->"
                    + e.getMessage());
            e.printStackTrace();
            closeApp();
        }
    }

    class CameraDeviceStateCallback extends CameraDevice.StateCallback {
        @Override
        public void onOpened(CameraDevice camera) {
            Log.d(TAG, "Camera " + camera.getId() + " opened");
            createCaptureSession(camera);
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            Log.d(TAG, "Camera disconnected");
            closeApp();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Log.e(TAG, "Unable to open camera. Error " + Integer.toString(error));
            closeApp();
        }
    }

    class CaptureSessionStateCallback extends CameraCaptureSession.StateCallback {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            try {
                CameraController.this.session = session;

                CaptureRequest.Builder builder = session.getDevice().createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                builder.addTarget(previewSurface);
                builder.addTarget(imageSurface);
                setParameters(builder);
                previewRequest = builder.build();

                CaptureRequest.Builder recorderBuilder = session.getDevice().createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                recorderBuilder.addTarget(previewSurface);
                recorderBuilder.addTarget(recorderSurface);
                recorderBuilder.addTarget(imageSurface);
                setParameters(builder);
                recordRequest = recorderBuilder.build();
            } catch (Exception e) {
                VCLog.error(Category.CAT_GUI, "CameraController: CaptureSessionStateCallback: Exception->"
                        + e.getMessage());
                e.printStackTrace();
                closeApp();
            }
            startTime = System.currentTimeMillis();
            startPreview();
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {
            Log.e(TAG, "Capture session failed");
            closeApp();
        }

        private void setParameters(CaptureRequest.Builder builder) {
            Boolean aeLock = Boolean.FALSE;
            int afMode = CaptureRequest.CONTROL_AF_MODE_AUTO;
            // Camera API: video stabilization was set to ON.
            //int vsMode = CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON;
            // Camera2 API: video stabilization is set to OFF to avoid drifting output.
            int vsMode = CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF;

            boolean isAeLockSupported = cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_LOCK_AVAILABLE);
            if (isAeLockSupported) {
                builder.set(CaptureRequest.CONTROL_AE_LOCK, aeLock);
            }

            boolean isAfAutoSupported = false;
            int[] afModes = cameraCharacteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
            for (int mode : afModes) {
                if (mode == afMode) {
                    isAfAutoSupported = true;
                    break;
                }
            }
            if (isAfAutoSupported) {
                builder.set(CaptureRequest.CONTROL_AF_MODE, afMode);
            }

            boolean isVsSupported = false;
            int[] vsModes = cameraCharacteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES);
            for (int mode : vsModes) {
                if (mode == vsMode) {
                    isVsSupported = true;
                    break;
                }
            }
            if (isVsSupported) {
                builder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, vsMode);
            }

            Log.d(TAG, "CONTROL_AE_LOCK_AVAILABLE supported: " + Boolean.toString(isAeLockSupported) +
                    (isAeLockSupported ? (" set to " + Boolean.toString(aeLock)) : ""));
            Log.d(TAG, "CONTROL_AF_MODE supported: " + Boolean.toString(isAfAutoSupported) +
                    (isAfAutoSupported ? (" set to " + Integer.toString(afMode)) : ""));
            Log.d(TAG, "VIDEO_STABILIZATION_MODE supported: " + Boolean.toString(isVsSupported) +
                    (isVsSupported ? (" set to " + Integer.toString(vsMode)) : ""));
        }
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        Image image = reader.acquireLatestImage();
        if (image != null) {
            ByteBuffer nv21Buf = convertImageToNv21(image);
            long ts = image.getTimestamp();
            image.close();
            if (nv21Buf != null) {
                processNv21(nv21Buf);
            }
            image.close();
        }
    }

    private ByteBuffer convertImageToNv21(Image image) {
        ByteBuffer output = null;
        int imageFormat = image.getFormat();
        if (imageFormat == ImageFormat.YUV_420_888) {
            final int Y = 0;
            final int U = 1;
            final int V = 2;
            Image.Plane[] planes = image.getPlanes();
            ByteBuffer y = planes[Y].getBuffer();
            ByteBuffer u = planes[U].getBuffer();
            ByteBuffer v = planes[V].getBuffer();

            int nv21Size = 3 * width * height / 2;
            if (nv21 == null || nv21.capacity() != nv21Size) {
                nv21 = ByteBuffer.allocate(nv21Size);
            }

            // Hack: if YUV_420_888 image is actually NV21 image represented as YUV_420_888,
            // then simple conversion to NV21 is possible.
            if (y.limit() == width * height &&
                    u.limit() == (width * height / 2 - 1) &&
                    v.limit() == (width * height / 2 - 1) &&
                    planes[U].getRowStride() == width &&
                    planes[U].getPixelStride() == 2 &&
                    planes[V].getRowStride() == width &&
                    planes[V].getPixelStride() == 2) {
                nv21.rewind();
                nv21.put(y);
                nv21.put(v);
                nv21.put(u.get(width * height / 2 - 2));
                output = nv21;
            } else {
                int s = IvAdasWrapper.convertToNv21(width, height,
                        y, planes[Y].getPixelStride(), planes[Y].getRowStride(),
                        u, planes[U].getPixelStride(), planes[U].getRowStride(),
                        v, planes[V].getPixelStride(), planes[V].getRowStride(),
                        nv21);
                if (s != IVAdasErrorCode.IV_ADAS_OK) {
                    Log.e(TAG, "convertToNv21() error code " + s);
                } else {
                    output = nv21;
                }
            }
        } else {
            Log.e(TAG, "Unsupported image format " + Integer.toString(imageFormat));
        }
        return output;
    }

    private void processNv21(ByteBuffer nv21) {
        if (!stopPrecessing) {
            try {
                long curTime = System.currentTimeMillis();

                ByteBuffer imageData;  // Initialize image data to appropriate byte buffer.
                int width = 1280;  // Set to appropriate value.
                int height = 720;  // Set to appropriate value.
                int format = IVAdasFrameFormat.IVFFNV21;  // ADAS engine supports NV21 only.
                long timestampMs = 1425654; // This is just example of time, use appropriate timestamps in ms.

                width = this.width;
                height = this.height;
                int previewFormat = ImageFormat.NV21;
                imageData = nv21;
                timestampMs = time;
                if (previewFormat == ImageFormat.NV21) {
                    IVAdasImage image = new IVAdasImage(width, height, imageData, format);
                    IVAdasVideoData videoData = new IVAdasVideoData(timestampMs, image);
                    time = time + curTime - startTime;
                    startTime = curTime;
                    processPreviewFrame(videoData);
                } else {
                    Log.e(TAG, "Frame format " + format + " is not supported");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void closeApp() {
        // TODO: close the app due to camera error
    }


//    @Override
//    public void closeCamera() {
//        try {
//            /**
//             * Cancel any pending autofocus cycles
//             */
//            _isContinuousAutofocusModeRunning = false;
//            //_camera.cancelAutoFocus();
//        } catch (Exception e) {
//            VCLog.error(Category.CAT_CONTROLLER,
//                    "CameraController: closeCamera: Exception while cancelAutofocus->"
//                            + e.getMessage());
//            e.printStackTrace();
//        }
//
//        try {
//            /**
//             * Reset the preview callback so that no new frames will be
//             * received.
//             */
//            //_camera.setPreviewCallback(null);
//        } catch (Exception e) {
//            VCLog.error(Category.CAT_CONTROLLER,
//                    "CameraController: closeCamera: Exception while resetting preview callback->"
//                            + e.getMessage());
//            e.printStackTrace();
//        }
//
//        try {
//            /**
//             * Stop the preview
//             */
//            //_camera.stopPreview();
//        } catch (Exception e) {
//            VCLog.error(Category.CAT_CONTROLLER,
//                    "CameraController: closeCamera: Exception while stopping preview->"
//                            + e.getMessage());
//            e.printStackTrace();
//        }
//
//        try {
//            /**
//             * Release the camera
//             */
//            //_camera.release();
//        } catch (Exception e) {
//            VCLog.error(Category.CAT_CONTROLLER,
//                    "CameraController: closeCamera: Exception while releasing camera->"
//                            + e.getMessage());
//            e.printStackTrace();
//        }
//        _cancelCheckContinuosPictureModeSupportedTimer();
//    }

    @Override
    public void startPreview() {
        startCaptureSession(previewRequest);
    }

    @Override
    public void startRecording() {
        startCaptureSession(recordRequest);
    }

//    @Override
//    public void setExposureParams() {
//
//        //Parameters params = _camera.getParameters();
//        if (_parameters.isAutoExposureLockSupported()) {
//            _parameters.setAutoExposureLock(false);
//        }
////        _camera.setParameters(_parameters);
//    }
//
//    @Override
//    public void setAutofocusMode() {
//        //Parameters params = _camera.getParameters();
//        if (_parameters.getSupportedFocusModes()
//                .contains(Parameters.FOCUS_MODE_AUTO)) {
//            _parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
//        }
//
//        if (_parameters.isVideoStabilizationSupported()) {
//            _parameters.setVideoStabilization(true);
//        }
////        _camera.setParameters(_parameters);
//    }

    @Override
    public void setDisplayOrientation() {
        /*
         * Display display = ( (WindowManager)
         * MainApplication.appContext.getSystemService( Context.WINDOW_SERVICE )
         * ).getDefaultDisplay( ); switch ( display.getRotation( ) ) { case
         * Surface.ROTATION_0: _camera.setDisplayOrientation( 90 ); break;
         *
         * case Surface.ROTATION_90: case Surface.ROTATION_180: break;
         *
         * case Surface.ROTATION_270: _camera.setDisplayOrientation( 180 );
         * break; }
         */
        if (Settings.getBoolean(Settings.B_REVERSE_LANDSCAPE)) {
//            _camera.setDisplayOrientation(0);
        }
    }

    // Note: autofocusing in case of accelerometer event has been commented out during
    // Camera -> Camera2 refactoring.
    @Override
    public synchronized void autofocus() {
//        if (_isContinuousAutofocusModeRunning) {
//            /**
//             * Ignore if continuous autofocus is running.
//             */
//            return;
//        }
//        if (!autofocusing) {
//            try {
//                /**
//                 * This call is required to remove any pending autofocus
//                 * callback events.
//                 */
////                _camera.cancelAutoFocus();
//            } catch (Exception e) {
//                VCLog.error(Category.CAT_CONTROLLER,
//                        "CameraController: autofocus: Exception while cancelling autofocus->"
//                                + e.getMessage());
//                e.printStackTrace();
//            }
//            try {
//                autofocusing = true;
//                isFocused = false;
//                startCancelAutofocusTimer();
//                try {
//                    /**
//                     * Callback to be received when running in continuous
//                     * autofocus mode.
//                     */
//                    _autofocusMoveCallback = new AutoFocusMoveCallback() {
//                        @Override
//                        public void onAutoFocusMoving(boolean start,
//                                                      Camera camera) {
//                            _cancelCheckContinuosPictureModeSupportedTimer();
//                        }
//                    };
//
////                    _camera.setAutoFocusMoveCallback(_autofocusMoveCallback);
//                } catch (Throwable e) {
//                    VCLog.debug(Category.CAT_CONTROLLER,
//                            "CameraController: Throwable: ->" + e.getMessage());
//                    e.printStackTrace();
//                }
////                _camera.autoFocus(_autoFocusCallback);
//            } catch (Exception e) {
//                VCLog.error(Category.CAT_CONTROLLER,
//                        "CameraController: autofocus: Exception while autofocusing camera->"
//                                + e.getMessage());
//                e.printStackTrace();
//                autofocusing = false;
//            }
//        }
    }

//    /**
//     * Callback to be received when autofocus is complete
//     */
//    private AutoFocusCallback _autoFocusCallback = new AutoFocusCallback() {
//        public void onAutoFocus(boolean autoFocusSuccess, Camera arg1) {
//            autofocusing = false;
//            isFocused = autoFocusSuccess;
//            _cancelCheckContinuosPictureModeSupportedTimer();
//
//            if (autoFocusSuccess && !_doNotUseContinuosPictureMode) {
//                Parameters params = null; //_camera.getParameters();
//                if (params != null) {
//                    if (params.getSupportedFocusModes().contains(
//                            Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
//                        params.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//                    }
////                    _camera.setParameters(params);
//                }
//                _isContinuousAutofocusModeRunning = true;
//
//                AccelerometerSensorManager.getInstance().stop();
//                try {
//                    _autofocusMoveCallback = new AutoFocusMoveCallback() {
//                        @Override
//                        public void onAutoFocusMoving(boolean start,
//                                                      Camera camera) {
//                            _cancelCheckContinuosPictureModeSupportedTimer();
//                        }
//                    };
////                    _camera.setAutoFocusMoveCallback(_autofocusMoveCallback);
//                } catch (Throwable e) {
//                    _doNotUseContinuosPictureMode = true;
//                    _isContinuousAutofocusModeRunning = false;
//                }
//                _startCheckContinuosPictureModeSupportedTimer();
//            }
//        }
//    };

//    /**
//     * This class extends {@link TimerTask} and is used to check if continuous
//     * autofocus mode is actually supported on device. Since on some devices
//     * even if we get the continuous autofocus mode in list of supported modes,
//     * it is not functional.
//     *
//     * @author Sayyad.abid
//     */
//    private class CheckContinuosPictureModeSupportedTask extends TimerTask {
//        @Override
//        public void run() {
//            _doNotUseContinuosPictureMode = true;
//            _isContinuousAutofocusModeRunning = false;
//            setAutofocusMode();
//            AccelerometerSensorManager.getInstance().start();
//        }
//    }
//
//    /**
//     * Method to start a timer to check if continuous autofocus mode is actually
//     * supported.
//     */
//    private void _startCheckContinuosPictureModeSupportedTimer() {
//        _cancelCheckContinuosPictureModeSupportedTimer();
//        try {
//            _checkContinuosPictureModeSupportedTimer = new Timer();
//            _checkContinuosPictureModeSupportedTask = new CheckContinuosPictureModeSupportedTask();
//            _checkContinuosPictureModeSupportedTimer.schedule(
//                    _checkContinuosPictureModeSupportedTask,
//                    CHECK_CONTINUOS_PICTURE_MODE_SUPPORTED_TIMEOUT);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Method to cancel a previously set timer to check if continuous autofocus
//     * mode is actually supported.
//     */
//    private void _cancelCheckContinuosPictureModeSupportedTimer() {
//        if (_checkContinuosPictureModeSupportedTimer != null) {
//            try {
//                _checkContinuosPictureModeSupportedTimer.cancel();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            _checkContinuosPictureModeSupportedTimer = null;
//        }
//
//        if (_checkContinuosPictureModeSupportedTask != null) {
//            try {
//                _checkContinuosPictureModeSupportedTask.cancel();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            _checkContinuosPictureModeSupportedTask = null;
//        }
//    }

    @Override
    public void setActivity(ScrMain act) {
        activity = act;

    }

}