package main;

import htmlhandling.HTMLScraper;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import dataobjects.Lab;
import setup.PropertyManager;
import error.Error;

// Main control class for LabTracker program
// When ready for production, update Property file path in PropertyManager class
// and change log output path in log4j2.xml
public class LabTracker {

	private static PropertyManager propManager = PropertyManager.getPropertyManagerInstance();

	private static Error error = Error.getErrorInstance();
	private static String errorFileOutputPath;

	private static final Logger logger = LogManager.getLogger("LabTracker");

	private static ArrayList<Lab> labs = new ArrayList<Lab>();

	public static void main(String[] args) throws IOException, SQLException, InterruptedException {

		logger.trace("*-----LabTracker Is Starting!-----*");
		logger.trace("Loading Property Manager");
		propManager.loadProps();

		logger.trace("Setting Error Properties");
		errorFileOutputPath = propManager.getErrorProperties().get(
				"errorFileOutputPath");
		error.setErrorFileOutputPath(errorFileOutputPath);

		// Check for Error File, if exists error out of program
		logger.trace("Checking For Error File");
		error.checkForErrorFile(errorFileOutputPath);
		logger.trace("Error File not detected!");

		// Initiate HTMLScraper
		logger.trace("Initiating HTMLScraper");
		HTMLScraper scraper = new HTMLScraper();
		scraper.run();

		// Check lab threshold
		logger.trace("Checking number of labs below parser threshold");
		//labsBelowThreshold();

		// Program End
		logger.trace("LabTracker has completed process, shutting down!!");
		logger.trace("*************************************************");
		logger.trace("*************************************************");
		logger.trace("*************************************************");
		logger.trace("*************************************************");
		logger.trace("                                                 ");
		logger.trace("                                                 ");
		logger.trace("                                                 ");
		
		System.exit(0);
	}

	public static void addLab(Lab lab) {
		LabTracker.labs.add(lab);
	}

	public static boolean labsBelowThreshold() {
		boolean below = false;
		int numBelow = 0;
		for (int i = 0; i < labs.size(); i++) {
			if (labs.get(i).isBelowThreshold()) {
				numBelow++;
			}
		}
		if (numBelow >= labs.size() / 2) {
			below = true;
			logger.trace("Too many labs below parser threshold!");
			logger.error("Too many labs below parser threshold!");
			// error.fatalError("Too many labs below parser threshold!");
		}

		return below;
	}
}
