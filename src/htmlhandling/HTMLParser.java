package htmlhandling;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import setup.PropertyManager;
import stations.StudentStation;

import com.mysql.jdbc.Statement;

/**
 * Created by Jordi Diaz on 12/22/14. Need to implement ability to read multiple
 * files and delete upon completion, no hard coding. Export ArrayList to local
 * file or DB.
 */
@SuppressWarnings("unused")
public class HTMLParser {
	
	// Parser properties
    private Map<String, String> parserProperties = new HashMap<String, String>();
    // HTML Templates & Properties
    private Map<String, String> htmlProperties = new HashMap<String, String>();
    // Error File property
    private Map<String, String> errorProperties = new HashMap<String, String>();
    // Database properties
    private Map<String, String> databaseProperties = new HashMap<String, String>();
    
	// Path to retrieve HTML for parsing
	private String parserInputPath = null;
	private String parserOutputPath = null;
	private String parserSuppressionFilePath = null;
	
	// Paths to HTML template pages
	private String htmlListTemplateFilePath = null;
	private String htmlMapTemplateFilePath = null;
	// Paths to output HTML pages
	private String htmlListOutputPath = null;
	private String htmlMapOutputPath = null;
	
	// DB props
	private String database = null;
	private String table = null;
	
	// Vars to track units
	private Integer numUnits = 0;	
	private Integer numInUse = 0;
	private Integer numAvail = 0;
	private Integer numNoStatus = 0;
	private Integer numOffline = 0;
	
	// Vars to hold HTML divs
	private Elements stationNameDivs;
	private Elements statusDivs;	
	private Elements osImageDivs; //currently unused
	
	// ArrayList to hold parsed and created stations
	private ArrayList<StudentStation> stuStations = new ArrayList<StudentStation>();
	
	// Count variables
	private String avail;
	private String inUse;
	private String off;
	
