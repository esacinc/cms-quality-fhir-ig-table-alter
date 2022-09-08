package com.icf.iglistchanger;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * This class manages the addition of search and sort capabilities to a table element.  It includes the jquery and jquery DataTables libraries and stylesheets to the html page if not already included. <br>
 * The behavior of this class is governed by the attributes found in the &lt;behavior&gt; element of a TableAlterDescrition element.<br><br>
 * 
  	 * A sample behavor element:
	 * 
	 * <pre>
	 * {@code
	 *              <behavior paging="true" sorting="true" searching="true" pageSize="5" pageSizeChange="false" useOnlineDataTables="true" />
	 * }	
	 * </pre>
	 * 
 * <b>behavior</b>  if present, specifies how to alter the table to include sorting, pagination and/or search capabilities (using the <b>DataTables</b> jquery plugin.) <ul>
 *  <li>       <b>sorting</b>        -  if present, and if value is <i>true</i>, then add ability to sort columns by clicking on the column header<br>
 *  <li>       <b>searching</b>      -  if present, and if value is <i>true</i>, then add a search bar above the table to enable searching table contents<br>
 *  <li>       <b>paging</b>         -  if present, and if value is <i>true</i>, then add pagination to the table<br>
 *  <li>       <b>pageSize</b>       -  if present, and if paging is <i>true</i>, then sets the size of the pages (number of rows) to display per page. Default is 10<br>
 *   <li>      <b>pageSizeChange</b> -  if paging is <i>true</i>, and if present and value is <i>true</i>, then provides a drop-down list allowing user to change page size.<br>  
 *   <li>      <b>useOnlineDataTables</b> - if <i>true</i>, then load jQuery DataTables library and css from online source. Otherwise, load from local "assets/js" and "assets/css" folders.</ul>
 * 
 * <p>
 * In general, the process method of this class will: <br><br>
  	 * Add a script element to the given document to include jquery if one is not already present.  <br>
	 * Add a script element to the given document to include DataTables  <i>after the jquery include element</i>  <br>  
	 * Add a link element to the given document to include DataTables stylesheet. <br> 
	 * Add a 'documentReady' script that initializes the table to be edited with DataTables capabilities.<br><br> 
	 * 
	 * The source javascript and css files for the DataTables library are loaded from either local folders, or via links to online sources. 
	 * This is controlled by the "useOnlineDataTables" attribute of the &lt;behavior&gt; element in the current TableAlterDescriptor. Typically one would set this attribute to 'false', forcing
	 * the DataTable files to be loaded from the local <i>assets/js</i> and <i>assets/css</i> folders.  If set to 'true', then the javascript and css files are loaded from:
	 * <ul>
	 * <li>https://cdn.datatables.net/1.12.1/js/jquery.dataTables.min.js</li>
	 * <li>https://cdn.datatables.net/1.12.1/css/jquery.dataTables.min.css</li>
	 * </ul>
	 * 
	 * Note that the standard jQuery library is already made available locally by the IG tooling mechanisms, so there is no need to load them from online sources.<br>
	 * 
	 * @see "https://cdn.datatables.net/1.12.1/js/jquery.dataTables.min.js"
	 * @see "https://cdn.datatables.net/1.12.1/css/jquery.dataTables.min.css"
	 * @see "jquery.js"

 * @author Dan Donahue
 *
 */
public class SearchSort {
	
	private boolean isActive = false;            // A shorthand variable. If any one of sorting, searching, or pagination is enabled, then this is set to true.
	private boolean doSort = false;              // If true, then add sorting capabilities to a table
	private boolean doSearch = false;            // If true, add search capabilities to a table
	private boolean doPage = false;              // If true, add pagination to a table
	private boolean allowSizeChange = false;     // If true, allows user to change the pages size of the displayed table when pagination is enabled.
	private boolean useOnline = false;           // User can specify via the <behavior> useOnlineDataTables attribute whether to load jQuery DataTables from online, or from local source.
	private int pageSize = 10;                   // If pagination enabled, this specifies the size (in table rows) of each page to display
	private String tableID = "";                 // The "id" attribute of the table we are altering
	private String tableClass = "display";       // The css "class" attribute of the table we are altering. Note: This value is appended to any existing "class" attribute value.
	
