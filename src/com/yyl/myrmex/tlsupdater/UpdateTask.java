package com.yyl.myrmex.tlsupdater;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class UpdateTask extends AsyncTask<Void, Void, Boolean> {

	private Context context;
	private SQLiteDatabase db;
	private DatabaseExporter dbe;
	private MyTLSClient client;
	private HttpPost httppost;
	private HttpResponse getResponse;
	private String ns;
	private NotificationManager mNotificationManager;
	private NotificationCompat.Builder builder;

	private String db_name;
	private ArrayList<String> result;
	private String DEBUG_TAG = "AsyncTask: UpdateTask";
	private static int TASK_ID = 1;

	public UpdateTask(Context ctx, String name) {
		context = ctx;
		db_name = name;
		ns = Context.NOTIFICATION_SERVICE;
		mNotificationManager = (NotificationManager) context
				.getSystemService(ns);
		builder = new NotificationCompat.Builder(context)
				.setContentTitle("Task test").setContentText("This is a test.")
				.setSmallIcon(R.drawable.ic_launcher).setOngoing(true);

		Log.d(DEBUG_TAG, "task created");
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		Log.i(DEBUG_TAG, "now doing it in the background!");

		Notification notification = builder.getNotification();
		mNotificationManager.notify(TASK_ID, notification);

		String db_path = context.getDatabasePath(db_name).getAbsolutePath();
		Log.i(DEBUG_TAG, "db full path: " + db_path);
		db = SQLiteDatabase.openDatabase(db_path, null,
				SQLiteDatabase.OPEN_READWRITE);
		dbe = new DatabaseExporter(db);

		long endTime = System.currentTimeMillis() + 5 * 1000;
		while (System.currentTimeMillis() < endTime) {
			synchronized (this) {
				try {
					wait(endTime - System.currentTimeMillis());
				} catch (Exception e) {
				}
			}
		}

		try {
			result = dbe.export(db_name);
			Log.i(DEBUG_TAG, "db data:" + result);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		// client = new MyTLSClient(context);
		// // HttpClient client = new DefaultHttpClient();
		// httppost = new HttpPost("https://107.22.124.12");
		// // httppost = new HttpPost("http://107.22.124.12");
		// try {
		// for (int i = 0; i < 5; i++) {
		// // Add your data
		// List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		// nameValuePairs.add(new BasicNameValuePair("id",
		// Integer.toString(i)));
		// httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		//
		// // Execute HTTP Post Request
		// Log.d(DEBUG_TAG, "send the post packet");
		// // getResponse = client.execute(httppost);
		// String response = client.execute(httppost, new
		// BasicResponseHandler());
		// Log.d(DEBUG_TAG, "sending complete");
		// // HttpEntity responseEntity = getResponse.getEntity();
		// Log.d(DEBUG_TAG, "get the response");
		// System.out.println(response);
		// Log.d(DEBUG_TAG, "print out the response");
		// }
		// return true;
		// } catch (IOException e) {
		// e.printStackTrace();
		// return false;
		// }
	}

	// can use UI thread here
	protected void onPostExecute(final Boolean success) {
		if (success) {
			Toast.makeText(context, "Task successful", Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(context, "Task failed", Toast.LENGTH_SHORT).show();
		}
		mNotificationManager.cancel(TASK_ID);
	}

}