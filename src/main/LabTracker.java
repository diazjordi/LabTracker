package main;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import dataobjects.Lab;
import retrieval.HTMLScraper;
import setup.PropertyManager;
import error.Error;

// Main control class for LabTracker program
// When ready for production, update Property file path in PropertyManager class
// and change log output path in log4j2.xml
public class LabTracker {

	// Singleton PropertyManager
	private static PropertyManager propManager = PropertyManager
			.getPropertyManagerInstance();

	// Singleton Error Handling
	private static Error error = Error.getErrorInstance();
	private static String errorFileOutputPath;

	// Logger
	private static final Logger logger = LogManager.getLogger("LabTracker");

	private static ArrayList<Lab> labs = new ArrayList<Lab>();

	public static void main(String[] args) throws IOException, SQLException,
			InterruptedException {

		logger.trace("*-----LabTracker Is Starting!-----*");
		logger.trace("Loading Property Manager");
		propManager.loadProps();

		logger.trace("Setting Error Properties");
		errorFileOutputPath = propManager.getErrorProperties().get("errorFileOutputPath");
		error.setErrorFileOutputPath(errorFileOutputPath);

		// Check for Error File, if exists error out of program
		logger.trace("Checking For Error File");
		error.checkForErrorFile(errorFileOutputPath);
		logger.trace("Error File not detected!");

		// Initiate HTMLScraper
		logger.trace("Initiating HTMLScraper");
		HTMLScraper scraper = new HTMLScraper();
		scraper.run();
		
		// Program End
		logger.trace("LabTracker has completed process, shutting down!!");

	}

	public static void addLab(Lab lab) {
		LabTracker.labs.add(lab);
	}
}
