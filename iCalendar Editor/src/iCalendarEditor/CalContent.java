package iCalendarEditor;

import java.io.PrintWriter;
import java.util.GregorianCalendar;
import java.util.TimeZone;

//Used as a variable type for calendar content (Course, NormalEvent)
public interface CalContent extends Cloneable{
	
	CalContent[] CLIPBOARD = new CalContent[1];
	
	String getSummary();
	
	//Purpose: Change the time zone the event is based on
	//			mostly used to change the American eastern time zone (-5) to Chinese time zone (+8)
	void changeTimeZoneBase(TimeZone src, TimeZone dest);
	
	//Purpose: Change the DST system to non-DST system
	//			mostly used to change the American DST to Chinese non-DST system
	void fixDST();
	
	//Purpose: Change the non-DST system to DST system
	void setDST();
	
	//Purpose: Export the content to ics format for output
	String exportICSFormat();
	
	//Purpose: Directly write the content to the output file
	void exportToFile(PrintWriter output);
	
	
	//Purpose: Describe the calendar content in the overview format
	String overviewString();
	
	//Purpose: Get the start time of the calendar content
	//			for Courses return the earliest start time
	GregorianCalendar getDtStart();
	
	//Purpose: Get the end time of the calendar content
	//			for Courses return the latest end time
	GregorianCalendar getDtLast();
	
	Object clone();
	
	//Purpose: Copy the calendar content -> put the clone of the calendar content into the clipboard
	//			Working with copy and paste (defined in calendarMenu)
	public default void copy() {
		CLIPBOARD[0] = (CalContent)this.clone();
	}
}
