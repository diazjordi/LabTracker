package setup;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.BasicConfigurator;

@SuppressWarnings("unused")
public class PropertyManager {

	// General properties path
	private static String propertyFilePath = "/home/superlib/Desktop/LabTracker-Testing-2/Library-North-1st/properties/LabTrackerProps.properties";
	private static Properties mainProperties = new Properties();

	// Scraper properties
	private static Map<String, String> scraperProperties = new HashMap<String, String>();
	private static Map<String, String> labURLs = new HashMap<String, String>();

	// Parser properties
	private static Map<String, String> parserProperties = new HashMap<String, String>();

	// Database properties
	private static Map<String, String> databaseProperties = new HashMap<String, String>();

	// Error File property
	private static Map<String, String> errorProperties = new HashMap<String, String>();

	// HTML Templates & Properties
	private static Map<String, String> htmlProperties = new HashMap<String, String>();

	// Run
	public static void main(String[] args) throws IOException, SQLException,
			InterruptedException {
		loadProps();
	}

	private static void loadProps() throws IOException {
		// Load prop file into main property object
		File mainPropertyFile = new File(propertyFilePath);
		FileInputStream mainInputStream = new FileInputStream(mainPropertyFile);
		mainProperties.load(mainInputStream);
		mainInputStream.close();
		// Check Property has actual values
		// if so proceed to retrieve properties
		if (!mainProperties.isEmpty()) {
			setProps();
		} else if (mainProperties.isEmpty()) {
			System.out.println("No Properties Found!");
			System.exit(0);
		}
	}

	private static void setProps() throws IOException {
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
			} else if (key.startsWith("error")) {
				errorProperties.put(key, mainProperties.getProperty(key));
			} else if (key.startsWith("html")) {
				htmlProperties.put(key, mainProperties.getProperty(key));
			}
		}

		// System.out.println("Scraper Properties");
		// for (String key : scraperProperties.keySet()) {
		// System.out.println(key + ": " + scraperProperties.get(key));
		// }
		//
		// System.out.println("Parser Properties");
		// for (String key : parserProperties.keySet()) {
		// System.out.println(key + ": " + parserProperties.get(key));
		// }
		//
		// System.out.println("Database Properties");
		// for (String key : databaseProperties.keySet()) {
		// System.out.println(key + ": " + databaseProperties.get(key));
		// }
		//
		// System.out.println("Error Properties");
		// for (String key : errorProperties.keySet()) {
		// System.out.println(key + ": " + errorProperties.get(key));
		// }
		//
		// System.out.println("HTML Properties");
		// for (String key : htmlProperties.keySet()) {
		// System.out.println(key + ": " + htmlProperties.get(key));
		// }
	}

	// Getters for property maps
	public static Map<String, String> getScraperProperties() {
		return scraperProperties;
	}

	public static Map<String, String> getLabURLs() {
		return labURLs;
	}

	public static Map<String, String> getParserProperties() {
		return parserProperties;
	}

	public static Map<String, String> getDatabaseProperties() {
		return databaseProperties;
	}

	public static Map<String, String> getErrorProperties() {
		return errorProperties;
	}

	public static Map<String, String> getHtmlProperties() {
		return htmlProperties;
	}

}