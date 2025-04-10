package controller;

import external.AuthenticationService;
import external.EmailService;
import model.*;
import view.View;

/**
 * Controller for inquirers. Provides functionality for browsing FAQs and contacting staff.
 */
public class InquirerController extends Controller {
    /**
     * Instantiates a new inquirer controller.
     *
     * @param sharedContext the shared context
     * @param view          the user interface view
     * @param auth          the authentication service
     * @param email         the email of the user
     */
    public InquirerController(SharedContext sharedContext, View view, AuthenticationService auth, EmailService email) {
        super(sharedContext, view, auth, email);
    }

    /**
     * Displays and manages menu for browsing the FAQ.
     * Allows filtering by course code according to R13.
     */
    public void consultFAQ() {
        FAQSection currentSection = null;
        int optionNo = 0;

        // Implement R13.b - Ask if they want to filter by course code
        String courseCode = null;
        String filterChoice = view.getInput("Would you like to filter FAQs by course code? (y/n): ");
        if (filterChoice.equalsIgnoreCase("y") || filterChoice.equalsIgnoreCase("yes")) {
            courseCode = view.getInput("Enter the course code: ");
            CourseManager courseManager = sharedContext.getCourseManager(); 
            if (courseManager != null && !courseManager.hasCourse(courseCode)) {
                view.displayWarning("Course code '" + courseCode + "' not found in the system. Showing all available FAQs instead.");
                courseCode = null; // Reset course code if invalid
            } else {
                view.displayInfo("Showing FAQs for course: " + courseCode);
            }
        }

        while (currentSection != null || optionNo != -1) {
            if (currentSection == null) {
                if (courseCode == null) {
                    // Normal FAQ display
                    view.displayFAQ(sharedContext.getFAQ());
                } else {
                    // Display FAQ with course code filter
                    view.displayFilteredFAQ(sharedContext.getFAQ(), courseCode);
                }
                view.displayInfo("[-1] Return to main menu");
            } else {
                if (courseCode == null) {
                    // Normal section display
                    view.displayFAQSection(currentSection);
                } else {
                    // Display section with course code filter
                    view.displayFilteredFAQSection(currentSection, courseCode);
                }
                view.displayInfo("[-1] Return to " + (currentSection.getParent() == null ? "FAQ" : currentSection.getParent().getTopic()));
            }

            String input = view.getInput("Please choose an option: ");

            try {
                optionNo = Integer.parseInt(input);

                if (optionNo != -1) {
                    try {
                        if (currentSection == null) {
                            currentSection = sharedContext.getFAQ().getSections().get(optionNo);
                        } else {
                            currentSection = currentSection.getSubsections().get(optionNo);
                        }
                    } catch (IndexOutOfBoundsException e) {
                        view.displayError("Invalid option: " + optionNo);
                    }
                }

                if (currentSection != null && optionNo == -1) {
                    currentSection = currentSection.getParent();
                    optionNo = 0;
                }
            } catch (NumberFormatException e) {
                view.displayError("Invalid option: " + input);
            }
        }
    }

    /**
     * Displays and manages menu for sending an {@link Inquiry} to staff.
     * Implements R15 allowing inquirers to specify a course code.
     */
    public void contactStaff() {
        String inquirerEmail;
        if (sharedContext.currentUser instanceof AuthenticatedUser) {
            AuthenticatedUser user = (AuthenticatedUser) sharedContext.currentUser;
            inquirerEmail = user.getEmail();
        } else {
            inquirerEmail = view.getInput("Enter your email address: ");
            // From https://owasp.org/www-community/OWASP_Validation_Regex_Repository
            if (!inquirerEmail.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")) {
                view.displayError("Invalid email address! Please try again");
                return;
            }
        }

        String subject = view.getInput("Describe the topic of your inquiry in a few words: ");
        if (subject.strip().isBlank()) {
            view.displayError("Inquiry subject cannot be blank! Please try again");
            return;
        }

        String text = view.getInput("Write your inquiry:" + System.lineSeparator());
        if (text.strip().isBlank()) {
            view.displayError("Inquiry content cannot be blank! Please try again");
            return;
        }

        // Implement R15.a - Allow users to provide an optional course code
        String provideCourseCode = view.getInput("Would you like to specify a course code for this inquiry? (y/n): ");
        String courseCode = null;
        if (provideCourseCode.equalsIgnoreCase("y") || provideCourseCode.equalsIgnoreCase("yes")) {
            courseCode = view.getInput("Enter the course code: ");
        }

        // Create inquiry with optional course code
        Inquiry inquiry = new Inquiry(inquirerEmail, subject, text, courseCode);
        sharedContext.inquiries.add(inquiry);

        String staffEmail;
        String emailSubject;
        String emailBody;

        // Implement R15.b and R15.c - Email routing based on course code
        if (courseCode == null || courseCode.strip().isBlank()) {
            // No course code provided, contact admin staff (R15.b)
            staffEmail = SharedContext.ADMIN_STAFF_EMAIL;
            emailSubject = "New inquiry from " + inquirerEmail;
            emailBody = "Subject: " + subject + System.lineSeparator() +
                    "Please log into the Self Service Portal to review and respond to the inquiry.";
        } else {
            // Course code provided, contact teaching staff for that course (R15.c)
            staffEmail = sharedContext.getTeachingStaffEmailForCourse(courseCode);
            emailSubject = "New course-related inquiry: " + courseCode;
            emailBody = "Subject: " + subject + System.lineSeparator() +
                    "Course: " + courseCode + System.lineSeparator() +
                    "Please log into the Self Service Portal to review and respond to the inquiry.";
        }

        email.sendEmail(
                staffEmail,
                SharedContext.ADMIN_STAFF_EMAIL, // CC the admin staff
                emailSubject,
                emailBody
        );

        view.displaySuccess("Your inquiry has been recorded. Someone will be in touch via email soon!");
    }
}