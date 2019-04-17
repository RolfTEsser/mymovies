package de.floresse.mymovies;

import java.io.File;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SimpleCursorAdapter;

public class MainActivity extends Activity {
	
	public static final String TAG = "MainActivity";
    public static final String NOTIFY = "NOTIFY";
	public static final String NOTIFY_TEXT = "NOTIFY_TEXT";

    private static final int PERMISSIONS_REQUEST_CODE = 192;


	private MyMoviesSeSuDatabase sesuDB = null;
	private boolean bNotify = false;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getIntent().getAction().equals("DUMMY")) {
        	// nur zur Positionierung in aktiver Task (pendingintent aus notification aus dbloader)
        	finish();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i(TAG, " checking Permission ");
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_DENIED) {
                Log.i(TAG, " Permission denied");
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.WAKE_LOCK
                }, PERMISSIONS_REQUEST_CODE);
                finish();
            }
        }


        File dbSesu = getDatabasePath(MyMoviesSeSuDatabase.DATABASE_NAME);
        Log.i("MyMovies", "Main / Database SearchSuggestions SIZE : " + dbSesu.length());
        
        File dbText = getDatabasePath(TextDatabase.DATABASE_NAME);
        Log.i("MyMovies", "Main / Database Text - Content SIZE : " + dbText.length());
       
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		sesuDB = new MyMoviesSeSuDatabase(getApplicationContext());

        setContentView(R.layout.activity_main);
		final Button goBtn = (Button)findViewById(R.id.goButton);
		final SearchView searchView = (SearchView)findViewById(R.id.searchView);
		
		final LinearLayout lilaCBs = (LinearLayout)findViewById(R.id.lilaCheckBoxen);
		final CheckBox cbMovies = (CheckBox)findViewById(R.id.checkbox_movies);
		final CheckBox cbBooks  = (CheckBox)findViewById(R.id.checkbox_books);
		
    	bNotify = getIntent().getBooleanExtra(NOTIFY, false);
    	if (bNotify) {
    		searchView.setEnabled(false);
    		searchView.setVisibility(View.INVISIBLE);
    		lilaCBs.setVisibility(View.INVISIBLE);
    		goBtn.setText(getIntent().getStringExtra(NOTIFY_TEXT));
    		goBtn.setOnClickListener(new OnClickListener() {
    			public void onClick(View v) {
    		    	finish();
    			}
    		});
    		return;
    	} else {
    		lilaCBs.setVisibility(View.VISIBLE);
    	}
    		

	    // Get the SearchView and set the searchable configuration
	    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
	    searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
	    searchView.setQueryRefinementEnabled(true);

		searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
			@Override
			public boolean onSuggestionSelect(int pos) {
		    	Log.i("MyMovies", "Main - onSuggestionSelect : " + pos);
		    	return false;
			}

			@Override
			public boolean onSuggestionClick(int pos) {
		    	//Log.i("MyMovies", "Main - onSuggestionClick : " + pos);
		    	Cursor sugg = searchView.getSuggestionsAdapter().getCursor();
		    	sugg.moveToPosition(pos);
		    	String searchString = sugg.getString(sugg.getColumnIndex(MyMoviesSeSuDatabase.KEY_SESU));
		    	//Log.i("MyMovies", "Main - onSuggestionSelect -- : " + searchString);
				Intent pager = new Intent(getApplication(), de.floresse.mymovies.PagerActivity.class);
				pager.setAction("MyMoviesSearch"); 
		    	pager.putExtra(SearchManager.QUERY, searchString);
				pager.putExtra(PagerActivity.MOVIES, cbMovies.isChecked());
				pager.putExtra(PagerActivity.BOOKS, cbBooks.isChecked());
		    	startActivity(pager);
		    	return true;
			}
		});
		
		searchView.setOnQueryTextListener(new OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String searchString) {
				Intent pager = new Intent(getApplication(), de.floresse.mymovies.PagerActivity.class);
				pager.setAction("MyMoviesSearch"); 
		    	pager.putExtra(SearchManager.QUERY, searchString);
				pager.putExtra(PagerActivity.MOVIES, cbMovies.isChecked());
				pager.putExtra(PagerActivity.BOOKS, cbBooks.isChecked());
		    	startActivity(pager);
		    	return true;
		    	// intent-start über searchmanager:
		    	//return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				
				goBtn.setText(updSearchView());

				return true;

				//return false;
			}
		});
		
		goBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent pager = new Intent(getApplicationContext(), de.floresse.mymovies.PagerActivity.class);
				pager.setAction("MyMoviesSearch"); 
				pager.putExtra(PagerActivity.MOVIES, cbMovies.isChecked());
				pager.putExtra(PagerActivity.BOOKS, cbBooks.isChecked());
		    	if (searchView.getQuery().length()>0) {
			    	pager.putExtra(SearchManager.QUERY, searchView.getQuery().toString());
		    	}	
		    	startActivity(pager);
			}
		});
		
		if (!bNotify) { 
			ImageView animView = (ImageView)findViewById(R.id.cube);
			ObjectAnimator rotright = ObjectAnimator.ofFloat(animView, "rotation", 0f, 360f);
			rotright.setDuration(3000);
			ObjectAnimator fadeIn = ObjectAnimator.ofFloat(animView, "alpha", 0f, 1f);
			fadeIn.setDuration(3000);
			ObjectAnimator scaleXIn = ObjectAnimator.ofFloat(animView, "scaleX", 0f, 1f);
			scaleXIn.setDuration(3000);
			ObjectAnimator scaleYIn = ObjectAnimator.ofFloat(animView, "scaleY", 0f, 1f);
			scaleYIn.setDuration(3000);
			AnimatorSet animatorSetScaleIn = new AnimatorSet();
			animatorSetScaleIn.play(scaleXIn).with(scaleYIn);
			AnimatorSet animatorSet = new AnimatorSet();
			animatorSet.play(animatorSetScaleIn).with(rotright);
			animatorSet.start();
			//animView.setVisibility(View.VISIBLE);
		}

    }

	@Override
	public void onResume() {
		super.onResume();
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		if (sharedPref.getBoolean("pref_isFullScreen", false)) {
	        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
	                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_main, menu);
    	if (bNotify) {
        	menu.findItem(R.id.action_settings).setEnabled(false);
        	menu.findItem(R.id.action_settings).setVisible(false);
    	}
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_settings:
    		Intent intent = new Intent(this, MyMoviesSettingsActivity.class);
    		startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void animCube() {
		final ImageView animView = (ImageView)findViewById(R.id.cube);
		ObjectAnimator fadeOut = ObjectAnimator.ofFloat(animView, "alpha", 0f);
		fadeOut.setDuration(2000);
		ObjectAnimator mover = ObjectAnimator.ofFloat(animView, "translationX", -500f, 0f);
		mover.setDuration(3000);
		ObjectAnimator moveOut = ObjectAnimator.ofFloat(animView, "translationX", 0f, -500f);
		moveOut.setDuration(4000);
		ObjectAnimator rotleft = ObjectAnimator.ofFloat(animView, "rotation", 0f, -360f);
		rotleft.setDuration(3000);
		ObjectAnimator rotright = ObjectAnimator.ofFloat(animView, "rotation", 0f, 360f);
		rotright.setDuration(3000);
		ObjectAnimator moveIn = ObjectAnimator.ofFloat(animView, "translationY", -500f, 0f);
		moveIn.setDuration(4000);
		ObjectAnimator fadeIn = ObjectAnimator.ofFloat(animView, "alpha", 0f, 1f);
		fadeIn.setDuration(2000);
		AnimatorSet animatorSet = new AnimatorSet();
		
		moveOut.addListener(new AnimatorListenerAdapter() {
		    @Override
			public void onAnimationEnd(Animator animation) {
		    	if (((String)animView.getTag()).equals("SELBY")) {
		    		animView.setImageDrawable(getResources().getDrawable(R.drawable.pan_small));
		    		animView.setTag("OBRIEN");
		    	} else {
		    		animView.setImageDrawable(getResources().getDrawable(R.drawable.cubemovie));
		    		animView.setTag("SELBY");
		    	}
			}
		});	

		animatorSet.play(moveOut).with(rotleft);
		AnimatorSet animatorSet2 = new AnimatorSet();
		animatorSet2.play(mover).with(rotright).after(animatorSet);
		animatorSet2.start();		
    }
    
    public void onCheckboxClicked(View view) {
		((Button)findViewById(R.id.goButton)).setText(updSearchView());
    }
    
    public void onMoviesClicked(View view) {
    	((CheckBox)findViewById(R.id.checkbox_movies)).toggle();
		((Button)findViewById(R.id.goButton)).setText(updSearchView());
    }
    
    public void onBooksClicked(View view) {
    	((CheckBox)findViewById(R.id.checkbox_books)).toggle();
		((Button)findViewById(R.id.goButton)).setText(updSearchView());
    }
    
    public String updSearchView() {
    	SearchView searchView = (SearchView)findViewById(R.id.searchView);
    	CheckBox cbMovies = (CheckBox)findViewById(R.id.checkbox_movies);
    	CheckBox cbBooks  = (CheckBox)findViewById(R.id.checkbox_books);
    	String cubeTag  = (String)findViewById(R.id.cube).getTag();
    	
    	/* neuen ButtonText basteln */
    	String goBtnText = (searchView.getQuery().length()>0 ? "Suche " : "Alle "); 
    	if (cbMovies.isChecked()) {
    		if (cbBooks.isChecked()) {
    			if (cubeTag.equals("OBRIEN")) {
    				animCube();
    			}
    			goBtnText += "Filme/Bücher";
    		} else {
    			if (cubeTag.equals("OBRIEN")) {
    				animCube();
    			}
    			goBtnText += "Filme";
    		}
    	} else {
    		if (cbBooks.isChecked()) {
    			if (cubeTag.equals("OBRIEN")) {
    				animCube();
    			}
    			goBtnText += "Bücher";
    		} else {
    			if (cubeTag.equals("SELBY")) {
    				animCube();
    			}
    			goBtnText = "Bicycles or Pancakes?";
    		}
    	}

    	/*
    	 * SearchSuggestions - CursorAdapter setzen
    	 */
		String[] dbColumns = new String[] {
				BaseColumns._ID,
				MyMoviesSeSuDatabase.KEY_SESU, 
				MyMoviesSeSuDatabase.KEY_DEFINITION, 
				SearchManager.SUGGEST_COLUMN_QUERY};
		Cursor cursor = null;
		if (cbMovies.isChecked() && cbBooks.isChecked()) {
			cursor = sesuDB.getSesuMatches(searchView.getQuery().toString(), dbColumns);
		} else {
			if (cbMovies.isChecked()) {
				cursor = sesuDB.getSesuMatchesTyped(searchView.getQuery().toString(), "MOVIE", dbColumns);
			} else {
				if (cbBooks.isChecked()) {
					cursor = sesuDB.getSesuMatchesTyped(searchView.getQuery().toString(), "BOOK", dbColumns);
				}	
			}
		}
		if (cursor!=null) {
			//Log.i("MyMovies", "Main SuggestionsCursor Count : " + cursor.getCount());
		}	
		String[] adapColumns = new String[] {
				MyMoviesSeSuDatabase.KEY_SESU, 
				MyMoviesSeSuDatabase.KEY_DEFINITION
		};
		int[] columnTextId = new int[] {android.R.id.text1, android.R.id.text2};

		SuggestionSimpleCursorAdapter simple = new SuggestionSimpleCursorAdapter(getApplicationContext(),
				R.layout.simple_list_item_2, cursor,
				adapColumns , columnTextId
				, 0);

		searchView.setSuggestionsAdapter(simple);

    	return goBtnText;
    }

	public static String elimUml(String s) {
		s = s.replace("ü", "Ae");
		s = s.replace("ü", "Ae");
		s = s.replace("ü", "Ae");
		s = s.replace("ü", "ae");
		s = s.replace("ü", "oe");
		s = s.replace("ü", "ue");
		s = s.replace("ü", "ss");
		return s;
	}

    
    // SuggestionSimpleCursorAdapter.java
    public class SuggestionSimpleCursorAdapter
        extends SimpleCursorAdapter
    {

        public SuggestionSimpleCursorAdapter(Context context, int layout, Cursor c,
                String[] from, int[] to) {
            super(context, layout, c, from, to);
        }

        public SuggestionSimpleCursorAdapter(Context context, int layout, Cursor c,
                String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public CharSequence convertToString(Cursor cursor) {

            int indexColumnSuggestion = cursor.getColumnIndex(MyMoviesSeSuDatabase.KEY_SESU);

            return cursor.getString(indexColumnSuggestion);
        }


    }
}