package de.floresse.mymovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;

public class MyMoviesSettingsActivity extends AppCompatActivity {
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		//Log.i("MyMovies", " starting Settings onCreate");

        //getLayoutInflater().inflate(R.layout.activity_settings, (ViewGroup)findViewById(android.R.id.content));
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.ic_launcher);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
        //        .replace(android.R.id.content, new MyMoviesSettingsFrag())
                .replace(R.id.fragment_settings, new MyMoviesSettingsFrag())
                .commit();

		//Log.i("MyMovies", " end  SettingsActivity onCreate");
		
    }

}
