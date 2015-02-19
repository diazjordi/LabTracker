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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by Jordi Diaz on 12/22/14.
 * Designed to open browser, navigate to status web page, find HTML code for current status of PC's
 * then fetch that specific HTML div and save it locally for parsing by separate class
 */

public class HTMLScraper {
    
    private static String libN1 = "http://10.84.89.162/MapViewerTemplateLib.html";
    // Path to HTMLScraper property file
    private static String propFilePath = "resources/scraper.properties";
    // Amount of time to sleep while page loads
    private static int threadSleep;
    // Path and file name to store  scraped HTML under
    private static String localfilePath = null;
    private static String localfileName = null;
    // Main properties
    private static Properties mainProps = new Properties();
    // Lab URL properties
    private static Properties labURLProps = new Properties();
    // Holds all Lab URLs to be scraped
    private static ArrayList<String> labURLs = new ArrayList<String>();
    
     
    @SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException, SQLException {
        // Run props
        getProps();
        // Run first floor, pass in URL
        getHtmlFromPage(libN1);
        // Run htmlhandling.HTMLParser on output from first floor
        HTMLParser parser = new HTMLParser();
        parser.run();
    }
    
    private void run(){
    	
    }
    
	private static void getProps() throws IOException {
		// Read in prop file
		File propFile = new File(propFilePath);
		FileInputStream fsInput = new FileInputStream(propFile);
		// Load prop file into Property object
		mainProps.load(fsInput);
		// Retrieve Lab URL
		labURLs.add(mainProps.getProperty("labURL"));
		// Test if multiple Lab URL prop is given
		if (!mainProps.getProperty("labURLsFile").isEmpty()) {
			try {
				File labUrlFile = new File(mainProps.getProperty("labURLsFile").toString());
				FileInputStream labFileInput = new FileInputStream(labUrlFile);
				labURLProps.load(labFileInput);
			} catch (IOException e) {
				System.out.println("Lab URLs File error!");
				e.printStackTrace();
			}
		}
		// Retrieve thread sleep time
		threadSleep = Integer.parseInt(mainProps.getProperty("threadSleep"));
		
		
		
		
		System.out.println("Number of lab URLS provided: " + labURLs.size());
		System.out.println(labURLs);
		System.out.println("Thread Sleep is set to: " + threadSleep + " millisecs");
	}
    
    
    // Takes URL for map page, loads map page into memory
    // searches for status div "the-pieces" and saves relevant html locally
    // for parsing
    private static void getHtmlFromPage(String url) throws IOException {
        // Gets Page, will currently only load script output for Firefox 24
        // will throw warnings and alerts, should be sorted out before production testing
        WebClient mapClient = new WebClient(BrowserVersion.FIREFOX_24);
        HtmlPage mapPage = mapClient.getPage(url);
        try {
            // Sleep is necessary for JS on page to execute
            Thread.sleep(threadSleep);
            // Create file to save HTML
            File lib1 = new File("data/raw/libN1");
            // Add HTML to string and write to file
            String libString = mapPage.getElementById("the-pieces").asXml();
            FileUtils.writeStringToFile(lib1, libString);
        } catch (FailingHttpStatusCodeException ex) {
            System.out.println("Error downloading page: " + ex.getMessage());
            return;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Close current web client
        mapClient.closeAllWindows();
    }


}
