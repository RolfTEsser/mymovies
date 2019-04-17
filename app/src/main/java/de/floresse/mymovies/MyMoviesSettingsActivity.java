package de.floresse.mymovies;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

public class MyMoviesSettingsActivity extends Activity {
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		//Log.i("MyMovies", " starting Settings onCreate");
        
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new MyMoviesSettingsFrag())
                .commit();

		//Log.i("MyMovies", " end  SettingsActivity onCreate");
		
    }
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            // app icon in action bar clicked; go home
        	finish();
            return true;

        }

    return super.onOptionsItemSelected(item);
    }
    
}
