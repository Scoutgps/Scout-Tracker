package com.gcscout.trackerdemo;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gcscout.networking.NetworkStatusListener;
import com.google.android.gms.location.LocationListener;

public class TrackerFragment extends Fragment implements NetworkStatusListener, LocationListener {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tracker_fragment, container, false);

        view.findViewById(R.id.tracker_tracking_btn).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                startOrStopTracking();
            }
        });

        view.findViewById(R.id.tracker_bottom_logo).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(App.getInstance(), AboutActivity.class));
            }
        });

        ((TextView) view.findViewById(R.id.tracker_tracking_btn)).setTypeface(Fonts.RobotoBold);
        ((TextView) view.findViewById(R.id.tracker_last_location_coordinates_label)).setTypeface(Fonts.RobotoMedium);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        App.addNetworkStatusListener(this);
        if (App.haveGps())
            App.addLocationListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        updateTrackingBtnState();
        updateGPSStatusInfo();
        updateNetworkStatusInfo();
        updateLocationInfo();
    }

    @Override
    public void onStop() {
        App.removeNetworkStatusListener(this);
        if (App.haveGps())
            App.removeLocationListener(this);
        super.onStop();
    }

    private void startOrStopTracking() {
        if (!App.getTrackingController().isTracking())
            App.getTrackingController().startTracking();
        else
            App.getTrackingController().stopTracking();
        updateTrackingBtnState();
        updateGPSStatusInfo();
    }

    private void updateTrackingBtnState() {
        TextView trackingBtn = (TextView) getView().findViewById(R.id.tracker_tracking_btn);
        TextView trackingTooltip = (TextView) getView().findViewById(R.id.tracker_tracking_tooltip);

        if (!App.haveGps()) {
            trackingBtn.setVisibility(View.GONE);
            trackingTooltip.setText(R.string.tracker_no_gps_tooltip);
            trackingTooltip.setVisibility(View.VISIBLE);
            return;
        } else
            trackingTooltip.setVisibility(View.GONE);

        trackingBtn.setVisibility(View.VISIBLE);
        trackingBtn.setText(App.getTrackingController().isTracking() ? R.string.tracker_stop_tracking
                : R.string.tracker_start_tracking);
        trackingBtn
                .setBackgroundResource(App.getTrackingController().isTracking() ? R.drawable.tracker_tracking_stop_btn_selector
                        : R.drawable.tracker_tracking_start_btn_selector);
    }

    private void updateGPSStatusInfo() {
        ImageView gpsStatus = (ImageView) getView().findViewById(R.id.tracker_gps_status);

        if (!App.haveGps()) {
            gpsStatus.setImageResource(R.drawable.indicator_off);
            return;
        }

        LocationManager manager = (LocationManager) App.getInstance().getSystemService(Context.LOCATION_SERVICE);

        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (App.getTrackingController().isTracking()) {
                if (App.getTrackingController().isGpsActive())
                    gpsStatus.setImageResource(R.drawable.indicator_on);
                else
                    gpsStatus.setImageResource(R.drawable.indicator_on);
            } else
                gpsStatus.setImageResource(R.drawable.indicator_on);
        } else
            gpsStatus.setImageResource(R.drawable.indicator_off);
    }

    private void updateNetworkStatusInfo() {
        ImageView networkStatus = (ImageView) getView().findViewById(R.id.tracker_internet_status);
        if (App.isOnline())
            networkStatus.setImageResource(R.drawable.indicator_on);
        else
            networkStatus.setImageResource(R.drawable.indicator_off);
    }

    private void updateLocationInfo() {
        Location location = App.haveGps() ? App.getTrackingController().getLocation() : null;

        TextView lastLocationTime = (TextView) getView().findViewById(R.id.tracker_lastlocation_datetime);
        TextView lastLocationLongitude = (TextView) getView().findViewById(R.id.tracker_lastlocation_latitude);
        TextView lastLocationLatitude = (TextView) getView().findViewById(R.id.tracker_lastlocation_longitude);
        if (location == null) {
            String noCoordinates = getResources().getString(R.string.tracker_no_coordinates_tooltip);
            lastLocationTime.setText(noCoordinates);
            lastLocationLongitude.setText(noCoordinates);
            lastLocationLatitude.setText(noCoordinates);
        } else {
            lastLocationTime.setText(Helpers.dateToString(location.getTime()));
            lastLocationLongitude.setText(Double.toString(location.getLongitude()));
            lastLocationLatitude.setText(Double.toString(location.getLatitude()));
        }
    }

    @Override
    public void onNetworkStatusChanged(boolean isOnline) {
        updateNetworkStatusInfo();
    }

    @Override
    public void onLocationChanged(Location location) {
        updateLocationInfo();
        updateGPSStatusInfo();
    }
}
