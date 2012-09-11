package com.yyl.myrmex.tlsupdater;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;


public class TLSUpdater {
	private Context context;
	private AlarmManager alarm_manager;
	
	public TLSUpdater(Context ctx) {
		context = ctx;
		alarm_manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	}
	
	public void run() {
		
	}
	
	public void stop() {
		
	}
	
	private class MyAlarm extends BroadcastReceiver {

		private static final String DEBUG_TAG = "MyAlarmReceiver";
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Log.d(DEBUG_TAG, "Recurring alarm; requesting location tracking.");
	        // start the service
	        Intent updater = new Intent(context, Updater.class);
	        context.startService(updater);
		}
		
	}
	
	private class Updater extends AsyncTask {

		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}