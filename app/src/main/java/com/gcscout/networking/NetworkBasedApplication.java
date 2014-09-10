package com.gcscout.networking;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.ArrayList;

public class NetworkBasedApplication extends Application {
    private static NetworkBasedApplication instance;
    private final static ArrayList<NetworkStatusListener> networkStatusListeners = new ArrayList<>();
    private final static Object receiverLock = new Object();
    private final static BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            NetworkBasedApplication.onNetworkStatusChanged();
        }
    };

    public static boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) instance.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static void addNetworkStatusListener(NetworkStatusListener listener) {
        synchronized (receiverLock) {
            networkStatusListeners.add(listener);
            if (networkStatusListeners.size() == 1) {
                IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
                instance.registerReceiver(receiver, filter);
            }
        }
    }

    private static void onNetworkStatusChanged() {
        synchronized (receiverLock) {
            boolean isOnline = isOnline();
            for (NetworkStatusListener listener : networkStatusListeners)
                listener.onNetworkStatusChanged(isOnline);
        }
    }

    public static void removeNetworkStatusListener(NetworkStatusListener listener) {
        synchronized (receiverLock) {
            networkStatusListeners.remove(listener);
            if (networkStatusListeners.size() == 0) {
                instance.unregisterReceiver(receiver);
            }
        }
    }
}
