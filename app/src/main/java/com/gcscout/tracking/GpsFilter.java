package com.gcscout.tracking;

import com.gcscout.settings.Settings;
import com.gcscout.tracking.Protocol.AndroidDevicePacket;
import com.gcscout.tracking.Protocol.TransferHelpers;

public class GpsFilter {
    private byte frequency;

    public byte getFrequency() {
        return frequency;
    }

    public void setFrequency(byte value) {
        frequency = value;
    }

    public GpsFilter() {
        frequency = 1;
    }

    private final static byte NavigationFilterSpeedThrehold_Special = 4;
    private final static byte NavigationFilterSpeedThreshold_Detail = 8;
    private final static byte NavigationFilterSpeedThreshold_Minimal = 12;
    private final static byte NavigationFilterSpeedThreshold_Standart = 20;

    public boolean NavigationFilterWork(AndroidDevicePacket oldPoint, AndroidDevicePacket newPoint) {
        byte minimalSpeed = NavigationFilterSpeedThreshold_Standart;
        long timeDelta = newPoint.getTime() - oldPoint.getTime();

        int detalization = Settings.getSetting(Settings.GpsFilterDetalization);
        if (detalization > NavigationFilterSpeedThreshold_Minimal)
            minimalSpeed = NavigationFilterSpeedThrehold_Special;
        else if (detalization > NavigationFilterSpeedThreshold_Standart)
            minimalSpeed = NavigationFilterSpeedThreshold_Minimal;
        else if (detalization > NavigationFilterSpeedThreshold_Detail)
            minimalSpeed = NavigationFilterSpeedThreshold_Standart;
        else
            minimalSpeed = NavigationFilterSpeedThreshold_Detail;

        if (newPoint.getSpeed() < minimalSpeed)
            newPoint.setSpeed(0);

        if (oldPoint.getSpeed() == 0 && newPoint.getSpeed() == 0) {
            newPoint.setLatitude(oldPoint.getLatitude());
            newPoint.setLongitude(oldPoint.getLongitude());
            newPoint.setCourse(oldPoint.getCourse());
            newPoint.setSpeed(oldPoint.getSpeed());
        }

        if (oldPoint.getSpeed() > 0 && newPoint.getSpeed() == 0)
            return true;

        if (oldPoint.getSpeed() == 0 && newPoint.getSpeed() != 0)
            return true;

        if ((TransferHelpers.toDotNetTicks(newPoint.getTime()) - TransferHelpers.toDotNetTicks(oldPoint.getTime())) / 60000 >= frequency)
            return true;

        double intV0 = (newPoint.getLongitude() - oldPoint.getLongitude()) * 10000;
        if (intV0 < 0)
            intV0 = -intV0;
        double factor = (float) (1.11 * Math.cos(oldPoint.getLatitude()));

        intV0 = intV0 * factor;

        if (intV0 > NavigationOptions.NavigationPlacestablezoneMin.getId() * timeDelta)
            return true;

        double intV1 = (newPoint.getLatitude() - oldPoint.getLatitude()) * 10000;

        if (intV1 < 0)
            intV1 = -intV1;
        intV1 = (float) (intV1 * 1.11);
        if (intV1 > NavigationOptions.NavigationPlacestablezoneMin.getId() * timeDelta)
            return true;

        intV0 = (float) Math.sqrt(intV0 * intV0 + intV1 * intV1);

        if (detalization > NavigationFilterSpeedThreshold_Minimal)
            intV1 = NavigationOptions.NavigationPlacestablezoneSpecial.getId();
        else if (detalization > NavigationFilterSpeedThreshold_Standart)
            intV1 = NavigationOptions.NavigationPlacestablezoneMin.getId();
        else if (detalization > NavigationFilterSpeedThreshold_Detail)
            intV1 = NavigationOptions.NavigationPlacestablezoneStd.getId();
        else
            intV1 = NavigationOptions.NavigationPlacestablezoneDtl.getId();

        intV1 = intV1 * timeDelta;

        if (intV0 > intV1)
            return true;

        if (timeDelta == 1) {
            intV0 = oldPoint.getCourse() - newPoint.getCourse();
            if (intV0 < 0)
                intV0 = -intV0;
            if (intV0 > 180)
                intV0 = 360 - intV0;
            intV0 *= newPoint.getSpeed() * 10;

            if (detalization > NavigationFilterSpeedThreshold_Minimal)
                intV1 = NavigationOptions.NavigationPlacestablezoneSpecial.getId();
            else if (detalization > NavigationFilterSpeedThreshold_Standart)
                intV1 = NavigationOptions.NavigationPlacestablezoneMin.getId();
            else if (detalization > NavigationFilterSpeedThreshold_Detail)
                intV1 = NavigationOptions.NavigationPlacestablezoneStd.getId();
            else
                intV1 = NavigationOptions.NavigationPlacestablezoneDtl.getId();

            if (oldPoint.getSpeed() == 0)
                intV1 *= 2;
            return intV0 > intV1;
        }
        return true;
    }

    private enum NavigationOptions {

        NavigationPlacestablezoneSpecial(50),

        NavigationPlacestablezoneDtl(90),

        NavigationPlacestablezoneStd(700),

        NavigationPlacestablezoneMin(900),

        NavigationCoursestablezoneSpecial(1500),

        NavigationCoursestablezoneDtl(2000),

        NavigationCoursestablezoneStd(3000),

        NavigationCoursestablezoneMin(5000);

        NavigationOptions(float id) {
            this.id = id;
        }

        private float id;

        public float getId() {
            return id;
        }
    }
}
