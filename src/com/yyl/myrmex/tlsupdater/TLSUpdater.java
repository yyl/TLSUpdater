package com.yyl.myrmex.tlsupdater;

import java.io.InputStream;
import java.security.KeyStore;
import java.util.Calendar;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

import com.yyl.myrmex.api.tlsnode.R;
import com.yyl.myrmex.api.tlsnode.Uploader.MyTLSClient;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;


public class TLSUpdater {
	private Context context;
	private PendingIntent alarm;
	private AlarmManager alarm_manager;
	private long UPDATE_INTERVAL = 3000;
	private MyAlarmReceiver my_receiver;
	private IntentFilter intentf;
	
	private int START_DELAY = 2;
	private String DEBUG_TAG = "TLSUpdater";
	
	public TLSUpdater(Context ctx) {
		context = ctx;
		alarm_manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		my_receiver = new MyAlarmReceiver();
		intentf = new IntentFilter("com.yyl.myrmex.tlsupdater.TLSUpdater.uploadalarm");
		
	}
	
	public void run() {
		Log.d(DEBUG_TAG, "TLSUpdater.run()");
		context.registerReceiver(my_receiver, intentf);
		// get a Calendar object with current time
		Calendar cal = Calendar.getInstance();
		// add 5 minutes to the calendar object
		cal.add(Calendar.SECOND, START_DELAY);
 
		Intent intent = new Intent("com.yyl.myrmex.tlsupdater.TLSUpdater.uploadalarm");
    
    	alarm = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    
    	alarm_manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), UPDATE_INTERVAL, alarm);
	}
	
	public void stop() {
		Intent intent = new Intent(context, MyAlarmReceiver.class);
		alarm = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		alarm_manager.cancel(alarm);
		context.unregisterReceiver(my_receiver);
	}
	
	private class MyAlarmReceiver extends BroadcastReceiver {

		private static final String DEBUG_TAG = "MyAlarmReceiver";
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Log.d(DEBUG_TAG, "Recurring alarm; requesting location tracking.");
			Toast.makeText(context, "hello alarm", Toast.LENGTH_SHORT).show();
	        // start the service
	        Intent updater = new Intent(context, Updater.class);
	        context.startService(updater);
		}
		
	}
	
	private class Updater extends AsyncTask {

		private MyTLSClient client;
		private Context context;
		
		public Updater(Context ctx) {
			context = ctx;
		}
		
		@Override
		protected Object doInBackground(Object... params) {
			client = new MyTLSClient(context);
			return null;
		}
		
	}
	
	private class MyTLSClient extends DefaultHttpClient {
		 
	    final Context context;
	 
	    public MyTLSClient(Context context) {
	        this.context = context;
	    }
	 
	    @Override
	    protected ClientConnectionManager createClientConnectionManager() {
	        SchemeRegistry registry = new SchemeRegistry();
	        // Register for port 443 our SSLSocketFactory with our keystore
	        // to the ConnectionManager
	        registry.register(new Scheme("https", newSslSocketFactory(), 443));
	        SingleClientConnManager cm = new SingleClientConnManager (
	        		getParams(), registry);
	        return cm;
	    }
	 
	    private SSLSocketFactory newSslSocketFactory() {
	        try {
	            // Get an instance of the Bouncy Castle KeyStore format
	            KeyStore trusted = KeyStore.getInstance("BKS");
	            // Get the raw resource, which contains the keystore with
	            // your trusted certificates (root and any intermediate certs)
	            InputStream in = context.getResources().openRawResource(R.raw.mykeystore);
	            try {
	                // Initialize the keystore with the provided trusted certificates
	                // Also provide the password of the keystore
	                trusted.load(in, "tlstest".toCharArray());
	            } finally {
	                in.close();
	            }
	            // Pass the keystore to the SSLSocketFactory. The factory is responsible
	            // for the verification of the server certificate.
	            SSLSocketFactory sf = new SSLSocketFactory(trusted);
	            // Hostname verification from certificate
	            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	            return sf;
	        } catch (Exception e) {
	            throw new AssertionError(e);
	        }
	    }
	}

}