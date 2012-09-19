package com.yyl.myrmex.tlsupdater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

public class DatabaseExporter {

   private static final String DEBUG_TAG = "DBExporter";
   private SQLiteDatabase db;
   private ArrayList<String> result;

   public DatabaseExporter(SQLiteDatabase db) {
	   result = new ArrayList<String>();
	   this.db = db;
   }

   public ArrayList export(String dbName) throws IOException {
      // get the tables
      String sql = "select * from sqlite_master";
      Cursor c = this.db.rawQuery(sql, new String[0]);
      Log.d(DEBUG_TAG, "select * from sqlite_master, cur size " + c.getCount());
      if (c.moveToFirst()) {
         do {
            String tableName = c.getString(c.getColumnIndex("name"));
            Log.d(DEBUG_TAG, "table name " + tableName);

            // skip metadata, sequence, and uidx (unique indexes)
            if (!tableName.equals("android_metadata") && !tableName.equals("sqlite_sequence")
                     && !tableName.startsWith("uidx")) {
               this.exportTable(tableName);
            }
         } while (c.moveToNext());
      }
      Log.i(DEBUG_TAG, "exporting database complete");
      return result;
   }

   private void exportTable(final String tableName) throws IOException {
      Log.d(DEBUG_TAG, "exporting table - " + tableName);
      String sql = "select * from " + tableName;
      Cursor c = this.db.rawQuery(sql, new String[0]);
      if (c.moveToFirst()) {
         int cols = c.getColumnCount();
         do {
        	String row = "";
            for (int i = 0; i < cols; i++) {
               row += c.getString(i);
               if(i != cols - 1)
            	   row += ", ";
            }
            result.add(row);
         } while (c.moveToNext());
      }
      c.close();
   }
}
