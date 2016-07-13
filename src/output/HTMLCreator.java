package output;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import dataobjects.Lab;
import dataobjects.StudentStation;
import setup.PropertyManager;
import error.Error;

@SuppressWarnings("unused")
public class HTMLCreator {
	
	private Lab lab;

	private Map<String, String> htmlProperties = new HashMap<String, String>();

	private String htmlMapTemplateFilePath = null;
	private String htmlMapOutputPath = null;

	private static Error error = Error.getErrorInstance();
	private static String errorInfo;

	private static final Logger logger = LogManager.getLogger("LabTracker");
	
	public HTMLCreator(){
	}

	public void getProps() throws IOException {
		PropertyManager propManager = PropertyManager.getPropertyManagerInstance();
		// Get props
		this.htmlProperties = propManager.getHtmlProperties();
		// Retrieve HTML props
		this.htmlMapTemplateFilePath = htmlProperties.get("htmlMapTemplateFilePath") + lab.getLabName() + ".html";
		this.htmlMapOutputPath = htmlProperties.get("htmlMapOutputPath") + lab.getLabName() + ".html";
		// Eventually log all of these out
		logger.trace("htmlMapTemplateFilePath:  " + htmlMapTemplateFilePath);
		logger.trace("htmlMapOutputPath:        " + htmlMapOutputPath);
	}
	
	// Writes stations to HTML Map File
	public void writeMapOfStationsToHTML() throws IOException{
		File htmlMapTemplateFile = new File(htmlMapTemplateFilePath);
		String htmlString = FileUtils.readFileToString(htmlMapTemplateFile);			
		// Color Strings
		String availColor =    "<FONT COLOR=\"#ffcb2f\">";
		String noStatusColor = "<FONT COLOR=\"#595138\">";
		String inUseColor =    "<FONT COLOR=\"#665113\">";
		// HTML Match Strings
		String begMatch = "<!--$";
		String endMatch = "-->";
		Integer numAvail = lab.getAvail();
		Integer numUnits = lab.getTotal();
		Integer numInUse = lab.getInUse();
		Integer numOffline = lab.getOffline();
		for (StudentStation station : lab.getStations()) {
			if (station.getStationStatus().matches("Available")) {
				String completeMatch = begMatch + station.getStationNameShort()	+ endMatch;
				if (htmlString.contains(completeMatch)) {
					htmlString = htmlString.replace(completeMatch, availColor);
				}
			}
			else if (station.getStationStatus().matches("InUse")) {
				String completeMatch = begMatch + station.getStationNameShort()	+ endMatch;
			} else {
				String completeMatch = begMatch + station.getStationNameShort()	+ endMatch;
				if (htmlString.contains(completeMatch)) {
					htmlString = htmlString.replace(completeMatch,noStatusColor);
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
		
		float percentAvail = (float) (numAvail / numUnits) * 100;
		float percentInUse = (float) (numInUse / numUnits) * 100;
		float percentOffline = (float) (numOffline / numUnits) * 100;
		int percAvail = (int) percentAvail;
		int percInUse = (int) percentInUse;
		int percOffline = (int) percentOffline;
		String avail = "(Available - " + numAvail   + ", " + numUnits + ")";
		String inUse = "(In Use    - " + numInUse   + ", " + numUnits + ")";
		String off = "(Offline   - "   + numOffline + ", " + numUnits + ")";
		logger.trace(avail);
		logger.trace(inUse);
		logger.trace(off);
		
		htmlString = htmlString.replace("$availSummary", avail);
		htmlString = htmlString.replace("$inUseSummary", inUse);
		htmlString = htmlString.replace("$offSummary", off);
		File newHtmlFile = new File(htmlMapOutputPath);
		FileUtils.writeStringToFile(newHtmlFile, htmlString);			
	}
	
	public Lab getLab() {
		return lab;
	}

	public void setLab(Lab lab) {
		this.lab = lab;
	}
}
