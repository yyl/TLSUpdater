package com.yyl.myrmex.tlsupdater;

import java.io.IOException;
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
import com.yyl.myrmex.tlsupdater.TLSUpdater;

public class UpdateIntent extends IntentService {

	private AlarmManager alarmm;
	private Intent alarm_intent;
	private PendingIntent upload;
	private Context context;
	private SQLiteDatabase db;
	private DataStreamer dstreamer;
	private String db_name;
	private SharedPreferences spref;

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
	}

	/**
	 * The IntentService calls this method from the default worker thread with
	 * the intent that started the service. When this method returns,
	 * IntentService stops the service, as appropriate.
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i(DEBUG_TAG, "Receiving an intent, update service starts...");
		db_name = intent.getStringExtra("dbName");
		if (hasConnectivity()) {
			Log.i(DEBUG_TAG,
					"Connectivity is good; now start the upload task...");
			String db_path = context.getDatabasePath(db_name).getAbsolutePath();

			db = SQLiteDatabase.openDatabase(db_path, null,
					SQLiteDatabase.OPEN_READWRITE);
			dstreamer = new DataStreamer(db, context);
			try {
				dstreamer.stream(db_name);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			Log.i(DEBUG_TAG, "No connectivity available at this time.");
			alarmm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			alarm_intent = new Intent(getBaseContext(), TLSAlarmReceiver.class);
			int hour = intent.getIntExtra("hour", 18);
			int minute = intent.getIntExtra("minute", 0) + 2;
			alarm_intent.putExtra("dbName", db_name);
			alarm_intent.putExtra("hour", hour);
			alarm_intent.putExtra("minute", minute);
			upload = PendingIntent.getBroadcast(getBaseContext(), 0,
					alarm_intent, PendingIntent.FLAG_CANCEL_CURRENT);
			// get a Calendar object with current time
			Calendar updateTime = Calendar.getInstance();
			updateTime.setTimeZone(TimeZone.getDefault());

			// reschedule: delay 1 hour or set back to default if it is another
			// day
			if (hour >= 24) {
				Log.i(DEBUG_TAG,
						"Stop the alarm due to consecutively fail to upload the data.");
				alarmm.cancel(upload);

				int default_hour = spref.getInt("hour", 18);
				int default_minute = spref.getInt("minute", 0);
				updateTime.set(Calendar.HOUR_OF_DAY, default_hour);
				updateTime.set(Calendar.MINUTE, default_minute);
				Log.i(DEBUG_TAG, "Reset to the initial alarm time: "
						+ updateTime.getTime());
			} else {
				updateTime.set(Calendar.HOUR_OF_DAY, hour);
				updateTime.set(Calendar.MINUTE, minute);
				Log.i(DEBUG_TAG,
						"Reset the alarm to the time: " + updateTime.getTime());
			}
			alarmm.setRepeating(AlarmManager.RTC_WAKEUP,
					updateTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY,
					upload);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(DEBUG_TAG, "Upload service finished.");
	}

	private boolean hasConnectivity() {
		ConnectivityManager cm = (ConnectivityManager) getBaseContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}
}