package iCalendarEditor;

import java.io.PrintWriter;
import java.util.GregorianCalendar;
import java.util.TimeZone;

//The frame of the event
public abstract class Event implements Comparable<Event>, Cloneable{
	
	private String summary;
	private GregorianCalendar dtStart;
	private GregorianCalendar dtEnd;
	private RepeatRule rRule;
	private Alarm alarm;
	private String description;
	
	public Event() {
		
	}
	
	//Basic Constructor
	public Event(String summary, GregorianCalendar dtStart, GregorianCalendar dtEnd) {
		this(summary, dtStart, dtEnd, null, null);
	}
	
	//Full Constructor
	public Event(String summary, GregorianCalendar dtStart, GregorianCalendar dtEnd, RepeatRule rRule, String description) {
		this.summary = summary;
		this.dtStart = dtStart;
		this.dtEnd = dtEnd;
		this.rRule = rRule;
		this.description = description;
	}
	
	public String getSummary() {
		return summary;
	}
	
	//Return summary of the event in certain length
	public String getSummary(int length) {
		if (summary.length() <= length) return summary;
		else return summary.substring(0, length - 3).concat("...");
	}
	
	public void setSummary(String summary) {
		this.summary = summary;
	}
	
	public GregorianCalendar getDtStart() {
		return dtStart;
	}
	
	public void setDtStart(GregorianCalendar dtStart) {
		this.dtStart = dtStart;
	}
	
	public GregorianCalendar getDtEnd() {
		return dtEnd;
	}
	
	public void setDtEnd(GregorianCalendar dtEnd) {
		this.dtEnd = dtEnd;
	}
	
	public RepeatRule getRRule() {
		return rRule;
	}
	
	public void setRRulr(RepeatRule rRule) {
		this.rRule = rRule;
	}
	
	public Alarm getAlarm() {
		return alarm;
	}
	
	public void setAlarm(Alarm alarm) {
		this.alarm = alarm;
	}
	
	public abstract String getLocation();
	
	//Return location of the event in certain length
	public String getLocation(int length) {
		String locationStr = getLocation();
		if (locationStr.length() <= length) return locationStr;
		else return locationStr.substring(0, length - 3).concat("...");
	}
	
	public abstract boolean isLocationAvailable();
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public GregorianCalendar getDtLast() {
		return this.getRRule() != null?this.getRRule().getEndDt(): this.getDtEnd();
	}
	
	//Comparable interface: enable the event to be sorted
	//Compare the start time of two events
	public int compareTo(Event obj) {
		return dtStart.compareTo(obj.getDtStart());
	}
	
	//Check whether the two lectures have same summary, start time, end time, location
	//Used in the optimize() to check whether the lecture can extend a lecture series
	public boolean basiclyEqualTo(Event event) {
		final long MILLISECONDS_PER_WEEK = 1000 * 60 * 60 * 24 * 7;
		if (this.getSummary().equals(event.getSummary()) && //summary
				this.getDtStart().getTimeInMillis() % MILLISECONDS_PER_WEEK == event.getDtStart().getTimeInMillis() % MILLISECONDS_PER_WEEK && //start time
				this.getDtEnd().getTimeInMillis() % MILLISECONDS_PER_WEEK == event.getDtEnd().getTimeInMillis() % MILLISECONDS_PER_WEEK && //end time
				this.getLocation().equals(event.getLocation())) return true;	//location
		else return false;
	}
	
	//Purpose: Change the time zone the event is based on
	//			mostly used to change the American east time zone (-5) to Chinese time zone (+8)
	public void changeTimeZoneBase(TimeZone src, TimeZone dest) {
		dtStart.add(GregorianCalendar.MILLISECOND, src.getRawOffset() - dest.getRawOffset());
		dtEnd.add(GregorianCalendar.MILLISECOND, src.getRawOffset() - dest.getRawOffset());
		if (rRule != null) rRule.getEndDt().add(GregorianCalendar.MILLISECOND, src.getRawOffset() - dest.getRawOffset());
	}
	
	//Purpose: Change the DST system to non-DST system
	//			mostly used to change the American DST to Chinese non-DST system
	public void fixDST() {
		if (isDST(dtStart)) dtStart.add(GregorianCalendar.HOUR_OF_DAY, 1);
		if (isDST(dtEnd)) dtEnd.add(GregorianCalendar.HOUR_OF_DAY, 1);
	}
	
	//Purpose: Change the non-DST system to DST system
	public void setDST() {
		if (isDST(dtStart)) dtStart.add(GregorianCalendar.HOUR_OF_DAY, -1);
		if (isDST(dtEnd)) dtEnd.add(GregorianCalendar.HOUR_OF_DAY, -1);
	}
	
	//Purpose: Check whether a time is among the DST
	//			used in fixDST() and setDST()
	private static boolean isDST(GregorianCalendar date) {
		return date.compareTo(DSTStartTime(date.get(GregorianCalendar.YEAR))) >= 0 && date.compareTo(DSTEndTime(date.get(GregorianCalendar.YEAR))) <= 0;
	}
	
	
	//Purpose: Get the start time of the DST (the second Sunday of March 2 A.M.)
	//			used to help check whether a time is among the DST
	private static GregorianCalendar DSTStartTime(int year) {
		GregorianCalendar temp = new GregorianCalendar(year, 2, 1, 2, 0, 0);	// The March 1st of that year
		temp.set(GregorianCalendar.DAY_OF_WEEK_IN_MONTH, 2);
		temp.set(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.SUNDAY);
		return temp;
	}
	
