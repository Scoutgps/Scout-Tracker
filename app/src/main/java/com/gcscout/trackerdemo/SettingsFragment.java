package com.gcscout.trackerdemo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.gcscout.settings.Settings;

public class SettingsFragment extends Fragment {
    private static boolean isChanging = false;
    private static String deviceId;
    private static String scoutServerAddress;
    private static String scoutServerPort;
    private static int journalSize;
    private static boolean isPacketsSendByInterval;
    private static long packetsSendInterval;
    private static int packetsSendThreshold;
    private static int filterAccuracyThreshold;
    private static int gpsFilterDetalization;

    public static boolean isChangesValid() {
        return Settings.DeviceId.checkValue(deviceId) && Settings.ScoutServerAddress
                .checkValue(scoutServerAddress) && checkPortString(scoutServerPort) && Settings.JournalSize
                .checkValue(journalSize) && Settings.PacketsSendInterval.checkValue(packetsSendInterval) && Settings.PacketsSendThreshold
                .checkValue(packetsSendThreshold) && Settings.FilterAccuracyThreshold
                .checkValue(filterAccuracyThreshold) && Settings.GpsFilterDetalization
                .checkValue(gpsFilterDetalization);
    }

    public static boolean haveChanges() {
        return isChanging && !(deviceId.equals(Settings.getSetting(Settings.DeviceId)) && scoutServerAddress
                .equals(Settings.getSetting(Settings.ScoutServerAddress)) && scoutServerPort.equals(Settings
                .getSetting(Settings.ScoutServerPort)
                .toString()) && journalSize == Settings.getSetting(Settings.JournalSize) && isPacketsSendByInterval == Settings
                .getSetting(Settings.PacketsSendMode)
                .equals(Settings.PACKETS_SEND_MODE_BY_INTERVAL) && packetsSendInterval == Settings
                .getSetting(Settings.PacketsSendInterval) && packetsSendThreshold == Settings
                .getSetting(Settings.PacketsSendThreshold)) && filterAccuracyThreshold == Settings
                .getSetting(Settings.FilterAccuracyThreshold) && gpsFilterDetalization == Settings
                .getSetting(Settings.GpsFilterDetalization);
    }

    public static void cancelChanges() {
        isChanging = false;
    }

    private static boolean checkPortString(String portString) {
        try {
            return Settings.ScoutServerPort.checkValue(Integer.parseInt(portString));
        } catch (Throwable ex) {
            return false;
        }
    }

    private int toSeekBarValue(long value, long minValue) {
        return (int) (value - minValue);
    }

    private long fromSeekBarValue(int value, long minValue) {
        return value + minValue;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, container, false);

