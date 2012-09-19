package com.yyl.myrmex.tlsupdater.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TestTable {

	// Database table
	public static final String TABLE_NAME = "ttable";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TIME = "time";
	public static final String COLUMN_VALUES = "value";

	// Database creation SQL statement
	private static final String TABLE_CREATE = "create table " 
			+ TABLE_NAME
			+ " (" 
			+ COLUMN_ID + " integer primary key autoincrement, " 
			+ COLUMN_TIME + " text not null, "
			+ COLUMN_VALUES + " text not null"
			+ ");";

	private static final String TABLE_ADD_SAMPLE = "INSERT INTO " +
			TABLE_NAME
			+ " ("
			+ COLUMN_TIME
			+ ", "
			+ COLUMN_VALUES
			+ ") VALUES ("
			+ "'2011', 'thevalue'"
			+ ");";
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		database.execSQL(TABLE_CREATE);
		System.out.println("database created");
		database.execSQL(TABLE_ADD_SAMPLE);
		System.out.println("new entry added");
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		Log.w(TestTable.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(database);
	}
}