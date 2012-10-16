package com.yyl.myrmex.tlsupdater.database;


import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TestTable2 {

	// Database table
	public static final String TABLE_NAME = "ttable2";
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
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		database.execSQL(TABLE_CREATE);
		System.out.println("database created");
		database.execSQL(addOneEntry("20120925", "value1of20120925"));
		database.execSQL(addOneEntry("20120925", "value2of20120925"));
		database.execSQL(addOneEntry("20120925", "value3of20120925"));
		database.execSQL(addOneEntry("20121001", "value1of20121001"));
		database.execSQL(addOneEntry("20121001", "value2of20121001"));
		database.execSQL(addOneEntry("20121003", "value1of20121003"));
		database.execSQL(addOneEntry("20121003", "value2of20121003"));
		database.execSQL(addOneEntry("20121003", "value3of20121003"));
		System.out.println("new entry added");
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		Log.w(TestTable2.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(database);
	}
	
	private static String addOneEntry(String time, String value) {
		String ADD = "INSERT INTO " +
				TABLE_NAME
				+ " ("
				+ COLUMN_TIME
				+ ", "
				+ COLUMN_VALUES
				+ ") VALUES ("
				+ "'" + time + "', '" + value + "'"
				+ ");";
		return ADD;
	}
}