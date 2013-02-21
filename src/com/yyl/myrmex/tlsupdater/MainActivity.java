package com.yyl.myrmex.tlsupdater;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.yyl.myrmex.tlsupdater.database.TestContentProvider;
import com.yyl.myrmex.tlsupdater.database.TestDatabaseHelper;
import com.yyl.myrmex.tlsupdater.database.TestTable;

public class MainActivity extends FragmentActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {

	private Button b1, b2;
	private TLSUpdater tls, tls2;
	private SQLiteDatabase db;
	private SimpleCursorAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		drawTable();
		TestDatabaseHelper database = new TestDatabaseHelper(getBaseContext());
		db = database.getWritableDatabase();
		// TestDatabase2Helper database2 = new
		// TestDatabase2Helper(getBaseContext());
		// database2.getWritableDatabase();
		b1 = (Button) findViewById(R.id.start);
		b1.setOnClickListener(mStartListener);
		b2 = (Button) findViewById(R.id.stop);
		b2.setOnClickListener(mStopListener);
		tls = new TLSUpdater(getBaseContext(), "test.db", 23, 25);
		tls.run();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private OnClickListener mStartListener = new OnClickListener() {
		public void onClick(View v) {
			ContentValues values = new ContentValues();
			values.put(TestTable.COLUMN_TIME, currentTime());
			values.put(TestTable.COLUMN_VALUES, Math.random() * 1000);
			db.insert(TestTable.TABLE_NAME, null, values);
		}
	};

	private OnClickListener mStopListener = new OnClickListener() {
		public void onClick(View v) {
			tls.stop();
			// tls2.stop();
		}
	};

	public String currentTime() {
		Calendar calendar = Calendar.getInstance();
		String format = "yyyyMMdd-hh:mm:ss";
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
		sdf.setTimeZone(TimeZone.getDefault());
		String gmtTime = sdf.format(calendar.getTimeInMillis());
		return gmtTime;
	}

	private void drawTable() {
		// Fields from the database (projection)
		// Must include the _id column for the adapter to work
		String[] from = new String[] { TestTable.COLUMN_ID,
				TestTable.COLUMN_TIME, TestTable.COLUMN_VALUES };
		// Fields on the UI to which we map
		int[] to = new int[] { R.id._id, R.id.time, R.id.value };
		getSupportLoaderManager().initLoader(0, null, this);
		adapter = new SimpleCursorAdapter(this, R.layout.row, null, from, to, 0);
		ListView listview = (ListView) findViewById(R.id.list);
		listview.setAdapter(adapter);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = { TestTable.COLUMN_ID, TestTable.COLUMN_TIME,
				TestTable.COLUMN_VALUES };
		CursorLoader cursorLoader = new CursorLoader(this,
				TestContentProvider.CONTENT_URI, projection, null, null, null);
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// TODO Auto-generated method stub
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// TODO Auto-generated method stub
		adapter.swapCursor(null);
	}

}
