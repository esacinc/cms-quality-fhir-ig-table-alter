package com.icf.iglistchanger;

import java.io.File;

public class IGListChanger {

	
	public static void main(String[] args) {
		
		String controlFile = "TableAlterDescriptors.xml";
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
