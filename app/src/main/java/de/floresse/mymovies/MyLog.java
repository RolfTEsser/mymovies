package de.floresse.mymovies;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MyLog {

    static String filename = "MyMoviesLog.txt";
    static boolean logfile = true;
    static boolean append = true;
    static File path = null;
    static File file = null;
    static Calendar calendar = Calendar.getInstance();
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static void i(String tag, String msg) {

        Log.i(tag, msg);

        String strDate = sdf.format(calendar.getTime());

        if (logfile) {
            if (path==null) {
                path = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOCUMENTS);
                path.mkdirs();
            }
            if (file==null) {
                file = new File(path, filename);
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    Log.w("MyMovies - LogFile", "Error creating " + file, e);
                }
            }
            try {
                String line = strDate + " " + tag + " " + msg + System.lineSeparator();
                OutputStream os = new FileOutputStream(file, append);
                os.write(line.getBytes());
                os.close();

            } catch (IOException e) {
                Log.w("MyMovies - LogFile", "Error writing " + file, e);
            }
        }
    }
}
