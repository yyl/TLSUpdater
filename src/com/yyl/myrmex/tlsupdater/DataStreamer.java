package com.yyl.myrmex.tlsupdater;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DataStreamer {

	private SQLiteDatabase db;
	private MyTLSClient tlser;
	private PostMaker pmaker;
	private Context context;
	private Cursor db_cursor, table_cursor;
	private TablePointer tp;
	private Utilities ut;

	private static String dbName;
	private static final String DEBUG_TAG = "DataStreamer";
	// private static final String SERVER_URL =
	// "https://209.59.213.83/cgi-bin/dbhandler2.py";
	private static final String SERVER_URL = "YOUR_SERVER_SIDE_URL_HERE";
	private static final String SQL_GET_TABLES = "select name from sqlite_master where name not in "
			+ "(\"android_metadata\", \"sqlite_sequence\") "
			+ "and name not like \"uidx\"";

	public DataStreamer(SQLiteDatabase db, String dbName, Context ctx) {
		this.db = db;
		this.context = ctx;
		tlser = new MyTLSClient(context);
		this.dbName = dbName.replace(".db", "");
		this.ut = new Utilities();
	}

	public boolean moveToFirst() {
		db_cursor = this.db.rawQuery(SQL_GET_TABLES, new String[0]);
		// assuming this db is not null, i.e., it has at least 1 table (table
		// could be null)
		db_cursor.moveToFirst();
		String tableName = db_cursor
				.getString(db_cursor.getColumnIndex("name"));
		tp = new TablePointer(tableName);
		// tp.updatePosition(0);
		String table_sql = "select * from " + tableName + " where _id>"
				+ tp.getPosition();
		Log.i(DEBUG_TAG, "DB pointer moves to the first table: " + tableName
				+ " with starting id " + tp.getPosition());
		ut.writeToFile("log.txt",
				"DataStreamer.moveToFirst(): DB pointer moves to the first table: "
						+ tableName + " with starting id " + tp.getPosition());
		table_cursor = this.db.rawQuery(table_sql, new String[0]);
		boolean first_table_not_null = table_cursor.moveToFirst();
		if (!first_table_not_null) {
			Log.i(DEBUG_TAG, "This table is null.");
			if (db_cursor.isLast()) {
				Log.i(DEBUG_TAG,
						"This db has this only table and the table has no data to upload. Abort.");
				ut.writeToFile(
						"log.txt",
						"DataStreamer.moveToFirst(): This db has this only table and the table has no data to upload. Abort.");
				this.close();
				return false;
			}
			return moveToNext();
		}
		Log.i(DEBUG_TAG, "Current row pointer moves to the " + tp.getPosition()
				+ " row.");
		return first_table_not_null;
	}

	public boolean moveToNext() {
		if (table_cursor.isLast()
				|| (table_cursor.isBeforeFirst() && !table_cursor.moveToFirst())) {
			do {
				tp.uploadResult();
				db_cursor.moveToNext();
				if (db_cursor.isAfterLast()) {
					Log.i(DEBUG_TAG, "There is no more table in this db.");
					ut.writeToFile("log.txt",
							"DataStreamer.moveToNext(): There is no more table in this db.");
					this.close();
					return false;
				}
				String tableName = db_cursor.getString(db_cursor
						.getColumnIndex("name"));
				tp = new TablePointer(tableName);
				String table_sql = "select * from " + tableName + " where _id>"
						+ tp.getPosition();
				table_cursor.close();
				table_cursor = this.db.rawQuery(table_sql, new String[0]);
				Log.i(DEBUG_TAG, "Table pointer moves to the next one: "
						+ tableName + "with " + table_cursor.getCount()
						+ " row(s)");
			} while (!table_cursor.moveToFirst());
			return true;
		}
		table_cursor.moveToNext();
		Log.i(DEBUG_TAG, "Current row pointer moves to the next one.");
		return true;
	}

	public void close() {
		if (!db_cursor.isClosed()) {
			db_cursor.close();
		}

		if (!table_cursor.isClosed()) {
			table_cursor.close();
		}

	}

	public boolean sendPkt() {
		int flag = 0, cols;
		String md5key;
		boolean success;

		pmaker = new PostMaker(SERVER_URL);
		md5key = uniqueId();
		cols = table_cursor.getColumnCount();
		pmaker.putData("md5key", md5key);
		pmaker.putData("dbName", dbName);
		pmaker.putData("tableName", tp.getTableName());
		for (int i = 0; i < cols; i++) {
			String col_name = table_cursor.getColumnName(i);
			String col_val;
			if (tp.getType(col_name).equals("real"))
				col_val = Double.toString(table_cursor.getDouble(i));
			else
				col_val = table_cursor.getString(i);
			pmaker.putData(col_name, col_val);
			if (col_name.equals("_id")) {
				Log.d(DEBUG_TAG, "current value of _id: " + col_val);
				flag = Integer.parseInt(col_val);
				Log.d(DEBUG_TAG, "set flag to " + flag);
			}
		}
		HttpPost pkt = pmaker.packIt();
		Log.d(DEBUG_TAG, "post packet packed.");
		success = send(pkt);
		if (success) {
			tp.updatePosition(flag);
			tp.passOneEntry();
		}
		return success;
	}

	private boolean send(HttpPost pkt) {
		try {
			Log.d(DEBUG_TAG, "trying to send the packet...");
			HttpResponse response = tlser.execute(pkt);
			Log.d(DEBUG_TAG, "Got the response.");
			int status_code = response.getStatusLine().getStatusCode();
			ut.writeToFile("log.txt", "Status: " + status_code);
			String response_txt = EntityUtils.toString(response.getEntity());
			Log.i(DEBUG_TAG + " SERVER RESPONSE", response_txt);
			ut.writeToFile("log.txt", response_txt);
			switch (status_code / 100) {
			case 5:
				Log.d(DEBUG_TAG,
						"Code 5xx: Server error, will try it again later.");
				return false;
			case 4:
				Log.d(DEBUG_TAG,
						"Code 4xx: Client error, please check the code. Transmission failed.");
				return false;
			case 2:
				Log.d(DEBUG_TAG, "Code 200: Uploading success.");
				break;
			default:
				Log.d(DEBUG_TAG, "Unhandled response: status "
						+ response.getStatusLine().getStatusCode()
						+ response.getStatusLine().getReasonPhrase());
				break;
			}

			Log.i(DEBUG_TAG, "Packet transmission complete");
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			ut.writeToFile("log.txt", "DataStreamer.send(): " + e.getMessage());
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			ut.writeToFile("log.txt", "DataStreamer.send(): " + e.getMessage());
			return false;
		}
		return true;
	}

	private String uniqueId() {
		MessageDigest digest;
		TelephonyManager tm = (TelephonyManager) this.context
				.getSystemService(Context.TELEPHONY_SERVICE);
		try {
			digest = MessageDigest.getInstance("MD5");
			digest.reset();
			digest.update(tm.getDeviceId().getBytes());
			byte[] a = digest.digest();
			int len = a.length;
			StringBuilder sb = new StringBuilder(len << 1);
			for (int i = 0; i < len; i++) {
				sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
				sb.append(Character.forDigit(a[i] & 0x0f, 16));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			ut.writeToFile("log.txt",
					"DataStreamer.uniqueId(): " + e.getMessage());
		} catch (NullPointerException e) {
			e.printStackTrace();
			ut.writeToFile("log.txt",
					"DataStreamer.uniqueId(): " + e.getMessage());
		}
		return null;
	}

	private class PostMaker {
		private HttpPost httppost;
		private List<NameValuePair> nameValuePairs;

		public PostMaker(String serverUrl) {
			httppost = new HttpPost(serverUrl);
			nameValuePairs = new ArrayList<NameValuePair>(2);
		}

		private void putData(String key, String value) {
			nameValuePairs.add(new BasicNameValuePair(key, value));
		}

		private HttpPost packIt() {
			try {
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return httppost;
		}
	}

	private class TablePointer {

		private SharedPreferences spref;
		private SharedPreferences.Editor editor;
		private String tableName;
		private int uploaded, start_point;
		private HashMap<String, String> types;

		private static final String DEBUG_TAG = "TablePointer";

		public TablePointer(String tableName) {
			this.tableName = tableName;
			spref = context.getSharedPreferences(TLSUpdater.TLS_PREF, 0);
			editor = spref.edit();
			uploaded = 0;
			types = typeScanner();
		}

		private HashMap<String, String> typeScanner() {
			HashMap<String, String> temp = new HashMap<String, String>();
			String Query = "PRAGMA table_info(" + tableName + ")";
			Cursor my_cursor = db.rawQuery(Query, null);
			String Column_name, Column_type;
			while (my_cursor.moveToNext()) {
				Column_name = my_cursor.getString(my_cursor
						.getColumnIndex("name"));
				Column_type = my_cursor.getString(my_cursor
						.getColumnIndex("type"));
				temp.put(Column_name, Column_type);
			}
			my_cursor.close();
			return temp;
		}

		public String getTableName() {
			return tableName;
		}

		public int getPosition() {
			int position = spref.getInt(tableName, -1);
			if (position == -1) {
				Log.d(TablePointer.DEBUG_TAG,
						"Initializing the table pointer to 0...");
				editor.putInt(tableName, 0);
				editor.commit();
				position = 0;
			}
			return position;
		}

		public String getType(String column_name) {
			if (!types.containsKey(column_name))
				return null;
			return types.get(column_name);
		}

		public void updatePosition(int new_position) {
			start_point = spref.getInt(tableName, 0);
			Log.d(TablePointer.DEBUG_TAG, "Update the pointer to "
					+ new_position + " from " + start_point);
			editor.putInt(tableName, new_position);
			editor.commit();
		}

		public void passOneEntry() {
			uploaded++;
		}

		public void uploadResult() {
			Log.i(TablePointer.DEBUG_TAG, "Table " + tableName + ": "
					+ uploaded + " packets sent.");
			ut.writeToFile("log.txt",
					"DataStreamer.TablePointer.uploadResult(): Table "
							+ tableName + ": " + uploaded + " packets sent.");
		}

	}

}
