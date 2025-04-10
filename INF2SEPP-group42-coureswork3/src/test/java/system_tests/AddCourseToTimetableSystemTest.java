package system_tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import controller.StudentController;
import external.MockAuthenticationService;
import external.MockEmailService;
import model.CourseManager;
import model.SharedContext;
import model.Timetable;
import view.TextUserInterface;
import view.View;

public class AddCourseToTimetableSystemTest extends TUITest {
    @Test
    @DisplayName("Test adding a valid course with automatic lecture selection")
    public void testAddValidCourseToTimetable() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "2",        // Select "Add course to timetable"
                "CS101",    // Enter course code
                "1",        // View timetable
                "5",
                "-1"        // Exit
        );

        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        CourseManager courseManager = context.getCourseManager();
        courseManager.addCourse(
                "admin1@university.edu", "CS101", "Intro to CS", "Basic CS course",
                true, "Prof A", "prof@university.edu", "Sec B", "sec@university.edu",
                1, 1
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

        assertOutputContains("The course was successfully added to your timetable. Lectures are automatically selected. Please select your required tutorials and labs.");
        assertOutputContains("You have to choose 1 tutorials for this course");
        assertOutputContains("You have to choose 1 labs for this course");
        assertOutputContains("MONDAY (2025-04-07):");
        assertOutputContains("Course: CS101");
        assertOutputContains("Lecture (ID: 1) - 09:00-10:30 [CHOSEN]");

        Timetable timetable = courseManager.getTimetable("student1@hindeburg.ac.uk");
        assertTrue(timetable.hasSlotsForCourse("CS101"), "CS101 should be in timetable after adding");
        assertEquals(1, timetable.numChosenActivities("CS101"), "One activity should be chosen for CS101");
        // Add assertion for countChosenActivitiesOfType
        assertEquals(1, timetable.countChosenActivitiesOfType("CS101", "Lecture", courseManager), "One lecture should be chosen");
        assertEquals(0, timetable.countChosenActivitiesOfType("CS101", "Tutorial", courseManager), "No tutorials chosen yet");
        assertEquals(0, timetable.countChosenActivitiesOfType("CS101", "Lab", courseManager), "No labs chosen yet");
    }
    @Test
    @DisplayName("Test adding a course with unrecorded lecture conflict")
    public void testAddCourseWithUnrecordedLectureConflict() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "2",        // Select "Add course to timetable"
                "CS101",    // Enter course code
                "2",        // Select "Add course to timetable"
                "CS102",    // Enter course code (unrecorded lecture)
                "1",        // View timetable
                "5",
                "-1"        // Exit
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        CourseManager courseManager = context.getCourseManager();

        // Add first course with a recorded lecture
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

        // Add second course with an unrecorded lecture at the same time
        courseManager.addCourse(
                "admin1@university.edu", "CS102", "Advanced CS", "Advanced CS course",
                true, "Prof B", "prof2@university.edu", "Sec C", "sec2@university.edu",
                0, 0
        );
        courseManager.addActivityToCourse(
                "CS102", LocalDate.parse("2025-09-01"), LocalTime.parse("09:00"),
                LocalDate.parse("2025-12-15"), LocalTime.parse("10:30"), "Room 102",
                DayOfWeek.MONDAY, "Lecture", false
        );

        loginAsStudent(context);
        StudentController studentController = new StudentController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        startOutputCapture();
        studentController.manageTimetable();

        // Verify output
        assertOutputContains("The course was successfully added to your timetable. Lectures are automatically selected. Please select your required tutorials and labs.");
        assertOutputContains("You have at least one clash with an unrecorded lecture. The course cannot be added to your timetable.");
        assertOutputContains("Unrecorded lecture conflict: Lecture: MONDAY 09:00-10:30 (2025-09-01 to 2025-12-15) at Room 102 (ID: 2) - Not Recorded with CS101 - Recorded Lecture - 09:00-10:30 (Activity ID: 1)");
        assertOutputContains("Timetable for student1@hindeburg.ac.uk");
        assertOutputContains("MONDAY (2025-04-07)");
        assertOutputContains("Course: CS101");
        assertOutputContains("Lecture (ID: 1) - 09:00-10:30 [CHOSEN]");

    }


    @Test
    @DisplayName("Test adding a course with recorded lecture conflict")
    public void testAddCourseWithRecordedLectureConflict() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "2",        // Select "Add course to timetable"
                "CS101",    // Enter course code
                "2",        // Select "Add course to timetable"
                "CS102",    // Enter course code
                "1",        // View timetable
                "5",
                "-1"        // Exit
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        CourseManager courseManager = context.getCourseManager();

        // Add first course with a recorded lecture
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

        // Add second course with an unrecorded lecture at the same time
        courseManager.addCourse(
                "admin1@university.edu", "CS102", "Advanced CS", "Advanced CS course",
                true, "Prof B", "prof2@university.edu", "Sec C", "sec2@university.edu",
                0, 0
        );
        courseManager.addActivityToCourse(
                "CS102", LocalDate.parse("2025-09-01"), LocalTime.parse("09:00"),
                LocalDate.parse("2025-12-15"), LocalTime.parse("10:30"), "Room 102",
                DayOfWeek.MONDAY, "Lecture", true
        );

        loginAsStudent(context);
        StudentController studentController = new StudentController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        startOutputCapture();
        studentController.manageTimetable();

        // Verify output
        assertOutputContains("The course was successfully added to your timetable. Lectures are automatically selected. Please select your required tutorials and labs.");
        assertOutputContains("Warning: Recorded lecture has potential conflict: Time slot conflicts with existing slots: CS101 - Recorded Lecture - 09:00-10:30 (Activity ID: 1)");
        assertOutputContains("Timetable for student1@hindeburg.ac.uk");
        assertOutputContains("MONDAY (2025-04-07)");
        assertOutputContains("Course: CS101");
        assertOutputContains("Lecture (ID: 1) - 09:00-10:30 [CHOSEN]");
        assertOutputContains("Course: CS102");
        assertOutputContains("Lecture (ID: 2) - 09:00-10:30 [CHOSEN]");

    }


    @Test
    @DisplayName("Test adding a course with an invalid course code")
    public void testAddCourseWithInvalidCode() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "2",        // Select "Add course to timetable"
                "INVALID",  // Enter invalid course code
                "1",        // View timetable
                "5",        // Exit
                "-1"        // Exit from main menu
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        loginAsStudent(context);
        StudentController studentController = new StudentController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        startOutputCapture();
        studentController.manageTimetable();

        // Verify output
        assertOutputContains("Incorrect course code");
        assertOutputContains("Failed to add course to your timetable. Please check course code and conflicts.");
    }

    @Test
    @DisplayName("Test adding a course already present in timetable")
    public void testAddCourseAlreadyInTimetable() throws URISyntaxException, IOException, ParseException {
        setMockInput("2",
                    "CS101",
                    "3",
                    "CS101",
                    "1",
                    "2",
                    "CS101",
                    "5",
                    "-1"
        );
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        CourseManager courseManager = context.getCourseManager();
        courseManager.addCourse("admin1@university.edu", "CS101", "Intro to CS", "Basic CS course",
                true, "Prof A", "prof@university.edu", "Sec B", "sec@university.edu", 0, 0);

        courseManager.addActivityToCourse("CS101", LocalDate.now(), LocalTime.of(9, 0),
                LocalDate.now(), LocalTime.of(10, 0), "Room 101", DayOfWeek.MONDAY, "Lecture", true);
        loginAsStudent(context);
        StudentController studentController = new StudentController(
                context, view, new MockAuthenticationService(), new MockEmailService());
        startOutputCapture();
        studentController.manageTimetable();
        assertOutputContains("The course was successfully added to your timetable.");
        assertOutputContains("Course is already in your timetable");
        Timetable timetable = courseManager.getTimetable("student1@hindeburg.ac.uk");
        assertTrue(timetable.hasSlotsForCourse("CS101"), "CS101 should be in timetable after first add");
    }

    @Test
    @DisplayName("Test adding multiple courses to timetable")
    public void testAddMultipleCoursesToTimetable() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "2",        // Select "Add course to timetable"
                "CS101",    // Enter first course code
                "2",        // Select "Add course to timetable"
                "CS102",    // Enter second course code
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

        // Add second course (CS102) with a lecture on a different day
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
        assertOutputContains("The course was successfully added to your timetable. Lectures are automatically selected. Please select your required tutorials and labs.");
        assertOutputContains("You have to choose 1 tutorials for this course");  // For CS101
        assertOutputContains("You have to choose 1 labs for this course");      // For CS102
        assertOutputContains("Timetable for student1@hindeburg.ac.uk");
        assertOutputContains("MONDAY (2025-04-07):");
        assertOutputContains("Course: CS101");
        assertOutputContains("Lecture (ID: 1) - 09:00-10:30 [CHOSEN]");
        assertOutputContains("TUESDAY (2025-04-08):");
        assertOutputContains("Course: CS101");
        assertOutputContains("Tutorial (ID: 2) - 11:00-12:00 [UNCHOSEN]");
        assertOutputContains("Course: CS102");
        assertOutputContains("Lecture (ID: 3) - 14:00-15:30 [CHOSEN]");
        assertOutputContains("WEDNESDAY (2025-04-09):");
        assertOutputContains("Course: CS102");
        assertOutputContains("Lab (ID: 4) - 09:00-10:00 [UNCHOSEN]");

        // Verify timetable state
        Timetable timetable = courseManager.getTimetable("student1@hindeburg.ac.uk");
        assertTrue(timetable.hasSlotsForCourse("CS101"), "CS101 should be in timetable");
        assertTrue(timetable.hasSlotsForCourse("CS102"), "CS102 should be in timetable");
        assertEquals(1, timetable.numChosenActivities("CS101"), "One activity (Lecture) should be chosen for CS101");
        assertEquals(1, timetable.numChosenActivities("CS102"), "One activity (Lecture) should be chosen for CS102");
        // Verify specific activity types
        assertEquals(1, timetable.countChosenActivitiesOfType("CS101", "Lecture", courseManager), "One lecture chosen for CS101");
        assertEquals(0, timetable.countChosenActivitiesOfType("CS101", "Tutorial", courseManager), "No tutorials chosen for CS101");
        assertEquals(1, timetable.countChosenActivitiesOfType("CS102", "Lecture", courseManager), "One lecture chosen for CS102");
        assertEquals(0, timetable.countChosenActivitiesOfType("CS102", "Lab", courseManager), "No labs chosen for CS102");
    }
}

