package com.icf.iglistchanger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

public class FileUtils {

	public static Document parseXHtmlFile(String filename) {
		org.jsoup.nodes.Document doc = Jsoup.parse(getHTML_String(filename), "UTF-8");
		return doc;
	}
	
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
