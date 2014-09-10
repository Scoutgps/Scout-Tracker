package com.gcscout.trackerdemo;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;

import com.gcscout.db.DatabaseHelper;
import com.gcscout.networking.NetworkBasedApplication;
import com.gcscout.tracking.TrackingController;
import com.gcscout.tracking.TrackingService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationListener;

import java.util.ArrayList;

public class App extends NetworkBasedApplication implements ServiceConnection, LocationListener {
    private static TrackingController trackingController;
    private static ArrayList<OnInitializationListener> initializationListeners = new ArrayList<OnInitializationListener>();
    private static ArrayList<LocationListener> locationListeners = new ArrayList<LocationListener>();
    private static final Object initializationLocker = new Object();
    private static boolean isInitialized = false;
    private static boolean haveGps;
    private final static Handler postHandler = new Handler();
    private static Application instance;

    public static TrackingController getTrackingController() {
        return trackingController;
    }

    public static boolean haveGps() {
        return haveGps;
    }

    public static Application getInstance() {
        return instance;
    }

    public static void waitForInitialization(OnInitializationListener initializationListener) {
        synchronized (initializationLocker) {
            if (isInitialized)
                initializationListener.onInitialized();
            else
                initializationListeners.add(initializationListener);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        DatabaseHelper.initialize(getApplicationContext());

        String locationInfo = "LOCATION STATE:\n";
        locationInfo += "HAS_FEATURE_LOCATION: " + getApplicationContext()
                .getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_LOCATION) + "\n";
        locationInfo += "HAS_FEATURE_LOCATION_NETWORK: " + getApplicationContext()
                .getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK) + "\n";
        locationInfo += "HAS_FEATURE_LOCATION_GPS: " + getApplicationContext()
                .getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS) + "\n";
        LocationManager locationManager = (LocationManager) getApplicationContext()
                .getSystemService(Context.LOCATION_SERVICE);
        locationInfo += "IS_NETWORK_PROVIDER_ENABLED: " + locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER) + "\n";
        locationInfo += "IS_GPS_PROVIDER_ENABLED: " + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) + "\n";
        locationInfo += "IS_PASSIVE_PROVIDER_ENABLED: " + locationManager
                .isProviderEnabled(LocationManager.PASSIVE_PROVIDER) + "\n";
        locationInfo += "IS_GOOGLE_PLAY_SERVICE_AVAILABLE: " + (GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS);

        Log.i(locationInfo);

        haveGps = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
        if (haveGps)
            bindService(new Intent(this, TrackingService.class), this, Context.BIND_AUTO_CREATE);
        else
            onInitialized();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        trackingController = ((TrackingService.TrackingBinder) binder).getTrackingController();
        trackingController.addLocationListener(this);
        onInitialized();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        trackingController.removeLocationListener(this);
        trackingController = null;
    }

    private void onInitialized() {
        synchronized (initializationLocker) {
            postHandler.postDelayed(new Runnable() {
                public void run() {
                    isInitialized = true;
                    for (OnInitializationListener listener : initializationListeners)
                        listener.onInitialized();
                    initializationListeners.clear();
                }
            }, 2500);
        }
    }

    public static void addLocationListener(LocationListener listener) {
        locationListeners.add(listener);
    }

    @Override
    public void onLocationChanged(Location location) {
        for (LocationListener listener : locationListeners)
            listener.onLocationChanged(location);
    }

    public static void removeLocationListener(LocationListener listener) {
        locationListeners.remove(listener);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        DatabaseHelper.dispose();
    }

    public interface OnInitializationListener {
        void onInitialized();
    }
}