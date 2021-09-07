package iCalendarEditor;

import java.util.Scanner; //Import .ics file
import java.util.TimeZone; //Transformation of time zone base & display/output time zone
import java.io.File; //import, export
import java.io.FileNotFoundException;
import java.io.PrintWriter; // Export .ics file
import java.util.ArrayList; // QuickUsable interface
import java.util.GregorianCalendar; // Represent the start time and end time of the events

public class ICalEditor {

	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		welcomePrint();
		ArrayList<ICalendar> calList = new ArrayList<>();
		calList.add(getCalendar(input));
		boolean flag = true;
		while (flag) {
			flag = mainMenu(calList.get(0), input);
		}
		flag = true;
		while (flag) {
			flag = MultipleCalMenu(calList, input);
		}
		System.out.println("Program exited");
	}

	// Ask user to import a file or create a new calendar
	private static ICalendar getCalendar(Scanner input) {
		// Let user choose to start with a file or create a new calendar
		System.out.println(
				"Where do you want to start with (A. a .ics file exported from KEANWISE / B. an empty calendar)?");
		System.out.print("Enter A or B to choose: ");
		String str = input.next();
		if (str.length() != 1) {
			System.out.println("\nPlease enter the right character (A or B).\n");
			return getCalendar(input);
		}
		input.nextLine();
		char choice = Character.toUpperCase(str.charAt(0));
		if (choice == 'A') { // start with a file
			// Get file path
			System.out.print("Please enter the path of the .cal file: ");
			File path = new File(pathInputFix(input.nextLine()));
			// File availability check
			if (!path.exists()) {
				System.out.println("\nThe path is not available. Please check and enter a available path.\n");
				return getCalendar(input);
			} else if (!path.isFile()) {
				System.out.println("\nThe path does not point to a file. Please check and enter a available path.\n");
				return getCalendar(input);
			} else if (!path.getName().substring(path.getName().lastIndexOf('.')).equals(".ics")) {
				System.out.println("\nThe file is not a .ics file. Please check and enter a available path.\n");
				return getCalendar(input);
			} else {
				ICalendar iCal = ICalendar.readFromFile(path);
				if (!TimeZone.getDefault().useDaylightTime())
					iCal.fixDST();
				iCal.optimizeCourses();
				iCal.changeTimeZoneBase(TimeZone.getTimeZone("America/New_York"), TimeZone.getDefault());
				return iCal;
			}
		} else if (choice == 'B') { // Start with a empty calendar
			System.out.print("Enter the name of the calendar: ");
			return new ICalendar(input.nextLine());
		} else {
			System.out.println("\nPlease enter the right character (A or B).\n");
			return getCalendar(input);
		}
	}

	private static String pathInputFix(String str) {
		// Remove spaces
		str = str.trim();
		// Remove "
		while (str.startsWith("\"") && str.endsWith("\"")) {
			str = str.substring(1, str.length() - 1);
		}
		return str;
	}

	// Print the welcome screen
	private static void welcomePrint() {
		System.out.println(
				"                                                          ///Simple iCal Editor///                                                                ");
		System.out.println(
				"--------------------------------------------------------------------------------------------------------------------------------------------------");
		System.out.println(
				"                                                    Welcome to use the simple iCal editor                                                         ");
		System.out.println(
				"                       This program can read a .ics file exported from KEANWISE and make it suitable for WKU students to use.                     ");
		System.out.println(
				"                                          Start editing from a new empty calendar is also supported.                                              ");
		System.out.println(
				"--------------------------------------------------------------------------------------------------------------------------------------------------");
	}

	// Print the calendar overview
	// Display all content of the calendar
	// Print number of content at the front of each line
	// If a content is a course display its summary with a * behind
	// If a content is a normal event display its summary, dtStart, dtEnd, location
	// and a string indicating whether it has a alarm and repeat rule
	private static void calendarOverviewPrint(ICalendar iCal) {
		// Print header
		System.out.println(
				"--------------------------------------------------------------------------------------------------------------------------------------------------");
		System.out.println(
				"                                                          //Calendar Overview//                                                                   ");
		System.out.println("Calendar Name: " + iCal.getName());
		System.out.println(
				"Number | Is Course |       Summary       |          Start Time          |            End Time          |         Location        | Alarm | Repeat ");
		// Check whether there is content in the calendar
		if (iCal.getContent().isEmpty()) {
			// The calendar has no content -> print notification
			System.out.println(
					"                                                      (No Content in the calendar)                                                                ");
		} else {
			// Print introduction of each item in the calendar
			// Example output: # 0 | Y | CPS*2231*W01 COM... | - | - | - | - | -
			for (int i = 0; i < iCal.getContent().size(); i++) {
				System.out.printf("#%5d |%s\n", i, iCal.getContent().get(i).overviewString());
			}
		}

	}

	// Print the course overview
	// Display all lectures in the course
	// Print number of lectures at the front of each line
	// Display the lecture's summary, dtStart, dtEnd, location and a string
	// indicating whether it has a alarm and repeat rule
	private static void courseOverviewPrint(Course course) {
		// Print header
		System.out.println(
				"--------------------------------------------------------------------------------------------------------------------------------------------------");
		System.out.println(
				"                                                           //Course Overview//                                                                    ");
		System.out.println("Course Summary: " + course.getSummary());
		System.out.println(
				"Number |      Summary       |          Start Time          |            End Time          |               Location               | Alarm | Repeat ");
		// Check whether there is lecture in the course
		if (course.getLectures().isEmpty()) {
			// The course has no lecture -> print notification
			System.out.println(
					"                                                      (No lecture in this course)                                                                 ");
		} else {
			// Print introduction of each item in the calendar
			// Example output: # 0 | CPS*2231*W01 CO... | Mon Feb 22 08:30:00 CST 2021 | Mon
			// Feb 22 10:15:00 CST 2021 | Location: W, Building:GEH, Room:C504 | Y | WEEKLY
			for (int i = 0; i < course.getLectures().size(); i++) {
				System.out.printf("#%5d |%s\n", i, course.getLectures().get(i).overviewString());
			}
		}

	}

	// Purpose: Get user input operation character
	// work with menu
	private static char getOperation(char[] legalOperations, Scanner input) {
		// Get user input
		System.out.print("Please enter the corresponding character to conduct operation: ");
		String str = input.next();
		// Check input length
		if (str.length() != 1) {
			// More than one char -> enter again
			System.out.println("\nPlease enter one character at a time to choose operation.\n");
			return getOperation(legalOperations, input);
		} else {
			char ch = str.charAt(0);
			if (Character.isLetter(ch))
				ch = Character.toUpperCase(ch);
			// Check whether the input is legal
			for (char lo : legalOperations) {
				// Legal -> return input
				if (ch == lo)
					return ch;
			}
			// Illegal -> enter again
			System.out.println("\nPlease enter the right operation.\n");
			return getOperation(legalOperations, input);
		}
	}

	// Purpose: Get user input calendar content/lecture number
	// used when opening or deleting something
	private static int getNumInput(int min, int max, Scanner input) {
		try {
			// Get user input number
			System.out.print("Enter the number (enter -1 to back to menu): ");
			int num = input.nextInt(); // can throw exception when input is not integer
			// Check whether input number is in the range
			if (num < max && num >= min || num == -1)
				return num;
			else {
				// Out of range -> enter again
				System.out.println("\nPlease enter the right number (between " + min + " and " + (max - 1) + ").\n");
				return getNumInput(min, max, input);
			}
		} catch (Exception ex) {
			// Input is not integer -> enter again
			System.out.println("\nIllegal input. Please enter an integer.\n");
			input.next();
			return getNumInput(min, max, input);
		}
	}

	// Purpose: Get input time from user
	private static GregorianCalendar getTimeInput(GregorianCalendar min, Scanner input) {
		// Record whether the input time is the same as min
		boolean flag = true;
		// Year
		System.out.print("Year: ");
		int year = getNumInput(min.get(GregorianCalendar.YEAR), 3000, input);
		if (year == -1)
			return null;
		if (year != min.get(GregorianCalendar.YEAR))
			flag = false;
		// Month
		System.out.print("Month: ");
		int month = getNumInput((flag ? min.get(GregorianCalendar.MONTH) + 1 : 1), 13, input) - 1;
		if (month == -1)
			return null;
		if (flag && month != min.get(GregorianCalendar.MONTH))
			flag = false;
		// Date
		System.out.print("Date: ");
		boolean flagDate = true;
		int date = 1;
		while (flagDate) {
			date = getNumInput((flag ? min.get(GregorianCalendar.DATE) : 1), 32, input);
			if (date == -1)
				return null;
			if (date > new GregorianCalendar(year, month, 1).getActualMaximum(GregorianCalendar.DATE))
				System.out.println("Wrong date.");
			else
				flagDate = false;
		}
		if (flag && date != min.get(GregorianCalendar.DATE))
			flag = false;
		// Hour
		System.out.print("Hour: ");
		int hour = getNumInput((flag ? min.get(GregorianCalendar.HOUR_OF_DAY) : 0), 24, input);
		if (hour == -1)
			return null;
		if (flag && hour != min.get(GregorianCalendar.HOUR_OF_DAY))
			flag = false;
		// Minute
		System.out.print("Minute: ");
		int minute = getNumInput((flag ? min.get(GregorianCalendar.MINUTE) : 0), 61, input);
		if (minute == -1)
			return null;
		if (flag && minute != min.get(GregorianCalendar.MINUTE))
			flag = false;
		// Second
		System.out.print("Second: ");
		int second = getNumInput((flag ? min.get(GregorianCalendar.SECOND) : 0), 61, input);
		if (second == -1)
			return null;
		// Return time
		return new GregorianCalendar(year, month, date, hour, minute, second);
	}

	// Purpose: Read string from user
	private static String readString(Scanner input) {
		// Get user input name/summary
		System.out.println("Enter a string(enter $ to back to menu): ");
		input.nextLine();
		String str = input.nextLine();
		// Check whether input is empty
		if (str.isEmpty()) {
			// Empty -> enter again
			System.out.println("Please enter something as a string.");
			return readString(input);
		}
		// Not empty -> return str
		else
			return str;
	}

	// Purpose: Ask user essential information to create a lecture/normal event
	private static Event createEvent(Event event, Scanner input) {
		System.out.println("\nBasic information of the event will be collected in the following part.");
		System.out.println("You can open the event later to add more information about it.");
		// Summary
		String summary = "My Event";
		if (event.getSummary() == null) {
			System.out.print("\nSummary of the event: ");
			summary = readString(input);
			if (summary.equals("$"))
				return null;
		}
		// Start time
		System.out.print("Start time of the event: ");
		GregorianCalendar dtStart = getTimeInput(new GregorianCalendar(1970, 0, 1, 0, 0, 0), input);
		if (dtStart == null)
			return null;
		// End time
		System.out.print("End time of the event: ");
		GregorianCalendar dtEnd = getTimeInput(dtStart, input);
		if (dtEnd == null)
			return null;
		// Set
		if (event.getSummary() == null)
			event.setSummary(summary);
		event.setDtStart(dtStart);
		event.setDtEnd(dtEnd);
		return event;
	}

	// Purpose: Print calendar overview and display main menu
	// provided functions: open content, add item, delete content, rename, export,
	// exit
	private static boolean mainMenu(ICalendar iCal, Scanner input) {
		calendarOverviewPrint(iCal);
		printMainMenu();
		char[] legalOperations = { '1', '2', '3', '4', '5', '6', '7', 'C', 'P', '@', '$' };
		switch (getOperation(legalOperations, input)) {
		// Open a calendar content (course/normal event)
		case '1':
			System.out.println("\nPLease enter the number of the calendar content (course/normal event) to open.");
			// Get user input number to open
			int num1 = getNumInput(0, iCal.getContent().size(), input);
			// Back to menu
			if (num1 == -1)
				break;
			// The content is course -> course menu
			else if (iCal.getContent().get(num1) instanceof Course) {
				boolean flag = true;
				while (flag) {
					flag = courseMenu((Course) iCal.getContent().get(num1), input);
				}
			}
			// The content is normal event -> event menu
			else {
				boolean flag = true;
				while (flag) {
					flag = eventMenu((NormalEvent) iCal.getContent().get(num1), input);
				}
			}
			break;
		// Add new normal event
		case '2':
			System.out.println("\nFollow the following steps to create a normal event.");
			NormalEvent normalEvent = new NormalEvent();
			normalEvent = (NormalEvent) createEvent(normalEvent, input);
			if (normalEvent != null)
				iCal.add(normalEvent);
			break;
		// Add new course
		case '3':
			System.out.println("\nPLease enter the summary of the new course.");
			// Get user input string to name the course
			String str3 = readString(input);
			// Back to menu
			if (str3.equals("$"))
				break;
			// Create new course and add to calendar
			else
				iCal.add(new Course(str3));
			break;
		// Add new lecture
		case '4':
			System.out.println("\nFollow the following steps to create a lecture.");
			Lecture lecture = new Lecture();
			lecture = (Lecture) createEvent(lecture, input);
			if (lecture != null)
				iCal.add(lecture);
			break;
		// Delete content from calendar
		case '5':
			System.out.println("\nPLease enter the number of the calendar content (course/normal event) to delete.");
			// Get user input number to delete
			int num5 = getNumInput(0, iCal.getContent().size(), input);
			// Back to menu
			if (num5 == -1)
				break;
			// Remove content
			else
				iCal.getContent().remove(num5);
			break;
		// Rename the calendar
		case '6':
			System.out.println("\nPLease enter the new name of the calender.");
			// Get user input string to rename
			String str6 = readString(input);
			// Back to menu
			if (str6.equals("$"))
				break;
			// Rename calendar
			else
				iCal.setName(str6);
			break;
		// Export the calendar to file
		case '7':
			if (iCal.getPath() != null) {
				System.out.print("Do you want to cover the source file? (Y/N)");
				char choice = Character.toUpperCase(input.next().charAt(0));
				input.nextLine();
				if (choice == 'Y') {
					try (PrintWriter output = new PrintWriter(iCal.getPath())) {
						iCal.exportToFile(output);
					} catch (FileNotFoundException e) {
						System.out.println("An error occurred when exporting.");
						break;
					}
					System.out.println("Successfully exported calendar to " + iCal.getPath().getAbsolutePath() + " !");
					break;
				} else if (choice != 'N') {
					System.out.println("\nPlease enter the right character. Back to the main menu.\n");
					break;
				}
			} else
				input.nextLine();
			System.out.print("Please enter the export path: ");
			File path = new File(pathInputFix(input.nextLine()));
			// File availability check
			if (path.exists() && path.isFile()) {
				System.out.print("The file already exists. Are you sure to cover it? (Y/N)");
				char choice = Character.toUpperCase(input.next().charAt(0));
				if (choice == 'Y') {
					try (PrintWriter output = new PrintWriter(path)) {
						iCal.exportToFile(output);
					} catch (FileNotFoundException e) {
						System.out.println("An error occurred when exporting.");
						break;
					}
					System.out.println("Successfully exported calendar to " + path.getAbsolutePath() + " !");
					break;
				} else if (choice != 'N') {
					System.out.println("\nPlease enter the right character. Back to the main menu.\n");
					break;
				}
			} else if (!path.getName().contains(".")
					|| !path.getName().substring(path.getName().lastIndexOf('.')).equals(".ics")) {
				System.out.println(
						"\nThe target is not a .ics file. Please check and enter a available path. Back to the main menu.\n");
				break;
			} else if (path.isDirectory()) {
				System.out.println(
						"\nThe path points to a directory. Please check and enter a available path. Back to the main menu.\n");
				break;
			} else {
				try (PrintWriter output = new PrintWriter(path)) {
					iCal.exportToFile(output);
				} catch (FileNotFoundException e) {
					System.out.println("An error occurred when exporting.");
					break;
				}
				System.out.println("Successfully exported calendar to " + path.getAbsolutePath() + " !");
			}
			break;
		// Copy a content (multiple-calendar)
		case 'C':
			System.out.println("\nPLease enter the number of the calendar content (course/normal event) to copy.");
			// Get user input number to copy
			int numC = getNumInput(0, iCal.getContent().size(), input);
			// Back to menu
			if (numC == -1)
				break;
			// Copy
			else
				iCal.getContent().get(numC).copy();
			break;
		// Paste (multiple-calendar)
		case 'P':
			if (CalContent.CLIPBOARD[0] == null) {
				// Check whether there is content in clipboard
				// No content -> notice the user
				System.out.println("There is no content in clipboard.");
				break;
			}
			// Paste the content in clipboard
			iCal.add((CalContent) CalContent.CLIPBOARD[0].clone());
			System.out.println("Pasting completed.");
			break;
		// Go to multiple-calendar menu
		case '@':
			return false;
		// Exit
		case '$':
			System.out.println("Program exited");
			System.exit(0);
		}
		// Stay in main menu
		return true;
	}

	// Purpose: Print main menu
	private static void printMainMenu() {
		System.out.println(
				"--------------------------------------------------------------------------------------------------------------------------------------------------");
		System.out.println(
				"                                                              //Main Menu//                                                                       ");
		System.out.println("1. Open a calendar content (course/normal event)");
		System.out.println("2. Add new normal event");
		System.out.println("3. Add new course");
		System.out.println("4. Add new lecture");
		System.out.println("5. Remove content from calendar");
		System.out.println("6. Rename the calendar");
		System.out.println("7. Export the calendar to file");
		System.out.println("C. Copy a content (multiple-calendar)");
		System.out.println("P: Paste (multiple-calendar)");
		System.out.println("@. Go to multiple-calendar menu");
		System.out.println("$. Exit");
	}

	// Purpose: Print course overview and display course menu
	// provided functions: open lecture, add lecture, delete lecture, reset summary,
	// back
	private static boolean courseMenu(Course course, Scanner input) {
		courseOverviewPrint(course);
		printCourseMenu();
		char[] legalOperations = { '1', '2', '3', '4', '$' };
		switch (getOperation(legalOperations, input)) {
		// Open a lecture
		case '1':
			System.out.println("\nPLease enter the number of the lecture to open.");
			// Get user input number to open
			int num1 = getNumInput(0, course.getLectures().size(), input);
			// Back to menu
			if (num1 == -1)
				break;
			// Open the lecture -> event menu
			else {
				boolean flag = true;
				while (flag) {
					flag = eventMenu((Lecture) course.getLectures().get(num1), input);
				}
			}
			break;
		// Add new lecture
		case '2':
			System.out.println("\nFollow the following steps to create a lecture.");
			Lecture lecture = new Lecture(course.getSummary());
			lecture = (Lecture) createEvent(lecture, input);
			if (lecture != null) {
				course.add(lecture);
				course.optimize();
			}
			break;
		// Delete lecture from course
		case '3':
			System.out.println("\nPLease enter the number of the lecture to delete.");
			// Get user input number to delete
			int num3 = getNumInput(0, course.getLectures().size(), input);
			// Back to menu
			if (num3 == -1)
				break;
			// Remove lecture
			else
				course.getLectures().remove(num3);
			break;
		// Reset summary of the course
		case '4':
			System.out.println("\nPLease enter the new summary of the course.");
			// Get user input string to rename
			String str = readString(input);
			// Back to menu
			if (str.equals("$"))
				break;
			// Reset summary
			else
				course.setSummary(str);
			break;
		// Back to the previous level
		case '$':
			return false;
		}
		// Stay in course menu
		return true;
	}

	// Purpose: Print course menu
	private static void printCourseMenu() {
		System.out.println(
				"--------------------------------------------------------------------------------------------------------------------------------------------------");
		System.out.println(
				"                                                             //Course Menu//                                                                      ");
		System.out.println("1. Open a lecture");
		System.out.println("2. Add new lecture");
		System.out.println("3. Remove lecture from course");
		System.out.println("4. Reset summary of the course");
		System.out.println("$. Back to main menu");
	}

	// Purpose: Print event (lecture/normal) information and display event menu
	// provided functions: open content, add item, delete content, rename, export,
	// exit
	private static boolean eventMenu(Event event, Scanner input) {
		eventInformationPrint(event);
		printEventMenu();
		char[] legalOperations = { '1', '2', '3', '4', '5', '6', '7', '8', '9', '$' };
		switch (getOperation(legalOperations, input)) {
		// Reset summary
		case '1':
			System.out.println("\nPLease enter the new summary of the event.");
			// Get user input string to rename
			String str = readString(input);
			// Back to menu
			if (str.equals("$"))
				break;
			// Reset summary
			else
				event.setSummary(str);
			break;
		// Change start time
		case '2':
			System.out.println("\nPlease enter the new start time below.");
			GregorianCalendar temp2 = getTimeInput(new GregorianCalendar(1970, 0, 1, 0, 0, 0), input);
			if (temp2 != null) {
				event.setDtStart(temp2);
				// Check the end time
				if (event.getDtStart().compareTo(event.getDtEnd()) > 0)
					event.setDtEnd((GregorianCalendar) event.getDtStart().clone());
			}
			break;
		// Change end time
		case '3':
			System.out.println("\nPlease enter the new end time below.");
			GregorianCalendar temp3 = getTimeInput(event.getDtStart(), input);
			if (temp3 != null)
				event.setDtEnd(temp3);
			break;
		// Add/Edit location
		case '4':
			if (event instanceof Lecture) {
				// Is lecture
				System.out.println("\nEnter the location, building, room of the lecture below.\n");
				// Location
				System.out.print("Location: ");
				String location = readString(input);
				if (location == null)
					return true;
				// Building
				System.out.print("Building: ");
				String building = readString(input);
				if (building == null)
					return true;
				// Room
				System.out.print("Room: ");
				String room = readString(input);
				if (room == null)
					return true;
				// Set
				((Lecture) event).setInnerLocation(location);
				((Lecture) event).setBuilding(building);
				((Lecture) event).setRoom(room);
			} else {
				System.out.println("\nEnter the location of the normal event below.\n");
				// Location
				System.out.print("Location: ");
				String location = readString(input);
				if (location == null)
					return true;
				((NormalEvent) event).setLocation(location);
			}
			break;
		// Add/Edit repeat rule
		case '5':
			System.out.println("\nThe following part will add/edit the repeat of the event.\n");
			System.out.println("Choice frequency:");
			// Frequency
			System.out.println("0. Daily 1. Weekly 2. Monthly 3. Yearly");
			int frequency = getNumInput(0, 4, input);
			if (frequency == -1)
				return true;
			// Time ahend
			System.out.println("\nEnter end time of repeat below");
			GregorianCalendar temp5 = getTimeInput(event.getDtEnd(), input);
			if (temp5 == null)
				return true;
			// Interval
			System.out.print("Enter interval: ");
			int interval = getNumInput(1, 100, input);
			if (interval == -1)
				return true;
			// Set
			event.setRRulr(new RepeatRule(frequency, temp5, interval));
			break;
		// Remove repeat rule
		case '6':
			event.setRRulr(null);
			System.out.println("Repeat rule removed.");
			break;
		// Add/Edit alarm
		case '7':
			System.out.println("\nThe following part will add/edit the repeat of the event.\n");
			// Time ahead
			System.out.print("Time ahead the start: ");
			int timeAhead = getNumInput(0, 60 * 8, input);
			if (timeAhead == -1)
				return true;
			// Description
			System.out.print("\nDescription: ");
			String description = readString(input);
			if (description == null)
				return true;
			// Set
			event.setAlarm(new Alarm(timeAhead, description));
			break;
		// Remove alarm
		case '8':
			event.setAlarm(null);
			System.out.println("Alarm removed.");
			break;
		// Add/Edit description
		case '9':
			System.out.println("\nThe following part will add/edit the description of the event.\n");
			// Description
			System.out.print("Description: ");
			String description7 = readString(input);
			if (description7 == null)
				return true;
			// Set
			event.setDescription(description7);
			break;
		// Back to the previous level
		case '$':
			return false;
		}
		// Stay in course menu
		return true;
	}

	// Print the event information
	// Display the summary, type (lecture/normal event), dtStart, dtEnd, location,
	// repeat rule, alarm, description of the event
	private static void eventInformationPrint(Event event) {
		// Print header
		System.out.println(
				"--------------------------------------------------------------------------------------------------------------------------------------------------");
		System.out.println(
				"                                                          //Event Information//                                                                   ");
		System.out.println("Event Type: " + (event instanceof Lecture ? "lecture" : "normal event"));
		System.out.println("Summary: " + event.getSummary());
		System.out.println("Start Time: " + event.getDtStart().getTime().toString());
		System.out.println("End Time: " + event.getDtEnd().getTime().toString());
		if (event instanceof Lecture)
			System.out.println(event.getLocation());
		else
			System.out.println("Location: " + event.getLocation());
		System.out.println("Repeat Rule: "
				+ (event.getRRule() != null ? setHanging(13, event.getRRule().getIntroducation()) : "not set yet"));
		System.out.println("Alarm: " + (event.getAlarm() != null ? event.getAlarm().getIntroduction() : "not set yet"));
		System.out.println("Description: "
				+ (event.getDescription() != null ? setHanging(13, event.getDescription()) : "not set yet"));
	}

	// Purpose: set hanging to a string
	// used in information printing
	private static String setHanging(int hanging, String str) {
		StringBuilder temp = new StringBuilder(str);
		int index = temp.indexOf("\n");
		while (index != -1) {
			for (int i = 0; i < hanging; i++) {
				temp.insert(index + 1, ' ');
			}
			index = temp.indexOf("\n", index + 1);
		}
		return temp.toString();
	}

	// Purpose: Print course menu
	private static void printEventMenu() {
		System.out.println(
				"--------------------------------------------------------------------------------------------------------------------------------------------------");
		System.out.println(
				"                                                              //Event Menu//                                                                      ");
		System.out.println("1. Reset summary");
		System.out.println("2. Change start time");
		System.out.println("3. Change end time");
		System.out.println("4. Add/Edit location");
		System.out.println("5. Add/Edit repeat rule");
		System.out.println("6. Remove repeat rule");
		System.out.println("7. Add/Edit alarm");
		System.out.println("8. Remove alarm");
		System.out.println("9. Add/Edit description");
		System.out.println("$. Back to previous menu");
	}

	// Purpose: Generate a temp file in the program path
	// used to temporarily store the exported calendar
	public static File generateTempFile() {
		File tempFile;
		do {
			StringBuilder name = new StringBuilder("Temp"); // Starting with Temp
			for (int i = 0; i < 6; i++) { // 6 random numbers
				name.append((int) (Math.random() * 10));
			}
			name.append(".ics"); // Ending with .ics
			tempFile = new File(name.toString());
		} while (tempFile.exists());
		return tempFile;
	}

	private static boolean MultipleCalMenu(ArrayList<ICalendar> calList, Scanner input) {
		calListPrint(calList);
		printMultiCalendarMenu();
		char[] legalOperations = { '1', '2', '3', '4', '$' };
		switch (getOperation(legalOperations, input)) {
		// Open a calendar
		case '1':
			System.out.println("\nPLease enter the number of the calendar to open.");
			// Get user input number to open
			int num1 = getNumInput(0, calList.size(), input);
			// Back to menu
			if (num1 == -1)
				break;
			// Open the calendar -> calendar menu
			else {
				boolean flag = true;
				while (flag) {
					flag = mainMenu(calList.get(num1), input);
				}
			}
			break;
		// Add new calendar
		case '2':
			calList.add(getCalendar(input));
			break;
		// Remove calendar from the list
		case '3':
			System.out.println("\nPLease enter the number of the calendar to delete.");
			// Get user input number to delete
			int num3 = getNumInput(0, calList.size(), input);
			// Back to menu
			if (num3 == -1)
				break;
			// Remove calendar
			else
				calList.remove(num3);
			break;
		// Merge two calendars
		case '4':
			if (calList.size() < 2) {
				System.out.println("\nYou must have at least two calendars to conduct this operation.");
				break;
			}
			System.out.println("\nPLease enter the number of one of the calendar to merge");
			// Get user input number to merge
			int num41 = getNumInput(0, calList.size(), input);
			// Back to menu
			if (num41 == -1)
				break;
			int num42 = -1;
			while (true) {
				System.out.println("\nPLease enter the number of the other calendar to merge");
				// Get user input number to merge
				num42 = getNumInput(0, calList.size(), input);
				if (num42 != num41)
					break;
				else
					System.out.println("You must use two different calendar to merge. Please check you enter.");
			}
			// Back to menu
			if (num42 == -1)
				break;
			// Merge the two calendars
			// Add the product to calendar list
			calList.add(ICalendar.mergenceOf(calList.get(num41), calList.get(num42)));
			break;
		// Exit
		case '$':
			return false;
		}
		// Stay in main menu
		return true;
	}

	private static void calListPrint(ArrayList<ICalendar> calList) {
		System.out.println(
				"--------------------------------------------------------------------------------------------------------------------------------------------------");
		System.out.println(
				"                                                            //Calendar List//                                                                     ");
		System.out.println(
				"Number |         Summary         |          Start Time          |            End Time          |   Number of Courses   |  Number of Normal Events ");
		// Check whether there is calendar
		if (calList.isEmpty()) {
			// The calendar list has no calendar -> print notification
			System.out.println(
					"                                                              (No calendar)                                                                       ");
		} else {
			// Print introduction of each calendar
			// Example output:
			for (int i = 0; i < calList.size(); i++) {
				System.out.printf("#%5d |%s\n", i, calList.get(i).overviewString());
			}
		}
	}

	// Purpose: Print Multi-Calendar menu
	private static void printMultiCalendarMenu() {
		System.out.println(
				"--------------------------------------------------------------------------------------------------------------------------------------------------");
		System.out.println(
				"                                                       //Multiple-Calendar Menu//                                                                 ");
		System.out.println("1. Open a calendar");
		System.out.println("2. Add new calendar");
		System.out.println("3. Remove calendar from the list");
		System.out.println("4. Merge two calendars");
		System.out.println("$. Exit");
	}

}
