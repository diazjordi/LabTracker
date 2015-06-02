package main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.SQLException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import htmlhandling.HTMLScraper;
import setup.PropertyManager;

public class LabTracker {
	
	// Main PropertyManager for LabTracker
	private static PropertyManager propManager = new PropertyManager();
	
	// Error Handling
	private static String errorFileOutputPath;
	private static String error;
	
	// Logger
	private static final Logger logger = LogManager.getLogger(LabTracker.class.getName());
	
	public static void main(String[] args) throws IOException, SQLException, InterruptedException {
		
		// Configure Logger
		logger.error("*-----LabTracker Is Starting!-----*");
		//System.out.println();
				
		// Instigate Property pull
		logger.trace("Loading Property Manager");
		//System.out.println("Loading Property Manager");
		propManager.loadProps();
		
		// Set props
		logger.trace("Setting Error Properties");
		errorFileOutputPath = propManager.getErrorProperties().get("errorFileOutputPath");
				
		// Check for Error File, if exists error out of program
		logger.trace("Checking For Error File");
		//System.out.println("Checking For Error File");
		if(checkForErrorFile(errorFileOutputPath)){
			System.exit(0);
		} else {
			logger.trace("Error File not detected!");
		}
		
		// Initiate and pass Maps to HTMLScraper 
		logger.trace("Initiating HTMLScraper");
		HTMLScraper scraper = new HTMLScraper();
		scraper.run();
	}
	
	private static Boolean checkForErrorFile(String errorFileOutputPath) {
		// logic boolean
		boolean exists = false;
		// Check for Error File existence, if exists update DB and exit
		File errorFile = new File(errorFileOutputPath);
		// Check for existence of error file
		if (errorFile.exists()) {
			error = "LabTracker terminating, Error File detected! Resolve error and remove file resolve error to continue with next run!";
			logger.fatal(error);
			fatalError(error);
			exists = true;
		}
		return exists;
	}
	
	private static void fatalError(String error) {
		try {
			File output = new File(errorFileOutputPath);
			ObjectOutputStream listOutputStream = new ObjectOutputStream(new FileOutputStream(output));
			if (error.isEmpty()) {
				listOutputStream.writeUTF("Error Detected in HTMLScraper, please review logs and delete this file to enable next run");
			} else {
				listOutputStream.writeUTF(error);
				System.out.println(error);
			}
			listOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
