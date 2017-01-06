package com.zenda.android.darkmode.free;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by deepak on 12/13/2016.
 */

public class AppSettings {

    static final String PREFERENCES_KEY = "preferences";
    static final String BRIGHTNESS_KEY = "brightness";
    static String AUTO_START_KEY = "autostart";
    static public int MAX_BRIGHTNESS = 9;
    static public int DEFAULT_BRIGHTNESS = 1;
    static public int APP_VERSION = 1;
    static boolean DEFAULT_AUTO_START = false;

    Context _ctx;

    public AppSettings(Context ctx)
    {
        _ctx = ctx;
    }


    public int getBrightnessValue() {
        SharedPreferences sp = _ctx.getSharedPreferences(PREFERENCES_KEY, 0);
        return sp.getInt(BRIGHTNESS_KEY, DEFAULT_BRIGHTNESS);
    }

    public void saveBrightnessValue(int brightness) {
        SharedPreferences.Editor e = _ctx.getSharedPreferences(PREFERENCES_KEY, 0).edit();

        e.putInt(BRIGHTNESS_KEY, brightness);

        savePref(e);

    }

    public boolean getAutoStart() {
        SharedPreferences sp = _ctx.getSharedPreferences(PREFERENCES_KEY, 0);
        return sp.getBoolean(AUTO_START_KEY, DEFAULT_AUTO_START);
    }

    public void saveAutoStartValue(boolean autoStart) {
        SharedPreferences.Editor e = _ctx.getSharedPreferences(PREFERENCES_KEY, 0).edit();


        e.putBoolean(AUTO_START_KEY, autoStart);

        savePref(e);

    }

    private void savePref(SharedPreferences.Editor e) {
        e.putInt("AppVersion", APP_VERSION);
        if(!e.commit())
        {
            //Log.e(Globals.TAG, "Failed to save settings.");
        }
    }

}
