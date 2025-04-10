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

public class ChooseActivityForCourseSystemTest extends TUITest {

    @Test
    @DisplayName("Test choosing a valid activity for a recorded course")
    public void testChooseValidActivityForRecordedCourse() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "2",        // Select "Add course to timetable"
                "CS101",    // Enter course code
                "3",        // Select "Choose activities"
                "CS101",    // Enter course code
                "2",        // Choose activity ID 1 (lecture)
                "1",        // View timetable
                "5",        // Exit timetable management
                "-1"        // Exit
        );

        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        CourseManager courseManager = context.getCourseManager();

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

        assertOutputContains("The course was successfully added to your timetable. Lectures are automatically selected. Please select your required tutorials and labs.");
        assertOutputContains("Activity successfully chosen for course: CS101");
        assertOutputContains("MONDAY (2025-04-07):");
        assertOutputContains("Course: CS101");
        assertOutputContains("Lecture (ID: 1) - 09:00-10:30 [CHOSEN]");
        assertOutputContains("TUESDAY (2025-04-08)");
        assertOutputContains("Course: CS101");
        assertOutputContains("Tutorial (ID: 2) - 10:00-11:00 [CHOSEN]");


        Timetable timetable = courseManager.getTimetable("student1@hindeburg.ac.uk");
        assertTrue(timetable.hasSlotsForCourse("CS101"), "CS101 should be in timetable");
        assertEquals(2, timetable.numChosenActivities("CS101"), "One activity should be chosen for CS101");
        // Add explicit checks for activity types
        assertEquals(1, timetable.countChosenActivitiesOfType("CS101", "Lecture", courseManager), "One lecture should be chosen");
        assertEquals(1, timetable.countChosenActivitiesOfType("CS101", "Tutorial", courseManager), "No tutorials chosen yet");
    }

    @Test
    @DisplayName("Test choosing a valid activity for an unrecorded lecture course with conflict")
    public void testChooseValidActivityForUnrecordedLectureCourse() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "2",        // Select "Add course to timetable"
                "CS101",    // Enter course code
                "3",        // Select "Choose activities"
                "CS101",    // Enter course code
                "2",        // Choose activity ID 1 (lecture)
                "1",        // View timetable
                "5",        // Exit timetable management
                "-1"        // Exit
        );

        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        CourseManager courseManager = context.getCourseManager();

        courseManager.addCourse(
                "admin1@university.edu", "CS101", "Intro to CS", "Basic CS course",
                true, "Prof A", "prof@university.edu", "Sec B", "sec@university.edu",
                1, 0  // Requires 1 tutorial
        );
        courseManager.addActivityToCourse(
                "CS101", LocalDate.parse("2025-09-01"), LocalTime.parse("09:00"),
                LocalDate.parse("2025-12-15"), LocalTime.parse("10:30"), "Room 101",
                DayOfWeek.MONDAY, "Lecture", false
        );
        courseManager.addActivityToCourse(
                "CS101", LocalDate.parse("2025-09-01"), LocalTime.parse("09:30"),
                LocalDate.parse("2025-12-15"), LocalTime.parse("11:00"), "Room 102",
                DayOfWeek.MONDAY, "Tutorial", 10
        );

        loginAsStudent(context);
        StudentController studentController = new StudentController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        startOutputCapture();
        studentController.manageTimetable();

        assertOutputContains("The course was successfully added to your timetable. Lectures are automatically selected. Please select your required tutorials and labs.");
        assertOutputContains("This activity conflicts with unrecorded lectures that require attendance:");
        assertOutputContains("- CS101 - Unrecorded Lecture - 09:00-10:30 (Activity ID: 1)");
        assertOutputContains("You cannot choose activities that conflict with unrecorded lectures.");
        assertOutputContains("MONDAY (2025-04-07):");
        assertOutputContains("Course: CS101");
        assertOutputContains("Lecture (ID: 1) - 09:00-10:30 [CHOSEN]");
        assertOutputContains("Tutorial (ID: 2) - 09:30-11:00 [UNCHOSEN]");


        Timetable timetable = courseManager.getTimetable("student1@hindeburg.ac.uk");
        assertTrue(timetable.hasSlotsForCourse("CS101"), "CS101 should be in timetable");
        assertEquals(1, timetable.numChosenActivities("CS101"), "One activity should be chosen for CS101");
        // Add explicit checks for activity types
        assertEquals(1, timetable.countChosenActivitiesOfType("CS101", "Lecture", courseManager), "One lecture should be chosen");
        assertEquals(0, timetable.countChosenActivitiesOfType("CS101", "Tutorial", courseManager), "No tutorials chosen yet");
    }


    @Test
    @DisplayName("Test choosing an activity for a non-existent course code")
    public void testChooseActivityForNonExistentCourseCode() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "2",        // Select "Add course to timetable"
                "CS101",    // Enter course code
                "3",        // Select "Choose activities"
                "CS999",    // Enter a non-existent course code
                "1",        // Choose activity ID 1 (irrelevant since course doesn't exist)
                "1",        // View timetable
                "5",        // Exit timetable management
                "-1"        // Exit
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        CourseManager courseManager = context.getCourseManager();

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
        loginAsStudent(context);
        StudentController studentController = new StudentController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        startOutputCapture();
        studentController.manageTimetable();

        // Verify output
        assertOutputContains("Course does not exist.");

        // Verify timetable state
        Timetable timetable = courseManager.getTimetable("student1@hindeburg.ac.uk");
        assertFalse(timetable.hasSlotsForCourse("CS999"), "CS999 should not be in timetable");
        assertEquals(0, timetable.numChosenActivities("CS999"), "No activities should be chosen for non-existent course");
    }


    @Test
    @DisplayName("Test choosing an activity for a recorded lecture course with conflict")
    public void testChooseActivityForRecordedLectureCourseWithConflict() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "2",        // Select "Add course to timetable"
                "CS101",    // Enter course code
                "3",        // Select "Choose activities"
                "CS101",    // Enter course code
                "2",
                "2",        // Select "Add course to timetable"
                "CS102",    // Enter course code
                "3",        // Select "Choose activities"
                "CS102",    // Enter course code
                "4",        // Choose activity ID 3 (conflicting lecture)
                "1",        // View timetable
                "5",        // Exit timetable management
                "-1"        // Exit
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        CourseManager courseManager = context.getCourseManager();

        // Add first course with a lecture
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
                "CS101", LocalDate.parse("2025-09-01"), LocalTime.parse("17:00"),
                LocalDate.parse("2025-12-15"), LocalTime.parse("18:30"), "Room 101",
                DayOfWeek.MONDAY, "Tutorial", 15
        );


        // Add second course with a conflicting lecture
        courseManager.addCourse(
                "admin1@university.edu", "CS102", "Advanced CS", "Advanced CS course",
                true, "Prof B", "prof2@university.edu", "Sec C", "sec2@university.edu",
                0, 0
        );
        courseManager.addActivityToCourse(
                "CS102", LocalDate.parse("2025-09-01"), LocalTime.parse("14:00"),
                LocalDate.parse("2025-12-15"), LocalTime.parse("15:30"), "Room 102",
                DayOfWeek.MONDAY, "Lecture", true
        );

        courseManager.addActivityToCourse(
                "CS102", LocalDate.parse("2025-09-01"), LocalTime.parse("17:00"),
                LocalDate.parse("2025-12-15"), LocalTime.parse("18:00"), "Room 101",
                DayOfWeek.MONDAY, "Tutorial", 15
        );



        loginAsStudent(context);
        StudentController studentController = new StudentController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        startOutputCapture();
        studentController.manageTimetable();

        // Verify output
        assertOutputContains("The course was successfully added to your timetable. Lectures are automatically selected. Please select your required tutorials and labs.");
        assertOutputContains("Activity successfully chosen for course: CS101");
        assertOutputContains("This activity conflicts with existing activities in your timetable:");
        assertOutputContains("CS101 - Tutorial - 17:00-18:30 (Activity ID: 2)");
        assertOutputContains("Warning: Recorded lecture has potential conflict: Time slot conflicts with existing slots: CS101 - Recorded Lecture - 09:00-10:30 (Activity ID: 1)");
        assertOutputContains("MONDAY (2025-04-07)");
        assertOutputContains("Course: CS101");
        assertOutputContains("Lecture (ID: 1) - 09:00-10:30 [CHOSEN]");
        assertOutputContains("Tutorial (ID: 2) - 17:00-18:30 [CHOSEN]");
        assertOutputContains("Course: CS102");
        assertOutputContains("Lecture (ID: 3) - 14:00-15:30 [CHOSEN]");
        assertOutputContains("Tutorial (ID: 4) - 17:00-18:00 [UNCHOSEN]");




        // Verify timetable state
        Timetable timetable = courseManager.getTimetable("student1@hindeburg.ac.uk");
        assertTrue(timetable.hasSlotsForCourse("CS101"), "CS101 should be in timetable");
        assertTrue(timetable.hasSlotsForCourse("CS102"), "CS102 should be in timetable");
        assertEquals(2, timetable.numChosenActivities("CS101"), "One activity should be chosen for CS101");
        assertEquals(1, timetable.numChosenActivities("CS102"), "No activities should be chosen for CS102 due to conflict");
    }

    @Test
    @DisplayName("Test choosing an activity for an unrecorded lecture course with conflict")
    public void testChooseActivityForUnRecordedLectureCourseWithConflict() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "2",        // Select "Add course to timetable"
                "CS101",    // Enter course code
                "3",        // Select "Choose activities"
                "CS101",    // Enter course code
                "2",
                "2",        // Select "Add course to timetable"
                "CS102",    // Enter course code
                "3",        // Select "Choose activities"
                "CS102",    // Enter course code
                "4",        // Choose activity ID 3 (conflicting lecture)
                "1",        // View timetable
                "5",        // Exit timetable management
                "-1"        // Exit
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        CourseManager courseManager = context.getCourseManager();

        // Add first course with a lecture
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
                "CS101", LocalDate.parse("2025-09-01"), LocalTime.parse("17:00"),
                LocalDate.parse("2025-12-15"), LocalTime.parse("18:30"), "Room 101",
                DayOfWeek.MONDAY, "Tutorial", 15
        );


        // Add second course with a conflicting lecture
        courseManager.addCourse(
                "admin1@university.edu", "CS102", "Advanced CS", "Advanced CS course",
                true, "Prof B", "prof2@university.edu", "Sec C", "sec2@university.edu",
                0, 0
        );
        courseManager.addActivityToCourse(
                "CS102", LocalDate.parse("2025-09-01"), LocalTime.parse("14:00"),
                LocalDate.parse("2025-12-15"), LocalTime.parse("15:30"), "Room 102",
                DayOfWeek.MONDAY, "Lecture", false
        );

        courseManager.addActivityToCourse(
                "CS102", LocalDate.parse("2025-09-01"), LocalTime.parse("17:00"),
                LocalDate.parse("2025-12-15"), LocalTime.parse("18:00"), "Room 101",
                DayOfWeek.MONDAY, "Tutorial", 15
        );



        loginAsStudent(context);
        StudentController studentController = new StudentController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        startOutputCapture();
        studentController.manageTimetable();

        // Verify output
        assertOutputContains("The course was successfully added to your timetable. Lectures are automatically selected. Please select your required tutorials and labs.");
        assertOutputContains("Activity successfully chosen for course: CS101");
        assertOutputContains("This activity conflicts with existing activities in your timetable:");
        assertOutputContains("CS101 - Tutorial - 17:00-18:30 (Activity ID: 2)");
        assertOutputContains("MONDAY (2025-04-07)");
        assertOutputContains("Course: CS101");
        assertOutputContains("Lecture (ID: 1) - 09:00-10:30 [CHOSEN]");
        assertOutputContains("Tutorial (ID: 2) - 17:00-18:30 [CHOSEN]");
        assertOutputContains("Course: CS102");
        assertOutputContains("Lecture (ID: 3) - 14:00-15:30 [CHOSEN]");
        assertOutputContains("Tutorial (ID: 4) - 17:00-18:00 [UNCHOSEN]");




        // Verify timetable state
        Timetable timetable = courseManager.getTimetable("student1@hindeburg.ac.uk");
        assertTrue(timetable.hasSlotsForCourse("CS101"), "CS101 should be in timetable");
        assertTrue(timetable.hasSlotsForCourse("CS102"), "CS102 should be in timetable");
        assertEquals(2, timetable.numChosenActivities("CS101"), "One activity should be chosen for CS101");
        assertEquals(1, timetable.numChosenActivities("CS102"), "No activities should be chosen for CS102 due to conflict");
    }
    @Test
    @DisplayName("Test choosing an invalid activity ID")
    public void testChooseInvalidActivityId() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "2",        // Select "Add course to timetable"
                "CS101",    // Enter course code
                "3",        // Select "Choose activities"
                "CS101",    // Enter course code
                "999",      // Choose invalid activity ID
                "1",        // View timetable
                "5",       // Exit timetable management
                "-1"        // Exit
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        CourseManager courseManager = context.getCourseManager();

        // Add a course with one activity
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

        loginAsStudent(context);
        StudentController studentController = new StudentController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        startOutputCapture();
        studentController.manageTimetable();

        // Verify output
        assertOutputContains("The course was successfully added to your timetable.");
        assertOutputContains("Invalid activity ID for this course");


        // Verify timetable state
        Timetable timetable = courseManager.getTimetable("student1@hindeburg.ac.uk");
        assertTrue(timetable.hasSlotsForCourse("CS101"), "CS101 should be in timetable");
        assertEquals(1, timetable.numChosenActivities("CS101"), "No activities should be chosen due to invalid ID");

    }

    @Test
    @DisplayName("Test choosing multiple tutorials for a recorded course")
    public void testChooseMultipleTutorialsInRecordedCourse() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "2",        // Add course
                "CS101",
                "3",        // Choose activities
                "CS101",
                "2",        // Choose tutorial 1 (ID 2)
                "3",        // Choose activities again
                "CS101",
                "3",        // Choose tutorial 2 (ID 3)
                "1",        // View timetable
                "5",
                "-1"
        );

        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        CourseManager courseManager = context.getCourseManager();

        courseManager.addCourse(
                "admin1@university.edu", "CS101", "Intro to CS", "Basic CS course",
                true, "Prof A", "prof@university.edu", "Sec B", "sec@university.edu",
                2, 0  // Requires 2 tutorials
        );
        courseManager.addActivityToCourse(
                "CS101", LocalDate.parse("2025-09-01"), LocalTime.parse("09:00"),
                LocalDate.parse("2025-12-15"), LocalTime.parse("10:00"), "Room 101",
                DayOfWeek.MONDAY, "Lecture", true
        );
        courseManager.addActivityToCourse(
                "CS101", LocalDate.parse("2025-09-01"), LocalTime.parse("10:10"),
                LocalDate.parse("2025-12-15"), LocalTime.parse("11:00"), "Room 102",
                DayOfWeek.TUESDAY, "Tutorial", 10
        );
        courseManager.addActivityToCourse(
                "CS101", LocalDate.parse("2025-09-01"), LocalTime.parse("11:10"),
                LocalDate.parse("2025-12-15"), LocalTime.parse("12:00"), "Room 103",
                DayOfWeek.TUESDAY, "Tutorial", 10
        );

        loginAsStudent(context);
        StudentController studentController = new StudentController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        startOutputCapture();
        studentController.manageTimetable();

        // Verify output
        assertOutputContains("The course was successfully added to your timetable.");
        assertOutputContains("You have to choose 2 tutorials for this course");
        assertOutputContains("Activity successfully chosen for course: CS101"); // First tutorial
        assertOutputContains("Activity successfully chosen for course: CS101"); // Second tutorial
        assertOutputContains("Week from 2025-04-07 to 2025-04-11");
        assertOutputContains("MONDAY (2025-04-07)");
        assertOutputContains("Course: CS101");
        assertOutputContains("Lecture (ID: 1) - 09:00-10:00 [CHOSEN]");
        assertOutputContains("TUESDAY (2025-04-08)");
        assertOutputContains("Course: CS101");
        assertOutputContains("Tutorial (ID: 2) - 10:10-11:00 [CHOSEN]");
        assertOutputContains("Tutorial (ID: 3) - 11:10-12:00 [CHOSEN]");


        // Verify timetable state
        Timetable timetable = courseManager.getTimetable("student1@hindeburg.ac.uk");
        assertEquals(3, timetable.numChosenActivities("CS101"), "Two activities should be chosen");
        assertEquals(2, timetable.countChosenActivitiesOfType("CS101", "Tutorial", courseManager), "Two tutorials should be chosen");
        assertEquals(1, timetable.countChosenActivitiesOfType("CS101", "Lecture", courseManager), "No lectures chosen");
    }

    @Test
    @DisplayName("Test choosing multiple tutorials for an unrecorded course")
    public void testChooseMultipleTutorialsInUnRecordedCourse() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "2",        // Add course
                "CS101",
                "3",        // Choose activities
                "CS101",
                "2",        // Choose tutorial 1 (ID 2)
                "3",        // Choose activities again
                "CS101",
                "3",        // Choose tutorial 2 (ID 3)
                "1",        // View timetable
                "5",
                "-1"
        );

        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        CourseManager courseManager = context.getCourseManager();

        courseManager.addCourse(
                "admin1@university.edu", "CS101", "Intro to CS", "Basic CS course",
                true, "Prof A", "prof@university.edu", "Sec B", "sec@university.edu",
                2, 0  // Requires 2 tutorials
        );
        courseManager.addActivityToCourse(
                "CS101", LocalDate.parse("2025-09-01"), LocalTime.parse("09:00"),
                LocalDate.parse("2025-12-15"), LocalTime.parse("10:00"), "Room 101",
                DayOfWeek.MONDAY, "Lecture", false
        );
        courseManager.addActivityToCourse(
                "CS101", LocalDate.parse("2025-09-01"), LocalTime.parse("10:10"),
                LocalDate.parse("2025-12-15"), LocalTime.parse("11:00"), "Room 102",
                DayOfWeek.TUESDAY, "Tutorial", 10
        );
        courseManager.addActivityToCourse(
                "CS101", LocalDate.parse("2025-09-01"), LocalTime.parse("11:10"),
                LocalDate.parse("2025-12-15"), LocalTime.parse("12:00"), "Room 103",
                DayOfWeek.TUESDAY, "Tutorial", 10
        );

        loginAsStudent(context);
        StudentController studentController = new StudentController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        startOutputCapture();
        studentController.manageTimetable();

        // Verify output
        assertOutputContains("The course was successfully added to your timetable.");
        assertOutputContains("You have to choose 2 tutorials for this course");
        assertOutputContains("Activity successfully chosen for course: CS101"); // First tutorial
        assertOutputContains("Activity successfully chosen for course: CS101"); // Second tutorial
        assertOutputContains("Week from 2025-04-07 to 2025-04-11");
        assertOutputContains("MONDAY (2025-04-07)");
        assertOutputContains("Course: CS101");
        assertOutputContains("Lecture (ID: 1) - 09:00-10:00 [CHOSEN]");
        assertOutputContains("TUESDAY (2025-04-08)");
        assertOutputContains("Course: CS101");
        assertOutputContains("Tutorial (ID: 2) - 10:10-11:00 [CHOSEN]");
        assertOutputContains("Tutorial (ID: 3) - 11:10-12:00 [CHOSEN]");


        // Verify timetable state
        Timetable timetable = courseManager.getTimetable("student1@hindeburg.ac.uk");
        assertEquals(3, timetable.numChosenActivities("CS101"), "Two activities should be chosen");
        assertEquals(2, timetable.countChosenActivitiesOfType("CS101", "Tutorial", courseManager), "Two tutorials should be chosen");
        assertEquals(1, timetable.countChosenActivitiesOfType("CS101", "Lecture", courseManager), "No lectures chosen");
    }

    @Test
    @DisplayName("Test counting activities with a null CourseManager")
    public void testCountActivitiesWithNullCourseManager() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "2",        // Add course
                "CS101",
                "1",        // View timetable
                "5",
                "-1"
        );

        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        CourseManager courseManager = context.getCourseManager();

        courseManager.addCourse(
                "admin1@university.edu", "CS101", "Intro to CS", "Basic CS course",
                true, "Prof A", "prof@university.edu", "Sec B", "sec@university.edu",
                0, 0
        );
        loginAsStudent(context);
        StudentController studentController = new StudentController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        startOutputCapture();
        studentController.manageTimetable();

        Timetable timetable = courseManager.getTimetable("student1@hindeburg.ac.uk");
        assertEquals(0, timetable.countChosenActivitiesOfType("CS101", "Lecture", null), "Should return 0 for null CourseManager");
    }
}