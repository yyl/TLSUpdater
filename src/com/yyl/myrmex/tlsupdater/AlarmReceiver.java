package com.yyl.myrmex.tlsupdater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

	private static final String DEBUG_TAG = "AlarmReceiver";
	private Context context;
	private UpdateTask task;
	private String dbname;
	private static final int DIALOG_PAUSED_ID = 0;
	private static final int DIALOG_GAMEOVER_ID = 1;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(DEBUG_TAG, "Recurring alarm; requesting update task!");
		this.context = context;
		dbname = (String) intent.getCharSequenceExtra("dbname");
		Log.i(DEBUG_TAG, "get the db name: " + dbname);
		
		task = new UpdateTask(context, dbname);
		task.execute();
	}
	
	public boolean hasConnectivity() {
	    ConnectivityManager cm =
	        (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
}