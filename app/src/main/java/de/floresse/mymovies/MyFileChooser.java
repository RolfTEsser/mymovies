package de.floresse.mymovies;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import de.floresse.mylibrary.activity.FileChooser;

public class MyFileChooser extends FileChooser {
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_filechooser, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_download:
                Intent dolo = new Intent(this, DownLoader.class);
                dolo.setAction("MyMoviesDownLoad");
                dolo.putExtra(DownLoader.TO_PATH, selectedFile.getPath());
                startActivity(dolo);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
