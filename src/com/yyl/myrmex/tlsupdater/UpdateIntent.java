package com.yyl.myrmex.tlsupdater;

import java.io.File;
import java.util.Calendar;
import java.util.TimeZone;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class UpdateIntent extends IntentService {

	private AlarmManager alarmm;
	private Intent alarm_intent;
	private PendingIntent upload;
	private Context context;
	private SQLiteDatabase db;
	private DataStreamer dstreamer;
	private String db_name;
	private SharedPreferences spref;
	private Utilities ut;

	private static final String DEBUG_TAG = "IntentService: UpdateIntent";

	/**
	 * A constructor is required, and must call the super IntentService(String)
	 * constructor with a name for the worker thread.
	 */
	public UpdateIntent() {
		super("UpdateIntent");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		context = getBaseContext();
		spref = getSharedPreferences(TLSUpdater.TLS_PREF, 0);
		this.ut = new Utilities();
	}

	/**
	 * The IntentService calls this method from the default worker thread with
	 * the intent that started the service. When this method returns,
	 * IntentService stops the service, as appropriate.
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i(DEBUG_TAG, "Receiving an intent, update service starts...");
		ut.writeToFile("log.txt",
				"UpdateIntent.onHandleIntent(): Receiving an intent to trigger the alarm...");
		db_name = intent.getStringExtra("dbName");
		String db_path = context.getDatabasePath(db_name).getAbsolutePath();
		File fdb = new File(db_path);

		// first check if the db has been created or not
		if (!fdb.exists()) {
			Log.i(DEBUG_TAG,
					"DB has been created yet. Gonna skip the uploading this time");
			ut.writeToFile(
					"log.txt",
					"UpdateIntent.onHandleIntent(): DB has not been created yet. Gonna skip the uploading this time.");
		} else {
			db = SQLiteDatabase.openDatabase(db_path, null,
					SQLiteDatabase.OPEN_READONLY);

			dstreamer = new DataStreamer(db, db_name, context);
			if (dstreamer.moveToFirst()) {
				boolean success = true;
				do {
					if (!hasConnectivity() || !success) {
						dstreamer.close();
						reschedule(intent);
						break;
					}
					success = dstreamer.sendPkt();
				} while (dstreamer.moveToNext());
			}
			dstreamer.close();
			db.close();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(DEBUG_TAG,
				"UpdateIntent.onHandleIntent(): Upload service finished.");
		ut.writeToFile("log.txt",
				"UpdateIntent.onHandleIntent(): Upload service finished.\n"
						+ "=============================================\n");
	}

	private void reschedule(Intent intent) {
		Log.i(DEBUG_TAG, "No connectivity available or uploading failed.");
		ut.writeToFile("log.txt",
				"UpdateIntent.reschedule(): no connectivity detected. Plan to reschedule");
		alarmm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarm_intent = new Intent(getBaseContext(), TLSAlarmReceiver.class);
		int hour = intent.getIntExtra("hour", 18);
		int minute = intent.getIntExtra("minute", 0);
		int alarm_id = intent.getIntExtra("alarmId", 0);
		alarm_intent.putExtra("dbName", db_name);
		alarm_intent.putExtra("hour", hour);
		alarm_intent.putExtra("minute", minute);
		alarm_intent.putExtra("alarmId", alarm_id);
		upload = PendingIntent.getBroadcast(getBaseContext(), alarm_id,
				alarm_intent, PendingIntent.FLAG_CANCEL_CURRENT);
		// get a Calendar object with current time
		Calendar updateTime = Calendar.getInstance();
		updateTime.setTimeZone(TimeZone.getDefault());

		// reschedule: delay 1 hour or set back to default if it is another
		// day
		if (hour == 0) {
			Log.i(DEBUG_TAG,
					"Stop the alarm due to consecutively fail to upload the data. "
							+ hour);

			updateTime.add(Calendar.HOUR_OF_DAY, spref.getInt("hour", 0));
			updateTime.set(Calendar.MINUTE, spref.getInt("minute", 0));
			Log.i(DEBUG_TAG,
					"Reset to the initial alarm time: " + updateTime.getTime());
			ut.writeToFile("log.txt",
					"UpdateIntent.reschedule(): Reset to the initial alarm time: "
							+ updateTime.getTime() + "\n");
		} else {
			updateTime.add(Calendar.HOUR_OF_DAY, 1);
			updateTime.set(Calendar.MINUTE, minute);
			Log.i(DEBUG_TAG,
					"Reset the alarm to the time: " + updateTime.getTime());
			ut.writeToFile("log.txt",
					"UpdateIntent.reschedule(): Reset the alarm to the time: "
							+ updateTime.getTime() + "\n");
		}
		alarmm.setRepeating(AlarmManager.RTC_WAKEUP,
				updateTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, upload);
	}

	private boolean hasConnectivity() {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}
}