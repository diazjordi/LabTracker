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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import stations.StudentStation;

import com.mysql.jdbc.Statement;

/**
 * Created by Jordi Diaz on 12/22/14. Need to implement ability to read multiple
 * files and delete upon completion, no hard coding. Export ArrayList to local
 * file or DB.
 */
@SuppressWarnings("unused")
public class HTMLParser {
	
	// Path to General Prop File
	private static String propFilePath = "/home/superlib/Desktop/LabTracker/Library-North-1st/properties/LabTrackerProps.properties";
    // Main properties
    private static Properties mainProps = new Properties();
	// Path to retrieve HTML for parsing
	private static String scraperOutputPath = null;
	// Path and file name to store parsed HTML under
	private static String parserOutputPath = null;
	// Path to suppression file
	private static String suppressionFilePath = null;
	// Paths to HTML template pages
	private static String htmlListTemplateFilePath = null;
	private static String htmlMapTemplateFilePath = null;
	// Path to output HTML pages
	private static String htmlListOutputPath = null;
	private static String htmlMapOutputPath = null;
	// Vars to track units
	private static Integer numUnits = 0;	
	private static Integer numInUse = 0;
	private static Integer numAvail = 0;
	private static Integer numOffline = 0;
	// Vars to hold HTML divs
	private static Elements stationNameDivs;
	private static Elements statusDivs;	
	private static Elements osImageDivs; //currently unused
	// ArrayList to hold parsed and created stations
	private static ArrayList<StudentStation> stuStations = new ArrayList<StudentStation>();

	public void run(String currentLab) throws IOException, SQLException {
		// Retrieve Properties
			System.out.println("Retrieving Parser Properties");
				getProps();
		// parse HTML for needed fields/divs
			System.out.println("Parsing HTML For Requested Data");
				parseHTML();
		// parse retrieved divs for data, create station stations and place in data structure
			System.out.println("Creating Station Objects");
				createStationObjects();
		// Write to HTML List Page
			System.out.println("Updating HTML List With Object Data");
				writeListOfStationsToHTML(stuStations);
		// Write to HTML Map Page
			System.out.println("Updating HTML Map With Object Data");
			writeMapOfStationsToHTML(stuStations);
		// Write to DB
			System.out.println("Writing Object Data To MYSQL DB");
				writeObjectsToTable(stuStations);
		// Write out objects to local file
			System.out.println("Writing Objects To Local Serialized File");
				writeObjectsToFile(stuStations);		
	}
	
	// Get properties from prop files
	private static void getProps() throws IOException {
		String scraperPropPath = propFilePath;
		// Load prop file into main property object
		File parserPropFile = new File(scraperPropPath);
		FileInputStream parserInputStream = new FileInputStream(parserPropFile);
		mainProps.load(parserInputStream);
		parserInputStream.close();
		// Retrieve thread sleep time
		scraperOutputPath = mainProps.getProperty("scraperOutputPath");
		// Retrieve local output file path
		parserOutputPath = mainProps.getProperty("parserOutputPath");
		// Retrieve local suppression file path
		suppressionFilePath = mainProps.getProperty("suppressionFilePath");
		// Retrieve local template paths
		htmlListTemplateFilePath = mainProps.getProperty("htmlListTemplateFilePath");
		htmlMapTemplateFilePath = mainProps.getProperty("htmlMapTemplateFilePath");
		// Retrieve output paths for HTML
		htmlListOutputPath = mainProps.getProperty("htmlListOutputPath");
		htmlMapOutputPath = mainProps.getProperty("htmlMapOutputPath");
		// Eventually log all of these out
		System.out.println("Scraper Output File Path: " + scraperOutputPath);
		System.out.println("Parser Local Output File Path: " + parserOutputPath);
		System.out.println("Supression File Path: " + suppressionFilePath);
		System.out.println("HTML List Template File Path: " + htmlListTemplateFilePath);	
		System.out.println("HTML Map Template File Path: " + htmlMapTemplateFilePath);
		System.out.println("HTML List Output Path: " + htmlListOutputPath);
		System.out.println("HTML Map Output Path: " + htmlMapOutputPath);
	}
	
