package output;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import setup.PropertyManager;
import stations.StudentStation;

@SuppressWarnings("unused")
public class HTMLCreator {

    // HTML Templates & Properties
    private Map<String, String> htmlProperties = new HashMap<String, String>();
    // Error File property
    private Map<String, String> errorProperties = new HashMap<String, String>();

	// Paths to HTML template pages
	private String htmlListTemplateFilePath = null;
	private String htmlMapTemplateFilePath = null;
	// Paths to output HTML pages
	private String htmlListOutputPath = null;
	private String htmlMapOutputPath = null;
	
	// Error Handling
	private String errorFileOutputPath;
	private String error;
	
	// Logger
	private static final Logger logger = LogManager.getLogger("LabTracker");	    
	
	// Get properties from prop files
	private void getProps() throws IOException {
		PropertyManager propManager = new PropertyManager();
		// Get props
		this.htmlProperties = propManager.getHtmlProperties();
		this.errorProperties = propManager.getErrorProperties();
		// Retrieve Error path
		this.errorFileOutputPath = errorProperties.get("errorFileOutputPath");
		// Retrieve HTML props
		this.htmlListTemplateFilePath = htmlProperties.get("htmlListTemplateFilePath");
		this.htmlListOutputPath = htmlProperties.get("htmlListOutputPath");
		this.htmlMapTemplateFilePath = htmlProperties.get("htmlMapTemplateFilePath");
		this.htmlMapOutputPath = htmlProperties.get("htmlMapOutputPath");
		// Eventually log all of these out
		logger.trace("htmlListTemplateFilePath: " + htmlListTemplateFilePath);
		logger.trace("htmlListOutputPath : " + htmlListOutputPath );
		logger.trace("htmlMapTemplateFilePath: " + htmlMapTemplateFilePath);
		logger.trace("htmlMapOutputPath: " + htmlMapOutputPath);
	}

	// Writes stations to HTML Map File
	public void writeMapOfStationsToHTML(ArrayList<StudentStation> stuStations, String avail, String inUse, String off) throws IOException {
		getProps();
			File htmlMapTemplateFile = new File(htmlMapTemplateFilePath);
			String htmlString = FileUtils.readFileToString(htmlMapTemplateFile);
			// Color Strings
			String availColor    = "<FONT COLOR=\"#ffcb2f\">";
			String noStatusColor = "<FONT COLOR=\"#595138\">";
			String inUseColor    = "<FONT COLOR=\"#665113\">";
			// HTML Match Strings
			String begMatch = "<!--$";
			String endMatch = "-->";
			//
			Integer numAvail = 0;
			Integer numUnits = 0;
			for (StudentStation station : stuStations) {				
				if (station.getStationStatus().matches("Available")) {
					numAvail++;
					numUnits++;
					String completeMatch = begMatch + station.getStationNameShort() + endMatch;
					if(htmlString.contains(completeMatch)){
						htmlString = htmlString.replace(completeMatch, availColor);
					}
				}// Not currently displaying anything for In Use stations, leave blank
				else if (station.getStationStatus().matches("InUse")) {
					numUnits++;
					String completeMatch = begMatch + station.getStationNameShort() + endMatch;			
				}
				else {
					numUnits++;
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
