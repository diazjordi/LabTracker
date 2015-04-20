package htmlhandling;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Jordi Diaz on 12/22/14.
 * Designed to open browser, navigate to status web page, find HTML code for current status of PC's
 * then fetch that specific HTML div and save it locally for parsing by separate class
 */
@SuppressWarnings({ "unchecked", "rawtypes", "unused"})
public class HTMLScraper {
   
	// Path to General Prop File
    private static String propFilePath = "/home/superlib/Desktop/LabTracker-Testing/Library-North-1st/properties/LabTrackerProps.properties";
    // Main properties
    private static Properties generalProps = new Properties();
    // Main properties
    private static Properties mainProps = new Properties();
    // Lab URL properties
    private static Properties labURLProps = new Properties();
    // Amount of time to sleep while page loads
    private static int threadSleep;
    // Number of times to try and scrape page
    private static int numberOfAttempts;
    // Path and file name to store  scraped HTML under
    private static String scraperOutputPath = null;
    // Holds all Lab URLs to be scraped
    private static Map<String, String> labURLs = new HashMap<String, String>();
    // Determines Whether To Attempt Parsing
    private static Boolean scrapeSuccess = false;
    // Name Of Current Lab being Scraped & Parsed
    protected static String currentLab = null;
    // Logger
    private static Logger log = Logger.getLogger(HTMLScraper.class);
    
    public static void main(String[] args) throws IOException, SQLException, InterruptedException {
        // Configure Logger
    	BasicConfigurator.configure();
    	// Run HTMLScraper
    	run();        
    }
        
	private static void run() throws IOException, SQLException, InterruptedException{
    	System.out.println("LabTracker Is Starting!");
    	// Retrieve props
    	getProps();
    	System.out.println("Starting Scraping Process, Properties Set");
    	// Iterate through Lab URLs and parse
    	Iterator it = labURLs.entrySet().iterator();
    	while(it.hasNext()){
    		Map.Entry<String, String> pair =(Map.Entry<String, String>)it.next();
    		scrapeSuccess = false;
    		System.out.println("Attempting To Scrape " + pair.getValue());
    		getHtmlFromPage(pair.getValue());
			if (scrapeSuccess) {
				System.out.println("Scrape Successful, Commencing Parsing");
				// Run HTMLParser on scraped output
				HTMLParser parser = new HTMLParser();
				parser.run(pair.getKey());
			}
    	}
    	System.out.println("LabTracker has completed process, shutting down!!");
    }
    
	private static void getProps() throws IOException {
		String scraperPropPath = propFilePath;
		// Load prop file into main property object
		File scraperPropFile = new File(scraperPropPath);
		FileInputStream	scraperInputStream = new FileInputStream(scraperPropFile);
		mainProps.load(scraperInputStream);
		scraperInputStream.close();
		// Test for LabURLs property
		if (!mainProps.getProperty("labURLsFile").isEmpty()) {
			try {
				File labUrlFile = new File(mainProps.getProperty("labURLsFile"));
				FileInputStream labFileInput = new FileInputStream(labUrlFile);
				labURLProps.load(labFileInput);
				Enumeration labURLKeys = labURLProps.keys();
				while (labURLKeys.hasMoreElements()) { // Iterate through props
					String labProp = labURLKeys.nextElement().toString();
					if (!labURLProps.getProperty(labProp).isEmpty()) {
						labURLs.put(labProp, labURLProps.getProperty(labProp));
					} else if (labURLProps.getProperty(labProp).isEmpty()) {
						// Log error for Lab URL
						System.out.println("URL for " + labProp+ " lab was not given!");
					}
				}
			} catch (IOException e) {
				System.out.println("Lab URLs File error!");
				e.printStackTrace();
			}
		} else if (mainProps.getProperty("labURLsFile").isEmpty()) {
			System.out.println("No Lab URL File path given!");
		} else { // Catches no LabURLs error
			System.out.println("No Lab URLs properties given!");
			System.out.println("Program can not continue successfully, must exit!");
			System.exit(0);
		}
		// Remove labURL props as they were already handled
		mainProps.remove("labURL");
		mainProps.remove("labURlsFile");
		// Check all properties have been provided
		Enumeration mainPropKeys = mainProps.keys();
		while (mainPropKeys.hasMoreElements()) { // Iterate through props
			String prop = mainPropKeys.nextElement().toString();
			if (mainProps.getProperty(prop).isEmpty()) { // If prop value log error
				System.out.println("No value given for " + prop + " property");
				System.out.println("Program can not continue successfuly, must exit!");
				System.exit(0);
			}
		}
		// Retrieve thread sleep time
		threadSleep = Integer.parseInt(mainProps.getProperty("threadSleep"));
		// Retrieve number of tries
		numberOfAttempts = Integer.parseInt(mainProps.getProperty("numberOfAttempts"));
		// Retrieve local file path
		scraperOutputPath = mainProps.getProperty("scraperOutputPath");
		// Eventually log all of these out
		System.out.println("Number Of Lab URLs provided: " + labURLs.size());
		System.out.println(labURLs);
		System.out.println("Thread Sleep Is Set To: " + threadSleep	+ " millisecs");
		System.out.println("Number Of Times To Attempt Scraping: " + numberOfAttempts);
	}
        
    // Takes URL for map page, loads map page into memory
    // searches for status div "the-pieces" and saves relevant html locally
    // for parsing
	private static void getHtmlFromPage(String url) throws IOException,	InterruptedException {
		// To keep track of whether loads are successful
		Boolean pageLoaded = true;
		Boolean divLoaded = false;
		// Gets Page, will currently only load script output for Firefox 24
		// will throw warnings and alerts, should be sorted.
		WebClient mapClient = new WebClient(BrowserVersion.FIREFOX_24);
		HtmlPage mapPage = null;		
		// Initial attempt to load URL, updates pageLoaded Boolean
		try {
			mapPage = mapClient.getPage(url);
		} catch (UnknownHostException a) {
			// Log out error
			System.out.println("Unknown Host Exception for: " + url);
			// Update Boolean to direct rest of process
			pageLoaded = false;
			// Close current web client
			mapClient.closeAllWindows();
		}
		// If page loaded successfully, continue with scrape attempt
		if (pageLoaded) {
			// Checks whether div has been found 
			if (!divLoaded) {
				// Loop to keep track of number of attempts to retrieve HTML
				for (int i = 1; i <= numberOfAttempts; i++) {
					// Sleep for amount of time set in props, for JS on page to execute
					Thread.sleep(threadSleep);
					// Check page for requested div
					if (mapPage.getElementById("the-pieces") != null) {
						// Create file to save HTML
						File scrapedHTML = new File(scraperOutputPath);
						// Create string from requested html div
						String htmlString = mapPage.getElementById("the-pieces").asXml();
						// Write to file
						FileUtils.writeStringToFile(scrapedHTML, htmlString);
						// Update Boolean
						divLoaded = true;
						// Log out successful scrape
						System.out.println(url + " contained requested HTML, successfully scraped and written to local file!");
						scrapeSuccess = true;
						break;
					} else {
						System.out.println(url + " did not contain requested HTML, will try again " + (numberOfAttempts - i)  + " times!");
					}
				}
			}
			// If no data was found update Boolean and log error
			if(!divLoaded){
				System.out.println(url + " did not contain requested HTML, made " + numberOfAttempts + " attempts to retrieve!");
			}
		}
	}
}