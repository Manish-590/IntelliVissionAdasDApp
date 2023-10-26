package com.intellivision.adas.demo.gpsinfo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

import com.intellivision.adas.demo.MainApplication;
import com.intellivision.adas.demo.logger.Category;
import com.intellivision.adas.demo.logger.VCLog;
import com.intellivision.adas.demo.ui.ScrMain;

import java.io.FileWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.LOCATION_SERVICE;

public class GpsInfo {
    private final ScrMain _screen;
    private static final String TAG = "GpsInfo";
    private static final String EMPTY_FNAME = "";
    private LocationManager locationManager;
    private OnNmeaMessageListener nmeaListener;

    private FileWriter writer = null;
    private String nmeaFilename = EMPTY_FNAME;
    public  String nmeaNewFilename = EMPTY_FNAME;
    public boolean _recording = false;

    private Pattern rmcPattern = Pattern.compile("\\$G\\wRMC,[0-9.]*,(\\w),");

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updateGpsInfo(location);
        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    public GpsInfo(ScrMain screen)
    {
        _screen = screen;
        locationManager = (LocationManager) MainApplication.appContext.getSystemService(LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            nmeaListener = new OnNmeaMessageListener() {
                @Override
                public void onNmeaMessage(String message, long timestamp) {
                    Matcher m = rmcPattern.matcher(message);
                    boolean isRmc = m.find();
                    if (isRmc) {
                        String activeStr = m.group(1);
                        if (activeStr.equals("A")) {
                            _screen.gpsActive = true;
                        } else if (activeStr.equals("V")) {
                            _screen.gpsActive = false;
                        }
                    }

                    if(_recording) {
                        if (nmeaNewFilename != EMPTY_FNAME) {
                            if (nmeaFilename != nmeaNewFilename) {
                                VCLog.error(Category.CAT_GPS_INFO, "Start writing " + nmeaNewFilename);
                                if (writer != null) {
                                    try {
                                        writer.close();
                                    } catch (Exception e) {
                                        VCLog.error(
                                                Category.CAT_GPS_INFO,
                                                TAG + ".GpsInfo() onNmeaMessage() writer.close(): Exception->"
                                                        + e.getMessage());
                                    }
                                }
                                nmeaFilename = nmeaNewFilename;
                                try {
                                    writer = new FileWriter(nmeaFilename, true);
                                    VCLog.error(Category.CAT_GPS_INFO, nmeaFilename + " is successfully created");
                                } catch (Exception e) {
                                    VCLog.error(
                                            Category.CAT_GPS_INFO,
                                            TAG + ".startRequestData().FileWriter(): Exception->"
                                                    + e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                            if (writer != null) {
                                try {
                                    writer.write(message);
                                    writer.flush();
                                } catch (Exception e) {
                                    VCLog.error(
                                            Category.CAT_GPS_INFO,
                                            TAG + ".GpsInfo() onNmeaMessage() writer.write(): Exception->"
                                                    + e.getMessage());
                                }
                            }
                        }
                    }
                }
            };
        }
    }

    public void startRequestData(){
        if (_screen.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                _screen.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(_screen, "No permission to use GPS", Toast.LENGTH_LONG).show();
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0, 0, locationListener, Looper.getMainLooper());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locationManager.addNmeaListener(nmeaListener);
            }

            if(_recording && nmeaNewFilename != EMPTY_FNAME){
                nmeaFilename = nmeaNewFilename;
                try(FileWriter _writer = new FileWriter(nmeaFilename, true)){
                    writer = _writer;
                }
                catch(Exception e){
                    VCLog.error(
                            Category.CAT_GPS_INFO,
                            TAG + ".startRequestData().FileWriter(): Exception->"
                                    + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public void stopRequestData() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locationManager.removeNmeaListener(nmeaListener);
        }
        locationManager.removeUpdates(locationListener);
        try {
            if(writer != null) {
                writer.close();
                writer = null;
            }
        }
        catch (Exception e) {
            VCLog.error(Category.CAT_GPS_INFO, TAG + ".GpsInfo() onNmeaMessage()" +
                    "writer.close(): Exception->" + e.getMessage());
        }
    }

    private void updateGpsInfo(Location location) {
        if (location == null)
            return;
        final float mpsToKmph = 3.6f;
        float speedKmph = location.getSpeed() * mpsToKmph;
        _screen.speed = speedKmph;
        _screen.bearing = location.getBearing();
        // location.getBearing() returns 0 if bearing is not available, but
        // engine expects -1 in this case.
        if (_screen.bearing == 0) _screen.bearing = -1;
    }
}