	// Error Handling
	private String errorFileOutputPath;
	private String error;
	
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
		// parse retrieved divs for data, create station stations and place in data structure
			logger.trace("Creating Station Objects");
				createStationObjects();
				setCountVariables();
		// Write to HTML Map Page
			logger.trace("Updating HTML Map With Object Data");
				writeMapOfStationsToHTML(stuStations);
		// Write to DB
			logger.trace("Writing Object Data To MYSQL DB");
				writeObjectsToTable(stuStations);
		// Write out objects to local file
			logger.trace("Writing Objects To Local Serialized File");
				writeObjectsToFile(stuStations);
		// Check % Offline, if above threshold error out
		if(numOffline > (numUnits * .2)){
			error = "Number of units reporting Offline is above threshold, LabTracker will shut down until manually restarted!";
			logger.error(error);
			fatalError(error);
		}
	}
	
	// Get properties from prop files
	private void getProps() throws IOException {
		PropertyManager propManager = new PropertyManager();
		// Get props
		this.parserProperties = propManager.getParserProperties();
		this.htmlProperties = propManager.getHtmlProperties();
		this.databaseProperties = propManager.getDatabaseProperties();
		// Set props
		this.parserInputPath = parserProperties.get("parserInputPath");
		this.parserOutputPath = parserProperties.get("parserOutputPath");
		this.parserSuppressionFilePath = parserProperties.get("parserSuppressionFilePath");
		// Retrieve local template paths
		this.htmlListTemplateFilePath = htmlProperties.get("htmlListTemplateFilePath");
		this.htmlMapTemplateFilePath = htmlProperties.get("htmlMapTemplateFilePath");
		this.htmlListOutputPath = htmlProperties.get("htmlListOutputPath");
		this.htmlMapOutputPath = htmlProperties.get("htmlMapOutputPath");
		// Retrieve Error path
		this.errorFileOutputPath = errorProperties.get("errorFileOutputPath");
		// Retrieve DB properties
		this.database = databaseProperties.get("db");
		this.table = databaseProperties.get("db.table");
		// Eventually log all of these out
		logger.trace("Parser Input File Path: " + parserInputPath);
		logger.trace("Parser Local Output File Path: " + parserOutputPath);
		logger.trace("Supression File Path: " + parserSuppressionFilePath);
		logger.trace("HTML List Template File Path: "	+ htmlListTemplateFilePath);
		logger.trace("HTML Map Template File Path: " + htmlMapTemplateFilePath);
		logger.trace("HTML List Output Path: " + htmlListOutputPath);
		logger.trace("HTML Map Output Path: " + htmlMapOutputPath);
		logger.trace("Storage Database: " + database);
		logger.trace("Storage Table: " + table);
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
			osImageDivs = doc.getElementsByClass("os-image"); // currently unused
			// Set number of units in lab equal to number of HTML divs
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
			StudentStation stu1 = new StudentStation(stationName, stationID, status);
			stuStations.add(k, stu1);
		}
	}	
	
	private void setCountVariables() {
		// Set count variables
		for (StudentStation station : stuStations) {
			if (station.getStationName().matches("ec-pg9-ln1000")) {
				station.setStationStatus("Suppressed");
			}
		}
		for (StudentStation station : stuStations) {
			if (station.getStationStatus().matches("Available")) {
				numAvail++;
			}else if (station.getStationStatus().matches("InUse")) {
				numInUse++;
			}else if (station.getStationStatus().matches("Offline")) {
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
		avail = "(Available - " + numAvail + ", " + numUnits + ", " + percAvail	+ "%)";
		inUse = "(In Use    - " + numInUse + ", " + numUnits + ", " + percInUse	+ "%)";
		off = "(Offline   - " + numOffline + ", " + numUnits + ", "	+ percOffline + "%)";
		logger.trace(avail);
		logger.trace(inUse);
		logger.trace(off);
	}	

	// Extracts station status from HTML div class="station"
	private String getStationStatus(String statusDiv) {
		// Use RegEx to extract station status from HTML
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
	
	// Writes stations to HTML Map File
	private void writeMapOfStationsToHTML( ArrayList<StudentStation> stuStations) throws IOException {
			File htmlMapTemplateFile = new File(htmlMapTemplateFilePath);
			String htmlString = FileUtils.readFileToString(htmlMapTemplateFile);
			// Color Strings
			String availColor = "<FONT COLOR=\"#ffcb2f\">";
			String noStatusColor = "<FONT COLOR=\"#595138\">";
			String inUseColor = "<FONT COLOR=\"#665113\">";
			// HTML Match Strings
			String begMatch = "<!--$";
			String endMatch ="-->";
			for (StudentStation station : stuStations) {				
				if (station.getStationStatus().matches("Available")) {
					String completeMatch = begMatch + station.getStationNameShort() + endMatch;
					if(htmlString.contains(completeMatch)){
						htmlString = htmlString.replace(completeMatch, availColor);
					}
				}// Not currently displaying anything for In Use stations, leave blank
				else if (station.getStationStatus().matches("InUse")) {
					String completeMatch = begMatch + station.getStationNameShort() + endMatch;
//					if(htmlString.contains(completeMatch)){
//						htmlString = htmlString.replace(completeMatch, inUseColor);
//					}					
				}
				else {
					String completeMatch = begMatch + station.getStationNameShort() + endMatch;
					if(htmlString.contains(completeMatch)){
						htmlString = htmlString.replace(completeMatch, noStatusColor);
					}
				}
			}
			Date date = new Date();
			DateFormat timeStamp = new SimpleDateFormat("h:mm a");
			DateFormat dateStamp = new SimpleDateFormat("E, MMM dd");
			String time = timeStamp.format(date).toString();
			String date1 = dateStamp.format(date).toString();
			htmlString = htmlString.replace("$time", time);
			htmlString = htmlString.replace("$date", date1);
			htmlString = htmlString.replace("$numAvail", numAvail.toString());
			htmlString = htmlString.replace("$numUnits", numUnits.toString());
			
			htmlString = htmlString.replace("$availSummary", avail);
			htmlString = htmlString.replace("$inUseSummary", inUse);
			htmlString = htmlString.replace("$offSummary", off);
			File newHtmlFile = new File(htmlMapOutputPath);
			FileUtils.writeStringToFile(newHtmlFile, htmlString);
		}

	// Writes station objects data to MySQL DB
	// table: allstationsv1
	// fields: StationName, StationID, StationStatus, OS, DATE
	private void writeObjectsToTable(ArrayList<StudentStation> stuStations) throws IOException,
			SQLException {
		// Iterate through ArrayList of student stations and write out to table
		// for use by Node.js or Apache front end
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			logger.error("MySQL JDBC Driver Not Found!");
			e.printStackTrace();
			return;
		}
		// Initiate DB connection
		Connection con = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/labtracker", "root", "MS2LflD?5");
		try {
			Statement stmt = (com.mysql.jdbc.Statement) con.createStatement();
			for (StudentStation station : stuStations) {
				String query = "INSERT INTO " + table + " (StationNameShort, StationName, StationID, StationStatus, OS, DATE) "
						+ " VALUES ('" + station.getStationNameShort()	+ "','"	+ station.getStationName() + "','" 
						+ station.getStationID() + "','"	+ station.getStationStatus() + "','" + station.getStationOS() + "', NOW())";
				logger.trace("MySQL Station Query: ");
				logger.trace(query);
				stmt.executeUpdate(query);
			}
			String logQuery = "INSERT INTO " + table + " (StationNameShort, StationName, StationID, StationStatus, OS, DATE) "
					+ " VALUES ('" + avail	+ "','"	+ inUse + "','" 
					+ off + "','RunStatus','" + null + "', NOW())";
			logger.trace("MySQL RunStatus Query: ");
			logger.trace(logQuery);
			stmt.executeUpdate(logQuery);
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		con.close();
	}

	// Writes station objects to serialized file
	private void writeObjectsToFile(ArrayList<StudentStation> stuStations) throws IOException {
		// Iterate through ArrayList of student stations and write out to file
		try {
			File output = new File(parserOutputPath);
			// Create output stream for parsed objects
			ObjectOutputStream listOutputStream = new ObjectOutputStream(
					new FileOutputStream(output));
			// Loop to iterate through list of objects
			for (int i = 0; i < stuStations.size(); i++) {
				listOutputStream.writeObject(stuStations.get(i).toString());
			}
			// Close and clean output stream
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
				listOutputStream.writeUTF("Error Detected in HTMLScraper, please review logs and delete this file to enable next run");
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