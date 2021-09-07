package iCalendarEditor;

import java.util.GregorianCalendar;

//Lecture: content of a course
public class Lecture extends Event{
	
	private String location;
	private String building;
	private String room;
	
	public Lecture(String summary, GregorianCalendar dtStart, GregorianCalendar dtEnd, RepeatRule rRule, String description, String location, String building, String room) {
		super(summary, dtStart, dtEnd, rRule, description);
		this.location = location;
		this.building = building;
		this.room = room;
	}
	
	public Lecture() {
		
	}
	
	public Lecture(String summary) {
		super.setSummary(summary);
	}

	public String getLocation() {
		return "Location:" + location + ", Building:" + building + ", Room:" + room;
	}
	
	public String getInnerLocation() {
		return location;
	}
	
	public void setInnerLocation(String location) {
		this.location = location;
	}
	
	public String getBuilding() {
		return building;
	}
	
	public void setBuilding(String building) {
		this.building = building;
	}
	
	public String getRoom() {
		return room;
	}
	
	public void setRoom(String room) {
		this.room = room;
	}
	
	public boolean isLocationAvailable() {
		return location != null && building != null && room != null;
	}
	
	//Attach the description of the extended lecture to the end of the description
	//Used in the lectureExtend()
	public void combineDescription(Lecture lecture) {
		if (this.getDescription() == null) this.setDescription(lecture.getDescription());
		else if (lecture.getDescription() != null) this.setDescription(this.getDescription() + "\n\n" + lecture.getDescription());
	}
	
	//Purpose: Describe the course in the overview format
	//			Used in course overview
	public String overviewString() {
		//System.out.println("      Summary       |          Start Time          |           End Time          |               Location               | Alarm | Repeat ");
		return String.format(" %18s | %s | %s | %36s |   %c   | %s ", getSummary(18), getDtStart().getTime().toString(), getDtEnd().getTime().toString(), getLocation(36), getAlarm() != null? 'Y': 'N', getRRule() != null? getRRule().getFrequencyString(): "   N");
	}
	
	@Override
	public Object clone() {
		try {
			Lecture temp = (Lecture)super.clone();
			temp.setDtStart((GregorianCalendar)getDtStart().clone());
			temp.setDtEnd((GregorianCalendar)getDtEnd().clone());
			temp.setAlarm((Alarm)getAlarm().clone());
			temp.setRRulr((RepeatRule)getRRule().clone());
			return temp;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	
}
