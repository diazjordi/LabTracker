package setup;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertyManager {
	
	// General properites path
    private static String propertyFilePath = "/home/superlib/Desktop/LabTracker-Testing-2/Library-North-1st/properties/LabTrackerProps.properties";
    private static Properties mainProperties = new Properties();
	
	// Scraper maps and fields
    private static Map<String, String> scraperProperties = new HashMap<String, String>();
    private static Map<String, String> labURLs = new HashMap<String, String>();

    // Parser maps and fields
    private static Map<String, String> parserProperties = new HashMap<String, String>();
    
    // Database fields
    private static Map<String, String> databaseProperties = new HashMap<String, String>();
    
    private static void loadProps() throws IOException{
		// Load prop file into main property object
		File mainPropertyFile = new File(propertyFilePath);
		FileInputStream	mainInputStream = new FileInputStream(mainPropertyFile);
		mainProperties.load(mainInputStream);
		mainInputStream.close();
		
		// Check Property has actual values
		if(!mainProperties.isEmpty()){
			setProps();
		}else if (mainProperties.isEmpty()){
			System.exit(0);
		}
	}
	
	private static void setProps() throws IOException {
		Enumeration e = mainProperties.propertyNames();
		
		
	    while (e.hasMoreElements()) {
	      String key = (String) e.nextElement();
	      System.out.println(key + " -- " + mainProperties.getProperty(key));
	
	    }
	}
	

}
