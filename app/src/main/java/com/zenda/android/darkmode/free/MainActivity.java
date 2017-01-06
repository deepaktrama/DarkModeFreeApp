
package com.zenda.android.darkmode.free;

import com.google.android.gms.ads.MobileAds;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.content.ComponentName;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

/*
TODO
allow to shut down service automatically at a certain time
restore brightness settings back to defaults
remove write_Settings permission
allow changing min and max progress bar
test on different devices
resize banner ad to width of device - it is small now
let button be default dark color change color after uses starts
change checkbox text to "dim brightness on app start"
*/


public class MainActivity extends Activity {

	private AdView mAdView;
	
	private Button _btnStartStop = null;
	private Button _btnBuy = null;
	private CheckBox _cbAutoStartService = null;
	private TextView _txtStatus = null;
	private SeekBar _sbNightBrightness = null;


	void disableorenablecontrols(boolean isServiceRunning)
	{
		if (isServiceRunning){
			_btnStartStop.setText(getString(R.string.btn_stop_text));
			_sbNightBrightness.setVisibility(View.VISIBLE);
			_txtStatus.setText(R.string.status_running);
			_txtStatus.setTextColor(getResources().getColor(R.color.StatusHealthy));
		}
		else {
			_btnStartStop.setText(getString(R.string.btn_start_text));
			_sbNightBrightness.setVisibility(View.GONE);
			_txtStatus.setText(R.string.status_stopped);
			_txtStatus.setTextColor(getResources().getColor(R.color.StatusError));
		}
	}

	// handler for received data from serv ice
	private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BrightnessService.BROADCAST_ACTION_FROM_BRIGHTNESSSERVICE)) {
				final String param = intent.getStringExtra(BrightnessService.EXTRA_PARAM_B);
				// do something
				if (param == BrightnessService.ONCREATE) {
					//AppSettings as = new AppSettings( context);
					//int b = as.getBrightnessValue();
					//_sbNightBrightness.setProgress(b);
					//BrightnessService.instance.applyRunningReading((float)b / (float)10);
					BrightnessService.instance.applyRunningReading((float)_sbNightBrightness.getProgress() / (float)10);
					disableorenablecontrols(true);
				}
				else if (param == BrightnessService.ONDESTROY)
					disableorenablecontrols(false);
			}
		}
	};

	private boolean isServiceRunning() {
		return (BrightnessService.instance != null);
	}

	void saveSettings() {
		AppSettings as = new AppSettings(this.getApplicationContext());
		as.saveBrightnessValue(_sbNightBrightness.getProgress());
		as.saveAutoStartValue(_cbAutoStartService.isChecked());
	}

	private void dostartStop() {
		if (! isServiceRunning()){
			Intent service = new Intent(this.getApplicationContext(), BrightnessService.class);
			ComponentName cn = startService(service);
		}
		else {
			stopService(new Intent(MainActivity.this, BrightnessService.class));
			saveSettings();
		}
		// start the service, controls will be updated once the service sends us a signal that it has successfully started
	}

	void printStuff(String msg) {
		//Log.i(getString(R.string.app_name), "[DT debug]: " + msg);
	}

	@Override
	protected void onDestroy() {
		LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
		bm.unregisterReceiver(mBroadcastReceiver);
		saveSettings();
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		registerReceiver();
		// hide or show certain controls depending on if the service is still running
		disableorenablecontrols(isServiceRunning());
		// restore control values
		AppSettings as = new AppSettings(this.getApplicationContext());
		_sbNightBrightness.setProgress(as.getBrightnessValue());
		// full version code starts
		/*
		if (as.getAutoStart()) {
			_cbAutoStartService.setChecked(true);
			// automatically start service if needed
			if (((MyAppContext) getApplication()).isFirstTimeLaunched) {
				((MyAppContext) getApplication()).isFirstTimeLaunched = false;
				dostartStop();
			}
		}
		*/
		// full version code ends
	}

	private void registerReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(BrightnessService.BROADCAST_ACTION_FROM_BRIGHTNESSSERVICE);
		LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
		bm.registerReceiver(mBroadcastReceiver, filter);
	}

	@Override
	public void onPause() {
		super.onPause();

		LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
		bm.unregisterReceiver(mBroadcastReceiver);

		saveSettings();
	}

	void goToGooglePlay() {
		try {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getString(R.string.pro_package_name))));
		} catch (android.content.ActivityNotFoundException anfe) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getString(R.string.pro_package_name))));
		}
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		printStuff("onCreate fired");
		printStuff("isFirstTimeLaunched value is " + String.valueOf(((MyAppContext)getApplication()).isFirstTimeLaunched));

		//IntentFilter filter = new IntentFilter();
		//filter.addAction(BrightnessService.BROADCAST_ACTION_FROM_BRIGHTNESSSERVICE);
		//LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
		//bm.registerReceiver(mBroadcastReceiver, filter);

        setContentView(R.layout.activity_main);

		// Free version code starts
		MobileAds.initialize(getApplicationContext(), getString(R.string.admob_app_id));
		mAdView = (AdView) findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);
		// Free version code ends

		setTitle(getString(R.string.app_name));

        _btnStartStop = (Button) findViewById(R.id.btnOnOff);
		_btnBuy = (Button) findViewById(R.id.btnBuyFullVersion);
		_cbAutoStartService = (CheckBox) findViewById(R.id.cbAutoStartService);
        _txtStatus = (TextView) findViewById(R.id.txtStatus);
        _sbNightBrightness = (SeekBar) findViewById(R.id.sbBrightness);

        _btnStartStop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dostartStop();
			}
        });

		// Free version code starts
		_btnBuy.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				goToGooglePlay();
			}
		});

		_cbAutoStartService.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// Free version code starts
				Toast.makeText(getApplicationContext(), "Available in pro version", Toast.LENGTH_LONG).show();
				// Free version code ends
			}
		});
		// Free version code ends

		// TODO
		_btnStartStop.setEnabled(true);
		_sbNightBrightness.setMax(AppSettings.MAX_BRIGHTNESS);
		//AppSettings as = new AppSettings(this.getApplicationContext());
		//_sbNightBrightness.setProgress(as.getBrightnessValue());
        _sbNightBrightness.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {	}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}


			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (! fromUser)
					return;
				if (BrightnessService.instance == null)
					return;
				//float brightness = (float)progress / (float)255;
				float brightness = (float)progress / (float)10;
				//_editTextBrightness.setText(Float.toString(brightness));
				BrightnessService.instance.applyRunningReading(brightness);
			}
		});

	}

}
