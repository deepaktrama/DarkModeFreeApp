package com.zenda.android.darkmode.free;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.WindowManager;

/**
 * Created by deepak on 10/16/2016.
 */



public class BrightnessService extends Service {

    public static final String BROADCAST_ACTION_FROM_BRIGHTNESSSERVICE = "com.zenda.android.darkmode.free.broadcast_action.service";
    public static final String EXTRA_PARAM_B = "com.zenda.android.darkmode.free.extra.PARAM_B";
    public static final String ONCREATE = "oncreate";
    public static final String ONDESTROY = "ondestroy";

    private static ActivatorView _av = null;
    //private static WindowManager wm = null;
    private WindowManager.LayoutParams _avLayoutParams = null;

    private Handler _h = new Handler();
    public static BrightnessService instance = null;
    private Handler mUiHandler;

    // called to send data to Activity
    public static void sendSignalToApp(String param) {
        Intent intent = new Intent(BROADCAST_ACTION_FROM_BRIGHTNESSSERVICE);
        intent.putExtra(EXTRA_PARAM_B, param);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(instance);
        bm.sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        showNotificationIcon(true);
        //setBrightness2(BrightnessValue);
        mUiHandler = new Handler();
        sendSignalToApp(ONCREATE);
    }

    // TODO cleanup, set brightness back to auto
    @Override
    public void onDestroy()
    {


        //Settings.System.putInt(BrightnessService.this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 0);
        //Settings.System.putInt(getContentResolver(),
        //        Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);

        /*
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new Runnable()
        {
            public void run()
            {
                mUiHandler.post(new Runnable()
                {
                    public void run()
                    {
                        //updateDockNow(...);
                        ((WindowManager) getApplicationContext().getSystemService(Service.WINDOW_SERVICE)).removeView(_av);
                        _av = null;
                        _avLayoutParams = null;
                    }
                });
            }
        }, 0, 1, TimeUnit.SECONDS);
        */


        synchronized (_h) {
            if (_av != null) {
                //WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
                //wm.removeView(_av);
                ((WindowManager) getApplicationContext().getSystemService(Service.WINDOW_SERVICE)).removeView(_av);
                _av = null;
                _avLayoutParams = null;
            }
        }

        showNotificationIcon(false);
        super.onDestroy();
        sendSignalToApp(ONDESTROY); // NOTE needs instance variable
        instance = null;
    }

    public void showNotificationIcon(boolean bShow) {
        //if(Log.isLoggable(Globals.TAG, Log.DEBUG)) Log.d(Globals.TAG, String.format("showNotificationIcon entering, bShow = %b", bShow));

        if (bShow) {
            Intent ni = new Intent(this, MainActivity.class);
            PendingIntent pi = PendingIntent.getActivity(this, 0, ni, PendingIntent.FLAG_CANCEL_CURRENT);


            NotificationCompat.Builder nb = new NotificationCompat.Builder(this);
            nb.setContentIntent(pi)
                    .setAutoCancel(false)
                    .setSmallIcon((Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)?R.drawable.ic_notif_white:R.drawable.ic_launcher)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle(getResources().getString(R.string.app_name))
                    .setContentText("screen is dimmed");

            startForeground(42, nb.getNotification());
        } else
        {
            stopForeground(true);
        }

        //if(Log.isLoggable(Globals.TAG, Log.DEBUG)) Log.d(Globals.TAG, "showNotificationIcon leave");
    }

    public synchronized void applyRunningReading(float brightness) {
        //setBrightness2(brightness); // WORKS
        //setBrightness2helper(brightness); // NOT WORKING
        setBrightness(brightness);
    }

    void setBrightness(float brightness) {
        boolean viewexists = (_av != null);
        //WindowManager wm = (WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE);
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        if (! viewexists) {
            _av = new ActivatorView(this);
            _avLayoutParams = new WindowManager.LayoutParams(0, 0, 0, 0,
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                    //WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    PixelFormat.OPAQUE);
            _avLayoutParams.screenBrightness = 20f;
            _avLayoutParams.screenBrightness = brightness;

            //WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            //WindowManager wm = (WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE);
            //wm = (WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE);
            if (!viewexists)
                wm.addView(_av, _avLayoutParams);
            //else
            //    wm.updateViewLayout(_av, _avLayoutParams);
        }

        /////////setBrightness2helper(brightness);

        //int iBrightness = (int) (brightness * 255);
        //Settings.System.putInt(BrightnessService.this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, iBrightness);

        boolean bUseDim = true;
        float dimAmount = 0.45f; // CHANGE THIS !!!
        int MIN_BRIGHTNESS_INT = 20;
        int MAX_BRIGHTNESS_INT = 255;

        synchronized (_h) {
            if(_av != null && _avLayoutParams != null)		// these will be null when the service is stopping
            {
                //WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);

                if(bUseDim)
                {
                    _avLayoutParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                    //_avLayoutParams.dimAmount =  dimAmount;//bc.getRunningDimAmount();
                    _avLayoutParams.dimAmount =  brightness;
                    //_avLayoutParams.screenBrightness = (float)MIN_BRIGHTNESS_INT/MAX_BRIGHTNESS_INT;
                    //_avLayoutParams.screenBrightness = brightness;
                    wm.updateViewLayout(_av, _avLayoutParams);
                    //((WindowManager) getSystemService(WINDOW_SERVICE)).updateViewLayout(_av, _avLayoutParams);
                }
                else
                {
                    _avLayoutParams.screenBrightness = brightness;
                    wm.updateViewLayout(_av, _avLayoutParams);
                }

            }
        }

    }
}
