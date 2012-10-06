package com.yyl.myrmex.tlsupdater;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.yyl.myrmex.tlsupdater.database.TestDatabaseHelper;

public class MainActivity extends Activity {

	private Button b1, b2;
	private TLSUpdater tls;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		b1 = (Button) findViewById(R.id.start);
		b1.setOnClickListener(mStartListener);
		b2 = (Button) findViewById(R.id.stop);
		b2.setOnClickListener(mStopListener);
		tls = new TLSUpdater(getBaseContext(), "test.db", 10, 39);

		TestDatabaseHelper database = new TestDatabaseHelper(getBaseContext());
		database.getWritableDatabase();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private OnClickListener mStartListener = new OnClickListener() {
		public void onClick(View v) {
			tls.run();
			// tls.exportSchema();

		}
	};

	private OnClickListener mStopListener = new OnClickListener() {
		public void onClick(View v) {
			tls.stop();
		}
	};

}
