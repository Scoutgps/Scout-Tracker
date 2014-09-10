package com.gcscout.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

import com.gcscout.trackerdemo.App;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

public abstract class Device {
    private static final String PREFS_FILE = "device_id.xml";
    private static final String PREFS_DEVICE_ID = "device_id";
    private volatile static UUID uuid;

    public static UUID getDeviceUUID() {
        Context context = App.getInstance().getApplicationContext();
        if (uuid == null) {
            final SharedPreferences preferences = context.getSharedPreferences(PREFS_FILE, 0);
            final String id = preferences.getString(PREFS_DEVICE_ID, null);
            if (id != null)
                uuid = UUID.fromString(id);
            else {
                final String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
                try {
                    if (!"9774d56d682e549c".equals(androidId))
                        uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
                    else {
                        final String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))
                                .getDeviceId();
                        uuid = deviceId != null ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")) : UUID.randomUUID();
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                preferences.edit().putString(PREFS_DEVICE_ID, uuid.toString()).apply();
            }
        }
        return uuid;
    }

    public static long getDeviceUUIDHash() {
        return (long) getDeviceUUID().toString().hashCode() + Integer.MAX_VALUE;
    }
}
