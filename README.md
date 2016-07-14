# LabTracker
Scrapes and parses LabStats Status Maps. 
Using HTMLUnit and JSOUP. Then can output resulting info as objects to DB or local custom HTML pages.

Major code refactor and program restructoring in the works!
Will be implementing new features and DB schema.

(Updated 07-11-16)

LabTracker Directory Structure

```
LabTracker
	|       
	|-------HTML
	|		|
	|		|-------Templates
	|       		|
	|       		|-------Maps
	|       		|
	|       		|-------Offline
	|
	|-------JARs
	|
	|-------Logging
	|
	|-------Properties
	|
	|-------Suppression
```
	
	
	(/LabTracker/HTML/Templates/Maps) - contains HTML templates for maps to be used as display output
	(/LabTracker/HTML/Templates/Maps) - contains bash script to run at end of business day, and HTML page to display    	 during non operational hours          
	
	(/LabTracker/JARs) - contains JARs generated from code 
	
	(/LabTracker/Logging) - contains all loggins output as well as "ErrorFile"
	
	(/LabTracker/Properties) - contains LabTrackerProps and LabURLs property files
	
	(/LabTracker/Suppression) - contains list of stations to suppress in property file format
	