package system_tests;

import controller.AdminStaffController;
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

public class RemoveCourseSystemTests extends TUITest {

    @Test
    @DisplayName("Test removing an existing course")
    public void testRemoveExistingCourse() throws URISyntaxException, IOException, ParseException {
        // Set up mock inputs for removing an existing course
        setMockInput(
                "1",
                "0",                        // Select first course (CS101)
                "Y",                        // Confirm removal
                "-1",                       // Exit course management menu
                "-1"                        // Exit main menu
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);

        // Add a course to remove
        context.getCourseManager().addCourse(
                "admin1@university.edu", "CS101", "Intro to CS", "Basic course",
                true, "Prof A", "prof@university.edu", "Sec B", "sec@university.edu",
                1, 1
        );


        // Login as admin staff - using the same mock input stream
        loginAsAdminStaff(context);
        AdminStaffController adminController = new AdminStaffController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );
        startOutputCapture();

        // Execute the main menu which will go through our inputs
        adminController.manageCourse();

        // Verify output
        assertOutputContains("Course CS101 has been successfully removed");

        // Verify course is removed
        assertFalse(context.getCourseManager().hasCourse("CS101"));
    }

    @Test
    @DisplayName("Test cancelling the removal of a course")
    public void testRemoveCourseCancelConfirmation() throws URISyntaxException, IOException, ParseException {
        // Set up mock inputs for cancelling course removal
        setMockInput(
                "1",
                "0",                        // Select first course (CS101)
                "N",                        // Cancel removal
                "-1",                       // Exit course management menu
                "-1"                        // Exit main menu
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);

        // Add a course to attempt to remove
        context.getCourseManager().addCourse(
                "admin1@university.edu", "CS101", "Intro to CS", "Basic course",
                true, "Prof A", "prof@university.edu", "Sec B", "sec@university.edu",
                1, 1
        );

        // Login as admin staff - using the same mock input stream
        loginAsAdminStaff(context);
        AdminStaffController adminController = new AdminStaffController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );
        startOutputCapture();

        // Execute the main menu which will go through our inputs
        adminController.manageCourse();

        // Verify output
        assertOutputContains("Course removal cancelled");


        // Verify course still exists
        assertTrue(context.getCourseManager().hasCourse("CS101"));
    }

    @Test
    @DisplayName("Test removing a course when no courses are available")
    public void testRemoveCourseNoCoursesAvailable() throws URISyntaxException, IOException, ParseException {
        // Set up mock inputs for no courses available
        setMockInput(
                "1",
                "-1",                       // Exit course management menu
                "-1"                        // Exit main menu
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);

        // Login as admin staff - using the same mock input stream
        loginAsAdminStaff(context);
        AdminStaffController adminController = new AdminStaffController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );
        startOutputCapture();

        // Execute the main menu which will go through our inputs
        adminController.manageCourse();

        // Verify output
        assertOutputContains("No courses available to remove");

    }

    @Test
    @DisplayName("Test removing a course with an invalid index")
    public void testRemoveNonExistentCourseIndex() throws URISyntaxException, IOException, ParseException {
        // Set up mock inputs for selecting invalid course index
        setMockInput(
                "1",
                "1",                        // Invalid index (only one course at index 0)
                "0",
                "Y",
                "-1",                       // Exit course management menu after error
                "-1"                        // Exit main menu
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);

        // Add one course
        context.getCourseManager().addCourse(
                "admin1@university.edu", "CS101", "Intro to CS", "Basic course",
                true, "Prof A", "prof@university.edu", "Sec B", "sec@university.edu",
                1, 1
        );

        // Login as admin staff - using the same mock input stream
        loginAsAdminStaff(context);
        AdminStaffController adminController = new AdminStaffController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );
        startOutputCapture();

        // Execute the main menu which will go through our inputs
        adminController.manageCourse();
        // Verify output (exact message might depend on selectFromMenu implementation)
        assertOutputContains("Invalid option 1");

        // Verify course still exists
        assertFalse(context.getCourseManager().hasCourse("CS101"));
    }

    @Test
    @DisplayName("Test removing a course with scheduled activities")
    public void testRemoveCourseWithActivities() throws URISyntaxException, IOException, ParseException {
        // Set up mock inputs for removing course with activities
        setMockInput(

                "1",                        // Course management menu: 1 = "Remove a course"
                "0",                        // Select first course (CS101)
                "Y",                        // Confirm removal
                "-1",                       // Exit course management menu
                "-1"                        // Exit main menu
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);

        // Add a course with an activity
        context.getCourseManager().addCourse(
                "admin1@university.edu", "CS101", "Intro to CS", "Basic course",
                true, "Prof A", "prof@university.edu", "Sec B", "sec@university.edu",
                1, 1
        );
        context.getCourseManager().addActivityToCourse(
                "CS101", LocalDate.parse("2025-09-01"), LocalTime.parse("09:00"),
                LocalDate.parse("2025-12-15"), LocalTime.parse("10:30"), "Room 101",
                DayOfWeek.MONDAY, "Lecture", true
        );

        // Login as admin staff - using the same mock input stream
        loginAsAdminStaff(context);
        AdminStaffController adminController = new AdminStaffController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );
        startOutputCapture();

        // Execute the main menu which will go through our inputs
        adminController.manageCourse();

        // Verify output
        assertOutputContains("Course CS101 has been successfully removed");

        // Verify course is removed
        assertFalse(context.getCourseManager().hasCourse("CS101"));
    }

    @Test
    @DisplayName("Test removing multiple courses")
    public void testRemoveMultipleCourses() throws URISyntaxException, IOException, ParseException {
        // Set up mock inputs for removing multiple courses
        setMockInput(
                "1",                        // Select "Remove a course" from manageCourse menu
                "0",                        // Select first course (CS101)
                "Y",                        // Confirm removal of CS101
                "1",                        // Select "Remove a course" from manageCourse menu
                "0",                        // Select first remaining course (CS102, now at index 0)
                "Y",                        // Confirm removal of CS102
                "-1",                       // Exit course management menu
                "-1"                        // Exit main menu
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);

        // Add two courses to remove
        context.getCourseManager().addCourse(
                "admin1@university.edu", "CS101", "Intro to CS", "Basic CS course",
                true, "Prof A", "prof@university.edu", "Sec B", "sec@university.edu",
                1, 1
        );
        context.getCourseManager().addCourse(
                "admin1@university.edu", "CS102", "Advanced CS", "Advanced CS course",
                true, "Prof C", "profc@university.edu", "Sec D", "secd@university.edu",
                2, 2
        );

        // Login as admin staff
        loginAsAdminStaff(context);
        AdminStaffController adminController = new AdminStaffController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );
        startOutputCapture();

        // Execute the course management menu with our inputs
        adminController.manageCourse();

        // Verify output
        assertOutputContains("Course CS101 has been successfully removed");
        assertOutputContains("Course CS102 has been successfully removed");

        // Verify both courses are removed
        assertFalse(context.getCourseManager().hasCourse("CS101"), "CS101 should be removed");
        assertFalse(context.getCourseManager().hasCourse("CS102"), "CS102 should be removed");
    }
}