	//Purpose: Get the end time of the DST (the first Sunday of November 2 A.M.)
	//			used to help check whether a time is among the DST
	private static GregorianCalendar DSTEndTime(int year) {
		GregorianCalendar temp = new GregorianCalendar(year, 10, 1, 2, 0, 0);	// The November 1st of that year
		temp.set(GregorianCalendar.DAY_OF_WEEK_IN_MONTH, 1);
		temp.set(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.SUNDAY);
		return temp;
	}
	
	//Purpose: Change a GregorianCalendar type value to a ics style format
	public static String formatGregorianCalendarTime(GregorianCalendar time) {
		GregorianCalendar temp = (GregorianCalendar)time.clone();
		//Change the time zone base if the time zone is not UTC
		if (!temp.getTimeZone().equals(TimeZone.getTimeZone("UTC"))) temp.add(GregorianCalendar.MILLISECOND, TimeZone.getTimeZone("UTC").getRawOffset() - temp.getTimeZone().getRawOffset());
		return String.format("%d%s%sT%s%s%sZ", temp.get(GregorianCalendar.YEAR), formatNumber(temp.get(GregorianCalendar.MONTH) + 1, 2), formatNumber(temp.get(GregorianCalendar.DATE), 2), formatNumber(temp.get(GregorianCalendar.HOUR_OF_DAY), 2), formatNumber(temp.get(GregorianCalendar.MINUTE), 2), formatNumber(temp.get(GregorianCalendar.SECOND), 2));
	}
	
	//Purpose: Change a integer value to a formated string with specified length
	//			increase the length by adding 0s at the beginning
	public static String formatNumber(int num, int length) {
		String temp = String.valueOf(num);
		while (temp.length() < length) temp = "0" + temp;
		return temp;
	}
	
	//Purpose: Generate the UID of the event
	//			UID: SimpleICalEditor-[dtStart]-[dtEnd]-[this.hashCode()]
	public String generateUID() {
		return String.format("SimpleICalEditor-%s-%s-%d", formatGregorianCalendarTime(dtStart), formatGregorianCalendarTime(dtEnd), this.hashCode());
	}
	
	//Purpose: Export the event in the ics format by string
	//Output example:
	//	BEGIN:VEVENT
	//	DTSTAMP:20210222T075532Z
	//	UID:SimpleICalEditor-
	//	SUMMARY:CPS*2231*W01 COMPUTER ORGAN &
	//	DTSTART:20210215T003000Z
	//	DTEND:20210215T021500Z
	//	RRULE:FREQ=WEEKLY;UNTIL=20210628T160000Z;INTERVAL=1
	//	LOCATION:Location: W, Building:GEH, Room:C504
	//	BEGIN:VALARM
	//	ACTION:DISPLAY
	//	TRIGGER;RELATED=START:-PT15M
	//	DESCRIPTION:CPS*2231*W01 COMPUTER ORGAN &
	//	END:VALARM
	//	END:VEVENT
	public String exportICSFormat() {
		StringBuilder temp = new StringBuilder("BEGIN:VEVENT\n");	//Event header
		temp.append(String.format("DTSTAMP:%s\n", formatGregorianCalendarTime(new GregorianCalendar())));	//DTSTAMP
		temp.append(String.format("UID:%s\n", generateUID()));	//UID
		temp.append(String.format("SUMMARY:%s\n", summary));	//Summary
		temp.append(String.format("DTSTART:%s\n", formatGregorianCalendarTime(dtStart)));	//dtStart
		temp.append(String.format("DTEND:%s\n", formatGregorianCalendarTime(dtEnd)));	//dtStart
		if (rRule != null) temp.append(rRule.exportICSFormat());	//Repeat rule
		if (isLocationAvailable()) temp.append(String.format("LOCATION:%s\n", getLocation()));	//Location
		if (alarm != null) temp.append(alarm.exportICSFormat());	//Alarm
		temp.append("END:VEVENT\n");	//Event end sign
		return temp.toString();
	}
	
	//Purpose: Directly write the event to the output file
	public void exportToFile(PrintWriter output) {
		output.println("BEGIN:VEVENT");	//Event header
		output.println(String.format("DTSTAMP:%s", formatGregorianCalendarTime(new GregorianCalendar())));	//DTSTAMP
		output.println(String.format("UID:%s", generateUID()));	//UID
		output.println(String.format("SUMMARY:%s", summary));	//Summary
		output.println(String.format("DTSTART:%s", formatGregorianCalendarTime(dtStart)));	//dtStart
		output.println(String.format("DTEND:%s", formatGregorianCalendarTime(dtEnd)));	//dtStart
		if (rRule != null) rRule.exportToFile(output);	//Repeat rule
		if (isLocationAvailable()) output.println(String.format("LOCATION:%s", getLocation()));	//Location
		if (alarm != null) alarm.exportToFile(output);	//Alarm
		output.println("END:VEVENT");	//Event end sign
	}
	
	//Purpose: Describe the course in the overview format
	//			Used in calendar overview
	public String overviewString() {
		//System.out.println(" Is Course |       Summary       |          Start Time          |           End Time          |         Location        | Alarm | Repeat ");
		return String.format("     N     | %19s | %s | %s | %23s |   %c   | %s ", getSummary(19), dtStart.getTime().toString(), dtEnd.getTime().toString(), isLocationAvailable()?getLocation(23): "          null         ", alarm != null? 'Y': 'N', rRule != null? rRule.getFrequencyString(): "  N");
	}
	
}
