package setup;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import main.LabTracker;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import error.Error;

//When ready for production, update Property file path in PropertyManager class
@SuppressWarnings("unused")
public class PropertyManager {

	private static PropertyManager propertyManagerInstance;

	// General properties path
	private static String propertyFilePath = "/home/superlib/Desktop/LabTracker-Dev/Properties/LabTrackerProps.properties";
	private static Properties mainProperties = new Properties();

	// Scraper properties
	private static Map<String, String> scraperProperties = new HashMap<String, String>();
	private static Map<String, String> labURLs = new HashMap<String, String>();

	// Parser properties
	private static Map<String, String> parserProperties = new HashMap<String, String>();
	private static Map<String, String> suppressionProperties = new HashMap<String, String>();

	// Database properties
	private static Map<String, String> databaseProperties = new HashMap<String, String>();
	private static Map<String, String> databaseTables = new HashMap<String, String>();

	// Error File property
	private static Map<String, String> errorProperties = new HashMap<String, String>();

	// HTML Templates & Properties
	private static Map<String, String> htmlProperties = new HashMap<String, String>();

	// Error Handling
	private static Error error = Error.getErrorInstance();
	private static String errorInfo;

	// Logger
	private static final Logger logger = LogManager.getLogger("LabTracker");

	private PropertyManager() {
	}

	public static PropertyManager getPropertyManagerInstance() {
		if (null == propertyManagerInstance) {
			propertyManagerInstance = new PropertyManager();
		}
		return propertyManagerInstance;
	}

	public void loadProps() throws IOException {
		// Load prop file into main property object
		File mainPropertyFile = new File(propertyFilePath);
		FileInputStream mainInputStream = new FileInputStream(mainPropertyFile);
		mainProperties.load(mainInputStream);
		mainInputStream.close();
		// Check Property has actual values
		// if so proceed to retrieve properties
		if (!mainProperties.isEmpty()) {
			this.setProps();
		} else if (mainProperties.isEmpty()) {
			error.fatalError("No Properties Found!");
		}
	}

	private void setProps() throws IOException {
		Set<Object> keys = mainProperties.keySet();
		// Iterate main property object and parse
		// properties into their respective maps
		// based on individual key value
		for (Object k : keys) {
			String key = (String) k;
			if (key.startsWith("scraper")) {
				scraperProperties.put(key, mainProperties.getProperty(key));
			} else if (key.startsWith("parser")) {
				parserProperties.put(key, mainProperties.getProperty(key));
			} else if (key.startsWith("db")) {
				databaseProperties.put(key, mainProperties.getProperty(key));
			} else if (key.startsWith("table")) {
				databaseTables.put(key, mainProperties.getProperty(key));
			} else if (key.startsWith("error")) {
				errorProperties.put(key, mainProperties.getProperty(key));
			} else if (key.startsWith("html")) {
				htmlProperties.put(key, mainProperties.getProperty(key));
			}
		}
		// Set LabURLs into Map
		retrieveLabURLs();
		// Set Suppression list into Map
		retrieveSuppressionList();
	}

	private void retrieveLabURLs() {
		// Temp Properties object to load props from file
		Properties labURLProps = new Properties();
		// Test for LabURLs property
		if (!mainProperties.getProperty("scraperLabURLsFile").isEmpty()) {
			try {
				File labUrlFile = new File(
						mainProperties.getProperty("scraperLabURLsFile"));
				FileInputStream labFileInput = new FileInputStream(labUrlFile);
				labURLProps.load(labFileInput);
				Enumeration<?> labURLKeys = labURLProps.keys();
				while (labURLKeys.hasMoreElements()) { // Iterate through props
					String labProp = labURLKeys.nextElement().toString();
					if (!labURLProps.getProperty(labProp).isEmpty()) {
						labURLs.put(labProp, labURLProps.getProperty(labProp));
					} else if (labURLProps.getProperty(labProp).isEmpty()) {
						// Log error for Lab URL
						errorInfo = "URL for " + labProp
								+ " lab was not given!";
						error.fatalError(errorInfo);// *REVISE*
					}
				}
			} catch (IOException e) {
				errorInfo = "Lab URLs File error!";
				error.fatalError(errorInfo);
			}
		} else if (mainProperties.getProperty("labURLsFile").isEmpty()) {
			errorInfo = "No Lab URL File path given!";
			error.fatalError(errorInfo);
		}
	}

	private void retrieveSuppressionList() {
		// Temp Properties object to load props from file
		Properties suppressionProps = new Properties();
		// Test for LabURLs property
		if (!mainProperties.getProperty("parserSuppressionFilePath").isEmpty()) {
			try {
				File suppressionFile = new File(
						mainProperties.getProperty("parserSuppressionFilePath"));
				FileInputStream suppressionFileInput = new FileInputStream(
						suppressionFile);
				suppressionProps.load(suppressionFileInput);
				Enumeration<?> suppressionKeys = suppressionProps.keys();
				while (suppressionKeys.hasMoreElements()) { // Iterate through
															// props
					String suppressionProp = suppressionKeys.nextElement()
							.toString();
					if (!suppressionProps.getProperty(suppressionProp)
							.isEmpty()) {
						suppressionProperties.put(suppressionProp,
								suppressionProps.getProperty(suppressionProp));
					} else if (suppressionProps.getProperty(suppressionProp)
							.isEmpty()) {
						// Log error for Suppression file
						logger.trace("No Suppression File Provided");
					}
				}
			} catch (IOException e) {
				logger.trace("No Suppression File Provided");
			}
		} else if (mainProperties.getProperty("parserSuppressionFilePath")
				.isEmpty()) {
			logger.trace("No Suppression File Provided");
		}
	}

	// Getters for property maps
	public Map<String, String> getScraperProperties() {
		return scraperProperties;
	}

	public Map<String, String> getLabURLs() {
		return labURLs;
	}

	public Map<String, String> getParserProperties() {
		return parserProperties;
	}

	public Map<String, String> getSuppressionProperties() {
		return suppressionProperties;
	}

	public Map<String, String> getDatabaseProperties() {
		return databaseProperties;
	}

	public Map<String, String> getDatabaseTables() {
		return databaseTables;
	}

	public Map<String, String> getErrorProperties() {
		return errorProperties;
	}

	public Map<String, String> getHtmlProperties() {
		return htmlProperties;
	}

}