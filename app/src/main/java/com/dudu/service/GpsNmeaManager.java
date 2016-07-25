package com.dudu.service;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.LocationManager;

import com.dudu.commonlib.CommonLib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static android.support.v4.content.PermissionChecker.checkSelfPermission;

/**
 * Created by Robi on 2016-04-05 16:43.
 */
public class GpsNmeaManager {

    private static GpsNmeaManager gpsNmeaManager;

    private LocationManager locationManager;

    private Logger logger;
    GpsStatus.NmeaListener nmeaListener = new GpsStatus.NmeaListener() {
        @Override
        public void onNmeaReceived(long timestamp, String nmea) {

            logger.debug("gpsNmea timestamp:{}, nmea:{}", timestamp, nmea);
        }
    };
    private boolean isAdd = false;

    public GpsNmeaManager() {

        logger = LoggerFactory.getLogger("gps_nmea");
    }

    public static GpsNmeaManager getInstance() {
        if (gpsNmeaManager == null) {
            gpsNmeaManager = new GpsNmeaManager();
        }
        return gpsNmeaManager;
    }


    public void addGpsNmeaListener() {


        if (isAdd) {
            return;
        }
        if (checkSelfPermission(CommonLib.getInstance().getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationManager == null) {
                locationManager = (LocationManager) CommonLib.getInstance().getContext().getSystemService(Context.LOCATION_SERVICE);
            }
            isAdd = true;
            locationManager.addNmeaListener(nmeaListener);
            logger.debug("addGpsNmeaListener");
        }

    }

    public void removeGpsNmeaListener() {


        if (locationManager != null) {
            locationManager.removeNmeaListener(nmeaListener);
            isAdd = false;
            logger.debug("removeGpsNmeaListener");
        }
    }
}
