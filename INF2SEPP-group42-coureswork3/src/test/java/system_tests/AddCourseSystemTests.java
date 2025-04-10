package system_tests;

import controller.AdminStaffController;
import controller.MenuController;
import controller.GuestController;
import external.MockAuthenticationService;
import external.MockEmailService;
import model.Course;
import model.SharedContext;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import view.TextUserInterface;
import view.View;


import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

public class AddCourseSystemTests extends TUITest {

    @Test
    @DisplayName("Test adding a basic course with minimal information")
    public void testAddBasicCourse() throws URISyntaxException, IOException, ParseException {
        // Set up mock inputs for the entire flow
        setMockInput(
                "0",                                 //[0] Add a new course
                "CS101",                             // Course code
                "Introduction to Programming",       // Name
                "A beginner course for programming", // Description
                "Y",                                 // Requires computers
                "Professor Smith",                   // Course organiser name
                "smith@university.edu",              // Course organiser email
                "Jane Doe",                          // Course secretary name
                "jane@university.edu",               // Course secretary email
                "1",                                 // Required tutorials
                "1",                                 // Required labs
                "-1",                                // Cancel adding activities (selectFromMenu returns -1 for "Cancel")
                "-1",                                // Exit course management menu
                "-1"                                 // Exit main menu
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

        // Verify output contains success message
        assertOutputContains("Course has been successfully created");

        // Verify the course exists in the course manager
        assertTrue(context.getCourseManager().hasCourse("CS101"));

        // Get the course and verify its properties
        Course addedCourse = context.getCourseManager().getCourseByCode("CS101");
        assertNotNull(addedCourse);
        assertEquals("Introduction to Programming", addedCourse.getName());
        assertEquals("A beginner course for programming", addedCourse.getDescription());
        assertTrue(addedCourse.isRequiresComputers());
        assertEquals("Professor Smith", addedCourse.getCourseOrganiserName());
        assertEquals("smith@university.edu", addedCourse.getCourseOrganiserEmail());
        assertEquals("Jane Doe", addedCourse.getCourseSecretaryName());
        assertEquals("jane@university.edu", addedCourse.getCourseSecretaryEmail());
        assertEquals(1, addedCourse.getRequiredTutorials());
        assertEquals(1, addedCourse.getRequiredLabs());
    }

    @Test
    @DisplayName("Test adding a course with scheduled activities")
    public void testAddCourseWithActivities() throws URISyntaxException, IOException, ParseException {
        // Set up mock inputs for the entire flow
        setMockInput(
                "0",                                 //[0] Add a new course
                "CS102",                             // Course code
                "Data Structures",                   // Name
                "Advanced data structures",          // Description
                "N",                                 // Doesn't require computers
                "Professor Johnson",                 // Course organiser name
                "johnson@university.edu",            // Course organiser email
                "Bob Roberts",                       // Course secretary name
                "bob@university.edu",                // Course secretary email
                "1",                                 // Required tutorials
                "2",                                 // Required labs
                "0",                                 // Select Lecture
                "MONDAY",                            // Day of week
                "2025-09-01",                        // Start date
                "09:00",                             // Start time
                "2025-12-15",                        // End date
                "10:30",                             // End time
                "Room 101",                          // Location
                "Y",                                 // Is recorded
                "Y",                                 // Yes, add another activity
                "1",                                 // Select Tutorial
                "WEDNESDAY",                         // Day of week
                "2025-09-03",                        // Start date
                "14:00",                             // Start time
                "2025-12-17",                        // End date
                "15:30",                             // End time
                "Room 205",                          // Location
                "25",                                // Capacity
                "-1",                                // No more activities
                "-1",                                // Exit course management menu
                "-1"                                 // Exit main menu
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

        // Verify output contains success message
        assertOutputContains("Course has been successfully created");

        // Verify the course exists in the course manager
        assertTrue(context.getCourseManager().hasCourse("CS102"));

        // Get the course and verify its properties
        Course addedCourse = context.getCourseManager().getCourseByCode("CS102");
        assertNotNull(addedCourse);
        assertEquals("Data Structures", addedCourse.getName());
        assertEquals("Advanced data structures", addedCourse.getDescription());
        assertFalse(addedCourse.isRequiresComputers());
        assertEquals("Professor Johnson", addedCourse.getCourseOrganiserName());
        assertEquals("johnson@university.edu", addedCourse.getCourseOrganiserEmail());
        assertEquals("Bob Roberts", addedCourse.getCourseSecretaryName());
        assertEquals("bob@university.edu", addedCourse.getCourseSecretaryEmail());
        assertEquals(1, addedCourse.getRequiredTutorials());
        assertEquals(2, addedCourse.getRequiredLabs());

        // Verify activities were added
        assertEquals(2, addedCourse.getActivities().size());
    }

    @Test
    @DisplayName("Test adding a course with invalid course code format")
    public void testAddCourseWithInvalidCourseCode() throws URISyntaxException, IOException, ParseException {
        // Set up mock inputs for the entire flow with invalid course code
        setMockInput(
                "0",                                 //[0] Add a new course
                "cs1",                                // Invalid format (should be like CS101)
                "Data Structures",                   // Name
                "Advanced data structures",          // Description
                "N",                                 // Doesn't require computers
                "Professor Johnson",                 // Course organiser name
                "johnson@university.edu",            // Course organiser email
                "Bob Roberts",                       // Course secretary name
                "bob@university.edu",                // Course secretary email
                "1",                                 // Required tutorials
                "2",                                 // Required labs
                "0",
                "CS103",                             // Correct format course code
                "Programming Paradigms",             // Name
                "Different programming paradigms",   // Description
                "Y",                                 // Requires computers
                "Professor Lee",                     // Course organiser name
                "lee@university.edu",                // Course organiser email
                "Sarah Johnson",                     // Course secretary name
                "sarah@university.edu",              // Course secretary email
                "2",                                 // Required tutorials
                "1",                                 // Required labs
                "-1",                                // No to adding activities
                "-1",                                // Exit course management menu
                "-1"                                 // Exit main menu
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

        // Verify output contains error message
        assertOutputContains("Provided courseCode is invalid");

        // Verify output contains success message
        assertOutputContains("Course has been successfully created");


        // Verify the course with valid code exists
        assertTrue(context.getCourseManager().hasCourse("CS103"));

        // Verify the invalid course code was not added
        assertFalse(context.getCourseManager().hasCourse("cs1"));
    }

    @Test
    @DisplayName("Test adding a duplicate course with same course code")
    public void testAddDuplicateCourse() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "0",                                 //[0] Add a new course
                "CS104",                             // Course code
                "Software Engineering",              // Name
                "Software development methodologies",// Description
                "N",                                 // Doesn't require computers
                "Professor White",                   // Course organiser name
                "white@university.edu",              // Course organiser email
                "Mark Brown",                        // Course secretary name
                "mark@university.edu",               // Course secretary email
                "1",                                 // Required tutorials
                "0",                                 // Required labs
                "-1",                                // Cancel adding activities (selectFromMenu returns -1 for "Cancel")
                "0",                                 //[0] Add a new course
                "CS104",                             // Same code as previous course
                "Different Name",                    // Different name
                "Different description",             // Different description
                "Y",                                 // Different computer requirement
                "Different Professor",               // Different organiser name
                "diff@university.edu",               // Different organiser email
                "Different Secretary",               // Different secretary name
                "diffsec@university.edu",            // Different secretary email
                "2",                                 // Different tutorial requirement
                "2",                                 // Different lab requirement
                "-1",                                // Cancel adding activities (selectFromMenu returns -1 for "Cancel")
                "-1",                                // Exit course management menu
                "-1"                                 // Exit main menu

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

        // Verify output contains success message
        assertOutputContains("Course has been successfully created");
        // Verify error message about duplicate course
        assertOutputContains("Course with that code already exists");
        // Verify the original course properties weren't changed
        Course course = context.getCourseManager().getCourseByCode("CS104");
        assertEquals("Software Engineering", course.getName());  // Still has original name
    }

    @Test
    @DisplayName("Test adding a course with missing required information")
    public void testAddCourseWithMissingInformation() throws URISyntaxException, IOException, ParseException {
        // Set up mock inputs with missing email
        setMockInput(
                "0",                                 //[0] Add a new course
                "CS105",                             // Course code
                "Database Systems",                  // Name
                "Introduction to database concepts", // Description
                "Y",                                 // Requires computers
                "Professor Davis",                   // Course organiser name
                "" ,                                  // Missing email (empty string)
                "John Smith",                        // Course secretary name
                "john@university.edu",               // Course secretary email
                "0",                                 //[0] Add a new course
                "CS105",                             // Course code
                "Database Systems",                  // Name
                "Introduction to database concepts", // Description
                "Y",                                 // Requires computers
                "Professor Davis",                   // Course organiser name
                "davis@university.edu",              // Course organiser email
                "John Smith",                        // Course secretary name
                "john@university.edu",               // Course secretary email
                "1",                                 // Required tutorials
                "1",                                 // Required labs
                "-1",                                // No to adding activities
                "-1",                                // Exit course management menu
                "-1"                                 // Exit main menu
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

        // Verify output contains error message
        assertOutputContains("Required course info not provided");
        // Verify the course exists with correct information
        assertTrue(context.getCourseManager().hasCourse("CS105"));

        // Get the course and verify its properties
        Course addedCourse = context.getCourseManager().getCourseByCode("CS105");
        assertNotNull(addedCourse);
        assertEquals("Database Systems", addedCourse.getName());
        assertEquals("Introduction to database concepts", addedCourse.getDescription());
        assertTrue(addedCourse.isRequiresComputers());
        assertEquals("Professor Davis", addedCourse.getCourseOrganiserName());
        assertEquals("davis@university.edu", addedCourse.getCourseOrganiserEmail());
        assertEquals("John Smith", addedCourse.getCourseSecretaryName());
        assertEquals("john@university.edu", addedCourse.getCourseSecretaryEmail());
        assertEquals(1, addedCourse.getRequiredTutorials());
        assertEquals(1, addedCourse.getRequiredLabs());
    }

    @Test
    @DisplayName("Test adding multiple different courses in sequence")
    public void testAddMultipleCourses() throws URISyntaxException, IOException, ParseException {
        // Set up mock inputs for adding multiple courses
        setMockInput(
                "0",                                 // [0] Add a new course
                "CS201",                            // Course 1: Course code
                "Operating Systems",                // Course 1: Name
                "OS fundamentals",                  // Course 1: Description
                "Y",                                // Course 1: Requires computers
                "Professor Adams",                  // Course 1: Course organiser name
                "adams@university.edu",             // Course 1: Course organiser email
                "Mary Jones",                       // Course 1: Course secretary name
                "mary@university.edu",              // Course 1: Course secretary email
                "2",                                // Course 1: Required tutorials
                "1",                                // Course 1: Required labs
                "-1",                               // Course 1: No activities
                "0",                                // [0] Add another new course
                "CS202",                            // Course 2: Course code
                "Computer Networks",                // Course 2: Name
                "Networking basics",                // Course 2: Description
                "Y",                                // Course 2: Requires computers
                "Professor Brown",                  // Course 2: Course organiser name
                "brown@university.edu",             // Course 2: Course organiser email
                "Tom Wilson",                       // Course 2: Course secretary name
                "tom@university.edu",               // Course 2: Course secretary email
                "1",                                // Course 2: Required tutorials
                "2",                                // Course 2: Required labs
                "-1",                               // Course 2: No activities
                "0",                                // [0] Add third new course
                "CS203",                            // Course 3: Course code
                "Algorithms",                       // Course 3: Name
                "Algorithm design",                 // Course 3: Description
                "N",                                // Course 3: Doesn't require computers
                "Professor Chen",                   // Course 3: Course organiser name
                "chen@university.edu",              // Course 3: Course organiser email
                "Lisa White",                       // Course 3: Course secretary name
                "lisa@university.edu",              // Course 3: Course secretary email
                "1",                                // Course 3: Required tutorials
                "0",                                // Course 3: Required labs
                "-1",                               // Course 3: No activities
                "-1",                               // Exit course management menu
                "-1"                                // Exit main menu
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


        // Verify all courses exist in the course manager
        assertTrue(context.getCourseManager().hasCourse("CS201"));
        assertTrue(context.getCourseManager().hasCourse("CS202"));
        assertTrue(context.getCourseManager().hasCourse("CS203"));

        // Verify Course 1 properties
        Course course1 = context.getCourseManager().getCourseByCode("CS201");
        assertNotNull(course1);
        assertEquals("Operating Systems", course1.getName());
        assertEquals("OS fundamentals", course1.getDescription());
        assertTrue(course1.isRequiresComputers());
        assertEquals("Professor Adams", course1.getCourseOrganiserName());
        assertEquals("adams@university.edu", course1.getCourseOrganiserEmail());
        assertEquals("Mary Jones", course1.getCourseSecretaryName());
        assertEquals("mary@university.edu", course1.getCourseSecretaryEmail());
        assertEquals(2, course1.getRequiredTutorials());
        assertEquals(1, course1.getRequiredLabs());

        // Verify Course 2 properties
        Course course2 = context.getCourseManager().getCourseByCode("CS202");
        assertNotNull(course2);
        assertEquals("Computer Networks", course2.getName());
        assertEquals("Networking basics", course2.getDescription());
        assertTrue(course2.isRequiresComputers());
        assertEquals("Professor Brown", course2.getCourseOrganiserName());
        assertEquals("brown@university.edu", course2.getCourseOrganiserEmail());
        assertEquals("Tom Wilson", course2.getCourseSecretaryName());
        assertEquals("tom@university.edu", course2.getCourseSecretaryEmail());
        assertEquals(1, course2.getRequiredTutorials());
        assertEquals(2, course2.getRequiredLabs());

        // Verify Course 3 properties
        Course course3 = context.getCourseManager().getCourseByCode("CS203");
        assertNotNull(course3);
        assertEquals("Algorithms", course3.getName());
        assertEquals("Algorithm design", course3.getDescription());
        assertFalse(course3.isRequiresComputers());
        assertEquals("Professor Chen", course3.getCourseOrganiserName());
        assertEquals("chen@university.edu", course3.getCourseOrganiserEmail());
        assertEquals("Lisa White", course3.getCourseSecretaryName());
        assertEquals("lisa@university.edu", course3.getCourseSecretaryEmail());
        assertEquals(1, course3.getRequiredTutorials());
        assertEquals(0, course3.getRequiredLabs());
    }
}