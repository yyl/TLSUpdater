package com.yyl.myrmex.tlsupdater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String DEBUG_TAG = "AlarmReceiver";
    private UpdateTask task;
    private String dbname;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(DEBUG_TAG, "Recurring alarm; requesting update task!");
        dbname = (String) intent.getCharSequenceExtra("dbname");
        Log.d(DEBUG_TAG, "get the db name: " + dbname);
        task = new UpdateTask(context, dbname);
		task.execute();
    }
}