package system_tests;
import controller.AdminStaffController;
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

import static org.junit.jupiter.api.Assertions.*;
public class RemoveFAQQASystemTest extends TUITest{

    @Test
    @DisplayName("Test removing an entire FAQ section")
    public void testRemoveEntireFAQSection() throws URISyntaxException, IOException, ParseException {
        // Set up mock input to simulate user actions: select section, remove entire section, confirm, and exit
        setMockInput(
                "0",           // Select the first section
                "-3",          // Choose to remove FAQ item/section
                "Y",           // Opt to remove the entire section
                "Y",           // Confirm the removal
                "-1",          // Exit FAQ management
                "-1"           // Exit main menu
        );

        // Initialize test environment: create a view, context, and a section with one FAQ item
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        FAQSection section = new FAQSection("General Info");
        section.addItem("What is this?", "A FAQ");
        context.getFAQ().addSection(section);
        loginAsAdminStaff(context); // Log in as admin staff
        AdminStaffController adminController = new AdminStaffController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        // Start capturing console output for assertions
        startOutputCapture();
        adminController.manageFAQ(); // Execute the FAQ management process

        // Verify that the entire section was removed successfully
        assertOutputContains("FAQ section 'General Info' and all its contents have been removed.");
        assertTrue(context.getFAQ().getSections().isEmpty(), "FAQ sections list should be empty after removal");
    }

    @Test
    @DisplayName("Test removing a single FAQ item from a section")
    public void testRemoveSingleFAQItem() throws URISyntaxException, IOException, ParseException {
        // Set up mock input: select section, remove single item, choose item 0, confirm, and exit
        setMockInput(
                "0",           // Select the first section
                "-3",          // Choose to remove FAQ item/section
                "N",           // Opt to remove a single item
                "0",           // Select the first FAQ item
                "Y",           // Confirm the removal
                "-1",          // Exit FAQ management
                "-1"           // Exit main menu
        );

        // Initialize test environment with a section containing one FAQ item
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        FAQSection section = new FAQSection("General Info");
        section.addItem("What is this?", "A FAQ");
        context.getFAQ().addSection(section);
        loginAsAdminStaff(context);
        AdminStaffController adminController = new AdminStaffController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        // Capture output and run the FAQ management
        startOutputCapture();
        adminController.manageFAQ();

        // Verify that only the single FAQ item was removed, but the section remains
        assertOutputContains("FAQ item 'What is this?' has been removed from section 'General Info'.");
        assertTrue(context.getFAQ().getSections().get(0).getItems().isEmpty(), "Section should still exist, but its items list should be empty");
    }

    @Test
    @DisplayName("Test cancelling the removal of an entire FAQ section")
    public void testCancelRemoveEntireSection() throws URISyntaxException, IOException, ParseException {
        // Set up mock input: select section, attempt to remove entire section, cancel, and exit
        setMockInput(
                "0",           // Select the first section
                "-3",          // Choose to remove FAQ item/section
                "Y",           // Opt to remove the entire section
                "N",           // Cancel the removal
                "-1",          // Exit FAQ management
                "-1"           // Exit main menu
        );

        // Initialize test environment with a section and one FAQ item
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        FAQSection section = new FAQSection("Policies");
        section.addItem("What is the policy?", "Read the handbook");
        context.getFAQ().addSection(section);
        loginAsAdminStaff(context);
        AdminStaffController adminController = new AdminStaffController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        // Capture output and run the process
        startOutputCapture();
        adminController.manageFAQ();

        // Verify that the operation was canceled and the section remains intact
        assertOutputContains("Operation cancelled.");
        assertFalse(context.getFAQ().getSections().isEmpty(), "FAQ sections list should not be empty");
        FAQSection remainingSection = context.getFAQ().getSections().get(0);
        assertTrue(remainingSection.hasTopic("Policies"), "Section 'Policies' should still exist");
        assertEquals(1, remainingSection.getItems().size(), "Section should still have 1 item");
    }

