package com.yyl.myrmex.tlsupdater;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

	private static final String DEBUG_TAG = "AlarmReceiver";
	private String dbname;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(DEBUG_TAG, "Recurring alarm; requesting upload task!");
		dbname = (String) intent.getCharSequenceExtra("dbname");
		Log.i(DEBUG_TAG, "get the db name: " + dbname);
		// constructing the intent for UpdateIntent 
		Intent i = new Intent(context, UpdateIntent.class);
		i.putExtra("dbname", dbname);
		i.putExtra("hour", intent.getIntExtra("hour", 18));
		i.putExtra("minute", intent.getIntExtra("minute", 0));
		context.startService(i);
	}

}