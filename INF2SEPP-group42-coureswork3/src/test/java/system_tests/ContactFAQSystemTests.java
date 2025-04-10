package system_tests;
import controller.InquirerController;
import external.MockAuthenticationService;
import external.MockEmailService;
import model.FAQSection;
import model.SharedContext;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import view.TextUserInterface;
import view.View;

import java.io.IOException;
import java.net.URISyntaxException;

public class ContactFAQSystemTests extends TUITest{
    @Test
    @DisplayName("Test navigating FAQ sections and subsections")
    public void testConsultFAQNavigation() throws URISyntaxException, IOException, ParseException {
        // Set up mock inputs for FAQ navigation
        setMockInput(
                "n",    // Don't filter by course code
                "0",    // Select first FAQ section
                "0",    // Select first subsection
                "-1",   // Go back to parent section
                "-1"    // Return to main menu
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);

        // Add FAQ structure to existing FAQ object
        FAQSection section = new FAQSection("General Questions");
        FAQSection subsection = new FAQSection("Technical Support");
        section.getSubsections().add(subsection);
        context.getFAQ().getSections().add(section);

        InquirerController inquirerController = new InquirerController(context, view, new MockAuthenticationService(), new MockEmailService());

        // Start capturing output
        startOutputCapture();

        // Execute FAQ consultation
        inquirerController.consultFAQ();

        // Verify output contains expected messages
        assertOutputContains("General Questions");  // Main section
        assertOutputContains("Technical Support");  // Subsection
        assertOutputContains("Return to FAQ");      // Navigation option
    }

    @Test
    @DisplayName("Test consulting FAQ with a course code filter")
    public void testConsultFAQWithCourseFilter() throws URISyntaxException, IOException, ParseException {
        // Set up mock inputs for FAQ navigation with course filter
        setMockInput(
                "y",        // Yes, filter by course code
                "CS101",    // Course code
                "-1",
                "-1"        // Return to main menu
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        context.getCourseManager().addCourse(
                "admin1@university.edu", "CS101", "Intro to CS", "Basic course",
                true, "Prof A", "prof@university.edu", "Sec B", "sec@university.edu",
                1, 1
        );

        // Add FAQ structure with course-specific content
        FAQSection section = new FAQSection("Course FAQs");
        context.getFAQ().getSections().add(section);

        InquirerController inquirerController = new InquirerController(context, view, new MockAuthenticationService(), new MockEmailService());

        // Start capturing output
        startOutputCapture();

        // Execute FAQ consultation
        inquirerController.consultFAQ();

        // Verify output contains expected messages
        assertOutputContains("Showing FAQs for course: CS101");
        assertOutputContains("Course FAQs");
    }
    @Test
    @DisplayName("Test consulting FAQ with invalid input options")
    public void testConsultFAQInvalidInput() throws URISyntaxException, IOException, ParseException {
        // Set up mock inputs with invalid option
        setMockInput(
                "n",     // Don't filter by course code
                "999",   // Invalid section number
                "abc",   // Invalid input
                "-1"     // Return to main menu
        );

        // Setup
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);

        // Add a sample section to existing FAQ
        context.getFAQ().getSections().add(new FAQSection("General Questions"));

        InquirerController inquirerController = new InquirerController(context, view, new MockAuthenticationService(), new MockEmailService());

        // Start capturing output
        startOutputCapture();

        // Execute FAQ consultation
        inquirerController.consultFAQ();

        // Verify error messages for invalid inputs
        assertOutputContains("Invalid option: 999");
        assertOutputContains("Invalid option: abc");
    }
}
