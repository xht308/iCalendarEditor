# iCalendar Editor for WKU/KEAN Students

The iCalendar Editor for WKU/KEAN Students is a program that specially designed for WKU/KEAN students to edit the iCalendar file exported from the KEANWISE system and help them keeping on their class schedule.

Since the iCalendar file exported from KEANWISE is unusable for WKU students due to the difference in time zone and DST policy between Zhejiang and New Jersey, the iCalendar Editor contains a automatic fixing process to solve this problem.

Besides, the iCalendar Editor will also automatic optimize the calendar when the file is read including the above mentioned time zone adjustment and other operations like the auto-serialization of events (lectures), addition of alarm before classes, etc. These changes are made for the convenience of schedule editing both before and after importing the iCalendar file to calendar applications (e.g. Outlook).

## How to use the iCalendar Editor

### 1. Install the Java Runtime Environment

Download and install the JRE (Java Runtime Environment). Recommend version is JRE 8+.

### 2. Download from the releases

The releases tab is at the right side of the page. Please download the latest version of the program. 
Place the downloaded file at a directory that you can easily find.

### 3. Run the program in command line (terminal)

Open the terminal (cmd or PowerShell for Windows) on the computer the following command to run the program.

Notice: PATH_TO_YOUR_DOWNLOADED_JAR_FILE should be replaced by your own path to the downloaded iCalendarEditor.jar file

```
java -jre PATH_TO_YOUR_DOWNLOADED_JAR_FILE
```

