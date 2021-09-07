package iCalendarEditor;

import java.io.PrintWriter;

public class Alarm implements Cloneable {

	private int timeAhead;
	private String description;

	public Alarm() {
		this("A event is about to begin.");
	}

	public Alarm(int timeAhead) {
		this(timeAhead, "A event is about to begin");
	}

	public Alarm(String description) {
		this(30, description);
	}

	public Alarm(int timeAhead, String description) {
		this.timeAhead = timeAhead;
		this.description = description;
	}

	public int getTimeAhead() {
		return timeAhead;
	}

	public void setTimeAhead(int timeAhead) {
		this.timeAhead = timeAhead;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	// Purpose: Get the introduction of the alarm
	// used in event information
	public String getIntroduction() {
		return String.format("%d minutes before the start time with description saying \"%s\".", timeAhead,
				description);
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	// Purpose: Export the alarm in the ics format by string
	// Output example:
	// BEGIN:VALARM
	// ACTION:DISPLAY
	// TRIGGER;RELATED=START:-PT15M
	// DESCRIPTION:CPS2231W01COMPUTER ORGAN & PROGRAMMING@GEH-C504\n
	// END:VALARM
	public String exportICSFormat() {
		StringBuilder temp = new StringBuilder("BEGIN:VALARM\nACTION:DISPLAY\n"); // First and second line
		temp.append(String.format("TRIGGER;RELATED=START:-PT%dM\n", timeAhead)); // Third line
		temp.append(String.format("DESCRIPTION:%s\n", description)); // Fourth line
		temp.append("END:VALARM\n"); // Fifth line
		return temp.toString();
	}

	// Purpose: Directly write the alarm to the output file
	// Output example:
	// BEGIN:VALARM
	// ACTION:DISPLAY
	// TRIGGER;RELATED=START:-PT15M
	// DESCRIPTION:CPS*2231*W01 COMPUTER ORGAN &
	// END:VALARM
	public void exportToFile(PrintWriter output) {
		output.print(exportICSFormat());
	}

}
