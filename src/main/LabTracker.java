package main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;

import htmlhandling.HTMLScraper;
import setup.PropertyManager;

public class LabTracker {
	
	// Main PropertyManager for LabTracker
	private static PropertyManager propManager = new PropertyManager();
	// Error Handling
	private static String errorFileOutputPath;
	private static String error;
	
	public static void main(String[] args) throws IOException, SQLException, InterruptedException {
		
		// Configure Logger
		BasicConfigurator.configure();
		System.out.println("*-----LabTracker Is Starting!-----*");
				
		// Instigate Property pull
		System.out.println("Loading Property Manager");
		propManager.loadProps();
		
		// Check for Error File, if exists error out of program
		System.out.println("Checking For Error File");
		if(checkForErrorFile(errorFileOutputPath)){
			fatalError(error);
			System.out.println(error);
			System.exit(0);
		} else {
			System.out.println("Error File not detected!");
		}
		
		// Initiate and pass Maps to HTMLScraper 
		System.out.println("Initiating HTMLScraper");
		HTMLScraper scraper = new HTMLScraper();
		scraper.run();
	}
	
	private static Boolean checkForErrorFile(String errorFileOutputPath) {
		// logic boolean
		boolean exists = false;
		// Check for Error File existence, if exists update DB and exit
		Map<String, String> errorProps = propManager.getErrorProperties();
		errorFileOutputPath = errorProps.get("errorFileOutputPath").toString();
		File errorFile = new File(errorFileOutputPath);
		// Check for existence of error file
		if (errorFile.exists()) {
			error = "LabTracker terminating, Error File detected! Resolve error and remove file resolve error to continue with next run!";
			System.out.println(error);
			fatalError(error);
			exists = true;
		}
		return exists;
	}
	
	private static void fatalError(String error) {
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
