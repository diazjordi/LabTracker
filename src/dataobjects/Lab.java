package dataobjects;

import java.util.ArrayList;

public class Lab {

	private String labName;
	private String scrapedHTML;
	private ArrayList<StudentStation> stations = new ArrayList<StudentStation>();
	private int inUse;
	private int avail;
	private int offline;
	private int total;
	
	public Lab() {
		super();
	}
	
	public Lab(String labName){
		this.labName = labName;
	}
	
	public String getLabName() {
		return labName;
	}

	public void setLabName(String labName) {
		this.labName = labName;
	}

	public String getScrapedHTML() {
		return scrapedHTML;
	}

	public void setScrapedHTML(String scrapedHTML) {
		this.scrapedHTML = scrapedHTML;
	}

	public ArrayList<StudentStation> getStations() {
		return stations;
	}

	public void setStations(ArrayList<StudentStation> stations) {
		this.stations = stations;
	}
	
	public void addStation(StudentStation station){
		this.stations.add(station);
	}

	
	public int getInUse() {
		return inUse;
	}

	public void setInUse(int inUse) {
		this.inUse = inUse;
	}

	public int getAvail() {
		return avail;
	}

	public void setAvail(int avail) {
		this.avail = avail;
	}

	public int getOffline() {
		return offline;
	}

	public void setOffline(int offline) {
		this.offline = offline;
	}

	public int getTotal() {
		return total;
	}

	public void setTotalInternal(int total) {
		total = inUse + avail + offline;
	}
	
	public void setTotal(int total){
		this.total = total;
	}

	@Override
	public String toString() {
		String lab = "Lab Name: " + labName + "/n" +
				"HTML: " + "/n" + scrapedHTML + "/n" +
				"In Use: " + inUse + "/n" +
				"Available: " + avail + "/n" +
				"Offline: " + offline + "/n" +
				"Total: " + total + "/n";
		
		return lab;
	}
	
	
	

}
