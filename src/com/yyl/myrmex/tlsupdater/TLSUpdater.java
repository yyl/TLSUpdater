package com.yyl.myrmex.tlsupdater;

import java.util.Calendar;
import java.util.TimeZone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class TLSUpdater {
	private Context context;
	private Intent alarm_intent;
	private PendingIntent upload;
	private AlarmManager alarmm;
	private String dbname = "nodb";
	private int hour, minute;
	private SharedPreferences spreference;

	private static String DEBUG_TAG = "TLSUpdater";
	public static final String TLS_PREF = "tlsupdater preference";

	public TLSUpdater(Context ctx, String dbname, int hour, int minute) {
		context = ctx;
		alarmm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		this.dbname = dbname;
		this.hour = hour;
		this.minute = minute;
		spreference = context.getSharedPreferences(TLS_PREF, 0);
	}

	public void run() {
		// get a Calendar object with current time
		Calendar updateTime = Calendar.getInstance();
		updateTime.setTimeZone(TimeZone.getDefault());
		updateTime.set(Calendar.HOUR_OF_DAY, this.hour);
		updateTime.set(Calendar.MINUTE, this.minute);
		// updateTime.add(Calendar.SECOND, START_DELAY);
		Log.i(DEBUG_TAG, "Set the alarm to the time: " + updateTime.getTime());

		alarm_intent = new Intent(context, TLSAlarmReceiver.class);
		alarm_intent.putExtra("dbName", dbname);
		alarm_intent.putExtra("hour", this.hour);
		alarm_intent.putExtra("minute", this.minute);
		upload = PendingIntent.getBroadcast(context, 0, alarm_intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		alarmm.setRepeating(AlarmManager.RTC_WAKEUP,
				updateTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, upload);
		
		SharedPreferences.Editor editor = spreference.edit();
		editor.putInt("hour", this.hour);
		editor.putInt("minute", this.minute);
		editor.commit();
	}

	public void stop() {
		Log.i(DEBUG_TAG, "Stop the alarm");
		alarm_intent = new Intent(context, TLSAlarmReceiver.class);
		upload = PendingIntent.getBroadcast(context, 0, alarm_intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		alarmm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmm.cancel(upload);
	}
}