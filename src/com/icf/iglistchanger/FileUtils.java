package com.icf.iglistchanger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

/**
 * This class contains static methods for opening, closing, and reading from various files used in the processing of table descriptors.
 * 
 * @author Dan Donahue
 *
 */
public class FileUtils {

	/**
	 * Returns a org.jsoup.nodes.Document created by reading an XHTML (or HTML) file.
	 *  
	 * @param  filename of an XHTML or HTML file
	 * @return org.jsoup.nodes.Document;
	 */
	public static Document parseXHtmlFile(String filename) {
		org.jsoup.nodes.Document doc = Jsoup.parse(getHTML_String(filename), "UTF-8");
		return doc;
	}
	
	/**
	 * Returns a string representing the HTML present in the given XHTML or HTML file.
	 * 
	 * @param  filename of an XHTML or HTML file
	 * @return HTML as a string
	 */
	public static String getHTML_String(String filename) {
	    StringBuilder contentBuilder = new StringBuilder();
	    try {
	        BufferedReader in = new BufferedReader(new FileReader(filename));
	        String str;
	        while ((str = in.readLine()) != null) {
	            contentBuilder.append(str);
	        }
	        in.close();
	    } catch (IOException e) {
	    	System.err.println("Exception reading html file: " + filename);
	    	e.printStackTrace();
	    }
	    return contentBuilder.toString();

	}
	
	/**
	 * Returns a org.jsoup.nodes.Document created by reading an XML file.
	 * 
	 * @param  filename of an XML file
	 * @return org.jsoup.nodes.Document;
	 */
	public static Document openXMLFile(String filename) {
		Document doc = null;
		try {
			File file = new File(filename);
			FileInputStream fis = new FileInputStream(file);
			doc = Jsoup.parse(fis,null,"", Parser.xmlParser());
		}
		catch (Exception e) {
			System.err.println("Exception reading XML file: " + filename);
			e.printStackTrace();
		}
		return doc;
	}
	
	/**
	 * Given a org.jsoup.nodes.Document object, writes that object into a file with the given filename.
	 * This writes the file in UTF-8 encoding.  
	 * 
	 * @param doc - an org.jsoup.nodes.Document to be written
	 * @param filename - pathname of file to be written to
	 * @return true if write was successful, false otherwise
	 */
	public static boolean writeXHtmlFile(Document doc, String filename) {
		boolean isOk = true;
		File file = new File(filename);
		PrintWriter writer;
		try {
			writer = new PrintWriter(file,"UTF-8");
			writer.write(doc.html() ) ;
			writer.flush();
			writer.close();

		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			System.err.println("Exception writing html file: " + filename);
			e.printStackTrace();
			isOk = false;
		}
		return isOk;
	}
	
	/**
	 * Give an directory name, returns a list of filenames in the top-level of that directory
	 * 
	 * @param directoryName - pathname of directory to examine
	 * @return List&lt;String&gt; - List of filename strings
	 */
	public static List<String> getDirFiles(String directoryName) {
		
		List<String> results = new ArrayList<String>();
		File[] files = new File(directoryName).listFiles();
		//If this pathname does not denote a directory, then listFiles() returns null. 

		if (files != null) {
			for (File file : files) {
			    if (file.isFile()) {
			        results.add(file.getName());
			    }
			}
		}
		return results;
	}
	
	/**
	 * Given a file pathname, reads the contents of the file as a JSON object and returns that object.
	 * 
	 * @param filename - pathname of JSON file to read
	 * @return JSONObject resulting for reading the given filename.
	 */
	public static JSONObject parseJsonFile(String filename) {
		 JSONObject jsonContent = null;
		File file = new File(filename);
        try {
            String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
            jsonContent = new JSONObject(content);
         } catch (IOException e) {
        	 System.err.println("Exception reading json file: " + filename);
            e.printStackTrace();
        }
		return jsonContent;
	}
}
