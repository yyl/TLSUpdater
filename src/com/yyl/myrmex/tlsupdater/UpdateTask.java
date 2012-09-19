package com.yyl.myrmex.tlsupdater;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import android.app.NotificationManager;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class UpdateTask extends AsyncTask<Void, Void, Boolean> {

		private Context context;
		private SQLiteDatabase db;
		private DatabaseExporter dbe;
		private MyTLSClient client;
		private HttpPost httppost;
		private HttpResponse getResponse;
		private String db_name;
		private ArrayList<String> result;
		private String DEBUG_TAG = "AsyncTask: UpdateTask";
		
		public UpdateTask (Context ctx, String name) {
			context = ctx;
			db_name = name;
			Log.d(DEBUG_TAG, "task created");
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			Log.d(DEBUG_TAG, "now doing it in the background!");
			db = SQLiteDatabase.openDatabase(context.getDatabasePath(db_name).getAbsolutePath(),
												null, SQLiteDatabase.OPEN_READWRITE);
			dbe = new DatabaseExporter(db);
			
//			try {
//				result = dbe.export(db_name);
//				Log.d(DEBUG_TAG, "db data:" + result);
//				return true;
//			} catch (IOException e) {
//				e.printStackTrace();
//				return false;
//			}
			
			client = new MyTLSClient(context);
			httppost = new HttpPost("https://107.22.124.12");
			try {
				for (int i = 0; i < 5; i++) {
					// Add your data
			        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			        nameValuePairs.add(new BasicNameValuePair("id", Integer.toString(i)));
			        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			        // Execute HTTP Post Request
			        Log.d(DEBUG_TAG, "send the post packet");
			        getResponse = client.execute(httppost);
			        Log.d(DEBUG_TAG, "sending complete");
			        HttpEntity responseEntity = getResponse.getEntity();
			        Log.d(DEBUG_TAG, "get the response");
			        System.out.println(responseEntity.getContent());
			        Log.d(DEBUG_TAG, "print out the response");
				}
		        return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		// can use UI thread here
	    protected void onPostExecute(final Boolean success) {
	       if (success) {
	          Toast.makeText(context, "Task successful", Toast.LENGTH_SHORT).show();
	       } else {
	          Toast.makeText(context, "Task failed", Toast.LENGTH_SHORT).show();
	       }
	    }
	    

	}