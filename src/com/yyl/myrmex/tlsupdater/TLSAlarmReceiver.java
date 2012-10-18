package com.yyl.myrmex.tlsupdater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TLSAlarmReceiver extends BroadcastReceiver {

	private static final String DEBUG_TAG = "AlarmReceiver";
	private String dbname;
	private Utilities ut;

	@Override
	public void onReceive(Context context, Intent intent) {
		ut = new Utilities();
		int hour = intent.getIntExtra("hour", 18);
		int minute = intent.getIntExtra("minute", 0);
		Log.i(DEBUG_TAG, "Invoke the repeating alarm...");
		dbname = (String) intent.getCharSequenceExtra("dbName");
		Log.i(DEBUG_TAG, "get the db name: " + dbname);
		// constructing the intent for UpdateIntent
		Intent i = new Intent(context, UpdateIntent.class);
		i.putExtra("dbName", dbname);
		i.putExtra("hour", hour);
		i.putExtra("minute", minute);
		ut.writeToFile("log.txt", "Receiving the alarm at " + hour + ":" + minute + "\n");
		context.startService(i);
	}

}