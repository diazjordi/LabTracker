package stations;

import java.io.IOException;
import java.io.ObjectOutputStream;
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
    private String os;//currently unused

    public StudentStation(String stationNameIn, String stationIDIn, String stationStatusIn) {
        this.stationName = stationNameIn;
        this.stationNameShort = setStationNameShort(stationNameIn);
        this.stationID = stationIDIn;
        this.stationStatus = stationStatusIn;
    }

    public StudentStation(String stationNameIn, String stationIDIn, String stationStatusIn, String osIN) {
        this.stationName = stationNameIn;
        this.stationNameShort = setStationNameShort(stationNameIn);
        this.stationID = stationIDIn;
        this.stationStatus = stationStatusIn;
        this.os = osIN;
    }
    
    private String setStationNameShort(String stationName){
		String name;
    	Pattern pat = Pattern.compile("(?<=ec-\\w)(\\w*)(?=-ln)");
		Matcher mat = pat.matcher(stationName);
		if (mat.find()) {
			name= mat.group().toString();
		}else{          
			name = this.stationName;
		}
		 return name;
    }
    
    public String getStationNameShort(){
        return stationNameShort;
    }
    
    public String getStationName(){
        return stationName;
    }

    public String getStationID(){
        return stationID;
    }

    public String getStationStatus(){
        return stationStatus;
    }

    public String getStationOS(){
        return os;
    }

    protected void setStationName(String stationNameIN){
        this.stationName = stationNameIN;
    }

    protected void setStationID(String stationIDIN){
        this.stationName = stationIDIN;
    }

    protected void setStationStatus(String stationStatusIN){
        this.stationName = stationStatusIN;
    }

    protected void setStationOS(String osIN){
        this.os = osIN;
    }

    private void writeObject(
            ObjectOutputStream aOutputStream
    ) throws IOException {
        //perform the default serialization for all non-transient, non-static fields
        aOutputStream.defaultWriteObject();
    }
    @Override
    public String toString(){
        String result = "Station Name: " + stationName + "\n" +
                        " Station ID: " + stationID +  "\n" +
                        " Station Status: " + stationStatus + "\n";
        return result;
    }



}
