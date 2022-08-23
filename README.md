# IG LIST CHANGER

The tooling used to generate the eCQM FHIR R4 Content IG (2021) generates a complete html website. In that site are html pages that contain generated lists of items; in particular a list of measures and a list of libraries.  The tooling generates these lists as tables with two columns: a name and a description. The data for these tables are located in input folders of the build project, and consist of json data files, each file representing a measure or a library.

The desire is to add and/or alter columns in these generated html tables. As the HL7 tooling is not easily altered, especially given the nature and scope of change required to implement a general capability. Many other projects use this tooling, and the risk is too great to implement such changes. Therefore, it was decided to provide an application that is run after the tooling builds the IG website that would update tables as desired. The altered html should either be written to a separate file for further QA, or overwrite the orginal file, if desired. This application serves that purpose.

This application will alter html tables in existing html files.  It uses a control file (xml) that defines how to change a particular table in a specific html file.  The applicaation will alter a table by keeping or removing existing columns of the table, and by adding new columns to a table.  The contents of the table (i.e. each row in the table) are preserved - but with column data removed or added as specified in the control file.  Data for new columns is read from resource files (json formatted) indicated in the control file.

The control file contains zero or more 'table-descriptor' elements that specify which html files to alter, and how to alter a table within those files. 

## Control File structure and elements
Below is an example of a control file containing a single table-descriptor that defines how to alter a table.

~~~  
<table-descriptors>
     <table-descriptor>
        <generatedHTMLFile>C:\ICF-work\Dev\Measures\Ecqm-content-r4-2021\git-myFork\ecqm-content-r4-2021\output\measures.html</generatedHTMLFile>
        <outputHTMLFile>measures-new.html</outputHTMLFile>
        <targetTablePos>0</targetTablePos>
        <resourceDirectory>C:\ICF-work\Dev\Measures\Ecqm-content-r4-2021\git-myFork\ecqm-content-r4-2021\input\resources\measure</resourceDirectory>
        <oldColumn oldPos="0" action="keep" resourceField="title"></oldColumn>
        <newColumn afterPos="0" label="CMS ID" resourceField="identifier" subField="value" type="array" nth="" maxLen="" regex=".*FHIR" default="-"></newColumn>
        <oldColumn oldPos="1" action="keep"  resourceField="description"></oldColumn>
        <newColumn afterPos="1" label="Name" resourceField="name" subField="value" type="string" nth="" maxLen="" regex=".*FHIR" default="-"></newColumn>
        <newColumn afterPos="1" label="Start" resourceField="effectivePeriod" subField="start" type="object" nth="" maxLen="" regex=".*FHIR" default="-"></newColumn>
        <newColumn afterPos="1" label="Contact" resourceField="contact" subField="telecom" type="array" nth="first" maxLen="" regex=".*FHIR" default="-"></newColumn>
    </table-descriptor>
    ...