    @Test
    @DisplayName("Test cancelling the removal of a single FAQ item")
    public void testCancelRemoveSingleItem() throws URISyntaxException, IOException, ParseException {
        // Set up mock input: select section, attempt to remove single item, cancel, and exit
        setMockInput(
                "0",           // Select the first section
                "-3",          // Choose to remove FAQ item/section
                "N",           // Opt to remove a single item
                "0",           // Select the first FAQ item
                "N",           // Cancel the removal
                "-1",          // Exit FAQ management
                "-1"           // Exit main menu
        );

        // Initialize test environment with a section and one FAQ item
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        FAQSection section = new FAQSection("Support");
        section.addItem("How to fix?", "Restart");
        context.getFAQ().addSection(section);
        loginAsAdminStaff(context);
        AdminStaffController adminController = new AdminStaffController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        // Capture output and run the process
        startOutputCapture();
        adminController.manageFAQ();

        // Verify that the operation was canceled and the item remains
        assertOutputContains("Operation cancelled.");
        assertEquals(1, context.getFAQ().getSections().get(0).getItems().size(),
                "Section should still have 1 item after cancellation");
    }

    @Test
    @DisplayName("Test attempting to remove an item from an empty FAQ section")
    public void testRemoveItemFromEmptySection() throws URISyntaxException, IOException, ParseException {
        // Set up mock input: select empty section, attempt to remove single item, and exit
        setMockInput(
                "0",           // Select the first section
                "-3",          // Choose to remove FAQ item/section
                "N",           // Opt to remove a single item
                "-1",          // Exit FAQ management (no items to select)
                "-1"           // Exit main menu
        );

        // Initialize test environment with an empty section
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        FAQSection section = new FAQSection("Empty Section");
        context.getFAQ().addSection(section);
        loginAsAdminStaff(context);
        AdminStaffController adminController = new AdminStaffController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        // Capture output and run the process
        startOutputCapture();
        adminController.manageFAQ();

        // Verify that an error is shown and the section remains
        assertOutputContains("No FAQ items available to remove in this section.");
        assertFalse(context.getFAQ().getSections().isEmpty(), "FAQ sections list should not be empty");
    }

    @Test
    @DisplayName("Test removing an FAQ item with an invalid item index")
    public void testInvalidItemIndex() throws URISyntaxException, IOException, ParseException {
        // Set up mock input: select section, attempt to remove single item with invalid index, and exit
        setMockInput(
                "0",           // Select the first section
                "-3",          // Choose to remove FAQ item/section
                "N",           // Opt to remove a single item
                "5",           // Invalid index (out of range)
                "-1",          // Exit FAQ management
                "-1"           // Exit main menu
        );

        // Initialize test environment with a section and one FAQ item
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        FAQSection section = new FAQSection("Support");
        section.addItem("How to fix?", "Restart");
        context.getFAQ().addSection(section);
        loginAsAdminStaff(context);
        AdminStaffController adminController = new AdminStaffController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        // Capture output and run the process
        startOutputCapture();
        adminController.manageFAQ();

        // Verify that an error is shown for invalid index and the item remains
        assertOutputContains("Invalid item number: 5");
        assertEquals(1, context.getFAQ().getSections().get(0).getItems().size(),
                "Section should still have 1 item after invalid input");
    }
    @Test
    @DisplayName("Test removing the last FAQ item and promoting subtopics")
    public void testRemoveLastFAQItemAndPromoteSubtopics() throws URISyntaxException, IOException, ParseException {
        // Set up mock input: select section, remove single item, choose item 0, confirm, and exit
        setMockInput(
                "0",           // Select the first section
                "-3",          // Choose to remove FAQ item/section
                "N",           // Opt to remove a single item
                "0",           // Select the first (and only) FAQ item
                "Y",           // Confirm the removal
                "-1",          // Exit FAQ management
                "-1"           // Exit main menu
        );

        // Initialize test environment with a section containing one FAQ item and a subsection
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        FAQSection parentSection = new FAQSection("Parent Topic");
        parentSection.addItem("What is this?", "A test FAQ"); // Single FAQ item
        FAQSection subSection = new FAQSection("Sub Topic");
        subSection.addItem("Sub question?", "Sub answer");
        parentSection.addSubsection(subSection); // Add subsection to parent
        context.getFAQ().addSection(parentSection);
        loginAsAdminStaff(context);
        AdminStaffController adminController = new AdminStaffController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        // Capture output and run the FAQ management
        startOutputCapture();
        adminController.manageFAQ();

        // Verify that the FAQ item and parent topic were removed, and the subsection was promoted
        assertOutputContains("FAQ item removed from 'Parent Topic'");
        assertOutputContains("The FAQ item 'What is this?' has been removed from the section 'Parent Topic'.");

        // Check the FAQ structure
        assertEquals(1, context.getFAQ().getSections().size(), "FAQ should have one top-level section after promotion");
        FAQSection promotedSection = context.getFAQ().getSections().get(0);

        assertNull(promotedSection.getParent(), "Promoted section should have no parent");
    }

