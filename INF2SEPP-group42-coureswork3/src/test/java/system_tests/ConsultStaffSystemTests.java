package system_tests;

import controller.InquirerController;
import external.MockAuthenticationService;
import external.MockEmailService;
import model.AuthenticatedUser;
import model.SharedContext;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import view.TextUserInterface;
import view.View;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

public class ConsultStaffSystemTests extends TUITest {
    @Test
    @DisplayName("Test contacting staff as a guest with successful submission")
    public void testContactStaffAsGuestSuccess() throws URISyntaxException, IOException, ParseException {
        // Set up mock inputs for contacting staff
        setMockInput(
                "user@example.com",           // Inquirer email
                "Technical Issue",           // Subject
                "Having trouble logging in",  // Inquiry content
                "n"                          // Don't specify course code
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        InquirerController inquirerController = new InquirerController(context, view, new MockAuthenticationService(), new MockEmailService());

        // Start capturing output
        startOutputCapture();

        // Execute contact staff
        inquirerController.contactStaff();

        // Verify success message and inquiry recording
        assertOutputContains("Your inquiry has been recorded. Someone will be in touch via email soon!");
        assertEquals(1, context.inquiries.size());

        // Verify inquiry details using correct method names
        assertEquals("user@example.com", context.inquiries.get(0).getInquirerEmail());
        assertEquals("Technical Issue", context.inquiries.get(0).getSubject());
        assertEquals("Having trouble logging in", context.inquiries.get(0).getContent());
        assertNull(context.inquiries.get(0).getCourseCode());
    }

    @Test
    @DisplayName("Test contacting staff with a course code")
    public void testContactStaffWithCourseCode() throws URISyntaxException, IOException, ParseException {
        // Set up mock inputs for contacting staff with course code
        setMockInput(
                "user@example.com",           // Inquirer email
                "Assignment Question",       // Subject
                "How do I submit assignment 2?",  // Inquiry content
                "y",                         // Yes, specify course code
                "CS101"                      // Course code
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        InquirerController inquirerController = new InquirerController(context, view, new MockAuthenticationService(), new MockEmailService());

        // Start capturing output
        startOutputCapture();

        // Execute contact staff
        inquirerController.contactStaff();

        // Verify success message and inquiry recording
        assertOutputContains("Your inquiry has been recorded. Someone will be in touch via email soon!");
        assertEquals(1, context.inquiries.size());

        // Verify inquiry details
        assertEquals("user@example.com", context.inquiries.get(0).getInquirerEmail());
        assertEquals("Assignment Question", context.inquiries.get(0).getSubject());
        assertEquals("How do I submit assignment 2?", context.inquiries.get(0).getContent());
        assertEquals("CS101", context.inquiries.get(0).getCourseCode());
    }

    @Test
    @DisplayName("Test contacting staff with an invalid email")
    public void testContactStaffInvalidEmail() throws URISyntaxException, IOException, ParseException {
        // Set up mock inputs with invalid email
        setMockInput(
                "invalid-email",             // Invalid email format
                "Technical Issue",          // Subject (won't reach this)
                "Having trouble logging in" // Content (won't reach this)
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        InquirerController inquirerController = new InquirerController(context, view, new MockAuthenticationService(), new MockEmailService());

        // Start capturing output
        startOutputCapture();

        // Execute contact staff
        inquirerController.contactStaff();

        // Verify error message and no inquiry recorded
        assertOutputContains("Invalid email address! Please try again");
        assertEquals(0, context.inquiries.size());
    }

    @Test
    @DisplayName("Test contacting staff with a blank subject")
    public void testContactStaffBlankSubject() throws URISyntaxException, IOException, ParseException {
        // Set up mock inputs with blank subject
        setMockInput(
                "user@example.com",         // Inquirer email
                "   ",                     // Blank subject
                "Having trouble logging in" // Content (won't reach this)
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        InquirerController inquirerController = new InquirerController(context, view, new MockAuthenticationService(), new MockEmailService());

        // Start capturing output
        startOutputCapture();

        // Execute contact staff
        inquirerController.contactStaff();

        // Verify error message and no inquiry recorded
        assertOutputContains("Inquiry subject cannot be blank! Please try again");
        assertEquals(0, context.inquiries.size());
    }

    @Test
    @DisplayName("Test contacting staff with blank content")
    public void testContactStaffBlankText() throws URISyntaxException, IOException, ParseException {
        // Set up mock inputs with blank content
        setMockInput(
                "user@example.com",  // Inquirer email
                "Technical Issue",  // Subject
                "   "              // Blank content
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        InquirerController inquirerController = new InquirerController(context, view, new MockAuthenticationService(), new MockEmailService());

        // Start capturing output
        startOutputCapture();

        // Execute contact staff
        inquirerController.contactStaff();

        // Verify error message and no inquiry recorded
        assertOutputContains("Inquiry content cannot be blank! Please try again");
        assertEquals(0, context.inquiries.size());
    }

    @Test
    @DisplayName("Test contacting staff as an authenticated user")
    public void testContactStaffAsAuthenticatedUser() throws URISyntaxException, IOException, ParseException {
        // Set up mock inputs for authenticated user (no email input needed)
        setMockInput(
                "Login Problems",           // Subject
                "Cannot access my account", // Inquiry content
                "n"                         // Don't specify course code
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);

        // Login as admin to set authenticated user
        loginAsAdminStaff(context);

        InquirerController inquirerController = new InquirerController(context, view, new MockAuthenticationService(), new MockEmailService());

        // Start capturing output
        startOutputCapture();

        // Execute contact staff
        inquirerController.contactStaff();

        // Verify success message and inquiry recording
        assertOutputContains("Your inquiry has been recorded. Someone will be in touch via email soon!");
        assertEquals(1, context.inquiries.size());

        // Verify inquiry uses authenticated user's email
        assertNotNull(context.currentUser);
        assertEquals(((AuthenticatedUser)context.currentUser).getEmail(), context.inquiries.get(0).getInquirerEmail());
        assertEquals("Login Problems", context.inquiries.get(0).getSubject());
        assertEquals("Cannot access my account", context.inquiries.get(0).getContent());
        assertNull(context.inquiries.get(0).getCourseCode());
    }

    @Test
    @DisplayName("Test contacting staff as an authenticated user with a course code")
    public void testContactStaffAsAuthenticatedUserWithCourseCode() throws URISyntaxException, IOException, ParseException {
        // Set up mock inputs for authenticated user with course code
        setMockInput(
                "Assignment Help",           // Subject
                "Need help with lab 3",      // Inquiry content
                "y",                         // Yes, specify course code
                "CS202"                      // Course code
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);

        // Login as student to set authenticated user
        loginAsStudent(context);

        InquirerController inquirerController = new InquirerController(context, view, new MockAuthenticationService(), new MockEmailService());

        // Start capturing output
        startOutputCapture();

        // Execute contact staff
        inquirerController.contactStaff();

        // Verify success message and inquiry recording
        assertOutputContains("Your inquiry has been recorded. Someone will be in touch via email soon!");
        assertEquals(1, context.inquiries.size());

        // Verify inquiry details
        assertNotNull(context.currentUser);
        assertEquals(((AuthenticatedUser)context.currentUser).getEmail(), context.inquiries.get(0).getInquirerEmail());
        assertEquals("Assignment Help", context.inquiries.get(0).getSubject());
        assertEquals("Need help with lab 3", context.inquiries.get(0).getContent());
        assertEquals("CS202", context.inquiries.get(0).getCourseCode());
    }
}