package com.yyl.myrmex.tlsupdater;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

public class TLSUpdater {
	private Context context;
	private Intent alarm_intent;
	private PendingIntent upload;
	private AlarmManager alarmm;
	private File logFile, dir;
	private String dbname = "nodb";
	private int hour, minute;

	private String DEBUG_TAG = "TLSUpdater";
	private static final String LOG_PATH = "/tlsupdater";

	public TLSUpdater(Context ctx, String dbname, int hour, int minute) {
		context = ctx;
		alarmm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		this.dbname = dbname;
		this.hour = hour;
		this.minute = minute;
		// initialization for file IO, create the folder to contain files
		dir = new File(Environment.getExternalStorageDirectory(), LOG_PATH);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	public void run() {
		// get a Calendar object with current time
		Calendar updateTime = Calendar.getInstance();
		updateTime.setTimeZone(TimeZone.getDefault());
		updateTime.set(Calendar.HOUR_OF_DAY, this.hour);
		updateTime.set(Calendar.MINUTE, this.minute);
		// updateTime.add(Calendar.SECOND, START_DELAY);
		Log.i(DEBUG_TAG, "Set the alarm to the time: " + updateTime.getTime());

		alarm_intent = new Intent(context, AlarmReceiver.class);
		alarm_intent.putExtra("dbname", dbname);
		alarm_intent.putExtra("hour", this.hour);
		alarm_intent.putExtra("minute", this.minute);
		upload = PendingIntent.getBroadcast(context, 0, alarm_intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		alarmm.setRepeating(AlarmManager.RTC_WAKEUP,
				updateTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, upload);
		// alarmm.setInexactRepeating(AlarmManager.RTC_WAKEUP,
		// updateTime.getTimeInMillis(), UPDATE_INTERVAL, upload);
	}

	public void stop() {
		Log.i(DEBUG_TAG, "Stop the alarm");
		alarm_intent = new Intent(context, AlarmReceiver.class);
		upload = PendingIntent.getBroadcast(context, 0, alarm_intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		alarmm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmm.cancel(upload);
	}

	public void exportSchema(String create_query) {
		String filename = this.dbname.replace(".db", "");
		Log.i(DEBUG_TAG, "Export schema to file " + filename);
		logFile = new File(dir, filename);
		if (!logFile.exists()) {
			try {
				Log.i(DEBUG_TAG, "File does not exist, creating it now");
				logFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			// BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
					true));
			buf.append(create_query);
			buf.newLine();
			buf.close();
			Log.i(DEBUG_TAG, "Writing a new line to the file");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}