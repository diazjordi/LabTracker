# LabTracker
Scrapes and parses LabStats Status Maps. 
Using HTMLUnit and JSOUP. Then can output resulting info as objects to DB or local custom HTML pages.

Major code refactor and program restructoring in the works!
Will be implementing new features and DB schema.

New Features (v 1.1)
- Intuitive Error Detection
    - Detect bad status page map loads
    - Variable page load timeout (to allow program to auto compensate for slow LabStats data)
  
- Increase Modularity of Code Logic
    - Further compartmentalize code sections to allow non standard program exectution dependent on available data
    - Further isolate and define error types and their subsequent logging and effect on program
  
- DB Schema
    - New run status table (with individual run IDs)
    - New error table (paired to individual run IDs)
    - New lab tables (one table per lab to allow easier suppression and updating)
    - New control table (to allow configurable time between runs on a per lab basis)
    - New commercial table (to allow non html configuration of web page commercials)
    
    
    
(Updated 12-14-15)
