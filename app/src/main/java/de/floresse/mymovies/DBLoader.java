package de.floresse.mymovies;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.content.DialogInterface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

public class DBLoader extends Activity {
	
    public static final String RESULT_TIMESTAMP = "DBTIMESTAMP";
    public static final String MY_CHANNEL_ID = "MyMovies DBLoader";

    private static final int LOADSTEP2 = -1;
    
    // was ist schöner ? ProgressBar oder ProgressDialog
    private boolean bproDia = false;  // ProgressBar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Log.i("MyMovies", "DBLoader - onCreate");
        
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

		setContentView(R.layout.activity_main);
		SearchView sv = (SearchView)findViewById(R.id.searchView);
		sv.setEnabled(false);
		sv.setInputType(InputType.TYPE_NULL);
		sv.setVisibility(View.INVISIBLE);
		final Button rb = (Button)findViewById(R.id.goButton);
		rb.setText("Loading DB");
		rb.setVisibility(View.INVISIBLE);
		rb.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
			}
		});
		if (sharedPref.getBoolean("pref_isFullScreen", false)) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}	
	

  		new DBLoadTask().execute
  	        (new TaskParms[] { new TaskParms(sharedPref.getString("pref_key_dir_odts_movies", ""),
  	        								 sharedPref.getString("pref_key_dir_odts_books", ""),	
  	    		                             getApplicationContext()) } );
    	
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            //Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }
	
	public void onBackPressed() {
		new AlertDialog.Builder(this)
				.setTitle("Really Exit?")
				.setMessage("Are you sure you want to exit?")
				.setNegativeButton(android.R.string.no, null)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface m, int v) {
						DBLoader.super.onBackPressed();
					}
				}).create().show();
    }
    
	private class DBLoadTask extends AsyncTask<TaskParms, Integer, Integer> {


		private ProgressDialog progressDialog;
		private LinearLayout lilaProgressBar;
		private ProgressBar progressBar;

		private NotificationManager mNotifyManager = null;
		private Notification.Builder mBuilder = null;
		private NotificationChannel channel = null;
		private static final int NOTIFY_DBLOAD = 54321;

		private PowerManager pm = null;
		private PowerManager.WakeLock wakeLock = null;
		
	    private TextDatabase myTextDatabase = new TextDatabase(getApplicationContext());

		@Override
		protected void onPreExecute() {

			progressDialog = new ProgressDialog(getApplicationContext());
			lilaProgressBar = (LinearLayout) findViewById(R.id.lilaProgressBar);
			progressBar = (ProgressBar) findViewById(R.id.progressBar);

			pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyMovies:WakeLock");
			wakeLock.acquire();
			if (bproDia) {
				progressDialog.setCancelable(true);
				progressDialog.setMessage("searching ...");
				progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				progressDialog.setProgress(0);
				progressDialog.setMax(100);
				progressDialog.show();
			} else {
				progressBar.setProgress(0);
				progressBar.setMax(100);
				lilaProgressBar.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.VISIBLE);
				TextView proz = (TextView)findViewById(R.id.proProz);
				proz.setText("0 %");
				TextView anz = (TextView)findViewById(R.id.proAnz);
				anz.setText("0 / 0");
			}
            mNotifyManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				CharSequence name = "MyMovies DBLoader";
				String description = "Bla Bla Bla";
				int importance = NotificationManager.IMPORTANCE_LOW;
				channel = new NotificationChannel(MY_CHANNEL_ID, name, importance);
				channel.setDescription(description);
				// Register the channel with the system; you can't change the importance
				// or other notification behaviors after this
				mNotifyManager.createNotificationChannel(channel);
			}
			mBuilder = new Notification.Builder(getApplicationContext(), MY_CHANNEL_ID);
            mBuilder.setContentTitle("MyMovies - Suchtabellen")
                    .setContentText("2 Steps : ")
					.setCategory(Notification.CATEGORY_PROGRESS)
                    .setSmallIcon(R.drawable.ic_launcher)
					.setAutoCancel(true);

		}

		@Override
		protected Integer doInBackground(TaskParms... tps) {
			//
			SeSuExtractor mySeSuExtractor = new SeSuExtractor(getApplicationContext());
			myTextDatabase.mDatabaseOpenHelper.initNewContent();
			int countODTs = 0;
			for (TaskParms tp : tps) {
				String[] allMovies = new File(tp.dirMovies).list(new ODTFilter());
				String[] allBooks = new File(tp.dirBooks).list(new ODTFilter());
				countODTs = allMovies.length + allBooks.length;
				ArrayList<String> allFiles = new ArrayList<String>(countODTs); 
				for (int i=0;i<allMovies.length;i++) {
					allFiles.add(tp.dirMovies + File.separator + allMovies[i]);
				}
				for (int i=0;i<allBooks.length;i++) {
					allFiles.add(tp.dirBooks + File.separator + allBooks[i]);
				}
				
				for (int i=0;i<allFiles.size();i++) {
					mySeSuExtractor.makeSeSus(allFiles.get(i));
					myTextDatabase.mDatabaseOpenHelper.addText(
							allFiles.get(i), 
							MainActivity.elimUml((new ODTFile(allFiles.get(i))).getContent())
							);

					//               durchsucht / von / gefunden
					publishProgress(i, allFiles.size(), countODTs);
				}
				MyMoviesSeSuDatabase.MyMoviesSeSuOpenHelper helper = 
						new MyMoviesSeSuDatabase.MyMoviesSeSuOpenHelper(getApplicationContext());

				/* alte Variante
					mySeSuExtractor.writeFile();
					helper.loadNewSeSus();
				 */
				int i = 0;
				helper.makeNew();
				TreeMap<String, SeSuExtractor.ValueElement> tm = mySeSuExtractor.getSesus();
				for (Map.Entry<String, SeSuExtractor.ValueElement> me : tm.entrySet()) {
					String refs = (me.getValue().cRefMovies > 0 ? " MOVIE " : "");
					refs += (me.getValue().cRefBooks > 0 ? " BOOK " : "");
					//if (refs.contains("MOVIE") && refs.contains("BOOK")) {
					//	Log.i("MyMovies", "DBLoader refs in : " + me.getKey() + " >" + refs + "<");
					//}
					helper.addSesu(me.getKey(), me.getValue().zweiteZeile, refs);
					publishProgress(i++, tm.size(), LOADSTEP2);
				}

			} // end for

			return countODTs;
		}

		@Override
		protected void onProgressUpdate(Integer... cnt) {
			//Log.i("MyMovies", "Pager AsyncTask Progress :" + cnt[0]);
			if (!(cnt[2]==LOADSTEP2)) {
				progressDialog.setMessage("Step 1/2  analysing Files ... ");
			} else {
				progressDialog.setMessage("Step 2/2  loading Database ... ");
			}
			progressDialog.setMax(cnt[1]);
			progressDialog.setProgress(cnt[0]);
			progressBar.setMax(cnt[1]);
			progressBar.setProgress(cnt[0]);
			TextView proz = (TextView)findViewById(R.id.proProz);
			TextView anz = (TextView)findViewById(R.id.proAnz);
			TextView text = (TextView)findViewById(R.id.proText);
			TextView found = (TextView)findViewById(R.id.proFound);
			proz.setText(((int) cnt[0]*100/cnt[1]) + " %");
			anz.setText(cnt[0] + " / " + cnt[1]);
			mBuilder.setProgress(cnt[1], cnt[0], false);
			if (!(cnt[2]==LOADSTEP2)) {
				text.setText("Step 1/2  analysing Files ... ");
				mBuilder.setContentText("Step 1/2  analysing Files ...      " +
						((int) cnt[0]*100/cnt[1]) + " %");
			} else {
				text.setText("Step 2/2  loading Database ... ");
				mBuilder.setContentText("Step 2/2  loading Database ...     " +
						((int) cnt[0]*100/cnt[1]) + " %");
			}
			found.setText("");
			Intent intent = new Intent(getApplicationContext(), de.floresse.mymovies.MainActivity.class);
			intent.setAction("DUMMY");
			PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
			mBuilder.setProgress(cnt[1], cnt[0], false)
				.setCategory(Notification.CATEGORY_PROGRESS)
				.setAutoCancel(true)
				.setContentIntent(pIntent);
			mNotifyManager.notify(NOTIFY_DBLOAD, mBuilder.build());
		}

		@Override
		protected void onPostExecute(Integer cnt) {
			wakeLock.release();
			progressDialog.dismiss();
			lilaProgressBar.setVisibility(View.INVISIBLE);
			String timeStamp = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY).format(new Date());
			Intent intent = new Intent(getApplicationContext(), de.floresse.mymovies.MainActivity.class);
			//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setAction("NOTIFY");
			intent.putExtra(MainActivity.NOTIFY, true);
			intent.putExtra(MainActivity.NOTIFY_TEXT, " Such-Tabellen geladen: " + timeStamp);
			intent.setData((Uri.parse("foobar://" + SystemClock.elapsedRealtime())));

			PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
			mBuilder.setContentText("Datenbank erfolgreich erstellt")
				.setContentIntent(pIntent)
				.setAutoCancel(true)
				.setCategory(Notification.CATEGORY_PROGRESS)
				// Removes the progress bar
				.setProgress(0,0,false)
			;
			mNotifyManager.cancel(NOTIFY_DBLOAD);
			mNotifyManager.notify(NOTIFY_DBLOAD, mBuilder.build());
			/*
    			Intent main = new Intent(getApplicationContext(), de.floresse.mymovies.MainActivity.class);
    			main.putExtra(MainActivity.NOTIFY, true);
    			main.putExtra(MainActivity.NOTIFY_TEXT, " Such-Tabellen geladen");
    			startActivity(main);
			 */
			getIntent().putExtra(RESULT_TIMESTAMP, timeStamp);
			setResult(RESULT_OK, getIntent());
			try {
				Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
				r.play();
			} catch (Exception e) {}
	        Log.i("MyMovies", "DBLoader - finish");
			finish();
		}

	} // end class DBLoadTask
	
	public class TaskParms {
		String dirMovies;
		String dirBooks;
		Context context;
		
		public TaskParms(String d1, String d2, Context ct) {
			dirMovies = d1;
			dirBooks = d2;
			context = ct;
		}
	}  // end class TaskParms
	
	public class ODTFilter implements FilenameFilter {
		
		public ODTFilter () {
		}
		
		public boolean accept(File dir, String filename) {
			if (filename.toUpperCase().endsWith(".ODT")) {
				return true;
			} else {
				return false;
			}
		}
		
	}  // end class ODTFilter

}


