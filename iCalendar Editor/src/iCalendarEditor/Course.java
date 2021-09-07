package iCalendarEditor;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class Course implements CalContent {

	private String summary;
	private ArrayList<Lecture> lectures;

	Course() {
		this("Unnamed course");
	}

	Course(String summary) {
		this(summary, new ArrayList<Lecture>());
	}

	Course(ArrayList<Lecture> lectures) {
		this("Unsummaryd course", lectures);
	}

	Course(Lecture lecture) {
		this(lecture.getSummary(), lecture);
	}

	Course(String summary, Lecture lecture) {
		this(summary);
		this.lectures.add(lecture);
	}

	Course(String summary, ArrayList<Lecture> lectures) {
		this.summary = summary;
		this.lectures = lectures;
	}

	public String getSummary() {
		return summary;
	}

	// Return summary of the course in certain length
	public String getSummary(int length) {
		if (summary.length() <= length)
			return summary;
		else
			return summary.substring(0, length - 3).concat("...");
	}

	public void setSummary(String summary) {
		// Reset summary for the course
		this.summary = summary;
		// Reset summary for the lectures in the course
		for (Lecture lecture : lectures) {
			lecture.setSummary(summary);
		}
	}

	public ArrayList<Lecture> getLectures() {
		return lectures;
	}

	public void setLectures(ArrayList<Lecture> lectures) {
		this.lectures = lectures;
	}

	// Purpose: Get the earliest start time of the course
	// Used in calendar list overview
	public GregorianCalendar getDtStart() {
		if (lectures.isEmpty())
			return null;
		GregorianCalendar temp = lectures.get(0).getDtStart();
		for (int i = 1; i < lectures.size(); i++) {
			if (lectures.get(i).getDtStart().compareTo(temp) < 0)
				temp = lectures.get(i).getDtStart();
		}
		return temp;
	}

	// Purpose: Get the latest end time of the course
	// Used in calendar list overview
	public GregorianCalendar getDtLast() {
		if (lectures.isEmpty())
			return null;
		GregorianCalendar temp = lectures.get(0).getDtLast();
		for (int i = 1; i < lectures.size(); i++) {
			if (lectures.get(i).getDtEnd().compareTo(temp) > 0)
				temp = lectures.get(i).getDtLast();
		}
		return temp;
	}

	public boolean isEmpty() {
		return lectures.isEmpty();
	}

	public void add(Lecture lecture) {
		lectures.add(lecture);
	}

	// Purpose: Change the time zone the event is based on
	// mostly used to change the American east time zone (-5) to Chinese time zone
	// (+8)
	public void changeTimeZoneBase(TimeZone src, TimeZone dest) {
		for (int i = 0; i < lectures.size(); i++) {
			lectures.get(i).changeTimeZoneBase(src, dest);
		}
	}

	// Purpose: Change the DST system to non-DST system
	// mostly used to change the American DST to Chinese non-DST system
	public void fixDST() {
		for (Lecture lecture : lectures) {
			lecture.fixDST();
		}
	}

	// Purpose: Change the non-DST system to DST system
	public void setDST() {
		for (Lecture lecture : lectures) {
			lecture.fixDST();
		}
	}

	// Purpose: Optimize the course to combine the individual lectures into series
	// and add alarms
	public void optimize() {
		// Sort the lectures of the course making them in time order
		java.util.Collections.sort(lectures);
		// Traverse all lectures make them base of a series of lectures or extend a
		// series of lectures
		for (int i = 0; i < lectures.size(); i++) {
			// Check whether this lecture is a lecture series
			if (lectures.get(i).getRRule() == null) {
				// Check whether this lecture can extend any of the existing series of lectures
				boolean flag = true;
				for (int j = 0; j < i; j++) {
					// Can extend -> extend
					if (lectureExtend(lectures.get(j), lectures.get(i))) {
						i--;
						flag = false;
						break;
					}
				}
				// Cannot extend -> make it base of a series of lectures -> add repeat rule and
				// alarm
				if (flag) {
					lectures.get(i).setRRulr(new RepeatRule((GregorianCalendar) lectures.get(i).getDtEnd().clone()));
					lectures.get(i).setAlarm(new Alarm(30, lectures.get(i).getSummary()));
				}
			}
		}
	}

	private boolean lectureExtend(Lecture series, Lecture lecture) {
		// Check the basic information of the lecture
		if (series.basiclyEqualTo(lecture)) {
			GregorianCalendar temp = (GregorianCalendar) series.getRRule().getEndDt().clone();
			temp.add(GregorianCalendar.DATE, 7);
			// Extend the lecture series
			series.getRRule().setEndDt((GregorianCalendar) lecture.getDtEnd().clone());
			series.combineDescription(lecture);
			lectures.remove(lecture);
			// Add exceptions (for vacations)
			while (temp.compareTo(series.getRRule().getEndDt()) < 0) {
				series.getRRule().addException((GregorianCalendar) temp.clone());
				temp.add(GregorianCalendar.DATE, 7);
			}
			return true;
		} else
			return false;
	}

	// Purpose: Export the course in the ics format by string
	public String exportICSFormat() {
		StringBuilder temp = new StringBuilder();
		// Connect each lecture in the course
		for (Lecture lecture : lectures) {
			temp.append(lecture.exportICSFormat());
		}
		return temp.toString();
	}

	// Purpose: Directly write the event to the output file
	public void exportToFile(PrintWriter output) {
		// Print each lecture in the course
		for (Lecture lecture : lectures) {
			lecture.exportToFile(output);
		}
	}

	// Purpose: Describe the course in the overview format
	// Used in calendar overview
	public String overviewString() {
		// System.out.println(" Is Course | Summary | Start Time | End Time | Location |
		// Alarm | Repeat ");
		return String.format(
				"     Y     | %19s |             -                |               -              |            -            |   -   |   -   ",
				getSummary(19));
	}

	public Object clone() {
		try {
			Course temp = (Course) super.clone();
			temp.setLectures(new ArrayList<Lecture>());
			for (int i = 0; i < getLectures().size(); i++) {
				temp.getLectures().add((Lecture) (getLectures().get(i).clone()));
			}
			return temp;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}
