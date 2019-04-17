package de.floresse.mymovies;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import de.floresse.mymovies.ODTFile.XMLElement;

public class SeSuExtractor {
	
	public static final String DELIMITER = ";";
	public static final String FILENAME = "MyMoviesSeSus.txt";
	
	private TreeMap<String, ValueElement> hm = new TreeMap<String, ValueElement>();
	private String title;
	private String dirMovies = null;
	private String dirBooks = null;
	private boolean bMovie,bBook;
	
	public SeSuExtractor(Context context) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		dirMovies = sharedPref.getString("pref_key_dir_odts_movies", "");
		dirBooks  = sharedPref.getString("pref_key_dir_odts_books", "");
	}
	
	public void makeSeSus(String filename) {
		if (filename.startsWith(dirMovies)) {
			bMovie = true;
			bBook  = false;
		}
		if (filename.startsWith(dirBooks)) {
			bMovie = false;
			bBook  = true;
		}
		ODTFile file = new ODTFile(filename, true);
		insertTitle(file.getHeader());
		ArrayList<XMLElement> vecLinks = file.getLinkElements();
		for (XMLElement link : vecLinks) {
			if ((!link.content.equals("")) &&
				(!link.content.equals(title))) {	// title kann auch Link sein ==> hier nicht einfügen 
				storeSeSu(link.content, 
						// "z. Bsp. in: " + 
						title);
			}
		}
	}
	
	public void insertTitle(String title) {
		this.title = new String(title);
		ValueElement vm = hm.get(title);
		if (vm==null) {
			hm.put(title, new ValueElement("", 0, Integer.valueOf(bMovie? 1 : 0), Integer.valueOf(bBook? 1 : 0)));
		} else {
			String zweiteZeile = vm.zweiteZeile;
			int newcount = vm.count;
			int cRefMovies = vm.cRefMovies + (bMovie? 1 : 0);  // wichtig wenn title schon aus anderem Typ
			int cRefBooks = vm.cRefBooks  + (bBook? 1 : 0);    //               "
			hm.put(title, new ValueElement(zweiteZeile, newcount, cRefMovies, cRefBooks));
		}
	}
	
	public void storeSeSu(String sesu, String ergZweiteZeile) {
		// insert or update zweiteZeile und count
		ValueElement vm = hm.get(sesu);
		if (vm==null) {
			hm.put(sesu, new ValueElement(ergZweiteZeile, 1, Integer.valueOf(bMovie? 1 : 0), Integer.valueOf(bBook? 1 : 0)));
		} else {
			int newcount = ++vm.count;
			int cRefMovies = vm.cRefMovies + (bMovie? 1 : 0);  // wichtig wenn title schon aus anderem Typ
			int cRefBooks = vm.cRefBooks  + (bBook? 1 : 0);    //               "
			if (newcount < 4) {
				String delim = (newcount==1 ? "" : ", ");
				hm.put(sesu, new ValueElement(new String(vm.zweiteZeile + delim + ergZweiteZeile),
						                          newcount, cRefMovies, cRefBooks));
			} else {
				hm.put(sesu, new ValueElement(vm.zweiteZeile,
                        newcount, cRefMovies, cRefBooks));
			}
		}
		
		// update title 
		vm = hm.get(title);
		int count = ++vm.count;
		int cRefMovies = vm.cRefMovies + (bMovie? 1 : 0);  // wichtig wenn title schon aus anderem Typ
		int cRefBooks = vm.cRefBooks  + (bBook? 1 : 0);    //               "
		if (count < 4) {
			String delim = (count==1 ? "" : ", ");
			hm.put(title, new ValueElement(new String(vm.zweiteZeile + delim + sesu), count, cRefMovies, cRefBooks));
		} else {
			hm.put(title, new ValueElement(vm.zweiteZeile, count, cRefMovies, cRefBooks));
		}	
	}
	
	public void writeFile() {
		// treemap in datei ausgeben
		try {
			File dir = Environment.getExternalStorageDirectory();
			File myFile = new File(dir.toString() + File.separator + FILENAME);
			Log.i("MyMovies", "SeSus write : " + myFile.toString());
			myFile.createNewFile();
			FileOutputStream fOut = new FileOutputStream(myFile);
			OutputStreamWriter myOutWriter = 
					new OutputStreamWriter(fOut);
			for (Map.Entry<String, ValueElement> me : hm.entrySet()) {
				String refs = (me.getValue().cRefMovies>0? "MOVIE" : "");
				refs += (me.getValue().cRefBooks>0? "BOOK" : "");
				String line = me.getKey() + DELIMITER + me.getValue().zweiteZeile 
						    //+ DELIMITER
						    //+ me.getValue().count
					    	+ DELIMITER
					    	+ refs
					   	    + System.getProperty("line.separator");
				//Log.i("MyMovies", "SeSus extracted : " + line);
				myOutWriter.write(line);
			}
			myOutWriter.close();
			fOut.close();
		} catch (IOException e) {
			//
			Log.i("MyMovies", "SeSus write Exception : ");
			e.printStackTrace();
		}
	}
	
	public TreeMap<String, ValueElement> getSesus() {
		return hm;
	}
	
	public class ValueElement {
		
		public String zweiteZeile;
		public int count;
		public int cRefMovies;
		public int cRefBooks;
		
		public ValueElement(String zweiteZeile, int count, int cRefMovies, int cRefBooks ) {
			this.zweiteZeile = zweiteZeile;
			this.count = count;
			this.cRefMovies = cRefMovies;
			this.cRefBooks  = cRefBooks; 
		}
	}  // end class ValueElement

}
