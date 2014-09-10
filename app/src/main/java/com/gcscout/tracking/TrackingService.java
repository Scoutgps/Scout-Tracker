package com.gcscout.tracking;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.gcscout.settings.Setting;
import com.gcscout.settings.Settings;
import com.gcscout.trackerdemo.Helpers;
import com.gcscout.trackerdemo.HomeActivity;
import com.gcscout.trackerdemo.Log;
import com.gcscout.trackerdemo.OnSettingChangedListener;
import com.gcscout.trackerdemo.R;
import com.gcscout.tracking.Protocol.AndroidDevicePacket;
import com.gcscout.tracking.Protocol.AndroidDevicePacketsSender;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import java.util.ArrayList;

public class TrackingService extends Service implements
        LocationListener,
        ConnectionCallbacks,
        OnConnectionFailedListener,
        TrackingController,
        OnSettingChangedListener {

    public class TrackingBinder extends Binder {
        public TrackingController getTrackingController() {
            return TrackingService.this;
        }
    }

    private final static int NOTIFICATION_ID = 404;

    private final ArrayList<LocationListener> locationListeners = new ArrayList<>();
    private final GpsFilter gpsFilter = new GpsFilter();
    private final LbsFilter lbsFilter = new LbsFilter();
    private LocationRequest request;
    private LocationClient locationClient;
    private boolean isTracking = false;
    private long lastLocationTime;
    private Location location;
    private AndroidDevicePacket lastSentPacket;
    private String deviceId;


    @Override
    public void addLocationListener(LocationListener locationListener) {
        locationListeners.add(locationListener);
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public boolean isGpsActive() {
        return SystemClock.elapsedRealtime() - lastLocationTime < 5000;
    }

    @Override
    public boolean isTracking() {
        return isTracking;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new TrackingBinder();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        location = locationClient.getLastLocation();
        if (location != null)
            onLocationChanged(location);
        locationClient.requestLocationUpdates(request, this);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        stopTracking();
        Toast.makeText(this, R.string.main_gpservices_connection_failed_alert, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        request = LocationRequest.create().setInterval(1000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        deviceId = Settings.getSetting(Settings.DeviceId);

        Settings.addSettingsChangedListener(this);

        locationClient = new LocationClient(this, this, this);

        AndroidDevicePacketsSender.Instance.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isTracking)
            stopTracking();

        AndroidDevicePacketsSender.Instance.stop();
        Settings.removeSettingsChangedListener(this);
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onLocationChanged(final Location location) {
        AndroidDevicePacket newPacket = new AndroidDevicePacket(deviceId, location);
        if (lastSentPacket != null)
            if (location.getAccuracy() < Settings.getSetting(Settings.FilterAccuracyThreshold)) {
                if (!gpsFilter.NavigationFilterWork(lastSentPacket, newPacket)) {
                    Log.i("Location received and filtered by GPS filter: " + Helpers.locationToString(location));
                    return;
                }
            } else if (!lbsFilter.NavigationFilterWork(lastSentPacket, newPacket)) {
                Log.i("Location received and filtered by LBS filter: " + Helpers.locationToString(location));
                return;
            }

        Log.i("Location received and applied: " + Helpers.locationToString(location));

        lastSentPacket = newPacket;
        this.location = location;

        lastLocationTime = SystemClock.elapsedRealtime();

        AndroidDevicePacketsSender.Instance.addPacket(newPacket);

        for (LocationListener listener : locationListeners)
            listener.onLocationChanged(location);
        updateNotification();
    }

    @Override
    public void removeLocationListener(LocationListener locationListener) {
        locationListeners.remove(locationListener);
    }

    @Override
    public void startTracking() {
        isTracking = true;
        locationClient.connect();
        lastLocationTime = SystemClock.elapsedRealtime();

        startForeground(NOTIFICATION_ID, createNotification());
    }

    @Override
    public void stopTracking() {
        isTracking = false;
        lastLocationTime = 0;
        locationClient.disconnect();

        stopForeground(true);
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, HomeActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        String noteString = "Ведется трекинг" + (location == null ? ". Данных пока не поступало" : "( " + location
                .getLongitude() + ", " + location.getLatitude() + " )");

        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notify_icon)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(noteString)
                .setWhen(location == null ? 0 : location.getTime())
                .setContentIntent(PendingIntent.getActivity(this,
                        0,
                        notificationIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT))
                .build();
    }

    private void updateNotification() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(NOTIFICATION_ID, createNotification());
    }

    public void onSettingChanged(Setting<?> setting) {
        if (setting == Settings.DeviceId)
            deviceId = Settings.getSetting(Settings.DeviceId);
    }
}