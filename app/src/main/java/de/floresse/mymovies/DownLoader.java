package de.floresse.mymovies;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;


public class DownLoader extends ListActivity {

	    public static final String TO_PATH = "TO_PATH";
	    
		private static String startUrl = "http://192.168.178.22:80/";
		private static boolean bDoloMan = true;
	    
        private static final String ITEM_KEY = "key";
        private static final String ITEM_IMAGE = "image";
	    
        private ArrayList<Directory.Entry> dirEntries = null;
        private ArrayList<HashMap<String, Object>> screenList;

        private TextView downloadFilesIcon;
        private TextView viewDownloadsIcon;

        private String downloadToPath = null;
        
        private long enqueue;
        private DownloadManager dm;
        
        private ColorStateList colorSave;
        
    	private TreeMap<String, String> fileSelection = new TreeMap<String, String>();
    	private TreeMap<String, TextView> fileSelecView = new TreeMap<String, TextView>();


        @SuppressLint("NewApi")
		@Override
        public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                //Log.i("MyMovies", "FileChooser : onCreate ");
                
                getActionBar().setDisplayHomeAsUpEnabled(true);
                
                final PackageManager pm = getPackageManager();
                Intent intDolo = new Intent();
                intDolo.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
                ResolveInfo ri = pm.resolveActivity(intDolo, 0);
                Drawable icViewDolo = ri.loadIcon(pm);
                Drawable icStartDolo = getResources().getDrawable(R.drawable.downloadserver48);
                
        		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplication());
        		startUrl = sharedPref.getString("pref_downloadServer", "");
        		bDoloMan = sharedPref.getBoolean("pref_isDownloadManager", true);

                downloadToPath = getIntent().getStringExtra(TO_PATH);
                
                setContentView(R.layout.downloader_main);
                TextView myPath = (TextView) findViewById(R.id.dlpath);
                myPath.setText("Download to : " + downloadToPath);

                downloadFilesIcon = (TextView) findViewById(R.id.dlDownloadFiles);
                downloadFilesIcon.setCompoundDrawablesRelativeWithIntrinsicBounds(null, icStartDolo, null, null); 
                viewDownloadsIcon = (TextView) findViewById(R.id.dlViewDownloads);
                viewDownloadsIcon.setCompoundDrawablesRelativeWithIntrinsicBounds(null, icViewDolo, null, null); 
                if (!bDoloMan) {
                	viewDownloadsIcon.setEnabled(false);
                	viewDownloadsIcon.setVisibility(View.INVISIBLE);
                }
                
                downloadFilesIcon.setEnabled(false);
                downloadFilesIcon.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
            			for (Map.Entry<String, String> dolo : fileSelection.entrySet()) {
            				String selectedSourceFileName = dolo.getKey();
            				String selectedFileName = dolo.getValue();
            				TextView tv = fileSelecView.get(selectedSourceFileName);
            				tv.setSelected(false);
            				tv.setTag(false);
            				tv.setTextColor(colorSave);
                        	if (bDoloMan) {
                        		//
                        		// Zieldirectory muss über storage/.. 
                        		// und nicht über mnt/.. angegeben sein !?!
                        		//
                        		dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                        		Request request = new Request(
                        				Uri.parse(selectedSourceFileName));
                        		//Restrict the types of networks over which this download may proceed.
                        		request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
                        		//Set whether this download may proceed over a roaming connection.
                        		request.setAllowedOverRoaming(false);
                        		//Set the title of this download, to be displayed in notifications (if enabled).
                        		request.setTitle(selectedFileName);
                        		//Set a description of this download, to be displayed in notifications (if enabled)
                        		request.setDescription(selectedFileName);
                        		request.setNotificationVisibility
                        		        (DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        		File fileDest = new File(downloadToPath, selectedFileName);
                        		if (fileDest.exists()) {
                        			if (!fileDest.delete()) {
                        				Log.e("MyMovies", "Downloader: could not delete old " + fileDest.getName());
                        			}
                        		}
                        		request.setDestinationUri(Uri.fromFile(fileDest));

                        		//Log.i("MyMovies", "Downloader : " + selectedSourceFileName + " "
                        		//		                          + selectedFileName);
                        		enqueue = dm.enqueue(request);
                        	} else {
                        		// ohne DownloadManager: einfacher File-Copy
                        		fileCopyInThread(selectedSourceFileName, downloadToPath, selectedFileName);
                        	}
                        }
                        downloadFilesIcon.setEnabled(false);
                        fileSelection.clear();
                        fileSelecView.clear();
                    }
                });

                viewDownloadsIcon.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intDolo = new Intent();
                            intDolo.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
                            startActivity(intDolo);
                        }

                });
                
                BroadcastReceiver receiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String action = intent.getAction();
                        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                            long downloadId = intent.getLongExtra(
                                    DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                            Query query = new Query();
                            query.setFilterById(enqueue);
                            Cursor c = dm.query(query);
                            if (c.moveToFirst()) {
                                int columnIndex = c
                                        .getColumnIndex(DownloadManager.COLUMN_STATUS);
                                if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                            		//Log.i("MyMovies", "DownLoader received download complete");
                                }
                            }
                        }
                    }
                };
                
                registerReceiver(receiver, new IntentFilter(
                        DownloadManager.ACTION_DOWNLOAD_COMPLETE));

                
        		downloadWebPage(startUrl);
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            super.onCreateOptionsMenu(menu);
            return true;
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
    	
        @Override
        protected void onListItemClick(ListView l, View vline, int position, long id) {

        	Directory.Entry entry =  dirEntries.get(position);
    		TextView tv = (TextView)vline.findViewById(R.id.dlrowtext);
        	boolean isSelected = (tv.getTag()==null ? false : (Boolean)tv.getTag());
        	if (!isSelected) {
            	if (entry.isDir) {
            		fileSelection.clear();
            		fileSelecView.clear();
        			//downloadFilesIcon.setText("Start Download");
        			downloadFilesIcon.setEnabled(false);
        			//Log.i("MyMovies", "Downloader Directory selected + >" + entry.ref + "<");
            		downloadWebPage(entry.ref);
            	} else {
            		tv.setSelected(true);
            		tv.setTag(true);
            		fileSelection.put(entry.ref, entry.name);
            		fileSelecView.put(entry.ref, tv);
            		colorSave = tv.getTextColors();
        			tv.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
            		//downloadFilesIcon.setText("Download : " + fileSelection.size() + " File(s)");
            		downloadFilesIcon.setEnabled(fileSelection.size()>0);
            	}
        	} else {
        		tv.setSelected(false);
        		tv.setTag(false);
        		fileSelection.remove(entry.ref);
        		fileSelecView.remove(entry.ref);
    			//tv.setTextColor(getResources().getColor(android.R.color.darker_gray));
    			tv.setTextColor(colorSave);
        		//downloadFilesIcon.setText(fileSelection.size()>0 ? 
        		//					( "Download : " + fileSelection.size() + " File(s)")
        		//					: "Start Download");
        		downloadFilesIcon.setEnabled(fileSelection.size()>0);
        	}
        }

    	public void downloadWebPage(final String url) {
    		//Log.i("MyMovies", "downloading >" + url + "<");
    		//netaccess.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade));
    		//netaccess.setVisibility(View.VISIBLE);
    		new DownloadWebPageTask().execute(url);
    	}
    	
        private void fileCopyInThread(final String srcFilename, final String destPath, final String destName) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        fileCopy(srcFilename, destPath, destName);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }
    	
    	public void fileCopy(String srcFilename, String destPath, String destName) throws IOException {
			URL url = new URL(srcFilename);
			URLConnection conn = url.openConnection();
			conn.setConnectTimeout(10000);
			conn.connect();
			InputStream in = conn.getInputStream();
    	    
            File dest   = new File(destPath, destName);
    	    OutputStream out = new FileOutputStream(dest);

    	    // Transfer bytes from in to out
    	    byte[] buf = new byte[1024];
    	    int len;
    	    while ((len = in.read(buf)) > 0) {
    	        out.write(buf, 0, len);
    	    }
    	    in.close();
    	    out.close();
    	}
    	
    	private class DownloadWebPageTask extends AsyncTask<String, Void, Directory> {
    		
    		@Override
    		protected void onPreExecute() {
    			// clear screen to show progressbar
    			setListAdapter(null);
                //((TextView)findViewById(android.R.id.empty)).setText("connecting ...");
                ((TextView)findViewById(android.R.id.empty)).setText("");
                findViewById(R.id.dlActivityIndicator).setVisibility(View.VISIBLE);

    		}
    		
    		@Override
    		protected Directory doInBackground(String... urls) {
    			Directory directory = null;
    			for (String url : urls) {
    				directory = new Directory(url);
    				try {
    					URL serverUrl = new URL(url);
    					URLConnection conn = serverUrl.openConnection();
    					conn.setConnectTimeout(10000);
    					conn.setReadTimeout(10000);
    					BufferedReader in = new BufferedReader(
    							//new InputStreamReader(serverUrl.openStream()));
    							new InputStreamReader(conn.getInputStream()));
    					String htmlLine;
    					while ((htmlLine = in.readLine()) != null) {
    						directory.setFromHtml(url, htmlLine);
    					}
    					in.close();
    					// Log.i(LOGTAG, htmlPage);
    				} catch (Exception e) {
    					directory.retCode = -1;
    					directory.errorMsg = "Fehler beim DownloadWebPage: " +  
    							System.getProperty("line.separator") + url +
    							System.getProperty("line.separator") +
    							e.getClass().getName() + ": " + e.getMessage();
    					Log.e("MyMovies", "Fehler beim DownloadWebPage: " + url);
    					e.printStackTrace();
    				}

    			} // end for
				//Log.i("MyMovies", "DownloadWebPage: beeendet ");

    			return directory;
    		}

    		@Override
    		protected void onPostExecute(Directory directory) {
    			
    			if (directory.retCode<0) {
                    ((TextView)findViewById(android.R.id.empty)).setText(directory.errorMsg);
                    findViewById(R.id.dlActivityIndicator).setVisibility(View.INVISIBLE);
    				return;
    			}
				
                screenList = new ArrayList<HashMap<String, Object>>();
                dirEntries = new ArrayList<Directory.Entry>();
    			for (Directory.Entry entry : directory.entries) {
    				if (entry.isDir) {
        				addItem(entry, R.drawable.folder);
    				}	
    			}
    			for (Directory.Entry entry : directory.entries) {
    				if (entry.isFile) {
        				addItem(entry, R.drawable.file);
    				}
    			}
    			SimpleAdapter fileList = new SimpleAdapter(getApplicationContext(), screenList,
    					                     R.layout.downloader_row, new String[] {
    					                     ITEM_KEY, ITEM_IMAGE }, new int[] { R.id.dlrowtext, R.id.dlrowimage });

    			//fileList.notifyDataSetChanged();

    			setListAdapter(fileList);

    			//netaccess.setVisibility(View.INVISIBLE);
    		}

            private void addItem(Directory.Entry entry, int imageId) {
                    HashMap<String, Object> item = new HashMap<String, Object>();
                    item.put(ITEM_KEY, entry.name);
                    item.put(ITEM_IMAGE, imageId);
                    screenList.add(item);
                    dirEntries.add(entry);
            }


    	} // end class DownloadWebPageTask
        
    	public class Directory {

    		public ArrayList<Entry> entries = new ArrayList<Entry>();
			public int retCode = 0;
			public String errorMsg = "";

    		public Directory(final String url) {
    			if (url.length()>startUrl.length()) {
    				// wir sind in subdir
    				String urlUp = url.substring(0, url.length()-1);
    				int pos = urlUp.lastIndexOf(File.separator);
    				urlUp = urlUp.substring(0, pos+1);
    				//Log.i("MyMovies", "Downloader startUrl : >" + startUrl + "<");
    				//Log.i("MyMovies", "Downloader      Url : >" + url + "<");
    				// parentdir mit Name ../ (als ersten) einstellen: 
					addEntry(urlUp, "..//", true, false);
    			}

    		}

    		public void setFromHtml(String dirRef, String html) { 
    			int start = 0, end = 0;
    			String found = "";
    			start = html.indexOf("alt=\"[");
    			if (start>=0) {
    				end = html.indexOf("]\"");
    				if (end >=0) {
    					start+=6;
    					found = html.substring(start, end);
    					boolean bDir = false;
    					boolean bFile = false;
    					if (found.equals("DIR")) {
    						bDir = true;
    					} else {
    						bFile = true;
    					}
    					html = html.substring(start);
    					start = html.indexOf("<a href=\"");
    					if (start>=0) {
    						end = html.indexOf("</a>");
    						if (end>=0) {
    							html = html.substring(start+9,end);
    							start = html.indexOf("\">");
    							if (start>=0) {
    								found = html.substring(0,start);
    								html = html.substring(start+2);
    								//Log.i(MainActivity.LOGTAG, "DirEntry ref>" + found + "<" );
    								//Log.i(MainActivity.LOGTAG, "DirEntry name>" + html + "<" );
    								if (found.startsWith("/")) {
    									//addEntry(found, "Parent Directory", bDir, bMp3, bImg, dirRef);
    								} else {
    									addEntry(dirRef + found, found, bDir, bFile);
    								}
    							}
    						}
    					}
    				}
    			}


    		}  // end function setFromHtml()

    		public void addEntry (String ref, String name, boolean isDir, boolean isFile) {
    			entries.add(new Entry(ref, name, isDir, isFile));

    		}

    		public class Entry {

    			public String name = "";
    			public String ref = "";
    			public boolean isDir = false;
    			public boolean isFile = false;

    			public Entry (String ref, String name, boolean isDir, boolean isFile) {
    				this.ref = ref;
    				this.name = name;
    				this.isDir = isDir;
    				this.isFile = isFile;
    				repls();
    				//Log.i("MyMovies", " Entry ref  : >" + ref + "<" );
    				//Log.i("MyMovies", " Entry name : >" + name + "<" );
    				//Log.i("MyMovies", " Entry isDirFile : >" + isDir + "/" + isFile + "<" );
    			}

    			public void repls() {
    				if (name.endsWith("/")) {
    					name = name.substring(0, name.length() - 1);
    				}
    				name = name.replaceAll("%20", " ");
    				name = name.replaceAll("%c2%b4", "ü");
    				name = name.replaceAll("%c3%a9", "ü");
    				name = name.replaceAll("%c3%bc", "ü");
    				name = name.replaceAll("%c3%b6", "ü");
    				name = name.replaceAll("%c3%a4", "ü");
    				name = name.replaceAll("%c3%84", "ü");
    				name = name.replaceAll("%c3%96", "ü");
    				name = name.replaceAll("%c3%9c", "ü");
    				name = name.replaceAll("%c3%9f", "ü");
    				name = name.replaceAll("%c3%b1", "ü");
    				name = name.replaceAll("&amp;", "&");

    				ref  = ref.replaceAll("&amp;", "&");

    			}  // end function repls

    		}  // end class Entry

    	}  // end class directory
    	

}
