package retrieval;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import errors.FatalError;
import errors.MinorError;
import main.LabTracker;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import setup.PropertyManager;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Jordi Diaz on 12/22/14. Designed to open browser, navigate to
 * status web page, find HTML code for current status of PC's then fetch that
 * specific HTML div and save it locally for parsing by separate class
 */

@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
public class HTMLScraper {

	// Scraper properties
	private Map<String, String> scraperProperties = new HashMap<String, String>();
	// URLs to scrape
	private Map<String, String> labURLs = new HashMap<String, String>();
	// Amount of time to sleep while page loads
	private int threadSleep;
	// Number of times to try and scrape page
	private int numberOfAttempts;
	// Path and file name to store scraped HTML under
	private String scraperOutputPath = null;
	// Determines Whether To Attempt Parsing
	private Boolean scrapeSuccess = false;
	// Error Handling
	private static FatalError fatalError = LabTracker.getFatalError();
	private static MinorError minorError = LabTracker.getMinorError();
	private static String error;
	
	// Logger
	private static final Logger logger = LogManager.getLogger("LabTracker");

	public void run() throws IOException, SQLException, InterruptedException {
		logger.trace("*-----HTMLScraper Is Starting!-----*");
		PropertyManager propManager = new PropertyManager();
		// Get props
		this.scraperProperties = propManager.getScraperProperties();
		// Set props
		this.labURLs = propManager.getLabURLs();
		this.threadSleep = Integer.parseInt(scraperProperties.get("scraperThreadSleep"));
		this.numberOfAttempts = Integer.parseInt(scraperProperties.get("scraperNumberOfAttempts"));
		this.scraperOutputPath = scraperProperties.get("scraperOutputPath");
		logger.trace("Properties Set, Starting Scraping Process!");
		// Run Parent Method to Control scraping
		iterateURLsAndScrape();
	}
	
	private void iterateURLsAndScrape() throws IOException, InterruptedException, SQLException {
		// Iterate through Lab URLs and parse
		Iterator it = labURLs.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> pair = (Map.Entry<String, String>) it.next();
			scrapeSuccess = false;
			logger.trace("Attempting To Scrape " + pair.getValue());
			getHtmlFromPage(pair.getValue());
			if (scrapeSuccess) {
				logger.trace("Scrape Successful, Commencing Parsing");
				// Run HTMLParser on scraped output
				HTMLParser parser = new HTMLParser();
				parser.run(pair.getKey());
			}
		}
		logger.trace("LabTracker has completed process, shutting down!!");
	}

	// Takes URL for map page, loads map page into memory
	// searches for status div "the-pieces" and saves relevant html locally
	// for parsing
	private void getHtmlFromPage(String url) throws IOException, InterruptedException {
		// To keep track of whether loads are successful
		boolean pageLoaded = true;
		boolean divLoaded = false;
		// Gets Page, will currently only load script output for Firefox 24
		// will throw warnings and alerts, should be sorted.
		WebClient mapClient = new WebClient(BrowserVersion.FIREFOX_24);
		HtmlPage mapPage = null;
		// Initial attempt to load URL, updates pageLoaded Boolean
		try {
			mapPage = mapClient.getPage(url);
		} catch (UnknownHostException a) {
			// Log out error
			error = "Unknown Host Exception for: " + url;
			logger.error(error);
			// Update Boolean to direct rest of process
			pageLoaded = false;
			// Close current web client
			mapClient.closeAllWindows();
			FatalError.fatalErrorEncountered(error);
		}
		// If page loaded successfully, continue with scrape attempt
		if (pageLoaded) {
			// Checks whether div has been found
			if (!divLoaded) {
				// Loop to keep track of number of attempts to retrieve HTML
				for (int i = 0; i <= numberOfAttempts; i++) {
					// Sleep for amount of time set in props, for JS on page to
					// execute
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
						logger.trace(url + " contained requested HTML, successfully scraped and written to local file!");
						scrapeSuccess = true;
						break;
					} else if (mapPage.getElementById("the-pieces") == null) {
						error = url	+ " did not contain requested HTML, will try again " + (numberOfAttempts - i) + " times!";
					}
				}
			}
			// If no data was found update Boolean and log error
			if (!divLoaded) {
				error = url + " did not contain requested HTML, made " + numberOfAttempts + " attempts to retrieve!";
				logger.error(error);
				FatalError.fatalErrorEncountered(error);
			}
		}
	}

}