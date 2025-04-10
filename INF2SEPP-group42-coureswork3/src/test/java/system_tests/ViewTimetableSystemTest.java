package system_tests;

import controller.StudentController;
import external.MockAuthenticationService;
import external.MockEmailService;
import model.CourseManager;
import model.SharedContext;
import model.Timetable;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import view.TextUserInterface;
import view.View;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

public class ViewTimetableSystemTest extends TUITest {

    @Test
    @DisplayName("Test viewing an empty timetable")
    public void testViewEmptyTimetable() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "1",        // Select "View timetable"
                "5",        // Exit timetable management
                "-1"        // Exit
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        CourseManager courseManager = context.getCourseManager();

        loginAsStudent(context);
        StudentController studentController = new StudentController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        startOutputCapture();
        studentController.manageTimetable();

        // Verify output
        assertOutputContains("=== Timetable Management ===");
        assertOutputContains("1. View Timetable");
        assertOutputContains("No courses in your timetable. Please add courses first.");


    }

    @Test
    @DisplayName("Test viewing a timetable with unchosen activities")
    public void testViewTimetableWithUnchosenActivities() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "2",        // Select "Add course to timetable"
                "CS101",    // Enter course code
                "1",        // View timetable
                "5",        // Exit timetable management
                "-1"        // Exit
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        CourseManager courseManager = context.getCourseManager();

        // Add a course with an activity
        courseManager.addCourse(
                "admin1@university.edu", "CS101", "Intro to CS", "Basic CS course",
                true, "Prof A", "prof@university.edu", "Sec B", "sec@university.edu",
                0, 0
        );
        courseManager.addActivityToCourse(
                "CS101", LocalDate.parse("2025-09-01"), LocalTime.parse("09:00"),
                LocalDate.parse("2025-12-15"), LocalTime.parse("10:30"), "Room 101",
                DayOfWeek.MONDAY, "Lecture", true
        );
        courseManager.addActivityToCourse(
                "CS101", LocalDate.parse("2025-09-01"), LocalTime.parse("10:00"),
                LocalDate.parse("2025-12-15"), LocalTime.parse("11:00"), "Room 102",
                DayOfWeek.TUESDAY, "Tutorial", 10
        );

        loginAsStudent(context);
        StudentController studentController = new StudentController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        startOutputCapture();
        studentController.manageTimetable();

        // Verify output
        assertOutputContains("The course was successfully added to your timetable. Lectures are automatically selected. Please select your required tutorials and labs.");
        assertOutputContains("Week from 2025-04-07 to 2025-04-11");
        assertOutputContains("MONDAY (2025-04-07)");
        assertOutputContains("Course: CS101");
        assertOutputContains("Lecture (ID: 1) - 09:00-10:30 [CHOSEN]");
        assertOutputContains("TUESDAY (2025-04-08)");
        assertOutputContains("Course: CS101");
        assertOutputContains("Tutorial (ID: 2) - 10:00-11:00 [UNCHOSEN]");

        assertOutputContains("WEDNESDAY (2025-04-09): No scheduled activities");
        assertOutputContains("THURSDAY (2025-04-10): No scheduled activities");
        assertOutputContains("FRIDAY (2025-04-11): No scheduled activities");


        // Verify timetable state
        Timetable timetable = courseManager.getTimetable("student1@hindeburg.ac.uk");
        assertTrue(timetable.hasSlotsForCourse("CS101"), "CS101 should be in timetable");
        assertEquals(1, timetable.numChosenActivities("CS101"), "No activities should be chosen for CS101");

    }

    @Test
    @DisplayName("Test viewing a timetable with chosen activities")
    public void testViewTimetableWithChosenActivities() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "2",        // Select "Add course to timetable"
                "CS101",    // Enter course code
                "3",        // Select "Choose activities"
                "CS101",    // Enter course code
                "2",        // Choose activity
                "1",        // View timetable
                "5",        // Exit timetable management
                "-1"        // Exit
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        CourseManager courseManager = context.getCourseManager();

        // Add a course with activities
        courseManager.addCourse(
                "admin1@university.edu", "CS101", "Intro to CS", "Basic CS course",
                true, "Prof A", "prof@university.edu", "Sec B", "sec@university.edu",
                0, 0
        );
        courseManager.addActivityToCourse(
                "CS101", LocalDate.parse("2025-09-01"), LocalTime.parse("09:00"),
                LocalDate.parse("2025-12-15"), LocalTime.parse("10:30"), "Room 101",
                DayOfWeek.MONDAY, "Lecture", true
        );
        courseManager.addActivityToCourse(
                "CS101", LocalDate.parse("2025-09-01"), LocalTime.parse("10:00"),
                LocalDate.parse("2025-12-15"), LocalTime.parse("11:00"), "Room 102",
                DayOfWeek.TUESDAY, "Tutorial", 10
        );

        loginAsStudent(context);
        StudentController studentController = new StudentController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        startOutputCapture();
        studentController.manageTimetable();

        // Verify output
        assertOutputContains("The course was successfully added to your timetable. Lectures are automatically selected. Please select your required tutorials and labs.");

        assertOutputContains("Timetable for student1@hindeburg.ac.uk");
        assertOutputContains("Week from 2025-04-07 to 2025-04-11");
        assertOutputContains("MONDAY (2025-04-07)");
        assertOutputContains("Course: CS101");
        assertOutputContains("Lecture (ID: 1) - 09:00-10:30 [CHOSEN]");
        assertOutputContains("TUESDAY (2025-04-08)");
        assertOutputContains("Course: CS101");
        assertOutputContains("Tutorial (ID: 2) - 10:00-11:00 [CHOSEN]");
        assertOutputContains("WEDNESDAY (2025-04-09): No scheduled activities");
        assertOutputContains("THURSDAY (2025-04-10): No scheduled activities");
        assertOutputContains("FRIDAY (2025-04-11): No scheduled activities");

        // Verify timetable state
        Timetable timetable = courseManager.getTimetable("student1@hindeburg.ac.uk");
        assertTrue(timetable.hasSlotsForCourse("CS101"), "CS101 should be in timetable");
        assertEquals(2, timetable.numChosenActivities("CS101"), "One activity should be chosen for CS101");

    }

    @Test
    @DisplayName("Test viewing a timetable with multiple courses")
    public void testViewTimetableWithMultipleCourses() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "2",        // Select "Add course to timetable"
                "CS101",    // Enter first course code
                "2",        // Select "Add course to timetable"
                "CS102",    // Enter second course code
                "3",        // Select "Choose activities"
                "CS101",    // Enter course code
                "2",        // Choose tutorial (ID: 2) for CS101
                "1",        // View timetable
                "5",        // Exit timetable management
                "-1"        // Exit
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        CourseManager courseManager = context.getCourseManager();

        // Add first course (CS101) with a lecture and a tutorial
        courseManager.addCourse(
                "admin1@university.edu", "CS101", "Intro to CS", "Basic CS course",
                true, "Prof A", "prof@university.edu", "Sec B", "sec@university.edu",
                1, 0  // Requires 1 tutorial
        );
        courseManager.addActivityToCourse(
                "CS101", LocalDate.parse("2025-09-01"), LocalTime.parse("09:00"),
                LocalDate.parse("2025-12-15"), LocalTime.parse("10:30"), "Room 101",
                DayOfWeek.MONDAY, "Lecture", true
        );
        courseManager.addActivityToCourse(
                "CS101", LocalDate.parse("2025-09-01"), LocalTime.parse("11:00"),
                LocalDate.parse("2025-12-15"), LocalTime.parse("12:00"), "Room 102",
                DayOfWeek.TUESDAY, "Tutorial", 10
        );

        // Add second course (CS102) with a lecture and a lab
        courseManager.addCourse(
                "admin1@university.edu", "CS102", "Advanced CS", "Advanced CS course",
                true, "Prof B", "prof2@university.edu", "Sec C", "sec2@university.edu",
                0, 1  // Requires 1 lab
        );
        courseManager.addActivityToCourse(
                "CS102", LocalDate.parse("2025-09-01"), LocalTime.parse("14:00"),
                LocalDate.parse("2025-12-15"), LocalTime.parse("15:30"), "Room 103",
                DayOfWeek.TUESDAY, "Lecture", true
        );
        courseManager.addActivityToCourse(
                "CS102", LocalDate.parse("2025-09-01"), LocalTime.parse("09:00"),
                LocalDate.parse("2025-12-15"), LocalTime.parse("10:00"), "Room 104",
                DayOfWeek.WEDNESDAY, "Lab", 20
        );

        loginAsStudent(context);
        StudentController studentController = new StudentController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        startOutputCapture();
        studentController.manageTimetable();

        // Verify output
        assertOutputContains("The course was successfully added to your timetable. Lectures are automatically selected. Please select your required tutorials and labs.");  // CS101 added
        assertOutputContains("You have to choose 1 tutorials for this course");  // CS101 tutorial requirement
        assertOutputContains("The course was successfully added to your timetable. Lectures are automatically selected. Please select your required tutorials and labs.");  // CS102 added
        assertOutputContains("You have to choose 1 labs for this course");  // CS102 lab requirement
        assertOutputContains("Activity successfully chosen for course: CS101");  // Tutorial chosen
        assertOutputContains("Timetable for student1@hindeburg.ac.uk");
        assertOutputContains("Week from 2025-04-07 to 2025-04-11");
        assertOutputContains("MONDAY (2025-04-07):");
        assertOutputContains("Course: CS101");
        assertOutputContains("Lecture (ID: 1) - 09:00-10:30 [CHOSEN]");
        assertOutputContains("TUESDAY (2025-04-08):");
        assertOutputContains("Course: CS101");
        assertOutputContains("Tutorial (ID: 2) - 11:00-12:00 [CHOSEN]");
        assertOutputContains("Course: CS102");
        assertOutputContains("Lecture (ID: 3) - 14:00-15:30 [CHOSEN]");
        assertOutputContains("WEDNESDAY (2025-04-09):");
        assertOutputContains("Course: CS102");
        assertOutputContains("Lab (ID: 4) - 09:00-10:00 [UNCHOSEN]");
        assertOutputContains("THURSDAY (2025-04-10): No scheduled activities");
        assertOutputContains("FRIDAY (2025-04-11): No scheduled activities");
        assertOutputContains("=== Timetable Issues ===");
        assertOutputContains("WARNING: Course CS102 requires 1 labs, but only 0 chosen.");

        // Verify timetable state
        Timetable timetable = courseManager.getTimetable("student1@hindeburg.ac.uk");
        assertTrue(timetable.hasSlotsForCourse("CS101"), "CS101 should be in timetable");
        assertTrue(timetable.hasSlotsForCourse("CS102"), "CS102 should be in timetable");
        assertEquals(2, timetable.numChosenActivities("CS101"), "Two activities (Lecture and Tutorial) should be chosen for CS101");
        assertEquals(1, timetable.numChosenActivities("CS102"), "One activity (Lecture) should be chosen for CS102");
        assertEquals(1, timetable.countChosenActivitiesOfType("CS101", "Lecture", courseManager), "One lecture chosen for CS101");
        assertEquals(1, timetable.countChosenActivitiesOfType("CS101", "Tutorial", courseManager), "One tutorial chosen for CS101");
        assertEquals(1, timetable.countChosenActivitiesOfType("CS102", "Lecture", courseManager), "One lecture chosen for CS102");
        assertEquals(0, timetable.countChosenActivitiesOfType("CS102", "Lab", courseManager), "No labs chosen for CS102");
    }
}
