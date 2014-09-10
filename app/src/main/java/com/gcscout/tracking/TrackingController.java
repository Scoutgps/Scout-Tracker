package com.gcscout.tracking;

import android.location.Location;

import com.google.android.gms.location.LocationListener;

public interface TrackingController {
    boolean isGpsActive();

    boolean isTracking();

    void startTracking();

    void stopTracking();

    Location getLocation();

    void addLocationListener(LocationListener locationListener);

    void removeLocationListener(LocationListener locationListener);
}
