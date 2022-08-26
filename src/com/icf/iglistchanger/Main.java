package com.icf.iglistchanger;

import java.io.File;

/**
 * This class consists of a single static method - 'main' - that reads a control file (xml) of table descriptors, creates a new Controller class, then loops through the control file's table descriptor elements and processes each by calling the appropriate Controller class methods.
 * The main method takes a single, optional, string argument that is the pathname of the control file to use.  If no argument is provided, then a default filname is used:  "TableAlterDescritors.xml", co-located with the application jar file.
 * 
 * @author Dan Donahue
 *
 */
public class Main {
	
	public static void main(String[] args) {
		
		String controlFile = "TableAlterDescriptors.xml";  // Relative pathname, implies this file is co-located with the application.
		if (args.length > 0) {
			controlFile = args[0];
		}
		
		File tst = new File(controlFile);
		if (!tst.exists()) {
			System.err.println("The control file '" + controlFile + "' was not found.");
			
		}
		else {
			System.out.println("Using control file: " + controlFile);
			Controller control = new Controller(controlFile);
			// For each descriptor in the above control file, alter the table in the .html file identified in each, then save changes to a new (or the same) html file.
			while (control.next()) {
				control.processTableHeader();
				control.processTableRows();
				control.updateOriginalDocument();
			}
		}

	}
			

}
