package com.gcscout.trackerdemo;

import android.graphics.Typeface;

public class Fonts {
    public final static Typeface RobotoRegular = Typeface.createFromAsset(App
                    .getInstance()
                    .getAssets(),
            "Roboto-Regular.ttf");
    public final static Typeface RobotoMedium = Typeface.createFromAsset(App
                    .getInstance()
                    .getAssets(),
            "Roboto-Medium.ttf");
    public final static Typeface RobotoBold = Typeface.createFromAsset(App.getInstance().getAssets(),
            "Roboto-Bold.ttf");
}
