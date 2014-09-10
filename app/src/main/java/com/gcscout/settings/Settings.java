package com.gcscout.settings;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;

import com.gcscout.trackerdemo.App;
import com.gcscout.trackerdemo.Helpers;
import com.gcscout.trackerdemo.OnSettingChangedListener;

public class Settings {
	private static final String SETTINGS_PREFERENCES_NAME = "SETTINGS";
	public static final int JOURNAL_MIN_SIZE = 10; // 10 days
	public static final int JOURNAL_MAX_SIZE = 30; // 30 days
	public static final long PACKETS_MIN_SEND_INTERVAL = 0 * 1000; // 0sec
	public static final long PACKETS_MAX_SEND_INTERVAL = 10 * 60 * 1000; // 10min
	public static final int PACKETS_MIN_SEND_THRESHOLD = 1;
	public static final int PACKETS_MAX_SEND_THRESHOLD = 200;
	public static final String PACKETS_SEND_MODE_BY_INTERVAL = "ByInterval";
	public static final String PACKETS_SEND_MODE_BY_THRESHOLD = "ByThreshold";
	public static final int FILTER_ACCURACY_MIN_THRESHOLD = 0;
	public static final int FILTER_ACCURACY_DEFAULT_THRESHOLD = 100;
	public static final int FILTER_ACCURACY_MAX_THRESHOLD = 800;
	public static final int GPS_FILTER_MIN_DETALIZATION = 4;
	public static final int GPS_FILTER_MAX_DETALIZATION = 20;

	// Серийный ID устройства (для протокола)
	public static final Setting<String> DeviceId = new Setting<String>("DeviceId", Long.toString(Device
			.getDeviceUUIDHash()), new SettingChecker<String>() {
		public boolean isValid(String value) {
			return Helpers.isStringValid(value);
		}
	});

	// Адрес или имя сервера
	public static final Setting<String> ScoutServerAddress = new Setting<String>("ScoutServerAddress",
			"144.76.140.113", new SettingChecker<String>() {
				public boolean isValid(String value) {
					return Helpers.isStringValid(value);
				}
			});

	// Порт сервера
	public static final Setting<Integer> ScoutServerPort = new Setting<Integer>("ScoutServerPort", 6565,
			new SettingChecker<Integer>() {
				public boolean isValid(Integer value) {
					return value > 0 && value < 65536;
				}
			});

	// Максимальный размер журнала
	public static final Setting<Integer> JournalSize = new Setting<Integer>("JournalSize", JOURNAL_MAX_SIZE,
			new SettingChecker<Integer>() {
				public boolean isValid(Integer value) {
					return value >= JOURNAL_MIN_SIZE && value <= JOURNAL_MAX_SIZE;
				}
			});

	// Режим передачи пакетов на сервер (по накоплению или по интервалу)
	public static final Setting<String> PacketsSendMode = new Setting<String>("PacketsSendMode",
			PACKETS_SEND_MODE_BY_INTERVAL, new SettingChecker<String>() {
				public boolean isValid(String value) {
					return value.equals(PACKETS_SEND_MODE_BY_INTERVAL) || value.equals(PACKETS_SEND_MODE_BY_THRESHOLD);
				}
			});

	// Интервал передачи пакетов на сервер
	public static final Setting<Long> PacketsSendInterval = new Setting<Long>("PacketsSendInterval",
			PACKETS_MIN_SEND_INTERVAL, new SettingChecker<Long>() {
				public boolean isValid(Long value) {
					return value >= PACKETS_MIN_SEND_INTERVAL && value <= PACKETS_MAX_SEND_INTERVAL;
				}
			});

	// Порог количества пакетов для передачи на сервер
	public static final Setting<Integer> PacketsSendThreshold = new Setting<Integer>("PacketsSendThreshold",
			PACKETS_MIN_SEND_THRESHOLD, new SettingChecker<Integer>() {
				public boolean isValid(Integer value) {
					return value >= PACKETS_MIN_SEND_THRESHOLD && value <= PACKETS_MAX_SEND_THRESHOLD;
				}
			});

	// Порог точности фильтрации
	public static final Setting<Integer> FilterAccuracyThreshold = new Setting<Integer>("FilterAccuracyThreshold", 220,
			new SettingChecker<Integer>() {
				public boolean isValid(Integer value) {
					return value >= FILTER_ACCURACY_MIN_THRESHOLD && value <= FILTER_ACCURACY_MAX_THRESHOLD;
				}
			});

	// Детализация GPS-фильтрации
	public static final Setting<Integer> GpsFilterDetalization = new Setting<Integer>("GPSFilterDetalization", 18,
			new SettingChecker<Integer>() {
				public boolean isValid(Integer value) {
					return value >= GPS_FILTER_MIN_DETALIZATION && value <= GPS_FILTER_MAX_DETALIZATION;
				}
			});

	private final static ArrayList<OnSettingChangedListener> settingsChangedListeners = new ArrayList<>();

	private static SharedPreferences getSettings() {
		return App.getInstance().getSharedPreferences(SETTINGS_PREFERENCES_NAME, Context.MODE_PRIVATE);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getSetting(Setting<T> setting) {
		switch (setting.getType()) {
			case String:
				return (T) getSettings().getString(setting.getName(), (String) setting.getDefaultValue());
			case Integer:
				return (T) (Integer) getSettings().getInt(setting.getName(), (Integer) setting.getDefaultValue());
			case Long:
				return (T) (Long) getSettings().getLong(setting.getName(), (Long) setting.getDefaultValue());
			case Boolean:
				return (T) (Boolean) getSettings().getBoolean(setting.getName(), (Boolean) setting.getDefaultValue());
			default:
				return null;
		}
	}

	public static <T> boolean setSetting(Setting<T> setting, T value) {
		if (getSetting(setting).equals(value))
			return true;
		if (!setting.checkValue(value))
			return false;

		switch (setting.getType()) {
			case String:
				getSettings().edit().putString(setting.getName(), (String) value).commit();
				break;
			case Integer:
				getSettings().edit().putInt(setting.getName(), (Integer) value).commit();
				break;
			case Long:
				getSettings().edit().putLong(setting.getName(), (Long) value).commit();
				break;
			case Boolean:
				getSettings().edit().putBoolean(setting.getName(), (Boolean) value).commit();
				break;
			default:
				return false;
		}

		onSettingChanged(setting);
		return true;
	}

	public static void addSettingsChangedListener(OnSettingChangedListener listener) {
		if (!settingsChangedListeners.contains(listener))
			settingsChangedListeners.add(listener);
	}

	private static void onSettingChanged(Setting<?> setting) {
		for (OnSettingChangedListener listener : settingsChangedListeners)
			listener.onSettingChanged(setting);
	}

	public static void removeSettingsChangedListener(OnSettingChangedListener listener) {
		if (settingsChangedListeners.contains(listener))
			settingsChangedListeners.remove(listener);
	}
}