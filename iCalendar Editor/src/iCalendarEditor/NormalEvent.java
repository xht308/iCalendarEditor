package iCalendarEditor;

import java.util.GregorianCalendar;

//Normal event
public class NormalEvent extends Event implements CalContent{
	
	private String location;

	public NormalEvent(String summary, GregorianCalendar dtStart, GregorianCalendar dtEnd, RepeatRule rRule, String description) {
		super(summary, dtStart, dtEnd, rRule, description);
	}
	
	public NormalEvent() {
		super();
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	
	public boolean isLocationAvailable() {
		return location != null;
	}
	
	@Override
	public Object clone() {
		try {
			NormalEvent temp = (NormalEvent)super.clone();
			temp.setDtStart((GregorianCalendar)getDtStart().clone());
			temp.setDtEnd((GregorianCalendar)getDtEnd().clone());
			if (getAlarm() != null) temp.setAlarm((Alarm)getAlarm().clone());
			if (getRRule() != null) temp.setRRulr((RepeatRule)getRRule().clone());
			return temp;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}
