package output;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import main.LabTracker;
import setup.PropertyManager;

import com.mysql.jdbc.Statement;

import dataobjects.Lab;
import dataobjects.StudentStation;
import error.Error;

@SuppressWarnings("unused")
public class DBConnector {
	
	private Connection con;
	
	private Map<String, String> databaseProperties = new HashMap<String, String>();

	private String database;
	private String flatTable;
	private String username;
	private String password;

	private static Error error = Error.getErrorInstance();
	private static String errorInfo;

	private static final Logger logger = LogManager.getLogger("LabTracker");
	
	
	public DBConnector(){
		try {
			getProps();
			createConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	private void getProps() throws IOException {
		PropertyManager propManager = PropertyManager.getPropertyManagerInstance();
		this.databaseProperties = propManager.getDatabaseProperties();
		
		this.database = databaseProperties.get("db");
		this.flatTable = databaseProperties.get("db.flattable");
		this.username = databaseProperties.get("db.username");
		this.password = databaseProperties.get("db.password");
	}
	
	private void createConnection(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
			this.con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + database, username, password);
		} catch (SQLException e) {
			e.printStackTrace();
			error.fatalError(e.toString());			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			error.fatalError(e.toString());	
		}
	}
	
	// Write To Lab Specific Table
	public void writeToLabTable(Lab currentLab) throws SQLException{
		logger.trace("*-----DBConnector is Writing to Lab Specific Table!-----*");
		try {
			Statement stmt = (com.mysql.jdbc.Statement) con.createStatement();
			for (StudentStation station : currentLab.getStations()) {
				String query = "INSERT INTO "
						+ currentLab.getLabName()
						+ " (StationName, StationStatus, StationNameShort, StationID, OS, DATE) "
						+ " VALUES ('" 
						+ station.getStationName().toUpperCase() + "','"
						+ station.getStationStatus().toUpperCase() + "','"
						+ station.getStationNameShort().toUpperCase() + "','"
						+ station.getStationID().toUpperCase() + "','"
						+ "NULL" //station.getStationOS() 
						+ "', NOW())";
				logger.trace(query);
				stmt.executeUpdate(query);
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		con.close();
	}
	
	//writeToRunStatusTable
	public void writeToRunStatusTable(Lab currentLab) throws SQLException{
		logger.trace("*-----DBConnector is Writing to Run Status Table!-----*");
		try {
			Statement stmt = (com.mysql.jdbc.Statement) con.createStatement();
			String query = "INSERT INTO "
					+ "RunStatus"
					+ " (Lab, TotalUnits, Available, InUse, Offline, Suppressed, Date) "
					+ " VALUES ('" 
					+ currentLab.getLabName().toUpperCase()	+ "','" 
					+ currentLab.getTotal() + "','"
					+ currentLab.getAvail() + "','" 
					+ currentLab.getInUse()	+ "','" 
					+ currentLab.getOffline() + "','"
					+ currentLab.getSuppressed() + "'," 
					+ " NOW())";
				logger.trace(query);
				stmt.executeUpdate(query);			
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		con.close();
	}
	
	/*
	//writeToFlatTable
	public void writeToFlatTable(ArrayList<Lab> labs) throws SQLException{
		String mainQuery;
		logger.trace("*-----DBConnector is Writing to Flat Table!-----*");
		try {
			Statement stmt = (com.mysql.jdbc.Statement) con.createStatement();
			String query = "INSERT INTO "
					+ "RunStatus"
					+ " (Lab, TotalUnits, Available, InUse, Offline, Suppressed, Date) "
					+ " VALUES ('" 
					+ currentLab.getLabName().toUpperCase()	+ "','" 
					+ currentLab.getTotal() + "','"
					+ currentLab.getAvail() + "','" 
					+ currentLab.getInUse()	+ "','" 
					+ currentLab.getOffline() + "','"
					+ currentLab.getSuppressed() + "'," 
					+ " NOW())";
				logger.trace(query);
				stmt.executeUpdate(query);			
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		con.close();
	}
	*/
	

}
