package com.gcscout.settings;

public interface SettingChecker<SettingT> {
	boolean isValid(SettingT value);
}
