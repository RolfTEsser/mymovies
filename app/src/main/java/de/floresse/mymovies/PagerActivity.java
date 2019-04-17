package de.floresse.mymovies;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

public class PagerActivity extends FragmentActivity {
	
	public static final String MOVIES = "Movies";
	public static final String BOOKS = "Books";
	
    public static final String RESULT_TIMESTAMP = "DBTIMESTAMP";
	
    /**
     * The number of pages (Anzahl der ODT - Files bzw Such - Treffer)
     */
    private static int numPages = 0;
    
    private boolean bSearch = false;
    private boolean bMovies = true;
    private boolean bBooks = true;
	private boolean bFullScreenNow = false;
	private boolean bFullScreen = false;
    private boolean bDBSearch = false;
    private String searchString = null;
    private String dirMovies = null;
    private String dirBooks = null;

    private FragmentManager fm = null;
    
    private TextDatabase myTextDatabase = null;
    
    // was ist schüner ? ProgressBar oder ProgressDialog
    private boolean bproDia = false;  // ProgressBar


    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager = null;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        myTextDatabase = new TextDatabase(this);
        
        handleIntent(getIntent());
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
    	
        setIntent(intent);
        handleIntent(intent);
    }
    
    protected void handleIntent(Intent intent) {
    	
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		dirMovies = sharedPref.getString("pref_key_dir_odts_movies", null);
		dirBooks  = sharedPref.getString("pref_key_dir_odts_books", null);
		bFullScreen  = sharedPref.getBoolean("pref_isFullScreen", false);
		bDBSearch  = sharedPref.getBoolean("pref_isDBSearch", false);

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            searchString = intent.getStringExtra(SearchManager.QUERY);
        } else {
            if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            	/* wird derzeit nicht genutzt
            	Uri uri = getIntent().getData();
          		Log.i("MyMovies", "Pager intentAction View : " + uri.toString());
          		Log.i("MyMovies", "Pager intentAction View : " + uri.getLastPathSegment());
            	*/
            } else {	
            	// direkt über startActivity gestartet
            	// mit oder ohne searchString
            	bMovies      = getIntent().getBooleanExtra(MOVIES, true);
            	bBooks       = getIntent().getBooleanExtra(BOOKS, true);
            	searchString = getIntent().getStringExtra(SearchManager.QUERY);
            }	
        }
      	
      	// Log.i("MyMovies", "Pager - intent Action : " + intent.getAction());
      	// Log.i("MyMovies", "Pager - Search : " + searchString);
      	// Log.i("MyMovies", "Pager -     Movies : " + bMovies);
      	// Log.i("MyMovies", "Pager -     Books  : " + bBooks);


      	if (searchString!=null) {
      		bSearch=true;
      	}
      	
      	if (!bMovies) dirMovies = "wirstdunichtfinden";  // keine Filme
      	if (!bBooks) dirBooks = "wirstdunichtfinden";    // keine Bücher
      	if (!bDBSearch) {
      		//Log.i("MyMovies", "onCreate to search for :" + searchString);
      		setContentView(R.layout.activity_main);
      		SearchView sv = (SearchView)findViewById(R.id.searchView);
      		sv.setQuery(searchString, false);
      		sv.setEnabled(false);
      		sv.setInputType(InputType.TYPE_NULL);
      		final Button rb = (Button)findViewById(R.id.goButton);
      		rb.setText("Searching");
			rb.setVisibility(View.INVISIBLE);
      		rb.setOnClickListener(new OnClickListener() {
      			public void onClick(View v) {
      			}
      		});
      	}

  		new SearchInFilesTask().execute
  	        (new TaskParms[] { new TaskParms(dirMovies,
  	        							  dirBooks,	
  	    		                          searchString,
  	    		                          getApplicationContext()) } );
    	
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_pager, menu);
        if ((numPages>0) && (mPager!=null)) {
        	menu.findItem(R.id.action_previous).setEnabled(mPager.getCurrentItem() > 0);
        	menu.findItem(R.id.action_previous).setVisible(mPager.getCurrentItem() > 0);
        	menu.findItem(R.id.action_next).setEnabled(mPager.getCurrentItem() < numPages-1);
        	menu.findItem(R.id.action_next).setVisible(mPager.getCurrentItem() < numPages-1);
        	menu.findItem(R.id.action_fullscreen).setEnabled(true);
        	menu.findItem(R.id.action_fullscreen).setVisible(true);
        } else {	
        	menu.findItem(R.id.action_previous).setEnabled(false);
        	menu.findItem(R.id.action_previous).setVisible(false);
        	menu.findItem(R.id.action_next).setEnabled(false);
        	menu.findItem(R.id.action_next).setVisible(false);
        	menu.findItem(R.id.action_fullscreen).setEnabled(false);
        	menu.findItem(R.id.action_fullscreen).setVisible(false);
        }
        return true;
        
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_previous:
                // Go to the previous step in the wizard. If there is no previous step,
                // setCurrentItem will do nothing.
                //mPager.setCurrentItem(mPager.getCurrentItem() - 1);
                mPager.setCurrentItem(5);
                return true;
            case R.id.action_next:
                // Advance to the next step in the wizard. If there is no next step, setCurrentItem
                // will do nothing.
                //mPager.setCurrentItem(mPager.getCurrentItem() + 1);
				mPager.setCurrentItem(5);
                return true;
            case R.id.action_fullscreen:
            	bFullScreenNow = true;
            	ActionBar ab = getActionBar();
            	ab.hide();
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
                return true;
            case android.R.id.home:
                // app icon in action bar clicked; go home
            	finish();
                return true;

        }

        return super.onOptionsItemSelected(item);
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
		if (bFullScreenNow) {
        	bFullScreenNow = false;
        	ActionBar ab = getActionBar();
        	ab.show();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			finish();
    	}
	}


    /**
     * A simple pager adapter that represents 5 {@link ScreenSlidePageFragment} objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
    	
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return (Fragment) ScreenSlidePageFragment.create(position);
        }

        @Override
        public int getCount() {
            return numPages;
        }
        
        @Override
        public int getItemPosition(Object object){
            return PagerAdapter.POSITION_NONE;
        } 
        
    }
    
	private class SearchInFilesTask extends AsyncTask<TaskParms, Integer, Integer> {
		
	    
	    private ProgressDialog progressDialog;
	    private LinearLayout lilaProgressBar;
	    private ProgressBar progressBar;
		
		private PowerManager pm = null;
		private PowerManager.WakeLock wakeLock = null;
		
		
		
		@Override
		protected void onPreExecute() {
			
      		progressDialog = new ProgressDialog(getApplicationContext());
      		lilaProgressBar = (LinearLayout) findViewById(R.id.lilaProgressBar);
      		progressBar = (ProgressBar) findViewById(R.id.progressBar);

			if (bSearch && !bDBSearch) { 
				pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
				wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "MyMovies");
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
			}
		}
		
		@Override
		protected Integer doInBackground(TaskParms... tps) {
			//
			ArrayList<String> odts = new ArrayList<String>();
			for (TaskParms tp : tps) {
				ArrayList<String> allFiles = new ArrayList<String>(); 
				String[] allMovies;
				if (tp.dirMovies!=null) {
					allMovies = new File(tp.dirMovies).list(new ODTFilter());
					if (allMovies!=null) {
						for (int i=0;i<allMovies.length;i++) {
							allFiles.add(tp.dirMovies + File.separator + allMovies[i]);
						}
					}
				}
				String[] allBooks;
				if (tp.dirBooks!=null) {
					allBooks = new File(tp.dirBooks).list(new ODTFilter());
					if (allBooks!=null) {
						for (int i=0;i<allBooks.length;i++) {
							allFiles.add(tp.dirBooks + File.separator + allBooks[i]);
						}
					}
				}
				FileSearchFilter filter = null;
				if (tp.searchString!=null) {
					filter = new FileSearchFilter(tp.searchString);
					if (bDBSearch) {
						String[] columns = new String[1];
						columns[0] = TextDatabase.COLNAME_FILE;
						// um auch abc-def zu finden:
						String ss = searchString.replace("-", " ");
						ss = MainActivity.elimUml(ss);
						Cursor treffer = myTextDatabase.getTextMatches(ss, columns);
						if (treffer!=null) {
							//Log.i("MyMovies", "Pager found : " + treffer.getCount());
							for (int i=0; i < treffer.getCount(); i++) {
								treffer.moveToPosition(i);
								if (treffer.getString(0).startsWith(tp.dirMovies) || 
									treffer.getString(0).startsWith(tp.dirBooks)) {
									odts.add(treffer.getString(0));
								}	
							}
							treffer.close();
						}
					}

				}
				for (int i=0;i<allFiles.size();i++) {
					if (tp.searchString==null) {
						odts.add(allFiles.get(i));
					} else {
						if (!bDBSearch) {
							if (filter.accept(allFiles.get(i))) {
								odts.add(allFiles.get(i));
							}
						}
					}
					//               durchsucht / von / gefunden
					publishProgress(i, allFiles.size(), odts.size());
				}
				
    			ScreenSlidePageFragment.init(odts,
    					tp.searchString,
    					tp.context);
			} // end for

			return odts.size();
		}

		@Override
		protected void onProgressUpdate(Integer... cnt) {
			if (bSearch && !bDBSearch) { 
				//Log.i("MyMovies", "Pager AsyncTask Progress :" + cnt[0]);
				progressDialog.setMessage("searching ...       found : " + cnt[2] );
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
				text.setText("searching ...");
				found.setText("found : " + cnt[2]);
			}
		}
		
		@Override
		protected void onPostExecute(Integer cnt) {
			numPages = cnt;
			if (bSearch && !bDBSearch) { 
				wakeLock.release();
				progressDialog.dismiss();
				lilaProgressBar.setVisibility(View.INVISIBLE);
				try {
					Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
					Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
					r.play();
				} catch (Exception e) {}
			}
			if (numPages>0) {
				loadPager();
			} else {
    			Intent main = new Intent(getApplicationContext(), de.floresse.mymovies.MainActivity.class);
    			main.setAction("NOTIFY");
    			main.putExtra(MainActivity.NOTIFY, true);
    			main.putExtra(MainActivity.NOTIFY_TEXT, "Nichts gefunden / Neue Suche");
    			startActivity(main);
    			finish();
			}
		}
		
		private void loadPager() {
        	setContentView(R.layout.pager);
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (bFullScreen) {
            	actionBar.hide();
            	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            						WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }	

			// Instantiate a ViewPager and a PagerAdapter.
			mPager = (ViewPager) findViewById(R.id.pager);
			mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
			mPager.setAdapter(mPagerAdapter);
			invalidateOptionsMenu();
			mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
				@Override
				public void onPageSelected(int position) {
					// When changing pages, reset the action bar actions since they are dependent
					// on which page is currently active. An alternative approach is to have each
					// fragment expose actions itself (rather than the activity exposing actions),
					// but for simplicity, the activity provides the actions in this sample.
					invalidateOptionsMenu();
				}
			});
			
		}

	} // end class SearchInFilesTask
	
	public class TaskParms {
		String dirMovies;
		String dirBooks;
		String searchString;
		Context context;
		
		public TaskParms(String d1, String d2, String ss, Context ct) {
			dirMovies = d1;
			dirBooks = d2;
			searchString = ss;
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


