package com.yyl.myrmex.tlsupdater;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

public class Utilities {
	private File dir;

	private static final String DEBUG_TAG = "Utilities";
	private static final String FILE_PATH = "/tlsupdater";

	public Utilities() {
		// create the dir that all files will go into
		dir = new File(Environment.getExternalStorageDirectory(), FILE_PATH);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	public boolean createFile(String filename) {
		File dateFile = new File(dir, filename);
		if (!dateFile.exists()) {
			try {
				Log.i(DEBUG_TAG, "File does not exist, creating it now");
				dateFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			Log.i(DEBUG_TAG, "File already exist, no need to create.");
		}
		return true;
	}

	public boolean writeToFile(String filename, String line) {
		createFile(filename);
		File dateFile = new File(dir, filename);
		try {
			// BufferedWriter for performance, true to set append to file flag
			Log.i(DEBUG_TAG, "Writing a new line to the file");
			BufferedWriter buf = new BufferedWriter(new FileWriter(dateFile,
					true));
			buf.append(line);
			buf.newLine();
			buf.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public String today() {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Date date = new Date();
		return dateFormat.format(date);
	}
}