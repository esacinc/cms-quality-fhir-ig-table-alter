package com.icf.iglistchanger;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * This class manages the addition of search and sort capabilities to a table element.  It includes the jquery and jquery DataTables libraries and stylesheets to the html page if not already included.
 * 
 * @author Dan Donahue
 *
 */
public class SearchSort {
	
	private boolean isActive = false;            // A shorthand variable. If any one of sorting, searching, or pagination is enabled, then this is set to true.
	private boolean doSort = false;              // If true, then add sorting capabilities to a table
	private boolean doSearch = false;            // If true, add search capabilities to a table
	private boolean doPage = false;              // If true, add pagination to a table
	private boolean allowSizeChange = false;     // If true, allows user to change the pages size of the displayed table when pagination is enabled.
	private int pageSize = 10;                   // If pagination enabled, this specifies the size (in table rows) of each page to display
	private String tableID = "";                 // The "id" attribute of the table we are altering
	private String tableClass = "display";       // The css "class" attribute of the table we are altering. Note: This value is appended to any existing "class" attribute value.
	
	// Static variables for various javascript and css files we may need to include.  Also format statements we'll use to add script and link elements to the html file.
	// Also format statements we'll use to add script and link elements to the html file.
	private final String fn_JQUERY_JS = "assets/js/jquery.js";
	private final String fn_DATATABLES_JS = "assets/js/jquery.dataTables.min.js";
	private final String fn_DATATABLES_CSS = "assets/css/jquery.dataTables.min.css";
	private final String fmt_SCRIPT_INCLUDE = "<script src=\"%s\" type=\"text/javascript\"></script>";
	private final String fmt_CSS_INCLUDE = "<link rel=\"stylesheet\" href=\"%s\" />";
	private final String fmt_DATATABLES_INIT = "<script>$( document ).ready(function() { $('#%s').DataTable({" +
	                                                                                              "paging: %s, " +
			                                                                                      "ordering: %s, " +
	                                                                                              "searching: %s, " +
			                                                                                      "pageLength: %d, " +
	                                                                                              "lengthChange: %s, " + 
			                                                                                      "bSort: true, " +
	                                                                                              "lengthMenu: [ %d, 10, 25, 50, 75, 100 ]" +
	                                                                                              "}); }); </script>";
	/**
	 * Calls the init() method of this class with the given &lt;behavior&gt; element as its argument.
	 * Also sets the tableID value within the class, using the given tablePos value. (A table ID string is created by appending the given tablePos value to the string "Table-". This ID is used as the "id" attribute in the table being altered, unless that table already has an "id" attribute.)
	 * 
	 * 
	 * A sample element:
	 * 
	 * <pre>
	 * {@code
	 *              <behavior paging="true" sorting="true" searching="true" pageSize="5" pageSizeChange="false" />
	 * }	
	 * </pre>
	 * 
	 * @param behavior - an org.jsoup.nodes.Element from a TableAlterDescriptor element in a TableAlterDescriptors xml file
	 * @param tablePos - an integer representing the nth table in an html document to alter.
	 */
	public SearchSort(Element behavior, int tablePos) {
		super();
		this.tableID = "Table-" + tablePos;
		init(behavior);
	}
	
	/**
	 * Given a <behavior> element, initializes the method variables within the SearchSort instance with the values from the element.
	 * 
	 * <pre>
	 * {@code
	 *              <behavior paging="true" sorting="true" searching="true" pageSize="5" pageSizeChange="false" />
	 * }	
	 * </pre>
	 * 
	 * @param behavior
	 */
	private void init(Element behavior) {
		// <behavior paging="false" pageSize="5" pageSizeChange="true" sort="true" search="true" />
		
		// Defaults are all set to do nothing if no <behavior> element is provided.
		if (behavior != null) {
			this.doPage = (behavior.attr("paging").equalsIgnoreCase("true"));
			this.doSearch = (behavior.attr("searching").equalsIgnoreCase("true"));
			this.doSort = (behavior.attr("sorting").equalsIgnoreCase("true"));
			this.isActive = this.doPage || this.doSearch || this.doSort;
			// If paging is enabled, then set up the related variables...
			if (this.doPage) { 
				this.allowSizeChange = (behavior.attr("pageSizeChange").equalsIgnoreCase("true"));
				try {
					this.pageSize = Integer.parseInt(behavior.attr("pageSize"));
				}
				catch (Exception e) {
					System.err.println("pageSize attribute of <behavior> element not found or not an integer. Defaults to " + this.pageSize + " table rows per page.");
				}
			}
		}	
	}
	