    @Test
    @DisplayName("Test removing multiple FAQ items from a section")
    public void testRemoveMultipleFAQItems() throws URISyntaxException, IOException, ParseException {
        // Set up mock input: select section, remove two FAQ items, confirm each, and exit
        setMockInput(
                "0",           // Select the first section
                "-3",          // Choose to remove FAQ item/section
                "N",           // Opt to remove a single item
                "0",           // Select the first FAQ item
                "Y",           // Confirm removal of first item
                "-3",          // Choose to remove another FAQ item/section
                "N",           // Opt to remove a single item
                "0",           // Select the new first FAQ item (after first removal)
                "Y",           // Confirm removal of second item
                "-1",          // Exit FAQ management
                "-1"           // Exit main menu
        );

        // Initialize test environment with a section containing three FAQ items
        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        FAQSection section = new FAQSection("General Info");
        section.addItem("What is this?", "A FAQ");
        section.addItem("Where is it?", "Over there");
        section.addItem("When does it start?", "Soon");
        context.getFAQ().addSection(section);
        loginAsAdminStaff(context);
        AdminStaffController adminController = new AdminStaffController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        // Capture output and run the FAQ management
        startOutputCapture();
        adminController.manageFAQ();

        // Verify output
        assertOutputContains("FAQ item 'What is this?' has been removed from section 'General Info'.");
        assertOutputContains("FAQ item 'Where is it?' has been removed from section 'General Info'.");

        // Verify the FAQ section state
        FAQSection updatedSection = context.getFAQ().getSections().get(0);
        assertEquals(1, updatedSection.getItems().size(), "Section should have 1 remaining FAQ item");
        assertEquals("When does it start?", updatedSection.getItems().get(0).getQuestion(),
                "Remaining item should be the third one");
        assertEquals("Soon", updatedSection.getItems().get(0).getAnswer(),
                "Remaining item answer should match");
    }

    @Test
    @DisplayName("Test removing multiple FAQ sections")
    public void testRemoveMultipleFAQSections() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "0",           // Select the first section
                "-3",          // Choose to remove FAQ item/section
                "Y",           // Opt to remove the entire section
                "Y",           // Confirm removal of first section
                "-1",          // Return to the ManageFAQ Menu
                "0",           // Select the new first section (after first removal)
                "-3",          // Choose to remove FAQ item/section
                "Y",           // Opt to remove the entire section
                "Y",           // Confirm removal of second section
                "-1",          // Exit FAQ management
                "-1"           // Exit main menu
        );

        View view = new TextUserInterface();
        SharedContext context = new SharedContext(view);
        FAQSection section1 = new FAQSection("Section 1");
        section1.addItem("Q1?", "A1");
        FAQSection section2 = new FAQSection("Section 2");
        section2.addItem("Q2?", "A2");
        context.getFAQ().addSection(section1);
        context.getFAQ().addSection(section2);
        loginAsAdminStaff(context);
        AdminStaffController adminController = new AdminStaffController(
                context, view, new MockAuthenticationService(), new MockEmailService()
        );

        startOutputCapture();
        adminController.manageFAQ();

        assertOutputContains("FAQ section 'Section 1' and all its contents have been removed.");
        assertOutputContains("FAQ section 'Section 2' and all its contents have been removed.");
        assertTrue(context.getFAQ().getSections().isEmpty(), "All FAQ sections should be removed");
    }
}
