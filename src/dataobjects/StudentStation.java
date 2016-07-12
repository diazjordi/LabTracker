package dataobjects;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jordi Diaz on 12/22/14.
 */

public class StudentStation implements Serializable {

	private static final long serialVersionUID = 7526472295622776147L;

	private String stationName;
	private String stationNameShort;
	private String stationID;
	private String stationStatus;
	private String os;
	private String statusCode;
	
	public StudentStation(String stationNameIn, String stationIDIn,
			String stationStatusIn) {
		this.stationName = stationNameIn;
		this.stationNameShort = parseStationNameShort(stationNameIn);
		this.stationID = stationIDIn;
		this.stationStatus = stationStatusIn;
	}

	public StudentStation(String stationNameIn, String stationIDIn,
			String stationStatusIn, String osIN) {
		this.stationName = stationNameIn;
		this.stationNameShort = parseStationNameShort(stationNameIn);
		this.stationID = stationIDIn;
		this.stationStatus = stationStatusIn;
		this.os = osIN;
	}

	public String getStationNameShort() {
		return stationNameShort;
	}

	@SuppressWarnings("unused")
	private void setStationNameShort(String stationNameShortIn) {
		this.stationNameShort = stationNameShortIn;
	}

	public String getStationName() {
		return stationName;
	}

	public void setStationName(String stationNameIN) {
		this.stationName = stationNameIN;
	}

	public String getStationID() {
		return stationID;
	}

	public void setStationID(String stationIDIN) {
		this.stationID = stationIDIN;
	}

	public String getStationStatus() {
		return stationStatus;
	}

	public void setStationStatus(String stationStatusIN) {
		this.stationStatus = stationStatusIN;
	}

	public String getStationOS() {
		return os;
	}

	public void setStationOS(String osIN) {
		this.os = osIN;
	}
	
	public String getStatusCode(){
		if(stationStatus.matches("Available")){
			statusCode = "A";
		}
		else if(stationStatus.matches("InUse")){
			statusCode = "I";
		}
		else if(stationStatus.matches("Offline")){
			statusCode = "F";
		}
		else if(stationStatus.matches("Suppressed")){
			statusCode = "S";
		}		
		return statusCode;
	}
	
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	private String parseStationNameShort(String stationName) {
		String name;
		Pattern pat = Pattern.compile("(?<=ec-\\w)(\\w*)(?=-ln)");// old pattern
		Matcher mat = pat.matcher(stationName);
		Pattern pat2 = Pattern.compile("(?<=\\w{2}-\\w{6}-\\w)(\\w*)");// new
																		// pattern
		Matcher mat2 = pat2.matcher(stationName);
		if (mat.find()) {
			name = mat.group().toString();
		} else if (mat2.find()) {
			name = mat2.group().toString();
		} else {
			name = this.stationName;
		}
		return name;
	}

	@Override
	public String toString() {
		String result = " Station Name:    " + stationName + "\n"
				      + " Station ID:      " + stationID + "\n"
				      + " Station Status:  " + stationStatus + "\n";
		return result;
	}	
}
