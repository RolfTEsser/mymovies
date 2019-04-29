package de.floresse.mymovies;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.floresse.mylibrary.activity.FileChooser;

public class MyMoviesSettingsFrag extends PreferenceFragment
        implements OnSharedPreferenceChangeListener
        // , StringPreference.OnStringPreferenceClickedListener
{

    private static final int REQUEST_FILECHOOSER_MOVIES = 1;
    private static final int REQUEST_FILECHOOSER_BOOKS = 2;
    private static final int REQUEST_DBREFRESH = 3;

    private StringPreference prefDirMovies = null;
    private StringPreference prefDirBooks = null;
    private StringPreference prefDBLoad = null;
    private CheckBoxPreference prefFS = null;
    private CheckBoxPreference prefDBSearch = null;
    private EditTextPreference prefDownloadServer = null;
    private CheckBoxPreference prefDownloadManager = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.i("MyMovies", "starting SettingsFragment onCreate");

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        Context myContext = getActivity();

        prefDirMovies = (StringPreference) findPreference("pref_key_dir_odts_movies");
        prefDirMovies.setOnStringPreferenceClickedListener(
                new StringPreference.OnStringPreferenceClickedListener() {
                    @Override
                    public void onStringPreferenceClicked() {
                        //Log.i("MyMovies", "Settings - onStringPreferenceClicked ");
                        Intent filechooser = new Intent(getActivity().getApplicationContext(), MyFileChooser.class);
                        filechooser.putExtra(FileChooser.START_PATH,
                                prefDirMovies.onRestoreString());
                        filechooser.putExtra(FileChooser.CANSELECTDIR, true);
                        filechooser.putExtra(FileChooser.CANSELECTFILE, false);

                        startActivityForResult(filechooser, REQUEST_FILECHOOSER_MOVIES);
                    }
                });

        prefDirBooks = (StringPreference) findPreference("pref_key_dir_odts_books");
        prefDirBooks.setOnStringPreferenceClickedListener(
                new StringPreference.OnStringPreferenceClickedListener() {
                    @Override
                    public void onStringPreferenceClicked() {
                        //Log.i("MyMovies", "Settings - onStringPreferenceClicked ");
                        Intent filechooser = new Intent(getActivity().getApplicationContext(), MyFileChooser.class);
                        filechooser.putExtra(FileChooser.START_PATH,
                                prefDirBooks.onRestoreString());
                        filechooser.putExtra(FileChooser.CANSELECTDIR, true);
                        filechooser.putExtra(FileChooser.CANSELECTFILE, false);

                        startActivityForResult(filechooser, REQUEST_FILECHOOSER_BOOKS);
                    }
                });

        prefDBLoad = (StringPreference) findPreference("pref_key_db");
        prefDBLoad.setOnStringPreferenceClickedListener(
                new StringPreference.OnStringPreferenceClickedListener() {
                    @Override
                    public void onStringPreferenceClicked() {

                        final AlertDialog.Builder alertDialogBuilder =
                                new AlertDialog.Builder(getActivity());

                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View dialogLayout = inflater.inflate(R.layout.alertdialog,
                                (ViewGroup) getActivity().getCurrentFocus(), false);
                        //
                        LinearLayout ll = (LinearLayout) dialogLayout.findViewById(R.id.ald_layout);
                        ll.setBackgroundResource(R.drawable.movies_holo_blau_small);
                        //ll.removeView(dialogLayout.findViewById(R.id.ald_title));
                        TextView title = (TextView) dialogLayout.findViewById(R.id.ald_title);
                        title.setText("Please Confirm");
                        TextView text = (TextView) dialogLayout.findViewById(R.id.ald_text);
                        text.setText(" Start Refesh / Load ?");
                        text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.refresh, 0, 0, 0);
                        //
                        Button cancel = (Button) dialogLayout.findViewById(R.id.ald_button_cancel);
                        Button okay = (Button) dialogLayout.findViewById(R.id.ald_button_okay);
                        alertDialogBuilder.setView(dialogLayout);
                        final AlertDialog alertDialog = alertDialogBuilder.create();
                        cancel.setText("Cancel");
                        cancel.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                alertDialog.cancel();
                            }
                        });
                        //LinearLayout ll = (LinearLayout)dialogLayout.findViewById(R.id.ald_two_buttons);
                        //ll.removeView(okay);
                        okay.setText("Okay");
                        okay.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                prefDBLoad.saveString(" gestartet : "
                                        + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date()));
                                Intent loader = new Intent(getActivity().getApplicationContext(),
                                        de.floresse.mymovies.DBLoader.class);
                                loader.setAction("");
                                startActivityForResult(loader, REQUEST_DBREFRESH);
                                alertDialog.cancel();
                            }
                        });

                        // show it
                        alertDialog.show();

                    }
                });


        // not needed:
        //prefDir.setOnPreferenceChangeListener(new myChangeListener());

        // Set summary to be the user-description for the selected value
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        prefDirMovies.setSummary(sharedPreferences.getString("pref_key_dir_odts_movies", ""));
        prefDirBooks.setSummary(sharedPreferences.getString("pref_key_dir_odts_books", ""));
        prefDBLoad.setSummary(sharedPreferences.getString("pref_key_db", ""));
        prefFS = (CheckBoxPreference) findPreference("pref_isFullScreen");
        prefFS.setSummary(prefFS.isChecked() ?
                "ist aktiviert" : "ist aus");
        if (prefFS.isChecked()) {
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        prefDBSearch = (CheckBoxPreference) findPreference("pref_isDBSearch");
        prefDBSearch.setSummary(prefDBSearch.isChecked() ?
                "ist aktiviert" : "nicht aktiviert");
        prefDownloadServer = (EditTextPreference) findPreference("pref_downloadServer");
        prefDownloadServer.setSummary(sharedPreferences.getString("pref_downloadServer", ""));
        prefDownloadManager = (CheckBoxPreference) findPreference("pref_isDownloadManager");
        prefDownloadManager.setSummary(prefDownloadManager.isChecked() ?
                "ist aktiviert" : "ohne (nur FileCopy)");

    }

    @Override
    public synchronized void onActivityResult(final int requestCode,
                                              int resultCode, final Intent data) {
        //Log.i("MyMovies", "Settings - onActivityResult " + requestCode );
        switch (requestCode) {
            case REQUEST_FILECHOOSER_MOVIES:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        String filePath = data.getStringExtra(FileChooser.RESULT_PATH);
                        // save the new path to Preferences:
                        prefDirMovies.saveString(filePath);
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                    default:
                        Log.e("MyMovies", "Settings - FileChooser unknown ResultCode " + resultCode);
                        break;
                }
                break;
            case REQUEST_FILECHOOSER_BOOKS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        String filePath = data.getStringExtra(FileChooser.RESULT_PATH);
                        // save the new path to Preferences:
                        prefDirBooks.saveString(filePath);
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                    default:
                        Log.e("MyMovies", "Settings - FileChooser unknown ResultCode " + resultCode);
                        break;
                }
                break;
            case REQUEST_DBREFRESH:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        String timestamp = data.getStringExtra(PagerActivity.RESULT_TIMESTAMP);
                        // save the new path to Preferences:
                        prefDBLoad.saveString(getResources().getString(R.string.pref_db_default) + " " + timestamp);
                        break;
                    case Activity.RESULT_CANCELED:
                        prefDBLoad.saveString(getResources().getString(R.string.pref_db_default) + " canceled");
                        break;
                    default:
                        Log.e("MyMovies", "Settings - DB refresh unknown ResultCode " + resultCode);
                        break;
                }
                break;
            default:
                Log.e("MyMovies", "Settings - onActivityResult unknown RequestCode " + requestCode);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //Log.i("MyMovies", "SettingsFrag - preference changed " + key );
        if (key.equals("pref_key_dir_odts_movies")) {
            findPreference(key)
                    .setSummary(sharedPreferences.getString(key, ""));
        }
        if (key.equals("pref_key_dir_odts_books")) {
            findPreference(key)
                    .setSummary(sharedPreferences.getString(key, ""));
        }
        if (key.equals("pref_key_db")) {
            findPreference(key)
                    .setSummary(sharedPreferences.getString(key, ""));
        }
        if (key.equals("pref_isFullScreen")) {
            findPreference(key)
                    .setSummary(sharedPreferences.getBoolean("pref_isFullScreen", false) ?
                            "ist aktiviert" : "ist aus");
            if (sharedPreferences.getBoolean("pref_isFullScreen", false)) {
                getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else {
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        }
        if (key.equals("pref_isDBSearch")) {
            findPreference(key)
                    .setSummary(sharedPreferences.getBoolean(key, false) ?
                            "ist aktiviert" : "nicht aktiviert");
        }
        if (key.equals("pref_downloadServer")) {
            findPreference(key)
                    .setSummary(sharedPreferences.getString(key, ""));
        }
        if (key.equals("pref_isDownloadManager")) {
            findPreference(key)
                    .setSummary(sharedPreferences.getBoolean(key, true) ?
                            "ist aktiviert" : "ohne (nur FileCopy)");
        }

    }

}
