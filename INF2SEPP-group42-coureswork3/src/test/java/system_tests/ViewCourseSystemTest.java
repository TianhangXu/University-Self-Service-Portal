package system_tests;
import controller.GuestController;
import controller.MenuController;
import controller.ViewController;
import external.MockAuthenticationService;
import external.MockEmailService;
import model.SharedContext;
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
public class ViewCourseSystemTest extends TUITest {
    @Test
    @DisplayName("Test viewing all courses when no courses exist")
    public void testViewAllCoursesEmpty() throws URISyntaxException, IOException, ParseException {
        // Set up mock inputs for viewing courses when none exist
        setMockInput(
                "3",                        // Main menu: 3 = VIEW_COURSES
                "-1"                        // Exit main menu
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);

        // Login as student
        loginAsStudent(context);

        ViewController viewController = new ViewController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        startOutputCapture();

        // Add first course
        viewController.viewCourses();

        // Verify output shows no courses available
        assertOutputContains("No courses available");
    }

    @Test
    @DisplayName("Test viewing all courses with one course")
    public void testViewAllCoursesWithOneCourse() throws URISyntaxException, IOException, ParseException {
        // Set up mock inputs for viewing courses
        setMockInput(

                "3",                        // Main menu: 3 = VIEW_COURSES
                "-1"                        // Exit main menu
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);

        // Add a course
        context.getCourseManager().addCourse(
                "admin1@university.edu", "CS101", "Intro to CS", "Basic CS course",
                true, "Prof A", "prof@university.edu", "Sec B", "sec@university.edu",
                1, 1
        );

        // Login as student
        loginAsStudent(context);

        ViewController viewController = new ViewController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        startOutputCapture();

        // Add first course
        viewController.viewCourses();

        // Verify output contains course details
        assertOutputContains("Course: CS101 - Intro to CS");
        assertOutputContains("Description: Basic CS course");
        assertOutputContains("Requires Computers: Yes");
        assertOutputContains("Course Organiser: Prof A (prof@university.edu)");
        assertOutputContains("Course Secretary: Sec B (sec@university.edu)");
        assertOutputContains("Required Tutorials: 1");
        assertOutputContains("Required Labs: 1");
    }

    @Test
    @DisplayName("Test viewing a course with scheduled activities")
    public void testViewCourseWithActivities() throws URISyntaxException, IOException, ParseException {
        // Set up mock inputs for viewing courses
        setMockInput(
                "3",                        // Main menu: 3 = VIEW_COURSES
                "-1"                        // Exit main menu
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);

        // Add a course
        context.getCourseManager().addCourse(
                "admin1@university.edu", "CS101", "Intro to CS", "Basic CS course",
                true, "Prof A", "prof@university.edu", "Sec B", "sec@university.edu",
                1, 1
        );

        context.getCourseManager().addActivityToCourse(
                "CS101", LocalDate.parse("2025-09-01"), LocalTime.parse("09:00"),
                LocalDate.parse("2025-12-15"), LocalTime.parse("10:30"), "Room 101",
                DayOfWeek.MONDAY, "Lecture", true
        );

        // Login as student
        loginAsStudent(context);

        ViewController viewController = new ViewController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        startOutputCapture();

        // Add first course
        viewController.viewCourses();

        // Verify output contains course details
        assertOutputContains("Course: CS101 - Intro to CS");
        assertOutputContains("Description: Basic CS course");
        assertOutputContains("Requires Computers: Yes");
        assertOutputContains("Course Organiser: Prof A (prof@university.edu)");
        assertOutputContains("Course Secretary: Sec B (sec@university.edu)");
        assertOutputContains("Required Tutorials: 1");
        assertOutputContains("Required Labs: 1");
        assertOutputContains("Activities:");
        assertOutputContains("0: Lecture: MONDAY 09:00-10:30 (2025-09-01 to 2025-12-15) at Room 101 (ID: 1) - Recorded");
        assertOutputContains("Lecture: MONDAY 09:00-10:30 (2025-09-01 to 2025-12-15) at Room 101 (ID: 1) - Recorded");
    }

    @Test
    @DisplayName("Test viewing a specific course with a valid course code")
    public void testViewSpecificCourseValidCode() throws URISyntaxException, IOException, ParseException {
        // Set up mock inputs for viewing a specific course
        setMockInput(
                "4",                        // Main menu: 4 = VIEW_SPECIFIC_COURSES
                "CS101",                    // Specific course code
                "-1"                        // Exit main menu
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);

        // Add a course
        context.getCourseManager().addCourse(
                "admin1@university.edu", "CS101", "Intro to CS", "Basic CS course",
                true, "Prof A", "prof@university.edu", "Sec B", "sec@university.edu",
                1, 1
        );

        // Login as student
        loginAsStudent(context);

        ViewController viewController = new ViewController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        startOutputCapture();

        // Add first course
        viewController.viewSpecificCourse("CS101");

        // Verify output contains detailed course information
        assertOutputContains("Course Details:");
        assertOutputContains("Code: CS101");
        assertOutputContains("Name: Intro to CS");
        assertOutputContains("Description: Basic CS course");
        assertOutputContains("Requires Computers: Yes");
        assertOutputContains("Course Organiser: Prof A (prof@university.edu)");
        assertOutputContains("Course Secretary: Sec B (sec@university.edu)");
        assertOutputContains("Required Tutorials: 1");
        assertOutputContains("Required Labs: 1");
    }

