package iCalendarEditor;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Scanner;
import java.util.TimeZone;

public class ICalendar implements Cloneable {

	private String name;
	private File path;
	private ArrayList<CalContent> content;

	public ICalendar() {
		this("My Calender");
	}

	public ICalendar(String name) {
		this(name, null);
	}

	public ICalendar(File path) {
		this(path.getName().substring(0, path.getName().lastIndexOf('.')), path);
	}

	public ICalendar(String name, File path) {
		this.name = name;
		this.path = path;
		content = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	// Return name of the calendar in certain length
	public String getName(int length) {
		if (name.length() <= length)
			return name;
		else
			return name.substring(0, length - 3).concat("...");
	}

	public void setName(String name) {
		this.name = name;
	}

	public File getPath() {
		return path;
	}

	public void setPath(File path) {
		this.path = path;
	}

	public ArrayList<CalContent> getContent() {
		return content;
	}

	private void setContent(ArrayList<CalContent> content) {
		this.content = content;
	}

	public boolean isEmpty() {
		return content.isEmpty();
	}

	// Read a file and convert it into a ICalendar object
	public static ICalendar readFromFile(File path) {
		// Set time zone to UTC (default in ics format)
		// Record the original time zone
		TimeZone backupTz = TimeZone.getDefault();
		// Set time zone
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		try (Scanner input = new Scanner(path)) { // Read the file
			// Check the basic format of the file
			if (!input.nextLine().equals("BEGIN:VCALENDAR"))
				throw new java.io.IOException("The file has incorrect format at Line 1");
			if (!input.nextLine().substring(0, 7).equals("PRODID:"))
				throw new java.io.IOException("The file has incorrect format at Line 2");
			if (!input.nextLine().equals("VERSION:2.0"))
				throw new java.io.IOException("The file has incorrect format at Line 3");
			// Declare the variables for reading
			boolean endFlag = false;
			boolean lectureReadingFlag = false;
			String summary = null;
			GregorianCalendar dtStart = null;
			GregorianCalendar dtEnd = null;
			RepeatRule rRule = null;
			String description = null;
			String location = null;
			String building = null;
			String room = null;
			ICalendar iCal = new ICalendar(path);
			int lineCount = 3; // For error notification
			// Reading line by line
			while (input.hasNext()) {
				String str = input.nextLine();
				lineCount++;
				// Check if the "END:VCALENDAR" is the end of the file
				if (endFlag && !str.isEmpty())
					throw new java.io.IOException("The file has incorrect format at Line " + lineCount);
				// Divide different type of input string
				if (str.equals("BEGIN:VEVENT")) { // Beginning of a event(lecture) -> begin lecture reading
					if (lectureReadingFlag)
						throw new java.io.IOException("The file has incorrect format at Line " + lineCount);
					lectureReadingFlag = true;
				} else if (str.startsWith("CATEGORIES:")) { // Feature of the event but have no meaning for a lecture
					// str example: CATEGORIES:CS
					if (!lectureReadingFlag)
						throw new java.io.IOException("The file has incorrect format at Line " + lineCount);
				} else if (str.startsWith("DTEND:")) { // The end time of the lecture -> set dtEnd
					// str example: DTEND:20210222T151500Z
					if (!lectureReadingFlag)
						throw new java.io.IOException("The file has incorrect format at Line " + lineCount);
					dtEnd = getGregorianCalendar(str);
				} else if (str.startsWith("DTSTAMP:")) { // Feature of the event (creating time of the event) but have
															// no meaning for a lecture
					// str example: DTSTAMP:20210515T151343
					if (!lectureReadingFlag)
						throw new java.io.IOException("The file has incorrect format at Line " + lineCount);
				} else if (str.startsWith("DTSTART:")) { // The start time of the lecture -> set dtStart
					// str example: DTSTART:20210222T133000Z
					if (!lectureReadingFlag)
						throw new java.io.IOException("The file has incorrect format at Line " + lineCount);
					dtStart = getGregorianCalendar(str);
				} else if (str.startsWith("LOCATION:")) { // The location of the classroom -> set location, building and
															// room
					// str example: LOCATION:Location: W\, Building:GEH\, Room:C504
					if (!lectureReadingFlag)
						throw new java.io.IOException("The file has incorrect format at Line " + lineCount);
					String[] temp = readLocations(str);
					location = temp[0];
					building = temp[1];
					room = temp[2];
				} else if (str.startsWith("SEQUENCE:")) { // Feature of the event but have no meaning for a lecture
					// str example: SEQUENCE:0
					if (!lectureReadingFlag)
						throw new java.io.IOException("The file has incorrect format at Line " + lineCount);
				} else if (str.startsWith("SUMMARY:")) { // The summary of the lecture(course) -> set summary
					// str example: SUMMARY:CPS*2231*W01 COMPUTER ORGAN &
					if (!lectureReadingFlag)
						throw new java.io.IOException("The file has incorrect format at Line " + lineCount);
					summary = str.substring(str.indexOf(':') + 1);
				} else if (str.startsWith("UID:")) { // Feature of the event but have no meaning for a lecture
					// str example: UID:af6a34ce-09dd-4f17-9427-49606f00b912
					if (!lectureReadingFlag)
						throw new java.io.IOException("The file has incorrect format at Line " + lineCount);
				} else if (str.startsWith("RRULE:")) { // Feature of the event but have no meaning for a lecture
					// str example: RRULE:FREQ=WEEKLY;UNTIL=20210628T160000Z;INTERVAL=1
					if (!lectureReadingFlag)
						throw new java.io.IOException("The file has incorrect format at Line " + lineCount);
					// TODO Optional because this part is used to represent the repeat rule of the
					// event
					// which is not used in a KEANWISE generated calendar
				}
				// TODO Add the parts related with the alarm
				// Optional because alarm is not used in a KEANWISE generated calendar
				else if (str.equals("END:VEVENT")) { // End of a event(lecture) -> save lecture & reset variables
					if (!lectureReadingFlag)
						throw new java.io.IOException("The file has incorrect format at Line " + lineCount);
					// Save lecture
					iCal.add(new Lecture(summary, dtStart, dtEnd, rRule, description, location, building, room));
					// Reset variables
					lectureReadingFlag = false;
					summary = null;
					dtStart = null;
					dtEnd = null;
					rRule = null;
					description = null;
					location = null;
					building = null;
					room = null;
				} else if (str.equals("END:VCALENDAR")) { // End of the calendar -> stop reading
					if (lectureReadingFlag)
						throw new java.io.IOException("The file has incorrect format at Line " + lineCount);
					endFlag = true;
				} else { // Not supported parts -> stop reading & return a empty calendar
					System.out.println("The formate of the calendar is wrong or is beyond the support of the program.");
					throw new java.io.IOException("Stop reading at Line " + lineCount);
				}

			}
			return iCal;
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
			System.out.println("A error is occured when reading the file. An empty calendar is created instead.");
			return new ICalendar();
		} finally {
			// Restore time zone
			TimeZone.setDefault(backupTz);
		}

	}

	// Work for the file reader to change a string containing time to
	// GregorianCalendar object (UTC)
	// str example: DTEND:20210222T151500Z
	private static GregorianCalendar getGregorianCalendar(String str) {
		// Find the start index of time
		int startIndex = str.indexOf(':') + 1;
		// Set year, month and date
		GregorianCalendar temp = new GregorianCalendar(Integer.parseInt(str.substring(startIndex, startIndex + 4)), // year
				Integer.parseInt(str.substring(startIndex + 4, startIndex + 6)) - 1, // month
				Integer.parseInt(str.substring(startIndex + 6, startIndex + 8))); // date
		// If the time includes hour minute and second -> read & set them
		if (str.length() >= startIndex + 9 && str.charAt(startIndex + 8) == 'T') {
			temp.set(GregorianCalendar.HOUR_OF_DAY, Integer.parseInt(str.substring(startIndex + 9, startIndex + 11))); // Hour
			temp.set(GregorianCalendar.MINUTE, Integer.parseInt(str.substring(startIndex + 11, startIndex + 13))); // Minute
			temp.set(GregorianCalendar.SECOND, Integer.parseInt(str.substring(startIndex + 13, startIndex + 15))); // Second
		}
		return temp;
	}

	// Work for the file reader to get the location, building and room
	// str example: LOCATION:Location: W\, Building:GEH\, Room:C504
	private static String[] readLocations(String str) {
		// get location
		int startIndex = str.indexOf(':') + 1;
		String[] temp = new String[3];
		for (int i = 0; i < temp.length; i++) {
			temp[i] = null;
		}
		for (int i = 0; i < 3; i++) {
			startIndex = str.indexOf(':', startIndex) + 1;
			if (startIndex != 0) {
				int endIndex = str.indexOf('\\', startIndex);
				if (endIndex == -1)
					endIndex = str.length();
				temp[i] = str.substring(startIndex, endIndex);
			} else
				break;
		}
		return temp;
	}

	// Add a calendar content to the calendar -> check the actual type of the
	// content -> use the corresponding add method
	// Used in mergenceOf method
	public void add(CalContent content) {
		// check the actual type of the content
		if (content instanceof Course)
			add((Course) content);
		else if (content instanceof Lecture)
			add((Lecture) content);
		else if (content instanceof NormalEvent)
			add((NormalEvent) content);
	}

	// Add a lecture to the calendar -> add to a course or create a new course
	public void add(Lecture lecture) {
		// Check if the lecture belongs to any existing course
		for (int i = 0; i < content.size(); i++) {
			if (content.get(i) instanceof Course) {
				// Belongs to an existing course -> add to it
				if (((Course) (content.get(i))).getSummary().equals(lecture.getSummary())) {
					((Course) (content.get(i))).add(lecture);
					return;
				}
			}
		}
		// Not belong to existing courses -> add a new course with the name of this
		// lecture
		this.add(new Course(lecture));
	}

	// Add a course to the calendar
	public void add(Course course) {
		content.add(course);
	}

	// Add a normal event to the calendar
	public void add(NormalEvent normalEvent) {
		content.add(normalEvent);
	}

	// Purpose: Change the time zone the event is based on
	// mostly used to change the American east time zone (-5) to Chinese time zone
	// (+8)
	public void changeTimeZoneBase(TimeZone src, TimeZone dest) {
		for (int i = 0; i < content.size(); i++) {
			content.get(i).changeTimeZoneBase(src, dest);
		}
	}

	// Purpose: Change the DST system to non-DST system
	// mostly used to change the American DST to Chinese non-DST system
	public void fixDST() {
		for (CalContent calContent : content) {
			calContent.fixDST();
		}
	}

	// Purpose: Change the non-DST system to DST system
	public void setDST() {
		for (CalContent calContent : content) {
			calContent.setDST();
		}
	}

	// Purpose: Optimize the courses to combine the individual lectures into series
	// and add alarms
	public void optimizeCourses() {
		for (int i = 0; i < content.size(); i++) {
			if (content.get(i) instanceof Course)
				((Course) content.get(i)).optimize();
		}
	}

	// Purpose: Export the calendar in the ics format by string
	public String exportICSFormat() {
		// Calendar header
		StringBuilder temp = new StringBuilder(
				"BEGIN:VCALENDAR\nPRODID:-//CPS2231Project//SimpleICalEditor//EN\nVERSION:2.0\n");
		// Connect each event in the calendar
		for (CalContent calContent : content) {
			temp.append(calContent.exportICSFormat());
		}
		// Calendar end sign
		temp.append("END:VCALENDAR\n");
		return temp.toString();
	}

	// Purpose: Directly write the calendar to the output file
	public void exportToFile(PrintWriter output) {
		// Calendar header
		output.println("BEGIN:VCALENDAR\nPRODID:-//CPS2231Project//SimpleICalEditor//EN\nVERSION:2.0");
		// Connect each event in the calendar
		for (CalContent calContent : content) {
			calContent.exportToFile(output);
		}
		// Calendar end sign
		output.println("END:VCALENDAR");
	}

	// Purpose: Describe the calendar in the overview format
	// Used in calendar list
	public String overviewString() {
		// System.out.println(" Summary | Start Time | End Time | Number of Courses |
		// Number of Normal Events ");
		return String.format(" %23s | %s | %s | %21d | %24d ", getName(23),
				getDtStart() != null ? getDtStart().getTime().toString() : "            -               ",
				getDtEnd() != null ? getDtEnd().getTime().toString() : "            -               ",
				getCourseNumber(), getNormalEventNumber());
	}

	// Purpose: Count the number of courses in the calendar
	// Used in calendar overview string
	private int getCourseNumber() {
		int count = 0;
		for (int i = 0; i < getContent().size(); i++) {
			if (getContent().get(i) instanceof Course)
				count++;
		}
		return count;
	}

	// Purpose: Count the number of normal events in the calendar
	// Used in calendar overview string
	private int getNormalEventNumber() {
		int count = 0;
		for (int i = 0; i < getContent().size(); i++) {
			if (getContent().get(i) instanceof NormalEvent)
				count++;
		}
		return count;
	}

	// Purpose: Get the start time of the calendar
	// Used in calendar overview string
	private GregorianCalendar getDtStart() {
		GregorianCalendar dtStart = null;
		int i = 0;
		for (; i < content.size(); i++) {
			if (content.get(i).getDtStart() != null) {
				dtStart = content.get(i).getDtStart();
				break;
			}
		}
		for (; i < content.size(); i++) {
			GregorianCalendar temp = content.get(i).getDtStart();
			if (temp != null && temp.compareTo(dtStart) < 0)
				dtStart = temp;
		}
		return dtStart;
	}

	// Purpose: Get the end time of the calendar
	// Used in calendar overview string
	private GregorianCalendar getDtEnd() {
		GregorianCalendar dtEnd = null;
		int i = 0;
		for (; i < content.size(); i++) {
			if (content.get(i).getDtLast() != null) {
				dtEnd = content.get(i).getDtLast();
				break;
			}
		}
		for (; i < content.size(); i++) {
			GregorianCalendar temp = content.get(i).getDtLast();
			if (temp != null && temp.compareTo(dtEnd) > 0)
				dtEnd = temp;
		}
		return dtEnd;
	}

	@Override
	public Object clone() {
		try {
			ICalendar temp = (ICalendar) super.clone();
			temp.setContent(new ArrayList<CalContent>());
			for (int i = 0; i < getContent().size(); i++) {
				temp.add((CalContent) getContent().get(i).clone());
			}
			return temp;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	// Purpose: Generate the mergence of two calendars
	// Used in multiple calendar menu
	public static ICalendar mergenceOf(ICalendar cal1, ICalendar cal2) {
		ICalendar cal = (ICalendar) cal1.clone();
		cal.setName("Mergence: " + cal1.getName() + "+" + cal2.getName());
		cal.setPath(null);
		for (int i = 0; i < cal2.getContent().size(); i++) {
			cal.add((CalContent) (cal2.getContent().get(i).clone()));
		}
		return cal;
	}
}
