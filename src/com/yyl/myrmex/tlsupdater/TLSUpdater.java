package com.yyl.myrmex.tlsupdater;

import java.io.File;
import java.util.Calendar;
import java.util.TimeZone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TLSUpdater {
	private Context context;
	private Intent alarm_intent;
	private PendingIntent upload;
	private AlarmManager alarmm;
	private String db_name = "nodb";
	private int hour, minute;
	private SharedPreferences spreference;
	private Utilities ut;

	private static String DEBUG_TAG = "TLSUpdater";
	public static final String TLS_PREF = "tlsupdater preference";
	private static final String SQL_DUMP_SCHEMA = "select sql from sqlite_master where name not in "
			+ "(\"android_metadata\", \"sqlite_sequence\") "
			+ "and name not like \"uidx\"";

	public TLSUpdater(Context ctx, String dbname, int hour, int minute) {
		context = ctx;
		alarmm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		this.db_name = dbname;
		this.hour = hour;
		this.minute = minute;
		spreference = context.getSharedPreferences(TLS_PREF, 0);
		this.ut = new Utilities();
	}

	public void run() {
		// get a Calendar object with current time
		Calendar updateTime = Calendar.getInstance();
		updateTime.setTimeZone(TimeZone.getDefault());
		updateTime.set(Calendar.HOUR_OF_DAY, this.hour);
		updateTime.set(Calendar.MINUTE, this.minute);
		Log.i(DEBUG_TAG, "Set the alarm to the time: " + updateTime.getTime());
		ut.writeToFile("log.txt",
				"TLSUpdater.run(): Set the alarm to the time: " + updateTime.getTime());
		// get an unique id for this alarm, this unique id should be stuck with it forever
		int alarm_id = (int) System.currentTimeMillis();
		// construct the intent
		alarm_intent = new Intent(context, TLSAlarmReceiver.class);
		alarm_intent.putExtra("dbName", db_name);
		alarm_intent.putExtra("hour", this.hour);
		alarm_intent.putExtra("minute", this.minute);
		alarm_intent.putExtra("alarmId", alarm_id);
		upload = PendingIntent.getBroadcast(context, alarm_id, alarm_intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		// set the alarm
		alarmm.setRepeating(AlarmManager.RTC_WAKEUP,
				updateTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, upload);

		// save necessary information
		SharedPreferences.Editor editor = spreference.edit();
		editor.putString("dbName", this.db_name);
		editor.putInt("hour", this.hour);
		editor.putInt("minute", this.minute);
		editor.putInt("alarmId", alarm_id);
		editor.commit();
	}

	public void stop() {
		Log.i(DEBUG_TAG, "Stop the alarm");
		alarm_intent = new Intent(context, TLSAlarmReceiver.class);
		alarmm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmm.cancel(upload);
	}

	public void exportSchema() {
		String db_path = context.getDatabasePath(this.db_name).getAbsolutePath();
		String filename = this.db_name.replace(".db", "");
		File fdb = new File(db_path);
		if (!fdb.exists()) {
			Log.i(DEBUG_TAG, "No such db exist yet.");
			ut.writeToFile(filename, "TLSUpdater.exportSchema(): No such db exist yet.");
		} else {
			SQLiteDatabase db = SQLiteDatabase.openDatabase(db_path, null,
					SQLiteDatabase.OPEN_READONLY);
			Cursor c = db.rawQuery(SQL_DUMP_SCHEMA, new String[0]);
			if (c.moveToFirst()) {
				do {
					String create = c.getString(c.getColumnIndex("sql"));
					ut.writeToFile(filename, create + ";");
					Log.d(DEBUG_TAG, "export schema to " + filename + ": "
							+ create);
				} while (c.moveToNext());
			}
			c.close();
			db.close();
		}

	}
}