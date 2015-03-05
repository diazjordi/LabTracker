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
	private static String generalPropFilePath = "/home/superlib/Desktop/generalprops.properties";
	// Main properties
    private static Properties generalProps = new Properties();
    // Main properties
    private static Properties mainProps = new Properties();
	// Path to retrieve HTML for parsing
	private static String scraperOutputFilePath = null;
	// Path and file name to store parsed HTML under
	private static String outputFilePath = null;
	private static String outputFileName = null;
	// Path to suppression file
	private static String suppressionFilePath = null;
	// Path to HTML template page
	private static String htmlTemplateFilePath = null;	
	// Vars to track units
	private static int numUnits = 0;
	private static int numInUse = 0;
	private static int numAvail = 0;
	private static int numOffline = 0;
	// Vars to hold HTML divs
	private static Elements stationNameDivs;
	private static Elements statusDivs;
	// private static Elements osImageDivs; //currently unused
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
		// Write to HTML Page
			System.out.println("Updating HTML File With Object Data");
				//writeObjectsToHTMLFile(stuStations);
		// Write to DB
			System.out.println("Writing Object Data To MYSQL DB");
				//writeObjectsToTable(stuStations);
		// Write out objects to local file
			System.out.println("Writing Objects To Local Serialized File");
				//writeObjectsToFile(stuStations);
		
	}
	
	// Get properties from prop files
	private static void getProps() throws IOException {
		// Read in general prop file
		File generalPropFile = new File(generalPropFilePath);
		FileInputStream generalInputStream = new FileInputStream(generalPropFile);
		generalProps.load(generalInputStream);
		String scraperPropPath = generalProps.getProperty("parserPropFile");
		generalInputStream.close();
		// Load prop file into main property object
		File parserPropFile = new File(scraperPropPath);
		FileInputStream parserInputStream = new FileInputStream(parserPropFile);
		mainProps.load(parserInputStream);
		parserInputStream.close();
		// Retrieve thread sleep time
		scraperOutputFilePath = mainProps.getProperty("scraperOutputFilePath");
		// Retrieve local output file path
		outputFilePath = mainProps.getProperty("outputFilePath");
		// Retrieve local output file name
		outputFileName = mainProps.getProperty("outputFileName");
		// Retrieve local suppression file path
		suppressionFilePath = mainProps.getProperty("suppressionFilePath");
		// Retrieve local suppression file path
		htmlTemplateFilePath = mainProps.getProperty("htmlTemplateFilePath");		
		// Combine for later use
		outputFilePath = outputFilePath + outputFileName;
		// Eventually log all of these out
		System.out.println("Scraper Parsed Output File Path: " + scraperOutputFilePath);
		System.out.println("Parser Local Output File Path: " + outputFilePath);
		System.out.println("Supression File Path: " + suppressionFilePath);
		System.out.println("HTML Template File Path: " + htmlTemplateFilePath);		
	}
	
	/**
	 * Methods to extract needed fields from HTML These methods are not really
	 * needed, but structure of HTML may change in the future. May be beneficial
	 * to make code as modular as possible to avoid coding confusion later
	 */
	private static void parseHTML() throws IOException {
		try {
			/**
			 * Load HTML pulled from page into File then load file into Document
			 * for parsing
			 */
			File input = new File(scraperOutputFilePath);
			Document doc = Jsoup.parse(input, "UTF-8", "");
			// Create elements out of relevant HTML divs
			stationNameDivs = doc.getElementsByClass("station-label");
			statusDivs = doc.getElementsByClass("station");
			// osImageDivs = doc.getElementsByClass("os-image"); //currently unused
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
		return stationStatus;
	}

	// Writes stations to HTML file
	private static void writeObjectsToHTMLFile( ArrayList<StudentStation> stuStations) throws IOException {
		File htmlTemplateFile = new File(htmlTemplateFilePath);
		String htmlString = FileUtils.readFileToString(htmlTemplateFile);
		StringBuilder list = new StringBuilder();
		for(StudentStation station: stuStations){
			if(station.getStationName().matches("ec-pg9-ln1000")){
				stuStations.remove(station);
				System.out.println(station.getStationNameShort() + " removed!");
				break;
			}
		}
		// Append table header
		list.append("<ul style=\"list-style-type:none\">");
		Date date = new Date();
		DateFormat timeStamp = new SimpleDateFormat("h:mm a");
		DateFormat dateStamp = new SimpleDateFormat("E, MMM dd");
		String time = timeStamp.format(date).toString();
		String date1 = dateStamp.format(date).toString();
		for (StudentStation station : stuStations) {
			if (station.getStationStatus().matches("Available")) {
				list.append("<li style=\"color:green\"><h1><b><strong>"
						+ station.getStationNameShort().toUpperCase()
						+ "</b></strong></h1></li>");
			}
		}
		list.append("</ul>");
		htmlString = htmlString.replace("$date", date1);
		htmlString = htmlString.replace("$list", list);
		htmlString = htmlString.replace("$time", time);
		File newHtmlFile = new File("/var/www/html/StatusPages/librarynorth1st.html");// should be a property
		FileUtils.writeStringToFile(newHtmlFile, htmlString);
	}

	// Writes station objects data to MySQL DB
	// table: allstationsv1
	// fields: StationName, StationID, StationStatus, OS, DATE
	private static void writeObjectsToTable(
			ArrayList<StudentStation> stuStations) throws IOException,
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
			// empty table
//			String empty = "TRUNCATE TABLE allstationsv1";
//			stmt.executeUpdate(empty);
			for (StudentStation station : stuStations) {
				String query = "INSERT INTO allstationsv1 (StationNameShort, StationName, StationID, StationStatus, OS, DATE) "
						+ " VALUES ('"
						+ station.getStationNameShort()
						+ "','"
						+ station.getStationName()
						+ "','"
						+ station.getStationID()
						+ "','"
						+ station.getStationStatus()
						+ "','"
						+ station.getStationOS() + "', NOW())";
				// System.out.println(query);
				stmt.executeUpdate(query);
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		con.close();
	}

	// Writes station objects to serialized file
	private static void writeObjectsToFile(ArrayList<StudentStation> stuStations)
			throws IOException {
		// Iterate through ArrayList of student stations and write out to file
		try {
			File output = new File(outputFilePath);
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