package de.floresse.mymovies;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Stack;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class ODTFile {
	
	private String filename = null;
	
	private InputStream isImage = null;
	private String header = null;
	private String content = null;
	private Stack<XMLElement> elemStack = new Stack<XMLElement>();
	private boolean bsamElem = false;
	private ArrayList<XMLElement> vecLinks = new ArrayList<XMLElement>();
	
	public ODTFile (String fn, boolean bsamElem) {
		this.bsamElem = bsamElem;
		filename = fn;
		fummelRaus();
	}
	
	public ODTFile (String fn) {
		this(fn, false);
	}
	
	public String getHeader () {
		return header;
	}
	
	public String getContent () {
		return content;
	}
	
	public InputStream getImage () {
		return isImage;
	}

	public ArrayList<XMLElement> getLinkElements () {
		if (!bsamElem) throw(new IllegalArgumentException("Anforderung im Konstruktor = false")); 
		return vecLinks;
	}
    
    public void fummelRaus() {

    	try {
    		ZipFile zipFile = new ZipFile( filename );
    		Enumeration<? extends ZipEntry> zipEntryEnum = zipFile.entries(); 
    		while ( zipEntryEnum.hasMoreElements() ) 
    		{ 
    			ZipEntry zipEntry = zipEntryEnum.nextElement(); 
    			//Log.i("MyMovies", "ZipEntry found : " + zipEntry.getName());
    			// wenn image datei :
    			if (!(zipEntry.getName().toUpperCase().startsWith("THUMBNAIL"))) {
    				if ((zipEntry.getName().toUpperCase().endsWith(".JPG")) ||
    						(zipEntry.getName().toUpperCase().endsWith(".GIF")) ||	
    						(zipEntry.getName().toUpperCase().endsWith(".PNG"))) {
    					isImage = zipFile.getInputStream( zipEntry ); 
    					//Log.i("MyMovies", "ZipEntry Image found : " + zipEntry.getName());
    				}
    			}		
    			// testing XML Parser :
    			if (zipEntry.getName().toUpperCase().startsWith("CONTENT")) {
   					//Log.i("MyMovies", "ZipEntry Content found : " + zipEntry.getName());
   					testContent(zipFile.getInputStream( zipEntry )); 
    			}		
    		}
    	} catch (ZipException e) {
    		Log.e("MyMovies", "ZipException binding ZipFile :" + filename);
    	} catch (IOException e) {
    		Log.e("MyMovies", "IOException binding ZipFile :" + filename);
    	}
		if (isImage==null) {
			Log.i("MyMovies", "ScreenSlidePage : no image in file : " + filename);
		}	
    	
    }
    
    
    public void testContent(InputStream isContent) {
    	
    	  // sax stuff 
    	  try { 
    	    SAXParserFactory spf = SAXParserFactory.newInstance(); 
    	    SAXParser sp = spf.newSAXParser(); 
    	 
    	    XMLReader xr = sp.getXMLReader(); 
    	 
    	    DataHandler dataHandler = new DataHandler(); 
    	    xr.setContentHandler(dataHandler); 
    	 
    	    xr.parse(new InputSource(isContent)); 
    	 
    	    header = dataHandler.getHeader(); 
    	    content = dataHandler.getContent(); 
    	 
    	  } catch(ParserConfigurationException pce) { 
    	    Log.e("SAX XML", "sax parse error", pce); 
    	  } catch(SAXException se) { 
    	    Log.e("SAX XML", "sax error", se); 
    	  } catch(IOException ioe) { 
    	    Log.e("SAX XML", "sax parse io error", ioe); 
    	  } 
    	 
    }
    
    public class DataHandler extends DefaultHandler { 

    	private String content = null;
    	private String header = null;

    	public String getHeader() { 
    		return header; 
    	} 

    	public String getContent() { 
    		return content; 
    	} 

    	@Override 
    	public void startDocument() throws SAXException { 
    		content = new String(); 
    	} 

    	@Override 
    	public void endDocument() throws SAXException { 

    	} 

    	@Override 
    	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException { 
    		if (bsamElem) {
    			elemStack.push(new XMLElement(namespaceURI, localName, qName, atts));
    		}	
    	} 

    	@Override 
    	public void endElement(String namespaceURI, String localName, String qName) throws SAXException { 

    		if (header==null && content.length()>0) {
    			header = new String(content);
    			// Log.i("MyMovies", "XML - header :" + chars);
    		}
    		if (bsamElem) {
    			XMLElement testit = elemStack.pop(); 
    			if (testit.qName.equals("text:a")) {
    				vecLinks.add(testit);
    			}
    		}	

    	} 

    	@Override 
    	public void characters(char ch[], int start, int length) { 
    		String chars = new String(ch, start, length); 
    		//chars = chars.trim(); 

    		//Log.i("MyMovies", "XML - characters() :" + chars);

    		content += " " + chars;
    		
    		if (bsamElem) {
    			XMLElement element = null;
    			for (Enumeration<XMLElement> e = elemStack.elements(); e.hasMoreElements();) {
    				element = e.nextElement();
    				element.content += " " + chars;
    			}
    		}	

    	} 
    }  // end class DataHandler
	
	public class XMLElement {
		
		String namespaceURI;
		String localName;
		String qName;
		Attributes atts;
		String content = new String("");
		
		public XMLElement(String namespaceURI, String localName, String qName, Attributes atts) {
			this.namespaceURI = namespaceURI;
			this.localName = localName;
			this.qName = qName; 
			this.atts = atts;
		}
		

	}  // end class Element
	

}
