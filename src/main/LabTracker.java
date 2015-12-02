package main;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import errors.FatalError;
import errors.MinorError;
import retrieval.HTMLScraper;
import setup.PropertyManager;

// Main control class for LabTracker program
// When ready for production, update Property file path in PropertyManager class
// and change log output path in log4j2.xml
public class LabTracker {
	
	// Main PropertyManager for LabTracker
	private static PropertyManager propManager = new PropertyManager();
	
	// Error Handling
	private static FatalError fatalError;
	private	static MinorError minorError;

	private static String errorFileOutputPath;
	private static String error;
	
	// Logger
	private static final Logger logger = LogManager.getLogger("LabTracker");
	
	public static void main(String[] args) throws IOException, SQLException, InterruptedException {
		
		// Configure Logger
		logger.trace("*-----LabTracker Is Starting!-----*");
				
		// Instigate Property pull
		logger.trace("Loading Property Manager");
		propManager.loadProps();
		
		// Set props
		logger.trace("Setting Error Properties");
		errorFileOutputPath = propManager.getErrorProperties().get("errorFileOutputPath");
		
		// Set Error Props
		FatalError.setErrorFileOutputPath(errorFileOutputPath);
		MinorError.setErrorFileOutputPath(errorFileOutputPath);
				
		// Check for Error File, if exists error out of program
		logger.trace("Checking For Error File");
		checkForErrorFile(errorFileOutputPath);
		
		logger.trace("Error File not detected!");
		
		// Initiate and pass Maps to HTMLScraper 
		logger.trace("Initiating HTMLScraper");
		HTMLScraper scraper = new HTMLScraper();
		scraper.run();
	}
	
	private static void checkForErrorFile(String errorFileOutputPath) {
		// Check for Error File existence, if exists update DB and exit
		File errorFile = new File(errorFileOutputPath);
		// Check for existence of error file
		if (errorFile.exists()) {
			error = "LabTracker terminating, Error File detected! Resolve error and remove file to continue with next run!";
			logger.fatal(error);
			FatalError.fatalErrorEncountered(error);
			System.exit(0);
		}
	}

	public static FatalError getFatalError() {
		return fatalError;
	}

	public static MinorError getMinorError() {
		return minorError;
	}	

}
