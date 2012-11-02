package com.yyl.myrmex.tlsupdater;

import java.util.Calendar;
import java.util.TimeZone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class TLSRebootReceiver extends BroadcastReceiver {

	private static final String DEBUG_TAG = "RebootReceiver";
	private String db_name;
	private int hour, minute, alarm_id;
	private Utilities ut;
	private SharedPreferences spreference;
	private AlarmManager reboot_alarm;
	private Intent alarm_intent;
	private PendingIntent upload;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(DEBUG_TAG, "Resetting the alarm after rebooting...");
		ut = new Utilities();
		reboot_alarm = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarm_intent = new Intent(context, TLSAlarmReceiver.class);
		// wait for a minute so that SDCard could be prepared after rebooting
		try {
			Thread.sleep(60000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		spreference = context.getSharedPreferences(TLSUpdater.TLS_PREF, 0);
		hour = spreference.getInt("hour", 20);
		minute = spreference.getInt("minute", 0);
		db_name = spreference.getString("dbName", null);
		alarm_id = spreference.getInt("alarmId", 0);
		alarm_intent.putExtra("dbName", db_name);
		alarm_intent.putExtra("hour", hour);
		alarm_intent.putExtra("minute", minute);
		alarm_intent.putExtra("alarmId", alarm_id);
		upload = PendingIntent.getBroadcast(context, alarm_id, alarm_intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		// get a Calendar object with current time
		Calendar updateTime = Calendar.getInstance();
		updateTime.setTimeZone(TimeZone.getDefault());
		updateTime.set(Calendar.HOUR_OF_DAY, this.hour);
		updateTime.set(Calendar.MINUTE, this.minute);
		reboot_alarm
				.setRepeating(AlarmManager.RTC_WAKEUP,
						updateTime.getTimeInMillis(),
						AlarmManager.INTERVAL_DAY, upload);
		ut.writeToFile("log.txt",
				"TLSRebootReceiver.onReceive(): Resetting the alarm at " + hour
						+ ":" + minute + " after rebooting.");
	}

}