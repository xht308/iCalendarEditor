package iCalendarEditor;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.GregorianCalendar;

public class RepeatRule implements Cloneable { // Use as a variable type for repeat representing the repeat rule

	public static int DAILY = 0;
	public static int WEEKLY = 1;
	public static int MONTHLY = 2;
	public static int YEARLY = 3;
	private int frequency;
	private GregorianCalendar endDt;
	private int interval;
	private ArrayList<GregorianCalendar> exceptions;

	public RepeatRule() {
		this(new GregorianCalendar());
	}

	public RepeatRule(GregorianCalendar endDt) {
		this(1, endDt, 1);
	}

	public RepeatRule(int frequency, GregorianCalendar endDt, int interval) {
		this.frequency = frequency;
		this.endDt = endDt;
		this.interval = interval;
		this.exceptions = new ArrayList<>();
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public GregorianCalendar getEndDt() {
		return endDt;
	}

	public void setEndDt(GregorianCalendar endDt) {
		this.endDt = endDt;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public ArrayList<GregorianCalendar> getExceptions() {
		return exceptions;
	}

	public void setExceptions(ArrayList<GregorianCalendar> exceptions) {
		this.exceptions = exceptions;
	}

	// Purpose: Get the introduction of the rRule
	// used in event information
	public String getIntroducation() {
		return String.format("Frequency: %s\nUntil: %s\nInterval: %d", getFrequencyString(), endDt.getTime().toString(),
				interval);
	}

	// Add exception to the repeat rule
	public void addException(GregorianCalendar exception) {
		exceptions.add(exception);
	}

	// Purpose: Export the exceptions to ics format
	// When there are exceptions -> export the exceptions
	// No exception -> return empty string
	// used in export methods
	private String exportExceptions() {
		// Check whether exceptions exist
		if (!exceptions.isEmpty()) {
			// Exist -> export
			StringBuilder temp = new StringBuilder("EXDATE:");
			for (int i = 0; i < exceptions.size(); i++) {
				// Add time
				temp.append(Event.formatGregorianCalendarTime(exceptions.get(i)));
				// Check whether this is the last exception
				// No -> add ","
				if (i < exceptions.size() - 1)
					temp.append(",");
				// Yes -> change line
				else
					temp.append("\n");
			}
			return temp.toString();
		}
		// Not exist -> return empty string
		else
			return "";
	}

	// Purpose: Get the word expression of the frequency for export
	// when the frequency value is wrong it is set to UNKNOWN by default
	public String getFrequencyString() {
		switch (frequency) {
		case 0:
			return "DAILY";
		case 1:
			return "WEEKLY";
		case 2:
			return "MONTHLY";
		case 3:
			return "YEARLY";
		default:
			System.out.println("A error occured when generate the repeat rules. Wrong frequency value.");
			return "UNKNOWN";
		}
	}

	// Purpose: Export the repeat rule in the ics format by string
	// when the frequency value is wrong it is set to weekly by default
	// Output example: RRULE:FREQ=WEEKLY;UNTIL=20210628T160000Z;INTERVAL=1
	public String exportICSFormat() {
		return String.format("RRULE:FREQ=%s;UNTIL=%s;INTERVAL=%d\n%s", getFrequencyString(),
				Event.formatGregorianCalendarTime(getEndDt()), interval, exportExceptions());
	}

	// Purpose: Directly write the repeat rule to the output file
	// Output example: RRULE:FREQ=WEEKLY;UNTIL=20210628T160000Z;INTERVAL=1
	public void exportToFile(PrintWriter output) {
		output.print(exportICSFormat());
	}

	@Override
	public Object clone() {
		try {
			RepeatRule temp = (RepeatRule) super.clone();
			temp.setEndDt((GregorianCalendar) endDt.clone());
			temp.setExceptions(new ArrayList<GregorianCalendar>());
			for (int i = 0; i < getExceptions().size(); i++) {
				temp.getExceptions().add((GregorianCalendar) getExceptions().get(i).clone());
			}
			return temp;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}
