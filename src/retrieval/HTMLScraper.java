package retrieval;

import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import setup.PropertyManager;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import dataobjects.Lab;
import error.Error;

/**
 * Created by Jordi Diaz on 12/22/14. Designed to open browser, navigate to
 * status web page, find HTML code for current status of PC's then fetch that
 * specific HTML div and save it locally for parsing by separate class
 */
public class HTMLScraper {

	private Map<String, String> scraperProperties = new HashMap<String, String>();
	private Map<String, String> labURLs = new HashMap<String, String>();
	private Lab currentLab = new Lab();
	private int threadSleep;
	private int numberOfAttempts;
	private Boolean scrapeSuccess = false;
	private String scrapedHTML = null;
	private static Error error = Error.getErrorInstance();
	private static String errorInfo;

	private static final Logger logger = LogManager.getLogger("LabTracker");

	public void run() throws IOException, SQLException, InterruptedException {
		PropertyManager propManager = PropertyManager.getPropertyManagerInstance();
		
		this.scraperProperties = propManager.getScraperProperties();
		
		this.labURLs = propManager.getLabURLs();
		this.threadSleep = Integer.parseInt(scraperProperties.get("scraperThreadSleep"));
		this.numberOfAttempts = Integer.parseInt(scraperProperties.get("scraperNumberOfAttempts"));
		
		logger.trace("Properties Set, Starting Scraping Process!");
		iterateURLsAndScrape();
	}

	private void iterateURLsAndScrape() throws IOException, InterruptedException, SQLException {
		Iterator<Entry<String, String>> it = labURLs.entrySet().iterator();
		while (it.hasNext()) {
			logger.trace("*-----HTMLScraper Is Starting!-----*");
			Map.Entry<String, String> pair = (Map.Entry<String, String>) it.next();
			scrapeSuccess = false;
			logger.trace("Attempting To Scrape " + pair.getValue());
			currentLab.setLabName(pair.getKey());
			getHtmlFromPage(pair.getValue());
			currentLab.setScrapedHTML(scrapedHTML);
			if (scrapeSuccess) {
				logger.trace("Scrape Successful, Commencing Parsing");
				HTMLParser parser = new HTMLParser();
				parser.run(currentLab);
			}
		}
	}

	// Takes URL for map page, loads map page into memory
	// searches for status div "the-pieces" and saves relevant html to lab object
	private void getHtmlFromPage(String url) throws IOException, InterruptedException {
		String htmlString;
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
			errorInfo = "Unknown Host Exception for: " + url;
			logger.error(error);
			// Update Boolean to direct rest of process
			pageLoaded = false;
			// Close current web client
			mapClient.closeAllWindows();
			error.fatalError(errorInfo);
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
						// Create string from requested html div
						htmlString = mapPage.getElementById("the-pieces").asXml();					
						// Update Boolean
						divLoaded = true;
						// Log out successful scrape
						logger.trace(url + " contained requested HTML, saved scraped HTML to Lab object!");
						scrapeSuccess = true;
						scrapedHTML = htmlString;
						break;
					} else if (mapPage.getElementById("the-pieces") == null) {
						errorInfo = url	+ " did not contain requested HTML, will try again " + (numberOfAttempts - i) + " times!";
						logger.trace(errorInfo);
					}
				}
			}
			// If no data was found update Boolean and log error
			if (!divLoaded) {
				errorInfo = url + " did not contain requested HTML, made " + numberOfAttempts + " attempts to retrieve!";
				logger.error(error);
				error.fatalError(errorInfo);
			}
		}
	}

}