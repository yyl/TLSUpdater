package com.yyl.myrmex.tlsupdater.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TestDatabaseHelper extends SQLiteOpenHelper {

	public static final String DEBUG_TAG = "databse debug";
	private static final String DATABASE_NAME = "test.db";
	private static final int DATABASE_VERSION = 3;

	public TestDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Method is called during creation of the database
	@Override
	public void onCreate(SQLiteDatabase database) {
		TestTable.onCreate(database);
	}

	// Method is called during an upgrade of the database,
	// e.g. if you increase the database version
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		TestTable.onUpgrade(database, oldVersion, newVersion);
	}
}