	/** 
	 * To be called after a SearchSort object is instantiated.  Processes the contents of the &lt;behavior&gt; element that was provided at instantiation.
	 * The given html document and table element are altered during this process.
	 * 
	 * @param theDoc the html document to alter
	 * @param theTable the table from within the document to alter
	 * 
	 * @see com.icf.iglistchanger.Controller for the code calling this object.
	 */
	public void process(Document theDoc, Element theTable) {
		if (this.isActive) { 
			maybeAddTableAttributes(theTable);
			maybeAddJQuery(theDoc);
		}
	}
	
	
	/** 
	 * Adds a script element to the given document to include jquery if one is not already present.
	 * Adds a script element to the given document to include DataTables  <i>after the jquery include element</i>  
	 * Adds a link element to the given document to include DataTables stylesheet.
	 * Adds a 'documentReady' script that initializes the table to be edited with DataTables capabilities.
	 * 
	 * @see jquery.dataTables.min.js
	 * @see jquery.dataTables.min.css
	 * @see jquery.js
	 * 
	 * @param theDoc - the org.jsoup.nodes.Document to alter
	 */
	private  void maybeAddJQuery(Document theDoc) {
		
		Element jqueryCSSAdd = theDoc.getElementsByAttributeValue("rel", "stylesheet").last();
		
	    jqueryCSSAdd.after(String.format(this.fmt_CSS_INCLUDE, this.fn_DATATABLES_CSS));
	    Element jqueryAdd = theDoc.getElementsByAttributeValue("src", this.fn_JQUERY_JS).last();
		
		if (jqueryAdd == null) {
			theDoc.body().append(String.format(this.fmt_SCRIPT_INCLUDE, this.fn_JQUERY_JS));
			theDoc.body().append(String.format(this.fmt_SCRIPT_INCLUDE, this.fn_DATATABLES_JS));
		}
		else {
			jqueryAdd.after(String.format(this.fmt_SCRIPT_INCLUDE, this.fn_DATATABLES_JS));
		}
		
		jqueryAdd = theDoc.getElementsByAttributeValue("src", this.fn_DATATABLES_JS).last();
		addScripts(jqueryAdd);
		
		//System.out.println("jqueryAdd = " + jqueryAdd);
	}

	/**
	 * Adds an id attribute to the given table if one does not exist. If the table has no "id" attribute, then one is added with a value of "Table-n" where n is the nth table found in the parent document.
	 * 
	 * @param theTable -  org.jsoup.nodes.Element the table element to add an id attribute to
	 */
	private  void maybeAddTableAttributes(Element theTable) {
			if (!theTable.hasAttr("id")) {
				theTable.attr("id",this.tableID);
			}
			String newClass = theTable.attr("class") + " " + this.tableClass;  
			theTable.attr("class", newClass);
	}
	
	/**
	 * Adds a jQuery document ready javascript to the given document that adds search and sort capabilities to the given table element
	 * 
	 * @param theDoc - the org.jsoup.nodes.Document to alter
	 * @param theTable - org.jsoup.nodes.Element the table element that will have the search sort capabilities added.
	 */
	private  void addScripts(Element afterElement) {
		String script = String.format(this.fmt_DATATABLES_INIT,this.tableID, this.doPage, this.doSort, this.doSearch, this.pageSize, this.allowSizeChange, this.pageSize );
		afterElement.after(script);
	}
}
