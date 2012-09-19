package com.yyl.myrmex.tlsupdater;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;



import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;


public class TLSUpdater {
	private Context context;
	private Intent alarm_intent;
	private PendingIntent upload;
	private AlarmManager alarmm;
	
	private String dbname;
	private long UPDATE_INTERVAL = 30000;
	private int START_DELAY = 2;
	private String DEBUG_TAG = "TLSUpdater";
	
	public TLSUpdater(Context ctx, String dbname) {
		context = ctx;
		alarmm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		this.dbname = dbname;
	}
	
	public void run() {
		Log.d(DEBUG_TAG, "Set the alarm");
		// get a Calendar object with current time
		Calendar cal = Calendar.getInstance();
		// add 5 minutes to the calendar object
		cal.add(Calendar.SECOND, START_DELAY);
		
		alarm_intent = new Intent(context, AlarmReceiver.class);
		alarm_intent.putExtra("dbname", dbname);
		upload = PendingIntent.getBroadcast(context, 0, alarm_intent, PendingIntent.FLAG_CANCEL_CURRENT);
		alarmm.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), UPDATE_INTERVAL, upload);
	}
	
	public void stop() {
		Log.d(DEBUG_TAG, "Stop the alarm");
		alarm_intent = new Intent(context, AlarmReceiver.class);
		upload = PendingIntent.getBroadcast(context, 0, alarm_intent, PendingIntent.FLAG_CANCEL_CURRENT);
		alarmm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmm.cancel(upload);
	}
}