	// Static variables for various javascript and css files we may need to include.  Also format statements we'll use to add script and link elements to the html file.
	// Also format statements we'll use to add script and link elements to the html file.
	private final String fn_JQUERY_JS = "assets/js/jquery.js";
	private final String fn_DATATABLES_JS = "assets/js/jquery.dataTables.min.js";
	private final String fn_DATATABLES_CSS = "assets/css/jquery.dataTables.min.css";
	private final String fn_DATATABLES_JS_ONLINE = "https://cdn.datatables.net/1.12.1/js/jquery.dataTables.min.js";
	private final String fn_DATATABLES_CSS_ONLINE = "https://cdn.datatables.net/1.12.1/css/jquery.dataTables.min.css";
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
	 *              <behavior paging="true" sorting="true" searching="true" pageSize="5" pageSizeChange="false" useOnlineDataTables="true" />
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
	 * To be called after a SearchSort object is instantiated.  Processes the contents of the &lt;behavior&gt; element that was provided at instantiation.
	 * The given html document and table element are altered during this process. <br><br>
	 * See "com.icf.iglistchanger.Controller" for the calling object for this object.
	 * 
	 * @param theDoc the html document to alter
	 * @param theTable the table from within the document to alter
	 * 
	 * 
	 */
	public void process(Document theDoc, Element theTable) {
		if (this.isActive) { 
			maybeAddTableAttributes(theTable);
			maybeAddJQuery(theDoc);
		}
	}
	

	/*
	  Given a <behavior> element, initializes the method variables within the SearchSort instance with the values from the element.

	   <behavior paging="true" sorting="true" searching="true" pageSize="5" pageSizeChange="false" useOnlineDataTables="true" />
	*/
	private void init(Element behavior) {
		// <behavior paging="false" pageSize="5" pageSizeChange="true" sort="true" search="true" />
		
		// Defaults are all set to do nothing if no <behavior> element is provided.
		if (behavior != null) {
			this.doPage = (behavior.attr("paging").equalsIgnoreCase("true"));
			this.doSearch = (behavior.attr("searching").equalsIgnoreCase("true"));
			this.doSort = (behavior.attr("sorting").equalsIgnoreCase("true"));
			this.isActive = this.doPage || this.doSearch || this.doSort;
			this.useOnline = (behavior.attr("useOnlineDataTables").equalsIgnoreCase("true"));
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

	
	/* 
	  Adds a script element to the given document to include jquery if one is not already present.  
	  Adds a script element to the given document to include DataTables  AFTER the jquery include element  
	  Adds a link element to the given document to include DataTables stylesheet.  
	  Adds a 'documentReady' script that initializes the table to be edited with DataTables capabilities. 
	  
	  The source javascript and css files for the DataTables library are loaded from either local folders, or via links to online sources. 
	  This is controlled by the "useOnlineDataTables" attribute of the<behavior>; element in the current TableAlterDescriptor. Typically one would set this attribute to 'false', forcing
	  the DataTable files to be loaded from the local assets/js and assets/css folders.  If set to 'true', then the javascript and css files are loaded from:
	  
	  https://cdn.datatables.net/1.12.1/js/jquery.dataTables.min.js
	  https://cdn.datatables.net/1.12.1/css/jquery.dataTables.min.css
	  
	  
	  Note that the standard jQuery library is already made available locally by the IG tooling mechanisms, so there is no need to load them from online sources.
	  
	 */
	private  void maybeAddJQuery(Document theDoc) {
		
		String cssLink = (this.useOnline)? this.fn_DATATABLES_CSS_ONLINE : this.fn_DATATABLES_CSS;
		String jsLink =  (this.useOnline)? this.fn_DATATABLES_JS_ONLINE : this.fn_DATATABLES_JS;
		
		Element jqueryCSSAdd = theDoc.getElementsByAttributeValue("rel", "stylesheet").last();
		
	    jqueryCSSAdd.after(String.format(this.fmt_CSS_INCLUDE, cssLink));
	    Element jqueryAdd = theDoc.getElementsByAttributeValue("src", this.fn_JQUERY_JS).last();
		
		if (jqueryAdd == null) {
			theDoc.body().append(String.format(this.fmt_SCRIPT_INCLUDE, this.fn_JQUERY_JS));
			theDoc.body().append(String.format(this.fmt_SCRIPT_INCLUDE, jsLink));
		}
		else {
			jqueryAdd.after(String.format(this.fmt_SCRIPT_INCLUDE, jsLink));
		}
		
		jqueryAdd = theDoc.getElementsByAttributeValue("src", jsLink).last();
		addScripts(jqueryAdd);
		
		//System.out.println("jqueryAdd = " + jqueryAdd);
	}

	/*
	  Adds an id attribute to the given table if one does not exist. If the table has no "id" attribute, then one is added with a value of "Table-n" where n is the nth table found in the parent document.	  
	*/
	private  void maybeAddTableAttributes(Element theTable) {
			if (!theTable.hasAttr("id")) {
				theTable.attr("id",this.tableID);
			}
			String newClass = theTable.attr("class") + " " + this.tableClass;  
			theTable.attr("class", newClass);
	}
	
	/*
	  Adds a jQuery document ready javascript to the given document that adds search and sort capabilities to the given table element
	*/
	private  void addScripts(Element afterElement) {
		String script = String.format(this.fmt_DATATABLES_INIT,this.tableID, this.doPage, this.doSort, this.doSearch, this.pageSize, this.allowSizeChange, this.pageSize );
		afterElement.after(script);
	}
}
