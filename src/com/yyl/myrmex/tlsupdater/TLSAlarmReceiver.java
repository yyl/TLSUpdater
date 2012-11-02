package com.yyl.myrmex.tlsupdater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TLSAlarmReceiver extends BroadcastReceiver {

	private static final String DEBUG_TAG = "AlarmReceiver";
	private String db_name;
	private int hour, minute;
	private Utilities ut;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(DEBUG_TAG, "Invoke the repeating alarm...");
		ut = new Utilities();
		hour = intent.getIntExtra("hour", 18);
		minute = intent.getIntExtra("minute", 0);
		db_name = (String) intent.getCharSequenceExtra("dbName");
		Log.i(DEBUG_TAG, "get the db name: " + db_name);
		// constructing the intent for UpdateIntent
		Intent i = new Intent(context, UpdateIntent.class);
		i.putExtra("dbName", db_name);
		i.putExtra("hour", hour);
		i.putExtra("minute", minute);
		ut.writeToFile("log.txt",
				"TLSAlarmReceiver.onReceive(): Receiving the alarm at " + hour
						+ ":" + minute);
		context.startService(i);
	}

}