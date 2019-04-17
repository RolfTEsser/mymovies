package de.floresse.mymovies;

import java.io.File;
import java.util.ArrayList;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ScreenSlidePageFragment extends Fragment {

	public static final String TAG = "MyMovies";
    /**
     * The argument key for the page number this fragment represents.
     */
    public static final String ARG_PAGE = "page";
    
    private static Context mContext;
	
	private static ArrayList<String> gefundeneODTs = null;
	private static ArrayList<String> anzeigeODTs = null; 
	
	private static String mySearchString = null;
	
	private static String dirBooks  = null;
	private static String dirMovies = null;

    /**
     * The fragment's page number, which is set to the argument value for {@link #ARG_PAGE}.
     */
    private int mPageNumber;
    
    public static void init(ArrayList<String> v, String searchString, Context ct) {
    	
    	
    	gefundeneODTs = v;
    	mySearchString=searchString;
		anzeigeODTs = new ArrayList<String>(gefundeneODTs.size());
    	mContext = ct;
    	
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		dirMovies = sharedPref.getString("pref_key_dir_odts_movies", null);
		
		for (int i=0; i<gefundeneODTs.size(); i++ ) {
			if (searchString==null) {
				// bei Anforderung Random ermitteln und merken fürs zurückscrollen
				anzeigeODTs.add(null);
			} else {
				// SuchanzeigeODTs, bleiben in dieser Reihenfolge
				anzeigeODTs.add(new String(gefundeneODTs.get(i)));
			}
		}
		
		//return odts.length;
   	
    }

    /**
     * Factory method for this fragment class. Constructs a new fragment for the given page number.
     */
    public static ScreenSlidePageFragment create(int pageNumber) {
        ScreenSlidePageFragment fragment = new ScreenSlidePageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ScreenSlidePageFragment() {
    	// für start intent mit file / ohne fileprovider
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(ARG_PAGE);
        
    }

    @SuppressLint("NewApi")
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		
		int ranpos = mPageNumber;
		
		Drawable drawBook  = getResources().getDrawable( R.drawable.book_green);
		Drawable drawMovie = getResources().getDrawable( R.drawable.movie);
		
		if (anzeigeODTs.get(mPageNumber)==null) {
			// noch nicht belegt: Random - Reihenfolge
			ranpos = (int) (Math.random() * gefundeneODTs.size());
			anzeigeODTs.set(mPageNumber, gefundeneODTs.get(ranpos));
			// nicht nochmal verwenden:
			gefundeneODTs.remove(ranpos);
		}	
		
		//FilmODTFile filmfile = new FilmODTFile(odts[ranpos].getPath());
		ODTFile filmfile = new ODTFile(anzeigeODTs.get(mPageNumber));
		BitmapDrawable img = new BitmapDrawable(mContext.getResources(), filmfile.getImage());


        // Inflate the layout containing a title and body text.
        ViewGroup rootView = (ViewGroup) inflater
                .inflate(R.layout.fragment, container, false);

        // Set the title view to show the page number.

        TextView tv = (TextView) rootView.findViewById(R.id.header);
		if (filmfile.getHeader()!=null) {
    		tv.setText(filmfile.getHeader());
    		if (anzeigeODTs.get(mPageNumber).startsWith(dirMovies)) {
    			tv.setCompoundDrawablesRelativeWithIntrinsicBounds(drawMovie, null, null, null);
    		} else {	
    			tv.setCompoundDrawablesRelativeWithIntrinsicBounds(drawBook, null, null, null);
    		}	
		} else {
			tv.setText(anzeigeODTs.get(mPageNumber));
			Log.i("MyMovies", "ScreenSlidePage : no header in file : " + anzeigeODTs.get(mPageNumber));
		}	
		tv.setGravity(Gravity.CENTER);
		tv.setClickable(true);
		tv.setTag(anzeigeODTs.get(mPageNumber));
		tv.setOnClickListener(new OnClickListener() {  
            public void onClick(View v) {
            	File file = new File((String) v.getTag());
            	Log.i(TAG, "URI is " + Uri.fromFile(file).toString());
        		String extension = android.webkit.MimeTypeMap
        				.getFileExtensionFromUrl(Uri.fromFile(file).toString());
        		String mimetype = android.webkit.MimeTypeMap
        				.getSingleton().getMimeTypeFromExtension(extension);
        		Intent i = new Intent();
        		i.setAction(android.content.Intent.ACTION_VIEW);
        		//i.setDataAndType(Uri.fromFile(file),mimetype);
				i.setData(Uri.fromFile(file));
				Log.i(TAG, "ScreenSlide  + URI : " + Uri.fromFile(file));
        		startActivity(i);
            }  
        }); 

		
        ImageView iv = (ImageView) rootView.findViewById(R.id.image);
        
        iv.setImageDrawable(img);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
		iv.setClickable(true);
		iv.setTag(anzeigeODTs.get(mPageNumber));
		iv.setOnClickListener(new OnClickListener() {  
            public void onClick(View v) {
            	File file = new File((String) v.getTag());
        		String extension = android.webkit.MimeTypeMap
        				.getFileExtensionFromUrl(Uri.fromFile(file).toString());
        		String mimetype = android.webkit.MimeTypeMap
        				.getSingleton().getMimeTypeFromExtension(extension);
        		Intent i = new Intent();
        		i.setAction(android.content.Intent.ACTION_VIEW);
        		i.setDataAndType(Uri.fromFile(file),mimetype);
        		startActivity(i);
            }  
        });
         
		
        TextView fore = (TextView) rootView.findViewById(R.id.footer_re);
        fore.setText(mPageNumber+1 + " / " + anzeigeODTs.size());

        TextView foli = (TextView) rootView.findViewById(R.id.footer_li);
        foli.setText(mySearchString!=null ? mySearchString : "");

        return rootView;
    }

    /**
     * Returns the page number represented by this fragment object.
     */
    public int getPageNumber() {
        return mPageNumber;
    }

}