    @Test
    @DisplayName("Test viewing a specific course with an invalid course code")
    public void testViewSpecificCourseInvalidCode() throws URISyntaxException, IOException, ParseException {
        // Set up mock inputs for viewing a non-existent course
        setMockInput(
                "4",                        // Main menu: 4 = VIEW_SPECIFIC_COURSES
                "CS999",                    // Invalid course code
                "-1"                        // Exit main menu
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);

        // Add a different course
        context.getCourseManager().addCourse(
                "admin1@university.edu", "CS101", "Intro to CS", "Basic CS course",
                true, "Prof A", "prof@university.edu", "Sec B", "sec@university.edu",
                1, 1
        );

        // Login as student
        loginAsStudent(context);

        ViewController viewController = new ViewController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        startOutputCapture();

        // Add first course
        viewController.viewSpecificCourse("CS999");

        // Verify error message for non-existent course
        assertOutputContains("Course not found. Please check the course code and try again.");
    }
    @Test
    @DisplayName("Test viewing a specific course with activities")
    public void testViewSpecificCourseWithActivities() throws URISyntaxException, IOException, ParseException {
        // Set up mock inputs for viewing a course with activities
        setMockInput(
                "4",                        // Main menu: 2 = VIEW_COURSES
                "CS101",                    // Specific course code
                "-1"                        // Exit main menu
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);

        // Add a course with an activity
        context.getCourseManager().addCourse(
                "admin1@university.edu", "CS101", "Intro to CS", "Basic CS course",
                true, "Prof A", "prof@university.edu", "Sec B", "sec@university.edu",
                1, 1
        );
        context.getCourseManager().addActivityToCourse(
                "CS101", LocalDate.parse("2025-09-01"), LocalTime.parse("09:00"),
                LocalDate.parse("2025-12-15"), LocalTime.parse("10:30"), "Room 101",
                DayOfWeek.MONDAY, "Lecture", true
        );

        // Login as student
        loginAsStudent(context);

        ViewController viewController = new ViewController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        startOutputCapture();

        // Add first course
        viewController.viewSpecificCourse("CS101");

        // Verify output contains course and activity details
        assertOutputContains("Course Details:");
        assertOutputContains("Code: CS101");
        assertOutputContains("Activities:");
        assertOutputContains("Lecture: MONDAY 09:00-10:30 (2025-09-01 to 2025-12-15) at Room 101");
        assertOutputContains("Recorded");
    }

    @Test
    @DisplayName("Test viewing multiple courses with tutorials")
    public void testViewMultipleCoursesWithTutorials() throws URISyntaxException, IOException, ParseException {
        // Set up mock inputs for viewing multiple courses
        setMockInput(
                "3",                        // Main menu: 3 = VIEW_COURSES
                "-1"                        // Exit main menu
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);

        // Add first course with tutorial
        context.getCourseManager().addCourse(
                "admin1@university.edu", "CS101", "Intro to CS", "Basic CS course",
                true, "Prof A", "prof@university.edu", "Sec B", "sec@university.edu",
                1, 1
        );
        context.getCourseManager().addActivityToCourse(
                "CS101", LocalDate.parse("2025-09-01"), LocalTime.parse("14:00"),
                LocalDate.parse("2025-12-15"), LocalTime.parse("15:30"), "Room 201",
                DayOfWeek.TUESDAY, "Lecture", false
        );

        // Add second course with tutorial
        context.getCourseManager().addCourse(
                "admin1@university.edu", "CS202", "Advanced Programming", "Advanced coding",
                false, "Prof C", "profC@university.edu", "Sec D", "secD@university.edu",
                2, 1
        );
        context.getCourseManager().addActivityToCourse(
                "CS202", LocalDate.parse("2025-09-02"), LocalTime.parse("10:00"),
                LocalDate.parse("2025-12-16"), LocalTime.parse("11:30"), "Room 301",
                DayOfWeek.WEDNESDAY, "Lecture", true
        );

        // Login as student
        loginAsStudent(context);

        ViewController viewController = new ViewController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        startOutputCapture();

        // View all courses
        viewController.viewCourses();

        // Verify first course details
        assertOutputContains("Course: CS101 - Intro to CS");
        assertOutputContains("Description: Basic CS course");
        assertOutputContains("Requires Computers: Yes");
        assertOutputContains("Course Organiser: Prof A (prof@university.edu)");
        assertOutputContains("Required Tutorials: 1");
        assertOutputContains("Required Labs: 1");
        assertOutputContains("Activities:");
        assertOutputContains("Lecture: TUESDAY 14:00-15:30 (2025-09-01 to 2025-12-15) at Room 201");
        assertOutputContains("Not Recorded");

        // Verify second course details
        assertOutputContains("Course: CS202 - Advanced Programming");
        assertOutputContains("Description: Advanced coding");
        assertOutputContains("Requires Computers: No");
        assertOutputContains("Course Organiser: Prof C (profC@university.edu)");
        assertOutputContains("Course Secretary: Sec D (secD@university.edu)");
        assertOutputContains("Required Tutorials: 2");
        assertOutputContains("Required Labs: 1");
        assertOutputContains("Activities:");
        assertOutputContains("Lecture: WEDNESDAY 10:00-11:30 (2025-09-02 to 2025-12-16) at Room 301");
        assertOutputContains("Recorded");
    }
}
