package com.yyl.myrmex.tlsupdater;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class UpdateIntent extends IntentService {

	private AlarmManager alarmm;
	private Intent alarm_intent;
	private PendingIntent upload;
	private Context context;
	private NotificationManager mNotificationManager;
	private NotificationCompat.Builder builder;
	private SQLiteDatabase db;
	// private DatabaseExporter dbe;
	private MyTLSClient tlser;

	private String db_name;
	private ArrayList<String> result;
	private static int TASK_ID = 1;
	private String DEBUG_TAG = "IntentService: UpdateIntent";

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
		Log.i(DEBUG_TAG, "Welcome to the new updateTask, update intent!");
	}

	/**
	 * The IntentService calls this method from the default worker thread with
	 * the intent that started the service. When this method returns,
	 * IntentService stops the service, as appropriate.
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i(DEBUG_TAG, "Receiving an intent");
		if (hasConnectivity()) {
			Log.i(DEBUG_TAG, "Connectivity is good; now start the upload task.");
			context = getBaseContext();
			
			mNotificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			builder = new NotificationCompat.Builder(context)
					.setContentTitle("Task test")
					.setContentText("This is a test.")
					.setSmallIcon(R.drawable.ic_launcher).setOngoing(true);
			Notification notification = builder.getNotification();
			mNotificationManager.notify(TASK_ID, notification);

			db_name = (String) intent.getCharSequenceExtra("dbname");
			String db_path = context.getDatabasePath(db_name).getAbsolutePath();
			Log.i(DEBUG_TAG, "db full path: " + db_path);
			db = SQLiteDatabase.openDatabase(db_path, null,
					SQLiteDatabase.OPEN_READWRITE);
			// dbe = new DatabaseExporter(db);
			// try {
			//
			// result = dbe.export(db_name);
			// Log.i(DEBUG_TAG, "db data:" + result);
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
			tlser = new MyTLSClient(context);
			String serverUrl = "https://ec2-174-129-109-240.compute-1.amazonaws.com/cgi-bin/echo.py";
			HttpPost httppost = new HttpPost(serverUrl);
			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("user", "yyl"));
				nameValuePairs.add(new BasicNameValuePair("time", "20120929"));

				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				HttpResponse response = tlser.execute(httppost);
				Log.d(DEBUG_TAG, "get response from the server");
				InputStream inputStream = response.getEntity().getContent();
				BufferedReader r = new BufferedReader(new InputStreamReader(
						inputStream));
				String line;
				while ((line = r.readLine()) != null) {
					Log.i(DEBUG_TAG, line);
				}
				Log.i(DEBUG_TAG, "request complete");
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			Log.i(DEBUG_TAG, "No connectivity available at this time.");
			alarmm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			alarm_intent = new Intent(getBaseContext(), AlarmReceiver.class);
			int hour = intent.getIntExtra("hour", 18);
			int minute = intent.getIntExtra("minute", 0) + 2;
			alarm_intent.putExtra("dbname",
					intent.getCharSequenceExtra("dbname"));
			alarm_intent.putExtra("hour", hour);
			alarm_intent.putExtra("minute", minute);
			upload = PendingIntent.getBroadcast(getBaseContext(), 0,
					alarm_intent, PendingIntent.FLAG_CANCEL_CURRENT);
			if (minute >= 60) {
				Log.i(DEBUG_TAG,
						"Stop the alarm due to consecutively fail to upload the data.");
				alarmm.cancel(upload);
			} else {
				// get a Calendar object with current time
				Calendar updateTime = Calendar.getInstance();
				updateTime.setTimeZone(TimeZone.getDefault());
				updateTime.set(Calendar.HOUR_OF_DAY, hour);
				updateTime.set(Calendar.MINUTE, minute);
				Log.i(DEBUG_TAG,
						"Reset the alarm to the time: " + updateTime.getTime());
				alarmm.setRepeating(AlarmManager.RTC_WAKEUP,
						updateTime.getTimeInMillis(),
						AlarmManager.INTERVAL_DAY, upload);
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mNotificationManager.cancel(TASK_ID);
		Log.i(DEBUG_TAG, "Intent service finished.");
	}
	
	public boolean hasConnectivity() {
		ConnectivityManager cm = (ConnectivityManager) getBaseContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}
}