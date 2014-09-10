package com.gcscout.settings;

public final class Setting<SettingT> {
    private final String name;
    private final SettingT defaultValue;
    private final SettingType type;
    private SettingChecker<SettingT> checker;

    public String getName() {
        return name;
    }

    public SettingT getDefaultValue() {
        return defaultValue;
    }

    public SettingType getType() {
        return type;
    }

    public boolean checkValue(SettingT value) {
        return checker == null || checker.isValid(value);
    }

    public Setting(String name, SettingT defaultValue, SettingChecker<SettingT> checker) {
        this(name, defaultValue);
        this.checker = checker;
    }

    public Setting(String name, SettingT defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        if (defaultValue instanceof String)
            type = SettingType.String;
        else if (defaultValue instanceof Integer)
            type = SettingType.Integer;
        else if (defaultValue instanceof Long)
            type = SettingType.Long;
        else if (defaultValue instanceof Boolean)
            type = SettingType.Boolean;
        else {
            throw new IllegalStateException("Unknown setting " + name + " type: " + defaultValue.getClass().getName());
        }
    }
}
