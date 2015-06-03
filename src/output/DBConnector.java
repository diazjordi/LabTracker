package output;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import setup.PropertyManager;
import stations.StudentStation;

import com.mysql.jdbc.Statement;

public class DBConnector {
	
	// Database properties
    private Map<String, String> databaseProperties = new HashMap<String, String>();
    
    // Error File property
    private Map<String, String> errorProperties = new HashMap<String, String>();
    
    // Error Handling
 	private String errorFileOutputPath;
 	private String error;
 	
 	// Database
 	private String database;
 	private String table;
 	private String username;
 	private String password;
 	
 	// Logger
 	private static final Logger logger = LogManager.getLogger("LabTracker");
    
	// Get properties from prop files
	private void getProps() throws IOException {
		PropertyManager propManager = new PropertyManager();
		// Get props
		this.databaseProperties = propManager.getDatabaseProperties();
		this.errorProperties    = propManager.getErrorProperties();
		// Retrieve Error path
		this.errorFileOutputPath = errorProperties.get("errorFileOutputPath");
		// Retrieve DB properties
		this.database = databaseProperties.get("db");
		this.table    = databaseProperties.get("db.table");
		this.username = databaseProperties.get("db.username");
		this.password = databaseProperties.get("db.password");
		// Eventually log all of these out
		logger.trace("Database: " + database);
		logger.trace("Table: "    + table);
		logger.trace("Username: " + username);
		logger.trace("Password: " + password);
	}

	// Writes station objects data to MySQL DB
	// table: allstationsv1
	// fields: StationName, StationID, StationStatus, OS, DATE
	public void writeObjectsToTable(ArrayList<StudentStation> stuStations, String avail, String inUse, String off)	throws IOException, SQLException {
		logger.trace("*-----DBConnector is Writing Station Data to Table!-----*");
		// Initiate properties before run
		getProps();
		// Iterate through ArrayList of student stations and write out to table
		// for use by Node.js or Apache front end
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			logger.error("MySQL JDBC Driver Not Found!");
			fatalError(error);
			e.printStackTrace();
			return;
		}
		// Initiate DB connection
		Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + database, username, password);
		try {
			Statement stmt = (com.mysql.jdbc.Statement) con.createStatement();
			for (StudentStation station : stuStations) {
				String query = "INSERT INTO "
						+ table
						+ " (StationNameShort, StationName, StationID, StationStatus, OS, DATE) "
						+ " VALUES ('" + station.getStationNameShort() + "','"
						+ station.getStationName() + "','"
						+ station.getStationID() + "','"
						+ station.getStationStatus() + "','"
						+ station.getStationOS() + "', NOW())";
				// System.out.println(query);
				stmt.executeUpdate(query);
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		con.close();
	}

	public void writeRunStatusToTable(String avail, String inUse, String off) throws IOException, SQLException {
		logger.trace("*-----DBConnector is Writing RunStatus Data to Table!-----*");
		// Initiate properties before run
		getProps();
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			logger.error("MySQL JDBC Driver Not Found!");
			fatalError(error);
			e.printStackTrace();
			return;
		}
		// Initiate DB connection
		Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + database, username, password);
		try {
			Statement stmt = (com.mysql.jdbc.Statement) con.createStatement();
			String logQuery = "INSERT INTO "
					+ table
					+ " (StationNameShort, StationName, StationID, StationStatus, OS, DATE) "
					+ " VALUES ('" + avail + "','" + inUse + "','" + off
					+ "','RunStatus','" + null + "', NOW())";
			System.out.println(logQuery);
			stmt.executeUpdate(logQuery);
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		con.close();
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
		System.exit(0);
	}

}
