package com.intellivision.adas.demo.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.intellivision.adas.IVAdasErrorCode;
import com.intellivision.adas.IvAdasWrapper;
import com.intellivision.adas.demo.R;
import com.intellivision.adas.demo.camera.CameraControllerBase;
import com.intellivision.adas.demo.datamodels.IVLicenseData;
import com.intellivision.adas.demo.utilities.DeviceUtils;
import com.intellivision.adas.demo.utilities.DialogHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class ScrSplash extends Activity {
    private Timer _splashTimer;
    private SplashTimerTask _splashTimerTask;

    private final long SPLASH_TIMEOUT = (long) (1 * 1000);
    public final int REQUEST_CODE_PERMISSION = 200;
    private LinearLayout llSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        llSplash = (LinearLayout) findViewById(R.id.ll_splash);
        verifyPermissions();
    }

    public void showErrorDialog(final int errorCode) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                CameraControllerBase.stopPrecessing = true;
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        ScrSplash.this);
                builder.setTitle("Error!");
                String errMsg = IVAdasErrorCode.getErrorString(errorCode);
                builder.setMessage(errMsg)
                        .setCancelable(false)
                        .setPositiveButton("Close",
                                new OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.dismiss();
                                        CameraControllerBase.stopPrecessing = false;
                                        if (errorCode == IVAdasErrorCode.IV_ADAS_LICENSE_INVALID) {
                                            IVLicenseData.getInstance()
                                                    .setLicenseKey(null);
                                            finish();
                                        }
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();

            }
        });
    }

    private void showEnterLiceseKeyDlg() {
        llSplash.post(new Runnable() {

            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        ScrSplash.this);
                builder.setTitle("License Key");
                final EditText input = new EditText(ScrSplash.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("OK",
                        new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                String key = input.getText().toString();
                                IVLicenseData.getInstance().setLicenseKey(key);
                                int lRet = IvAdasWrapper.getInstance().setLicense(key);
                                if (lRet == IVAdasErrorCode.IV_ADAS_OK) {
                                    dialog.dismiss();
                                    verifyPermissions();
                                } else {
                                    dialog.dismiss();
                                    showErrorDialog(lRet);
                                }
                            }
                        });
                builder.setNegativeButton("Cancel",
                        new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.cancel();
                                finish();
                            }
                        });

                builder.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void showWiFiDisabledDialog() {
        {
            llSplash.post(new Runnable() {
                @Override
                public void run() {
                    OnClickListener dialogButtonClickListener = new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    ScrSplash.this.finish();
                                    IVLicenseData.getInstance().setLicenseKey("");
                                    break;
                            }
                        }
                    };
                    DialogHelper.showDialog(ScrSplash.this,
                            R.string.dlg_title_warning,
                            R.string.msg_wifi_disabled, -1, -1,
                            R.string.dlg_btn_ok, dialogButtonClickListener);
                }
            });
        }

    }

    private String readLicenseFromFile(File licenseFileName) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(licenseFileName));
            String keyFromFile = reader.readLine();
            reader.close();
            return keyFromFile;
        }
        catch (IOException e) {
            return "";
        }
    }

    private void showLicenseIfNeeded() {
        boolean isLicenseValid = false;
        // 1. Read license from file and check it
        File externalStorageDir = Environment.getExternalStorageDirectory();
        File licenseFileName = new File(externalStorageDir.getAbsolutePath() + File.separator
                + getString(R.string.app_label), "license.txt");
        String keyFromFile = readLicenseFromFile(licenseFileName);
        if (!TextUtils.isEmpty(keyFromFile)) {
            int lRet = IvAdasWrapper.getInstance().setLicense(keyFromFile);
            if (lRet == IVAdasErrorCode.IV_ADAS_OK) {
                // Save valid license from file to shared preferences
                IVLicenseData.getInstance().setLicenseKey(keyFromFile);
                String text = "License has been read from " + licenseFileName.getAbsolutePath();
                Toast.makeText(this, text, Toast.LENGTH_LONG).show();
                isLicenseValid = true;
            }
        }
        // 2. If license from file is invalid read license from shared preferences
        if (!isLicenseValid) {
            String key = IVLicenseData.getInstance().getLicenseKey();
            if (!TextUtils.isEmpty(key)) {
                int lRet = IvAdasWrapper.getInstance().setLicense(key);
                if (lRet == IVAdasErrorCode.IV_ADAS_OK) {
                    isLicenseValid = true;
                }
            }
        }
        // 3. If no valid license found ask for it
        if (!isLicenseValid) {
            showEnterLiceseKeyDlg();
        } else {
            // Valid license found, show main screen
            _scheduleTimer();
        }
    }

    /**
     * Method to schedule a timer to launch {@link ScrMain}
     */
    private void _scheduleTimer() {
        _splashTimer = new Timer();
        _splashTimerTask = new SplashTimerTask();
        _splashTimer.schedule(_splashTimerTask, SPLASH_TIMEOUT);

    }

    private class SplashTimerTask extends TimerTask {
        @Override
        public void run() {
            Intent mainIntent = new Intent(ScrSplash.this, ScrMain.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
            ScrSplash.this.finish();
        }
    }


    @TargetApi(23)
    protected void askPermissions(String permissions[]) {
        requestPermissions(permissions, REQUEST_CODE_PERMISSION);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            boolean allEnabled = true;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] < 0) {
                    allEnabled = false;
                    break;
                }
            }
            if (allEnabled) {
                showLicenseIfNeeded();
            }
        }
    }


    public void verifyPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            String permissions[] = DeviceUtils.getAllPermissionsNotEnabled();
            if (permissions.length > 0) {
                askPermissions(permissions);
            } else {
                showLicenseIfNeeded();
            }
        }
    }


}