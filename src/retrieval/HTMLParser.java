package retrieval;

import java.io.*;
import java.sql.*;
import java.sql.Connection;
import java.text.*;
import java.util.*;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mysql.jdbc.Statement;

import org.apache.commons.*;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.*;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import output.DBConnector;
import output.HTMLCreator;
import setup.PropertyManager;
import stations.StudentStation;

/**
 * Created by Jordi Diaz on 12/22/14. Need to implement ability to read multiple
 * files and delete upon completion, no hard coding. Export ArrayList to local
 * file or DB.
 */
@SuppressWarnings("unused")
public class HTMLParser {

	// Parser properties
	private Map<String, String> parserProperties = new HashMap<String, String>();
	private Map<String, String> suppressionProperties = new HashMap<String, String>();

	// Error File property
	private Map<String, String> errorProperties = new HashMap<String, String>();

	// Path to retrieve HTML for parsing
	private String parserInputPath = null;
	private String parserOutputPath = null;
	private String parserSuppressionFilePath = null;

	// Vars to track units
	private Integer numUnits = 0;
	private Integer numInUse = 0;
	private Integer numAvail = 0;
	private Integer numNoStatus = 0;
	private Integer numOffline = 0;

	// Vars to hold HTML divs
	private Elements stationNameDivs;
	private Elements statusDivs;
	private Elements osImageDivs; // currently unused

	// ArrayList to hold parsed and created stations
	private ArrayList<StudentStation> stuStations = new ArrayList<StudentStation>();

	// Count variables
	private String avail;
	private String inUse;
	private String off;

	// Error Handling
	private String errorFileOutputPath;
	private String error;

	// Data Output Classes
	private DBConnector dbConnector = new DBConnector();
	private HTMLCreator htmlCreator = new HTMLCreator();

	// Logger
	private static final Logger logger = LogManager.getLogger("LabTracker");

	public void run(String currentLab) throws IOException, SQLException {
		logger.trace("*-----HTMLParser Is Starting!-----*");
		// Set props
		logger.trace("Retrieving Parser Properties");
		getProps();
		// parse HTML for needed fields/divs
		logger.trace("Parsing HTML For Requested Data");
		parseHTML();
		// parse retrieved divs for data, create station stations and place in
		// data structure
		logger.trace("Creating Station Objects");
		createStationObjects();
		setCountVariables();
		// Update suppressed stations
		logger.trace("Setting Suppressed Stations");
		setSuppressedStations(stuStations, suppressionProperties);
		// Write to HTML Map Page
		logger.trace("Updating HTML Map Page");
		// writeMapOfStationsToHTML(stuStations);
		htmlCreator.writeMapOfStationsToHTML(stuStations, avail, inUse, off);
		// Write to DB
		logger.trace("Writing Data To MYSQL DB");
		dbConnector.writeObjectsToTable(stuStations, avail, inUse, off);
		dbConnector.writeRunStatusToTable(avail, inUse, off);
		// Write out objects to local file
		logger.trace("Writing Objects To Local Serialized File");
		writeObjectsToFile(stuStations);
		// Check % Offline, if above threshold error out
		logger.trace("Checking Error Reporting Threshold");
		if (numOffline > (numUnits * .2)) {
			error = "Number of units reporting Offline is above threshold, LabTracker will shut down until manually restarted!";
			logger.error(error);
			fatalError(error);
		}
	}

	// Get properties from prop files
	private void getProps() throws IOException {
		PropertyManager propManager = new PropertyManager();
		this.parserProperties = propManager.getParserProperties();
		this.suppressionProperties = propManager.getSuppressionProperties();
		this.parserInputPath = parserProperties.get("parserInputPath");
		this.parserOutputPath = parserProperties.get("parserOutputPath");
		this.parserSuppressionFilePath = parserProperties
				.get("parserSuppressionFilePath");
		this.errorFileOutputPath = errorProperties.get("errorFileOutputPath");
		logger.trace("Parser Input File Path:        " + parserInputPath);
		logger.trace("Parser Local Output File Path: " + parserOutputPath);
		logger.trace("Supression File Path:          "
				+ parserSuppressionFilePath);
	}