	/**
	 * Methods to extract needed fields from HTML These methods are not really
	 * needed, but structure of HTML may change in the future. May be beneficial
	 * to make code as modular as possible to avoid coding confusion later
	 */
	private static void parseHTML() throws IOException {
		try {
			/** Load HTML pulled from page into File then load file into Document
			 * for parsing
			 */
			File input = new File(scraperOutputPath);
			Document doc = Jsoup.parse(input, "UTF-8", "");
			// Create elements out of relevant HTML divs
			stationNameDivs = doc.getElementsByClass("station-label");
			statusDivs = doc.getElementsByClass("station");
			osImageDivs = doc.getElementsByClass("os-image"); //currently unused
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
	private static void createStationObjects() {
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

	// Extracts station status from HTML div class="station"
	private static String getStationStatus(String statusDiv) {
		// Use RegEx to extract station status from HTML
		Pattern pat = Pattern.compile("(?<=Computer-01-)(\\w*)(?=\\.png)");
		Matcher mat = pat.matcher(statusDiv);
		String stationStatus;
		if (mat.find()) {
			stationStatus = mat.group().toString();
			if (stationStatus.matches("InUse")) {
				stationStatus = "InUse";
				numInUse++;
			} else if (stationStatus.matches("PoweredOn")) {
				stationStatus = "Available";
				numAvail++;
			} else {
				numOffline++;
			}
		} else {
			stationStatus = "NoStatusAvailable";
			numOffline++;
		}
		numUnits = numAvail + numInUse;
		return stationStatus;
	}

	// Writes stations to HTML file
	private static void writeListOfStationsToHTML( ArrayList<StudentStation> stuStations) throws IOException {
		// Suppresses G9
		for (StudentStation station : stuStations) {
			if (station.getStationName().matches("ec-pg9-ln1000")) {
				stuStations.remove(station);
				numAvail--;
				station.setStationStatus("Suppressed");
				System.out.println(station.getStationNameShort() + " removed!");
				break;
			}
		}
		File htmlTemplateFile = new File(htmlListTemplateFilePath);
		String htmlString = FileUtils.readFileToString(htmlTemplateFile);
		StringBuilder list = new StringBuilder();
		
		// Append table header
		list.append("<ul style=\"list-style-type:none\">");
		Date date = new Date();
		DateFormat timeStamp = new SimpleDateFormat("h:mm a");
		DateFormat dateStamp = new SimpleDateFormat("E, MMM dd");
		String time = timeStamp.format(date).toString();
		String date1 = dateStamp.format(date).toString();
		for (StudentStation station : stuStations) {
			if (station.getStationStatus().matches("Available")) {
				list.append("<li style=\"color:green\"><h1><big><strong>"
						+ station.getStationNameShort().toUpperCase()
						+ "</big></strong></h1></li>");
			}
		}
		list.append("</ul>");
		htmlString = htmlString.replace("$date", date1);
		htmlString = htmlString.replace("$list", list);
		htmlString = htmlString.replace("$time", time);
		htmlString = htmlString.replace("$numAvail", numAvail.toString());
		htmlString = htmlString.replace("$numUnits", numUnits.toString());
		File newHtmlFile = new File(htmlListOutputPath);
		FileUtils.writeStringToFile(newHtmlFile, htmlString);
	}
	
		// Writes stations to HTML Map File
	private static void writeMapOfStationsToHTML( ArrayList<StudentStation> stuStations) throws IOException {
			File htmlMapTemplateFile = new File(htmlMapTemplateFilePath);
			String htmlString = FileUtils.readFileToString(htmlMapTemplateFile);
			// Available Color String
			String availColor = "<FONT COLOR=\"#ffcb2f\">";
			String begMatch = "<!--$";
			String endMatch ="-->";
			for (StudentStation station : stuStations) {				
				if (station.getStationStatus().matches("Available")) {
					String completeMatch = begMatch + station.getStationNameShort() + endMatch;
					if(htmlString.contains(completeMatch)){
						htmlString = htmlString.replace(completeMatch, availColor);
					}					
				}
			}
			File newHtmlFile = new File(htmlMapOutputPath);// should be a property
			FileUtils.writeStringToFile(newHtmlFile, htmlString);
		}

	// Writes station objects data to MySQL DB
	// table: allstationsv1
	// fields: StationName, StationID, StationStatus, OS, DATE
	private static void writeObjectsToTable(ArrayList<StudentStation> stuStations) throws IOException,
			SQLException {
		// Iterate through ArrayList of student stations and write out to table
		// for use by Node.js or Apache front end
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("MySQL JDBC Driver Not Found!");
			e.printStackTrace();
			return;
		}
		// Initiate DB connection
		Connection con = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/labtracker", "root", "MS2LflD?5");
		try {
			Statement stmt = (com.mysql.jdbc.Statement) con.createStatement();
			for (StudentStation station : stuStations) {
				String query = "INSERT INTO allstationsv1 (StationNameShort, StationName, StationID, StationStatus, OS, DATE) "
						+ " VALUES ('" + station.getStationNameShort()	+ "','"	+ station.getStationName() + "','" 
						+ station.getStationID() + "','"	+ station.getStationStatus() + "','" + station.getStationOS() + "', NOW())";
				stmt.executeUpdate(query);
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		con.close();
	}

	// Writes station objects to serialized file
	private static void writeObjectsToFile(ArrayList<StudentStation> stuStations) throws IOException {
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

}