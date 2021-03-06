package com.yyl.myrmex.tlsupdater;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Scanner;
import java.util.TimeZone;

import android.os.Environment;
import android.util.Log;

public class Utilities {
	private File dir;
	private Scanner sc;
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
			Log.i(DEBUG_TAG, "Writing " + line + " to the file");
			BufferedWriter buf = new BufferedWriter(new FileWriter(dateFile,
					true));
//			buf.append("[" + currentTime() + "]:" + line);
			buf.append(line);
			buf.newLine();
			buf.close();
			Log.i(DEBUG_TAG, "Writing complete");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public String readLineFromFile(String filename) {
		sc = null;
		try {
			sc = new Scanner(new File(this.dir, filename));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (sc.hasNextLine()) {
			Log.i(DEBUG_TAG, "Read one line from the file " + filename);
			return sc.nextLine();
		} else
			return null;
	}

	public boolean hasNextLine(String filename) {
		try {
			sc = new Scanner(new File(this.dir, filename));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sc.hasNextLine();
	}

	public void removeFromFile(String file, String lineToRemove) {
		Log.i(DEBUG_TAG, "Deleting " + lineToRemove + " from file " + file);
		try {
			File inFile = new File(dir, file);

			if (!inFile.isFile()) {
				System.out.println("Parameter is not an existing file");
				return;
			}

			// Construct the new file that will later be renamed to the original
			// filename.
			File tempFile = new File(inFile.getAbsolutePath() + ".tmp");

			BufferedReader br = new BufferedReader(new FileReader(inFile));
			PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

			String line = null;

			// Read from the original file and write to the new
			// unless content matches data to be removed.
			while ((line = br.readLine()) != null) {

				if (!line.trim().equals(lineToRemove)) {

					pw.println(line);
					pw.flush();
				}
			}
			pw.close();
			br.close();

			// Delete the original file
			if (!inFile.delete()) {
				System.out.println("Could not delete file");
				return;
			}

			// Rename the new file to the filename the original file had.
			if (!tempFile.renameTo(inFile))
				System.out.println("Could not rename file");

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		Log.i(DEBUG_TAG, "Deleting complete.");
	}

	public String currentTime() {
		Calendar calendar = Calendar.getInstance();
		String format = "yyyyMMdd-hh:mm:ss";
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
		sdf.setTimeZone(TimeZone.getDefault());
		String gmtTime = sdf.format(calendar.getTimeInMillis());
		return gmtTime;
	}
}