	/**
	 * Methods to extract needed fields from HTML These methods are not really
	 * needed, but structure of HTML may change in the future. May be beneficial
	 * to make code as modular as possible to avoid coding confusion later
	 */
	private void parseHTML() throws IOException {
		try {
			/**
			 * Load HTML pulled from page into File then load file into Document
			 * for parsing
			 */
			File input = new File(parserInputPath);
			Document doc = Jsoup.parse(input, "UTF-8", "");
			// Create elements out of relevant HTML divs
			stationNameDivs = doc.getElementsByClass("station-label");
			statusDivs = doc.getElementsByClass("station");
			osImageDivs = doc.getElementsByClass("os-image");
			numUnits = stationNameDivs.size();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Iterate through station divs, get relevant information out, then turn
	 * them into stations and/or store all retrieved data in some sort of
	 * persistent/saved data structure
	 */
	private void createStationObjects() {
		// Iterates through divs containing station ID info
		for (int k = 0; k < stationNameDivs.size(); k++) {
			// Retrieves each station name from station name divs
			String stationName = stationNameDivs.get(k).text();
			// Retrieves each station ID from status divs
			String stationID = statusDivs.get(k).id();
			// Retrieves each station status
			String status = getStationStatus(statusDivs.get(k).toString());
			// Create stations.StudentStation object with extracted data and add
			// station to ArrayList
			StudentStation stu1 = new StudentStation(stationName, stationID,
					status);
			stuStations.add(k, stu1);
		}
	}

	private void setCountVariables() {
		for (StudentStation station : stuStations) {
			if (station.getStationStatus().matches("Available")) {
				numAvail++;
			} else if (station.getStationStatus().matches("InUse")) {
				numInUse++;
			} else if (station.getStationStatus().matches("Offline")) {
				numOffline++;
			}
		}
		numUnits = numAvail + numInUse + numOffline;
		logger.trace("Total Number of Units: " + numUnits);
		logger.trace("Number of Available: " + numAvail);
		logger.trace("Number of In Use: " + numInUse);
		logger.trace("Number of No Status: " + numOffline);
		float numUnits1 = numUnits;
		float numAvail1 = numAvail;
		float numInUse1 = numInUse;
		float numOffline1 = numOffline;
		float percentAvail = (numAvail1 / numUnits1) * 100;
		float percentInUse = (numInUse1 / numUnits1) * 100;
		float percentOffline = (numOffline1 / numUnits1) * 100;
		int percAvail = (int) percentAvail;
		int percInUse = (int) percentInUse;
		int percOffline = (int) percentOffline;
		avail = "(Available - " + numAvail + ", " + numUnits + ", " + percAvail
				+ "%)";
		inUse = "(In Use    - " + numInUse + ", " + numUnits + ", " + percInUse
				+ "%)";
		off = "(Offline   - " + numOffline + ", " + numUnits + ", "
				+ percOffline + "%)";
		logger.trace(avail);
		logger.trace(inUse);
		logger.trace(off);
	}

	// Extracts station status from HTML div class="station"
	private String getStationStatus(String statusDiv) {
		Pattern pat = Pattern.compile("(?<=Computer-01-)(\\w*)(?=\\.png)");
		Matcher mat = pat.matcher(statusDiv);
		String stationStatus;
		if (mat.find()) {
			stationStatus = mat.group().toString();
			if (stationStatus.matches("InUse")) {
				stationStatus = "InUse";
			} else if (stationStatus.matches("PoweredOn")) {
				stationStatus = "Available";
			}
		} else {
			stationStatus = "NoStatusAvailable";
		}
		return stationStatus;
	}

	@SuppressWarnings("rawtypes")
	private void setSuppressedStations(ArrayList<StudentStation> stuStations,
			Map<String, String> suppressionProperties) {
		for (int i = 0; i < stuStations.size(); i++) {
			Iterator it = suppressionProperties.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				if (stuStations.get(i).getStationName()
						.matches(pair.getValue().toString())) {
					stuStations.get(i).setStationStatus("Suppressed");
				}
			}
		}
	}

	// Writes station objects to serialized file
	private void writeObjectsToFile(ArrayList<StudentStation> stuStations)
			throws IOException {
		try {
			File output = new File(parserOutputPath);
			ObjectOutputStream listOutputStream = new ObjectOutputStream(
					new FileOutputStream(output));
			for (int i = 0; i < stuStations.size(); i++) {
				listOutputStream.writeObject(stuStations.get(i).toString());
			}
			listOutputStream.flush();
			listOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void fatalError(String error) {
		try {
			File output = new File(errorFileOutputPath);
			ObjectOutputStream listOutputStream = new ObjectOutputStream(
					new FileOutputStream(output));
			if (error.isEmpty()) {
				listOutputStream
						.writeUTF("Error Detected in HTMLScraper, please review logs and delete this file to enable next run");
			} else {
				System.out.println(error);
				listOutputStream.writeUTF(error);
			}
			listOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}