		/*((EditText) view.findViewById(R.id.settings_device_id)).setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
			public void onFocusChange(View v, boolean hasFocus) {
				deviceId = ((EditText) v).getText().toString();
				updateDeviceId();
			}
		});*/
        ((EditText) view.findViewById(R.id.settings_device_id)).setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                return true;
            }
        });

        ((EditText) view.findViewById(R.id.settings_scout_rx_server_address))
                .setOnFocusChangeListener(new OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        scoutServerAddress = ((EditText) v).getText().toString();
                        updateScoutServerAddress();
                    }
                });

        ((EditText) view.findViewById(R.id.settings_scout_rx_server_port))
                .setOnFocusChangeListener(new OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        scoutServerPort = ((EditText) v).getText().toString();
                        updateScoutServerPort();
                    }
                });

        ((TextView) view.findViewById(R.id.settings_journal_min_size)).setText(Settings.JOURNAL_MIN_SIZE + " дн");
        ((TextView) view.findViewById(R.id.settings_journal_max_size)).setText(Settings.JOURNAL_MAX_SIZE + " дн");
        SeekBar journalSizeSeekBar = (SeekBar) view.findViewById(R.id.settings_journal_size);
        journalSizeSeekBar.setMax(toSeekBarValue(Settings.JOURNAL_MAX_SIZE, Settings.JOURNAL_MIN_SIZE));
        journalSizeSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                journalSize = (int) fromSeekBarValue(progress, Settings.JOURNAL_MIN_SIZE);
                updateJournalSize();
            }
        });

        view.findViewById(R.id.settings_packets_send_mode).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                isPacketsSendByInterval = !v.isSelected();
                updatePacketsSendModeLayout();
            }
        });

        ((TextView) view.findViewById(R.id.settings_packets_min_send_interval)).setText(Helpers
                .msToMinAndSec(Settings.PACKETS_MIN_SEND_INTERVAL));
        ((TextView) view.findViewById(R.id.settings_packets_max_send_interval)).setText(Helpers
                .msToMinAndSec(Settings.PACKETS_MAX_SEND_INTERVAL));
        SeekBar packetsSendIntervalSeekBar = (SeekBar) view.findViewById(R.id.settings_packets_send_interval);
        packetsSendIntervalSeekBar.setMax(toSeekBarValue(Settings.PACKETS_MAX_SEND_INTERVAL,
                Settings.PACKETS_MIN_SEND_INTERVAL));
        packetsSendIntervalSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                packetsSendInterval = (int) fromSeekBarValue(progress, Settings.PACKETS_MIN_SEND_INTERVAL);
                updatePacketsSendInterval();
            }
        });

        ((TextView) view.findViewById(R.id.settings_packets_min_send_threshold)).setText(Integer
                .toString(Settings.PACKETS_MIN_SEND_THRESHOLD));
        ((TextView) view.findViewById(R.id.settings_packets_max_send_threshold)).setText(Integer
                .toString(Settings.PACKETS_MAX_SEND_THRESHOLD));
        SeekBar packetsSendThresholdSeekBar = (SeekBar) view.findViewById(R.id.settings_packets_send_threshold);
        packetsSendThresholdSeekBar.setMax(toSeekBarValue(Settings.PACKETS_MAX_SEND_THRESHOLD,
                Settings.PACKETS_MIN_SEND_THRESHOLD));
        packetsSendThresholdSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                packetsSendThreshold = (int) fromSeekBarValue(progress, Settings.PACKETS_MIN_SEND_THRESHOLD);
                updatePacketsSendThreshold();
            }
        });

        ((TextView) view.findViewById(R.id.settings_filter_accuracy_min_threshold)).setText(Integer
                .toString(Settings.FILTER_ACCURACY_MIN_THRESHOLD) + "м");
        ((TextView) view.findViewById(R.id.settings_filter_accuracy_max_threshold)).setText(Integer
                .toString(Settings.FILTER_ACCURACY_MAX_THRESHOLD) + "м");
        SeekBar filterAccuracyThresholdSeekBar = (SeekBar) view.findViewById(R.id.settings_filter_accuracy_threshold);
        filterAccuracyThresholdSeekBar.setMax(toSeekBarValue(Settings.FILTER_ACCURACY_MAX_THRESHOLD,
                Settings.FILTER_ACCURACY_MIN_THRESHOLD));
        filterAccuracyThresholdSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                filterAccuracyThreshold = (int) fromSeekBarValue(progress, Settings.FILTER_ACCURACY_MIN_THRESHOLD);
                updateFilterAccuracyThreshold();
            }
        });

        ((TextView) view.findViewById(R.id.settings_gps_filter_min_detalization)).setText(Integer
                .toString(Settings.GPS_FILTER_MIN_DETALIZATION));
        ((TextView) view.findViewById(R.id.settings_gps_filter_max_detalization)).setText(Integer
                .toString(Settings.GPS_FILTER_MAX_DETALIZATION));
        SeekBar gpsFilterDetalizationSeekBar = (SeekBar) view.findViewById(R.id.settings_gps_filter_detalization);
        gpsFilterDetalizationSeekBar.setMax(toSeekBarValue(Settings.GPS_FILTER_MAX_DETALIZATION,
                Settings.GPS_FILTER_MIN_DETALIZATION));
        gpsFilterDetalizationSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                gpsFilterDetalization = (int) fromSeekBarValue(progress, Settings.GPS_FILTER_MIN_DETALIZATION);
                updateGpsFilterDetalization();
            }
        });

        ((TextView) view.findViewById(R.id.settings_apply_btn)).setTypeface(Fonts.RobotoBold);
        view.findViewById(R.id.settings_apply_btn).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                getView().clearFocus();
                if (!isChangesValid()) {
                    Toast.makeText(App.getInstance(),
                            getResources().getString(R.string.settings_alert_not_valid_changes),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Settings.setSetting(Settings.DeviceId, deviceId);
                Settings.setSetting(Settings.ScoutServerAddress, scoutServerAddress);
                Settings.setSetting(Settings.ScoutServerPort, Integer.parseInt(scoutServerPort));
                Settings.setSetting(Settings.JournalSize, journalSize);
                Settings.setSetting(Settings.PacketsSendMode,
                        isPacketsSendByInterval ? Settings.PACKETS_SEND_MODE_BY_INTERVAL
                                : Settings.PACKETS_SEND_MODE_BY_THRESHOLD);
                Settings.setSetting(Settings.PacketsSendInterval, packetsSendInterval);
                Settings.setSetting(Settings.PacketsSendThreshold, packetsSendThreshold);
                Settings.setSetting(Settings.FilterAccuracyThreshold, filterAccuracyThreshold);
                Settings.setSetting(Settings.GpsFilterDetalization, gpsFilterDetalization);

                Toast.makeText(App.getInstance(),
                        getResources().getString(R.string.settings_info_settings_applied),
                        Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!isChanging) {
            deviceId = Settings.getSetting(Settings.DeviceId);
            scoutServerAddress = Settings.getSetting(Settings.ScoutServerAddress);
            scoutServerPort = Settings.getSetting(Settings.ScoutServerPort).toString();
            journalSize = Settings.getSetting(Settings.JournalSize);
            isPacketsSendByInterval = Settings
                    .getSetting(Settings.PacketsSendMode)
                    .equals(Settings.PACKETS_SEND_MODE_BY_INTERVAL);
            packetsSendInterval = Settings.getSetting(Settings.PacketsSendInterval);
            packetsSendThreshold = Settings.getSetting(Settings.PacketsSendThreshold);
            filterAccuracyThreshold = Settings.getSetting(Settings.FilterAccuracyThreshold);
            gpsFilterDetalization = Settings.getSetting(Settings.GpsFilterDetalization);
            isChanging = true;
        }

        updateDeviceId();
        updateScoutServerAddress();
        updateScoutServerPort();
        updateJournalSize();
        updatePacketsSendModeLayout();
        updatePacketsSendInterval();
        updatePacketsSendThreshold();
        updateFilterAccuracyThreshold();
        updateGpsFilterDetalization();
    }

    private void updateDeviceId() {
        EditText view = (EditText) getView().findViewById(R.id.settings_device_id);
        view.setText(deviceId);
        if (view.isFocused() || Settings.DeviceId.checkValue(deviceId))
            view.setBackgroundColor(getResources().getColor(R.color.settings_edit_text_bg));
        else
            view.setBackgroundColor(getResources().getColor(R.color.settings_edit_text_error_bg));
    }

    private void updateScoutServerAddress() {
        EditText view = (EditText) getView().findViewById(R.id.settings_scout_rx_server_address);
        view.setText(scoutServerAddress);
        if (view.isFocused() || Settings.ScoutServerAddress.checkValue(scoutServerAddress))
            view.setBackgroundColor(getResources().getColor(R.color.settings_edit_text_bg));
        else
            view.setBackgroundColor(getResources().getColor(R.color.settings_edit_text_error_bg));
    }

    private void updateScoutServerPort() {
        EditText view = (EditText) getView().findViewById(R.id.settings_scout_rx_server_port);
        view.setText(scoutServerPort);
        if (view.isFocused() || checkPortString(scoutServerPort))
            view.setBackgroundColor(getResources().getColor(R.color.settings_edit_text_bg));
        else
            view.setBackgroundColor(getResources().getColor(R.color.settings_edit_text_error_bg));
    }

    private void updateJournalSize() {
        ((SeekBar) getView().findViewById(R.id.settings_journal_size))
                .setProgress(toSeekBarValue(journalSize, Settings.JOURNAL_MIN_SIZE));
        ((TextView) getView().findViewById(R.id.settings_journal_size_label)).setText(journalSize + " дн");
    }

    private void updatePacketsSendModeLayout() {
        getView().findViewById(R.id.settings_packets_send_mode).setSelected(isPacketsSendByInterval);
        if (isPacketsSendByInterval) {
            ((TextView) getView().findViewById(R.id.settings_packets_send_mode_value_label)).setText(getResources()
                    .getString(R.string.settings_packets_send_interval_label));
            getView().findViewById(R.id.settings_packets_send_interval_layout).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.settings_packets_send_threshold_layout).setVisibility(View.GONE);
        } else {
            ((TextView) getView().findViewById(R.id.settings_packets_send_mode_value_label)).setText(getResources()
                    .getString(R.string.settings_packets_send_threshold_label));
            getView().findViewById(R.id.settings_packets_send_threshold_layout).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.settings_packets_send_interval_layout).setVisibility(View.GONE);
        }
    }

    private void updatePacketsSendInterval() {
        ((SeekBar) getView().findViewById(R.id.settings_packets_send_interval))
                .setProgress(toSeekBarValue(packetsSendInterval, Settings.PACKETS_MIN_SEND_INTERVAL));
        ((TextView) getView().findViewById(R.id.settings_packets_send_interval_label)).setText(Helpers
                .msToMinAndSec(packetsSendInterval));
    }

    private void updatePacketsSendThreshold() {
        ((SeekBar) getView().findViewById(R.id.settings_packets_send_threshold))
                .setProgress(toSeekBarValue(packetsSendThreshold, Settings.PACKETS_MIN_SEND_THRESHOLD));
        ((TextView) getView().findViewById(R.id.settings_packets_send_threshold_label)).setText(Integer
                .toString(packetsSendThreshold));
    }

    private void updateFilterAccuracyThreshold() {
        ((SeekBar) getView().findViewById(R.id.settings_filter_accuracy_threshold))
                .setProgress(toSeekBarValue(filterAccuracyThreshold, Settings.FILTER_ACCURACY_MIN_THRESHOLD));
        ((TextView) getView().findViewById(R.id.settings_filter_accuracy_threshold_label)).setText(Integer
                .toString(filterAccuracyThreshold) + "м");
    }

    private void updateGpsFilterDetalization() {
        ((SeekBar) getView().findViewById(R.id.settings_gps_filter_detalization))
                .setProgress(toSeekBarValue(gpsFilterDetalization, Settings.GPS_FILTER_MIN_DETALIZATION));
        ((TextView) getView().findViewById(R.id.settings_gps_filter_detalization_label)).setText(Integer
                .toString(gpsFilterDetalization));
    }
}
