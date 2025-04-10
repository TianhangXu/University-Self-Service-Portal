package system_tests;
import controller.AdminStaffController;
import external.MockAuthenticationService;
import external.MockEmailService;
import model.FAQItem;
import model.FAQSection;
import model.SharedContext;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import view.TextUserInterface;
import view.View;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;
public class AddFAQQASystemTest extends TUITest{


    @Test
    @DisplayName("Test adding a basic FAQ item without course tag")
    public void testAddFAQItemBasic() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "-2",                                // Add FAQ item
                "Course Registration",              // New topic
                "How do I register for courses?",    // Question
                "Visit the portal and select courses", // Answer
                "N",                                 // No course tag
                "-1",                                // Exit FAQ management
                "-1"                                 // Exit main menu
        );

        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        loginAsAdminStaff(context);
        AdminStaffController adminController = new AdminStaffController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );
        startOutputCapture();
        adminController.manageFAQ();

        assertOutputContains("Created topic 'Course Registration'");
        assertOutputContains("The new FAQ item was added");

        FAQSection section = context.getFAQ().getSections().stream()
                .filter(s -> s.hasTopic("Course Registration"))
                .findFirst()
                .orElse(null);
        assertNotNull(section);
        assertNull(section.getParent());
        assertEquals(1, section.getItems().size());
        FAQItem item = section.getItems().get(0);
        assertEquals(0, item.getId());
        assertEquals("How do I register for courses?", item.getQuestion());
        assertEquals("Visit the portal and select courses", item.getAnswer());
        assertFalse(item.hasTag("CS101"));
    }

    @Test
    @DisplayName("Test adding an FAQ item with a course tag")
    public void testAddFAQItemWithCourseTag() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "-2",                                // Add FAQ item
                "Course Issues",                    // New topic
                "What if I miss a lecture?",         // Question
                "Contact your course organiser",     // Answer
                "Y",                                 // Yes to course tag
                "CS101",                             // Course code
                "-1",                                // Exit FAQ management
                "-1"                                 // Exit main menu
        );

        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        context.getCourseManager().addCourse(
                "admin1@university.edu",
                "CS101", "Test Course", "Description", true,
                "Prof", "prof@university.edu",
                "Sec", "sec@university.edu",
                1, 1
        );
        loginAsAdminStaff(context);
        AdminStaffController adminController = new AdminStaffController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );
        startOutputCapture();
        adminController.manageFAQ();

        assertOutputContains("Created topic 'Course Issues'");
        assertOutputContains("The new FAQ item was added");

        FAQSection section = context.getFAQ().getSections().stream()
                .filter(s -> s.hasTopic("Course Issues"))
                .findFirst()
                .orElse(null);
        assertNotNull(section);
        assertEquals(1, section.getItems().size());
        FAQItem item = section.getItems().get(0);
        assertEquals(0, item.getId());
        assertEquals("What if I miss a lecture?", item.getQuestion());
        assertEquals("Contact your course organiser", item.getAnswer());
        assertTrue(item.hasTag("CS101"));
        String taggedItems = section.getItemsByTag("CS101");
        assertTrue(taggedItems.contains("What if I miss a lecture?"));
        assertTrue(taggedItems.contains("Contact your course organiser"));
    }

    @Test
    @DisplayName("Test adding an FAQ item with an empty question")
    public void testAddFAQItemWithEmptyQuestion() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "-2",                                // Add FAQ item
                "Technical Issues",                 // New topic
                "",                                  // Empty question
                "Try restarting",                    // Answer
                "N",                                 // No course tag
                "-1",                                // Exit FAQ management
                "-1"                                 // Exit main menu
        );

        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        loginAsAdminStaff(context);
        AdminStaffController adminController = new AdminStaffController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );
        startOutputCapture();
        adminController.manageFAQ();

        assertOutputContains("Created topic 'Technical Issues'");
        assertOutputContains("The question cannot be empty");

        FAQSection section = context.getFAQ().getSections().stream()
                .filter(s -> s.hasTopic("Technical Issues"))
                .findFirst()
                .orElse(null);
        assertNotNull(section);
        assertEquals(0, section.getItems().size());
    }

    @Test
    @DisplayName("Test adding an FAQ item to an existing section")
    public void testAddFAQItemToExistingSection() throws URISyntaxException, IOException, ParseException {
        setMockInput(

                "0",                                 // Select first section
                "-2",                                // Add FAQ item
                "N",                                 // No to new topic
                "How to reset password?",            // Question
                "Use the forgot password link",      // Answer
                "N",                                 // No course tag
                "-1",                                // Return to FAQ
                "-1",                                // Exit FAQ management
                "-1"                                 // Exit main menu
        );

        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        context.getFAQ().addSection(new FAQSection("Account Management"));
        loginAsAdminStaff(context);
        AdminStaffController adminController = new AdminStaffController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );
        startOutputCapture();
        adminController.manageFAQ();

        assertOutputContains("The new FAQ item was added");

        FAQSection section = context.getFAQ().getSections().get(0);
        assertTrue(section.hasTopic("Account Management"));
        assertNull(section.getParent());
        assertEquals(1, section.getItems().size());
        FAQItem item = section.getItems().get(0);
        assertEquals(0, item.getId());
        assertEquals("How to reset password?", item.getQuestion());
        assertEquals("Use the forgot password link", item.getAnswer());
        assertFalse(item.hasTag("CS101"));
    }

    @Test
    @DisplayName("Test adding an FAQ item with an empty answer")
    public void testAddFAQItemWithEmptyAnswer() throws URISyntaxException, IOException, ParseException {
        setMockInput(

                "-2",                                // Add FAQ item
                "Support Issues",                   // New topic
                "What is the support number?",       // Question
                "",                                  // Empty answer
                "N",                                 // No course tag
                "-1",                                // Exit FAQ management
                "-1"                                 // Exit main menu
        );

        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        loginAsAdminStaff(context);
        AdminStaffController adminController = new AdminStaffController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );
        startOutputCapture();
        adminController.manageFAQ();

        assertOutputContains("Created topic 'Support Issues'");
        assertOutputContains("The answer cannot be empty");

        FAQSection section = context.getFAQ().getSections().stream()
                .filter(s -> s.hasTopic("Support Issues"))
                .findFirst()
                .orElse(null);
        assertNotNull(section);
        assertEquals(0, section.getItems().size());
    }

    @Test
    @DisplayName("Test adding an FAQ item with a duplicate topic")
    public void testAddFAQItemWithDuplicateTopic() throws URISyntaxException, IOException, ParseException {
        setMockInput(

                "-2",                                // Add another FAQ item
                "General",                          // Duplicate topic
                "What is the deadline?",             // Question
                "Check the portal",                  // Answer
                "N",                                 // No course tag
                "-2",                                // Add FAQ item
                "General",                          // New topic
                "Where is the office?",              // Question
                "Building A",                        // Answer
                "N",                                 // No course tag
                "-1",                                // Exit FAQ management
                "-1"                                 // Exit main menu
        );

        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        loginAsAdminStaff(context);
        AdminStaffController adminController = new AdminStaffController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );
        startOutputCapture();
        adminController.manageFAQ();

        assertOutputContains("Created topic 'General'");
        assertOutputContains("Topic 'General' already exists!");
        assertOutputContains("The new FAQ item was added");

        FAQSection section = context.getFAQ().getSections().stream()
                .filter(s -> s.hasTopic("General"))
                .findFirst()
                .orElse(null);
        assertNotNull(section);
        assertEquals(2, section.getItems().size());
        FAQItem item1 = section.getItems().get(0);
        assertEquals(0, item1.getId());
        assertEquals("What is the deadline?", item1.getQuestion());
        FAQItem item2 = section.getItems().get(1);
        assertEquals(1, item2.getId());
        assertEquals("Where is the office?", item2.getQuestion());
    }

    @Test
    @DisplayName("Test adding an FAQ item with no available courses for tagging")
    public void testAddFAQItemWithNoAvailableCourse() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "-2",                                // Add FAQ item
                "Course Problems",                  // New topic
                "How to drop a course?",             // Question
                "Submit a request",                  // Answer
                "Y",                                 // Yes to course tag
                "DATA101",
                "-1",                                // Exit FAQ management
                "-1"                                 // Exit main menu
        );

        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        loginAsAdminStaff(context);
        AdminStaffController adminController = new AdminStaffController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        startOutputCapture();
        adminController.manageFAQ();

        assertOutputContains("Created topic 'Course Problems'");
        assertOutputContains("No courses available in the system");

        FAQSection section = context.getFAQ().getSections().stream()
                .filter(s -> s.hasTopic("Course Problems"))
                .findFirst()
                .orElse(null);
        assertNotNull(section);
        assertEquals(1, section.getItems().size());
    }

    @Test
    @DisplayName("Test adding an FAQ item with an invalid course tag")
    public void testAddFAQItemWithInvalidCourseTag() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "-2",                                // Add FAQ item
                "Course Problems",                  // New topic
                "How to drop a course?",             // Question
                "Submit a request",                  // Answer
                "Y",                                 // Yes to course tag
                "INVALID101",                        // Invalid course code
                "-1",                                // Exit FAQ management
                "-1"                                 // Exit main menu
        );

        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        loginAsAdminStaff(context);
        AdminStaffController adminController = new AdminStaffController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );
        context.getCourseManager().addCourse(
                "admin1@university.edu",
                "CS101", "Test Course", "Description", true,
                "Prof", "prof@university.edu",
                "Sec", "sec@university.edu",
                1, 1
        );
        startOutputCapture();
        adminController.manageFAQ();

        assertOutputContains("Created topic 'Course Problems'");
        assertOutputContains("The tag must correspond to a course code");

        FAQSection section = context.getFAQ().getSections().stream()
                .filter(s -> s.hasTopic("Course Problems"))
                .findFirst()
                .orElse(null);
        assertNotNull(section);
        assertEquals(0, section.getItems().size());
    }

    @Test
    @DisplayName("Test adding an FAQ item to a subsection")
    public void testAddFAQItemToSubsection() throws URISyntaxException, IOException, ParseException {
        setMockInput(

                "0",                                 // Select first section
                "-2",                                // Add FAQ item
                "Y",                                 // Yes to create new subtopic
                "Subtopic 1",                       // New subtopic
                "How to access materials?",          // Question
                "Via the online portal",             // Answer
                "N",                                 // No course tag
                "-1",                                // Return to parent
                "-1",                                // Exit FAQ management
                "-1"                                 // Exit main menu
        );

        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        FAQSection parentSection = new FAQSection("Learning Resources");
        context.getFAQ().addSection(parentSection);
        loginAsAdminStaff(context);
        AdminStaffController adminController = new AdminStaffController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );
        startOutputCapture();
        adminController.manageFAQ();

        assertOutputContains("Created topic 'Subtopic 1' under 'Learning Resources'");
        assertOutputContains("The new FAQ item was added");

        FAQSection parent = context.getFAQ().getSections().get(0);
        assertTrue(parent.hasTopic("Learning Resources"));
        assertEquals(1, parent.getSubsections().size());
        FAQSection subsection = parent.getSubsections().stream()
                .filter(s -> s.hasTopic("Subtopic 1"))
                .findFirst()
                .orElse(null);
        assertNotNull(subsection);
        assertEquals(parent, subsection.getParent());
        assertEquals(1, subsection.getItems().size());
        FAQItem item = subsection.getItems().get(0);
        assertEquals(0, item.getId());
        assertEquals("How to access materials?", item.getQuestion());
        assertEquals("Via the online portal", item.getAnswer());
    }

    @Test
    @DisplayName("Test adding multiple FAQ items to the same section")
    public void testAddMultipleFAQItems() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "-2",                                // Add first FAQ item
                "Student Services",                 // New topic
                "How do I get a student ID?",        // First question
                "Visit the admin office",            // First answer
                "N",                                 // No course tag for first item
                "0",                                 // Choose one subSection
                "-2",                                // Add second FAQ item
                "N",                                 // No to new topic (use existing)
                "Where is the library?",             // Second question
                "Building B, 2nd floor",             // Second answer
                "N",                                 // No course tag for second item
                "-1",                                // Exit FAQ management
                "-1"                                 // Exit main menu
        );

        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        loginAsAdminStaff(context);
        AdminStaffController adminController = new AdminStaffController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );
        startOutputCapture();
        adminController.manageFAQ();

        // Verify output
        assertOutputContains("Created topic 'Student Services'");
        assertOutputContains("The new FAQ item was added"); // Should appear twice

        // Verify the FAQ section and items
        FAQSection section = context.getFAQ().getSections().stream()
                .filter(s -> s.hasTopic("Student Services"))
                .findFirst()
                .orElse(null);
        assertNotNull(section, "FAQ section 'Student Services' should exist");
        assertNull(section.getParent(), "Section should have no parent");
        assertEquals(2, section.getItems().size(), "Section should contain 2 FAQ items");

        // Verify first FAQ item
        FAQItem item1 = section.getItems().get(0);
        assertEquals(0, item1.getId(), "First item ID should be 0");
        assertEquals("How do I get a student ID?", item1.getQuestion(), "First item question mismatch");
        assertEquals("Visit the admin office", item1.getAnswer(), "First item answer mismatch");
        assertFalse(item1.hasTag("CS101"), "First item should have no course tag");

        // Verify second FAQ item
        FAQItem item2 = section.getItems().get(1);
        assertEquals(1, item2.getId(), "Second item ID should be 1");
        assertEquals("Where is the library?", item2.getQuestion(), "Second item question mismatch");
        assertEquals("Building B, 2nd floor", item2.getAnswer(), "Second item answer mismatch");
        assertFalse(item2.hasTag("CS101"), "Second item should have no course tag");
    }

    @Test
    @DisplayName("Test adding FAQ items across multiple sections")
    public void testAddFAQItemsAcrossMultipleSubsections() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "-2",                                // Add first FAQ item
                "Student Services",                 // First topic
                "How do I get a student ID?",        // First question
                "Visit the admin office",            // First answer
                "N",                                 // No course tag for first item
                "-2",                                // Add second FAQ item
                "Academic Support",                 // Second topic
                "When are final exams?",             // Second question
                "Check the academic calendar",       // Second answer
                "N",                                 // No course tag for second item
                "-1",                                // Exit FAQ management
                "-1"                                 // Exit main menu
        );

        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        loginAsAdminStaff(context);
        AdminStaffController adminController = new AdminStaffController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );
        startOutputCapture();
        adminController.manageFAQ();

        // Verify output
        assertOutputContains("Created topic 'Student Services'");
        assertOutputContains("Created topic 'Academic Support'");
        assertOutputContains("The new FAQ item was added"); // Should appear twice

        // Verify the "Student Services" section
        FAQSection studentServicesSection = context.getFAQ().getSections().stream()
                .filter(s -> s.hasTopic("Student Services"))
                .findFirst()
                .orElse(null);
        assertNotNull(studentServicesSection, "FAQ section 'Student Services' should exist");
        assertNull(studentServicesSection.getParent(), "Student Services should have no parent");
        assertEquals(1, studentServicesSection.getItems().size(), "Student Services should contain 1 FAQ item");

        FAQItem studentItem = studentServicesSection.getItems().get(0);
        assertEquals(0, studentItem.getId(), "Student Services item ID should be 0");
        assertEquals("How do I get a student ID?", studentItem.getQuestion(), "Student Services question mismatch");
        assertEquals("Visit the admin office", studentItem.getAnswer(), "Student Services answer mismatch");
        assertFalse(studentItem.hasTag("CS101"), "Student Services item should have no course tag");

        FAQSection academicSupportSection = context.getFAQ().getSections().stream()
                .filter(s -> s.hasTopic("Academic Support"))
                .findFirst()
                .orElse(null);
        assertNotNull(academicSupportSection, "FAQ section 'Academic Support' should exist");
        assertNull(academicSupportSection.getParent(), "Academic Support should have no parent");
        assertEquals(1, academicSupportSection.getItems().size(), "Academic Support should contain no direct FAQ items");
    }


}
