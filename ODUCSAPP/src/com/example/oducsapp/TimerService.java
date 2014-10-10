package com.example.oducsapp;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class TimerService extends Service {
	
	int counter = 0;
	long sleepTime = 1000;
	int timerForEventIndex = -1;
	static int snoozTime = 10000;
	private Timer timer = new Timer();
	private final IBinder binder = new MyBinder();
	public int count1 = 0;
	boolean isTimerOn = false;
	final String TAG = "TimerService";  
	
	public TimerService() {
	}
	
	public class MyBinder extends Binder {
		TimerService getService() {
			return TimerService.this;
		}
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		Log.d(TAG, "Starting timer service");
		Toast.makeText(this, "Timer Started for " + (sleepTime/1000) + " Sec", Toast.LENGTH_LONG).show();		
		startTimerAndSnooz();
		isTimerOn = true;
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "Timer service destroyed");
		
		if (timer != null){
			Log.d(TAG, "Timer destroyed");
			timer.cancel();
		}
	}
	
	private void startTimerAndSnooz() {
		timer.schedule(new TimerTask() {
			public void run() {
				counter++;
				timerFinished(counter);
			}
		}, sleepTime);
	}
	
	public void timerFinished(int counter)
	{
		Log.d(TAG, "Timer goes off");
		
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction("TIMER_GOES_OFF");
		getBaseContext().sendBroadcast(broadcastIntent);
		
		stopSelf();
	}
	
	public int getCount()
	{
		return ++count1;
	}
	
	public int isTimerSwitchedOn()
	{
		//if timer still on then return the index
		if(isTimerOn)
			return timerForEventIndex;
		else
			return -1;
	}
}