</table-descriptors>
~~~ 
### Legend:     
- **generatedHTMLFile** Indicates the full pathname, or pathname relative to the location of the IGListChanger application, of the html file to edit. (The name 'generated-...' implies editing an html file that was generated by the IG tooling)  
- **outputHTMLFfile** This is an optional element. If provided, then the edited html document will be written to a file with this full or relative pathname.  If not provided, the edited html document will overwrite the original file (indicated by the generatedHTMLFile pathname).  
- **tagetTablePos** indicates the nth table in the html file to alter.  (If the file only has one table, this value should be 0)
- **resourceDirectory** is the full pathname of the directory that holds the resource json files that were used to populate the table being altered. These resource files are used to populate any new columns added to the table.    
- **oldColumn** is a specification of how to handle existing columns in the table. The attributes in this element dictate how the existing column is to be handled:  
     - **oldPos** indicates the column's position in the original table.  0 = first column, 1 = second column, and so on.
     - **action** is either *keep* or *remove*.  If 'keep', then that column and its existing data is preserved as-is in the altered table. If 'remove', then that column (and its data in the table's rows) are not included in the new, altered table.  
     - **resourceField** is optional and refers to the json field in the resources that populate this column 
- **newColumn** is a specification of how to add a new column of data to the table. The attributes for this element dictate how the new column is to be added:
   - **afterPos** indicates the position in the row for the new column. For example, 0 means add the column after the first column in the old table, and so on. Note that if multiple new columns have the same afterPos value, then they are added to the table row in  the order they appear in the table descriptor.
   - **label** is the name of the column to show in the table header
   - **resourceField** is the field name in the json resources to retrieve when populating the column data in the table. The following attributes in the newColumn element dictate how to process the resource field data:
   - **type** indicates the type of data found in the resource field. It is one of *string*, *object* or *array*. 
      - String fields: The field data is read as a simple string
      - Object fields: The field data is assumed to be another json object. The **subField** attribute indicates which subsequent field to read from that object. The subField data is assumed to be a simple string.
        - Array fields: The field data is assumed to be an array of simple, unnamed, json objects. The following attributes dictate how to specify which object in the array to access:
          - **nth** can be one of *first*, *last*, or a number. This means to simply get the subField data of the first, last, or nth object in the array.
          - **maxLen** If 'nth' is not specified, then maxLen is used. This is a number. The first object in the array whose subField data string length is less than the maxLen value is used.
          - **regex** If 'nth' and 'maxLen' is not specified, then regex is used. The first object in the array whose subField data string matches the regex regular expression is used.
          - **default** If 'nth', 'maxLen' and 'regex' is not specified, OR if any of those fail to return a value, then the given default value is used.
            
## Building the App

This application is a maven project, using Java 1.8 or later.  Build it using 'mvn clean install'.  When successful, the build puts the 'IGListChange-0.0.1-SNAPSHOT.jar' in the target folder of the project.

## Running the App

If you run the application with no arguments, then it will look for a control file with the the name *TableAlterDescriptors.xml* in the same directory as the jar file.  If you want to use your own control file, then supply the full or relative pathname to the file as the single argument to the program.

~~~
java -jar IGListChange-0.0.1-SNAPSHOT.jar  
java -jar IGListChange-0.0.1-SNAPSHOT.jar controlFilename 
~~~
  
## Notes and Assumptions

- This app will be used to alter tables generated by the HL7 and eCQM Measures IG tooling.  
- The data used to populate the IG tooling-generated tables are json data files located folders under the input\resources directory of the IG tooling project
- The order of the rows in these tables correspond 1-1 with the order of the json data resource files  in the directory in which they are located
- It is assumed that the json resource files to be accessed are all in a single folder, and that all the resources have fields that comply with whatever access procedure is dictated in the newColumn element. (Otherwise, the default value specified in the newColumn element will be used as the data for the column.)
- This app will successfully gather data from a json resource file if the desired field to retrieve is either a simple string, an unnamed json object, or an array of unnamed json objects:
  - String example:  
  ``` 
  "id": "AdvancedIllnessandFrailtyExclusionECQMFHIR4"
  
  <newCol afterPos"0" label="Measure ID" type="string"  resourceField="id" default="N/A" />  
  ``` 
  - Object example:  
  ``` 
  "meta": {
       "versionId": "1",
       "lastUpdated": "2021-07-01T12:32:57.000-06:00",
       "source": "#lFh1NBD3JeX5ETtb",
       "profile": [ "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/computable-library-cqfm" ]
  }
  
  <newCol afterPos"0"  label="Source ID"  type="object" resourceField="meta" subField="source" default="unknown"/>  
  ```      
  - Array example:  
  ```
  "identifier": [ {
    "use": "official",
    "system": "http://hl7.org/fhir/cqi/ecqm/Measure/Identifier/guid",
    "value": "2138c351-1c17-4298-aebc-43b42b1aa1ba"
  }, {
    "use": "official",
    "system": "http://hl7.org/fhir/cqi/ecqm/Measure/Identifier/cms",
    "value": "124FHIR"
  } ]
  
  <newColumn afterPos="0" label="CMS ID" resourceField="identifier" subField="value" type="array" regex="*.FHIR" default="-"></newColumn>
```  


References
  - HL7 IG Tooling documentation:  https://confluence.hl7.org/display/FHIR/IG+Publisher+Documentation  
  - HL7 IG Publisher: https://github.com/HL7/fhir-ig-publisher  
  - eCQM Measure IG tooling, 2021: https://github.com/cqframework/ecqm-content-r4-2021 
  
  
  
  
  


            
