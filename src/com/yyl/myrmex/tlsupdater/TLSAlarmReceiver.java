package com.yyl.myrmex.tlsupdater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TLSAlarmReceiver extends BroadcastReceiver {

	private static final String DEBUG_TAG = "AlarmReceiver";
	private String dbname;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(DEBUG_TAG, "Receiving intent for repeating alarm: requesting upload task!");
		dbname = (String) intent.getCharSequenceExtra("dbname");
		Log.i(DEBUG_TAG, "get the db name: " + dbname);
		// constructing the intent for UpdateIntent
		Intent i = new Intent(context, UpdateIntent.class);
		i.putExtra("dbName", dbname);
		i.putExtra("hour", intent.getIntExtra("hour", 18));
		i.putExtra("minute", intent.getIntExtra("minute", 0));
		context.startService(i);
	}

}