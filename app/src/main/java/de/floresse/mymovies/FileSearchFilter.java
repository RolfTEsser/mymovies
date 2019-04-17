package de.floresse.mymovies;

public class FileSearchFilter
                              { 

	private String content = null;
	private String searchString = null;
	private String[] sSs = null;

	public FileSearchFilter(String sS) { 
		searchString = sS.toUpperCase(); 
		sSs = searchString.split(",");
		for (int i=0; i<sSs.length; i++) {
			sSs[i] = sSs[i].trim(); 
			//Log.i("MyMovies", "Filter string : -" + sSs[i] + "-");
		}
	} 

	//@Override
	//public boolean accept(File dir, String filename) {
	public boolean accept(String file) {
		if (searchString==null) {
			return true;
		}
		//Log.i("MyMovies"," Filter : " + file);
		boolean found = false;
		content = new ODTFile(file).getContent().toUpperCase();
		for (int i=0; i<sSs.length; i++) {
			if (content.contains(sSs[i])) {
				found=true;
			} else {
				return false;
			}	
		}
		return found;
	} 
	
}  // end class MyFilenameFilter 
