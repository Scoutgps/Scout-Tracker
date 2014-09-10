package com.gcscout.trackerdemo;

import com.gcscout.settings.Setting;

public interface OnSettingChangedListener {
	void onSettingChanged(Setting<?> setting);
}
