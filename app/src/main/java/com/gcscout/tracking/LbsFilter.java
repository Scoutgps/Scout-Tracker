package com.gcscout.tracking;

import com.gcscout.tracking.Protocol.AndroidDevicePacket;

public class LbsFilter {
    private final static long MAX_INTERVAL_BETWEEN_POINTS = 2 * 60 * 1000; //2min

    public boolean NavigationFilterWork(AndroidDevicePacket oldPoint, AndroidDevicePacket newPoint) {
        return newPoint.getAccuracy() < oldPoint.getAccuracy() || newPoint.getTime() - oldPoint.getTime() > MAX_INTERVAL_BETWEEN_POINTS;
    }
}
