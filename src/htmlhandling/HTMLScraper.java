package htmlhandling;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import htmlhandling.HTMLParser;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Created by Jordi Diaz on 12/22/14.
 * Designed to open browser, navigate to status web page, find HTML code for current status of PC's
 * then fetch that specific HTML div and save it locally for parsing by separate class
 */

public class HTMLScraper {
    
//    private static String libN1 = "http://10.84.89.162/MapViewerTemplateLib.html";
    // Path to HTMLScraper property file
    private static String propFilePath = "resources/scraper.properties";
    // Main properties
    private static Properties mainProps = new Properties();
    // Lab URL properties
    private static Properties labURLProps = new Properties();
    // Amount of time to sleep while page loads
    private static int threadSleep;
    // Number of times to try and scrape page
    private static int numberOfTries;
    // Path and file name to store  scraped HTML under
    private static String localFilePath = null;
    private static String localFileName = null;
    // Holds all Lab URLs to be scraped
    private static ArrayList<String> labURLs = new ArrayList<String>();
    
    public static void main(String[] args) throws IOException, SQLException {
        run();        
    }
    
    private static void run() throws IOException, SQLException{
    	// Retrieve props
    	getProps();
    	// Iterate through Lab URLs and and scrape
    	for(String labURL: labURLs){
    		getHtmlFromPage(labURL);
    		// Run HTMLParser on scraped output 
            HTMLParser parser = new HTMLParser();
            parser.run();
    	}
    }
    
	@SuppressWarnings("rawtypes")
	private static void getProps() throws IOException {
		// Read in main prop file
		File propFile = new File(propFilePath);
		FileInputStream fsInput = new FileInputStream(propFile);
		// Load prop file into main property object
		mainProps.load(fsInput);
		// Test for single LabURL or multiple LabURLs property
		if (!mainProps.getProperty("labURL").isEmpty()) {
			labURLs.add(mainProps.getProperty("labURL"));
		} else if (!mainProps.getProperty("labURLsFile").isEmpty()){ // Test if multiple Lab URL prop is given
			try {
				File labUrlFile = new File(mainProps.getProperty("labURLsFile"));
				FileInputStream labFileInput = new FileInputStream(labUrlFile);
				labURLProps.load(labFileInput);
				Enumeration e = labURLProps.elements();
				while(e.hasMoreElements()){// Add all labURLS from file to labURLs ArrayList
					String url = e.nextElement().toString();
					labURLs.add(url);
				}
			} catch (IOException e) {
				System.out.println("Lab URLs File error!");
				e.printStackTrace();
			}
		}else{
			System.out.println("No Lab URLs properties given!");
		}
		// Retrieve thread sleep time
		threadSleep = Integer.parseInt(mainProps.getProperty("threadSleep"));
		// Retrieve number of tries
		numberOfTries = Integer.parseInt(mainProps.getProperty("numberOfTries"));
		// Retrieve local file path
		localFilePath = mainProps.getProperty("localFilePath");
		// Retrieve local file name
		localFileName = mainProps.getProperty("localFileName");
		// Combine for ease
		localFilePath = localFilePath + localFileName;
		System.out.println("Number of lab URLS provided: " + labURLs.size());
		System.out.println(labURLs);
		System.out.println("Thread Sleep is set to: " + threadSleep + " millisecs");
		System.out.println("Number Of Times To Try Scraping: " + numberOfTries);
		System.out.println("Local File Path: " + localFilePath);
	}
    
    
    // Takes URL for map page, loads map page into memory
    // searches for status div "the-pieces" and saves relevant html locally
    // for parsing
    private static void getHtmlFromPage(String url) throws IOException {
        // Gets Page, will currently only load script output for Firefox 24
        // will throw warnings and alerts, should be sorted out before production testing
        WebClient mapClient = new WebClient(BrowserVersion.FIREFOX_24);
        HtmlPage mapPage;
        mapPage = mapClient.getPage(url);
        	try {
                // Sleep is necessary for JS on page to execute
                Thread.sleep(threadSleep);
                // Create file to save HTML
                File scrapedHTML = new File(localFilePath);
                // Add HTML to string and write to file
                String htmlString = mapPage.getElementById("the-pieces").asXml();
                FileUtils.writeStringToFile(scrapedHTML, htmlString);
            } catch (FailingHttpStatusCodeException ex) {
                System.out.println("Error downloading page: " + url + ex.getMessage());
                return;
            } catch (InterruptedException e) {
                System.out.println("Error scraping " + url);
            	e.printStackTrace();
            } catch (UnknownHostException ext){
            	System.out.println("Error connecting to " + url);
            	System.out.println(ext.getCause());
            	System.out.println(ext.getClass());
            }
        
        // Close current web client
        mapClient.closeAllWindows();
    }


}
