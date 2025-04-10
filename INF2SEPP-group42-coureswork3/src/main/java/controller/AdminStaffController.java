package controller;

import external.AuthenticationService;
import external.EmailService;
import model.*;
import view.View;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for admin staff. Provides functionality for management of FAQs, inquiries and courses.
 */
public class AdminStaffController extends StaffController {
    private final CourseManager courseManager;

    /**
     * Instantiates a new Admin staff controller.
     *
     * @param sharedContext the shared context
     * @param view          the user interface view
     * @param auth          the authentication service
     * @param email         the email of the user
     */
    public AdminStaffController(
            SharedContext sharedContext, View view, AuthenticationService auth, EmailService email) {
        super(sharedContext, view, auth, email);
        this.courseManager = sharedContext.getCourseManager();
    }


    /**
     * Displays and manages the menu that allows AdminStaff to browse, add or remove FAQ items and sections.
     */
    public void manageFAQ() {
        FAQSection currentSection = null;

        while (true) {
            if (currentSection == null) {
                view.displayFAQ(sharedContext.getFAQ());
                view.displayInfo("[-1] Return to main menu");
            } else {
                view.displayFAQSection(currentSection);
                view.displayInfo("[-1] Return to " + (currentSection.getParent() == null ? "FAQ" : currentSection.getParent().getTopic()));
            }
            view.displayInfo("[-2] Add FAQ item");
            view.displayInfo("[-3] Remove FAQ item");
            String input = view.getInput("Please choose an option: ");
            try {
                int optionNo = Integer.parseInt(input);

                if (optionNo == -2) {
                    addFAQItem(currentSection);
                } else if (optionNo == -3) {
                    if (currentSection == null) {
                        view.displayError("Please select a section first");
                    } else {
                        removeFAQItem(currentSection);
                    }
                } else if (optionNo == -1) {
                    if (currentSection == null) {
                        break;
                    } else {
                        currentSection = currentSection.getParent();
                    }
                } else {
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
            } catch (NumberFormatException e) {
                view.displayError("Invalid option: " + input);
            }
        }
    }

    /**
     * Handles Logic for adding new FAQ items.
     * @param currentSection    the current FAQ section that the item should be added to
     */
    private void addFAQItem(FAQSection currentSection) {
        view.displayInfo("=== Add New FAQ Question-Answer Pair ===");
        boolean createSection = (currentSection == null);
        if (!createSection) {
            createSection = view.getYesNoInput("Would you like to create a new topic for the FAQ item?");
        }
        if (createSection) {
            String newTopic = view.getInput("Enter new topic title: ");
            FAQSection newSection = new FAQSection(newTopic);
            if (currentSection == null) {
                if (sharedContext.getFAQ().getSections().stream().anyMatch(section -> section.getTopic().equals(newTopic))) {
                    view.displayWarning("Topic '" + newTopic + "' already exists!");
                    newSection = sharedContext.getFAQ().getSections().stream().filter(section -> section.getTopic().equals(newTopic)).findFirst().orElseThrow();
                } else {
                    sharedContext.getFAQ().addSection(newSection);
                    view.displayInfo("Created topic '" + newTopic + "'");
                }
            } else {
                if (currentSection.getSubsections().stream().anyMatch(section -> section.getTopic().equals(newTopic))) {
                    view.displayWarning("Topic '" + newTopic + "' already exists under '" + currentSection.getTopic() + "'!");
                    newSection = currentSection.getSubsections().stream().filter(section -> section.getTopic().equals(newTopic)).findFirst().orElseThrow();
                } else {
                    currentSection.addSubsection(newSection);
                    view.displayInfo("Created topic '" + newTopic + "' under '" + currentSection.getTopic() + "'");
                }
            }
            currentSection = newSection;
        }
        String question = view.getInput("Enter the question: ");
        if (question == null || question.isEmpty()) {
            TinyLogLogger.log(
                    System.currentTimeMillis(),
                    ((AuthenticatedUser) sharedContext.currentUser).getEmail(),
                    "addFAQItem",
                    currentSection.getTopic(),
                    "FAILURE" + "(Error: the question cannot be empty)"
            );
            view.displayError("The question cannot be empty");
            return;
        }

        String answer = view.getInput("Enter the answer: ");
        if (answer == null || answer.isEmpty()) {
            TinyLogLogger.log(
                    System.currentTimeMillis(),
                    ((AuthenticatedUser) sharedContext.currentUser).getEmail(),
                    "addFAQItem",
                    currentSection.getTopic(),
                    "FAILURE (Error: the answer cannot be empty)"
            );
            view.displayError("The answer cannot be empty");
            return;
        }

        String courseTag = null;
        boolean addTag = view.getYesNoInput("Would you like to add a course tag?");

        if (addTag) {
            CourseManager courseManager = sharedContext.getCourseManager();

            // Call viewCourses() method as shown in sequence diagram
            String coursesOverview = courseManager.viewCourses();

            if (coursesOverview.equals("No courses available.")) {
                view.displayInfo("No courses available in the system");
            } else {
                view.displayInfo("Available courses: ");

                // Display courses as per sequence diagram
                for (String line : coursesOverview.split("\n")) {
                    if(line.startsWith("Course:")) {
                        line = line.replace("Course:", "");
                        String courseCode = line.split("-")[0].strip();
                        String courseName = line.split("-")[1].strip();
                        view.displayInfo(courseCode + ":" + courseName);
                    }
                }

                courseTag = view.getInput("Enter course code to add as tag: ");

                if (!courseManager.hasCourse(courseTag)) {
                    TinyLogLogger.log(
                            System.currentTimeMillis(),
                            ((AuthenticatedUser) sharedContext.currentUser).getEmail(),
                            "addFAQItem",
                            currentSection.getTopic(),
                            "FAILURE" + "(Error: the tag must correspond to a course code)"
                    );
                    view.displayError("The tag must correspond to a course code");
                    return;
                }
            }

            currentSection.addItem(question, answer, courseTag);
        }
        else {
            currentSection.addItem(question, answer);
        }

        TinyLogLogger.log(
                System.currentTimeMillis(),
                ((AuthenticatedUser) sharedContext.currentUser).getEmail(),
                "addFAQItem",
                currentSection.getTopic(),
                "SUCCESS (A new FAQ item was added)"
        );

        view.displaySuccess("The new FAQ item was added");

        // Email notification logic
        String emailSubject = "FAQ topic '" + currentSection.getTopic() + "' updated";
        StringBuilder emailContentBuilder = new StringBuilder();
        emailContentBuilder.append("Updated Q&As:");
        for (FAQItem faqItem : currentSection.getItems()) {
            emailContentBuilder.append("\n\n");
            emailContentBuilder.append("Q: ");
            emailContentBuilder.append(faqItem.getQuestion());
            emailContentBuilder.append("\n");
            emailContentBuilder.append("A: ");
            emailContentBuilder.append(faqItem.getAnswer());
            if (faqItem.getCourseTag() != null) {
                emailContentBuilder.append("\nCourse Tag: ");
                emailContentBuilder.append(faqItem.getCourseTag());
            }
        }
        String emailContent = emailContentBuilder.toString();

        String userEmail = ((AuthenticatedUser) sharedContext.currentUser).getEmail();
        email.sendEmail(
                userEmail,
                SharedContext.ADMIN_STAFF_EMAIL,
                emailSubject,
                emailContent
        );

    }

    /**
     * Handles logic for removing FAQ Items from a section.
     * @param currentSection    the FAQ section to remove items from
     */
    private void removeFAQItem(FAQSection currentSection) {
        if (currentSection == null) {
            view.displayError("No section selected.");
            return;
        }

        view.displayInfo("=== Remove FAQ Item ===");
        view.displayInfo("You are in section: '" + currentSection.getTopic() + "'");

        boolean removeEntireSection = view.getYesNoInput("Would you like to remove the entire section '" + currentSection.getTopic() + "' (including all items and subsections)? (Yes = entire section, No = single item)");

        String userEmail = ((AuthenticatedUser) sharedContext.currentUser).getEmail();
        String sectionTopic = currentSection.getTopic();

        if (removeEntireSection) {
            view.displayInfo("This will delete the title, all FAQ items, and all subsections.");
            boolean confirm = view.getYesNoInput("Are you sure you want to proceed? (Yes/No)");
            if (!confirm) {
                view.displayInfo("Operation cancelled.");
                return;
            }

            if (currentSection.getParent() == null) {
                sharedContext.getFAQ().getSections().remove(currentSection);
            } else {
                FAQSection parentSection = currentSection.getParent();
                parentSection.getSubsections().remove(currentSection);
            }

            TinyLogLogger.log(
                    System.currentTimeMillis(),
                    userEmail,
                    "removeFAQItem",
                    sectionTopic,
                    "SUCCESS (FAQ section and all contents removed)"
            );

            view.displaySuccess("FAQ section '" + sectionTopic + "' and all its contents have been removed.");

            String emailSubject = "FAQ topic '" + sectionTopic + "' removed";
            String emailContent = "The FAQ section '" + sectionTopic + "' including its title, items, and subsections has been removed.";
            email.sendEmail(userEmail, SharedContext.ADMIN_STAFF_EMAIL, emailSubject, emailContent);
        } else {
            if (currentSection.getItems().isEmpty()) {
                view.displayError("No FAQ items available to remove in this section.");
                return;
            }

            view.displayInfo("FAQ items in section '" + currentSection.getTopic() + "':");
            for (int i = 0; i < currentSection.getItems().size(); i++) {
                FAQItem item = currentSection.getItems().get(i);
                String tagInfo = item.getCourseTag() != null ? " [Course: " + item.getCourseTag() + "]" : "";
                view.displayInfo("[" + i + "] Q: " + item.getQuestion() + tagInfo + " | A: " + item.getAnswer());
            }

            String input = view.getInput("Enter the number of the FAQ item to remove (or -1 to cancel): ");
            int itemIndex;
            try {
                itemIndex = Integer.parseInt(input);
                if (itemIndex == -1) {
                    view.displayInfo("Operation cancelled.");
                    return;
                }
                if (itemIndex < 0 || itemIndex >= currentSection.getItems().size()) {
                    view.displayError("Invalid item number: " + itemIndex);
                    return;
                }
            } catch (NumberFormatException e) {
                view.displayError("Invalid input: " + input);
                return;
            }

            FAQItem itemToRemove = currentSection.getItems().get(itemIndex);
            String question = itemToRemove.getQuestion();

            boolean confirm = view.getYesNoInput("Are you sure you want to remove the FAQ item: '" + question + "'? (Yes/No)");
            if (!confirm) {
                view.displayInfo("Operation cancelled.");
                return;
            }

            currentSection.getItems().remove(itemIndex);

            TinyLogLogger.log(
                    System.currentTimeMillis(),
                    userEmail,
                    "removeFAQItem",
                    sectionTopic,
                    "SUCCESS (FAQ item '" + question + "' removed)"
            );

            view.displaySuccess("FAQ item '" + question + "' has been removed from section '" + sectionTopic + "'.");

            String emailSubject = "FAQ item removed from '" + sectionTopic + "'";
            String emailContent = "The FAQ item '" + question + "' has been removed from the section '" + sectionTopic + "'.";
            email.sendEmail(userEmail, SharedContext.ADMIN_STAFF_EMAIL, emailSubject, emailContent);

            // Check if the section is now empty of items and has a parent section
            if (currentSection.getItems().isEmpty() && currentSection.getParent() != null) {
                FAQSection parentSection = currentSection.getParent();

                // Create a copy of the subsections list to avoid concurrent modification issues
                List<FAQSection> subsectionsCopy = new ArrayList<>(currentSection.getSubsections());

                // Move all subsections one level up to the parent
                for (FAQSection subsection : subsectionsCopy) {
                    parentSection.addSubsection(subsection);
                    // No need to remove from original list as addSubsection updates the parent
                }

                // Remove the now-empty section from its parent
                parentSection.getSubsections().remove(currentSection);

                TinyLogLogger.log(
                        System.currentTimeMillis(),
                        userEmail,
                        "removeFAQItem",
                        sectionTopic,
                        "INFO (Empty section removed and subsections promoted)"
                );

                view.displayInfo("Section '" + sectionTopic + "' was empty and has been removed. All subsections were moved up one level.");

                // Email notification about section restructuring
                String restructureSubject = "FAQ structure updated - Empty section removed";
                String restructureContent = "The section '" + sectionTopic + "' became empty after item removal and has been removed automatically. " +
                        "All subsections have been moved up one level in the hierarchy.";
                email.sendEmail(userEmail, SharedContext.ADMIN_STAFF_EMAIL, restructureSubject, restructureContent);

                // Update current section to point to parent since this one was removed
                currentSection = parentSection;
            }
        }
    }

    /**
     * Displays and manages the menu that allows AdminStaff to redirect or respond to Inquiries.
     */
    public void manageInquiries() {
        String[] inquiryTitles = getInquiryTitles(sharedContext.inquiries);

        while (true) {
            view.displayInfo("Pending inquiries");
            int selection = selectFromMenu(inquiryTitles, "Back to main menu");
            if (selection == -1) {
                return;
            }
            Inquiry selectedInquiry = sharedContext.inquiries.get(selection);

            while (true) {
                view.displayDivider();
                view.displayInquiry(selectedInquiry);
                view.displayDivider();
                String[] followUpOptions = { "Redirect inquiry", "Respond to inquiry" };
                int followUpSelection = selectFromMenu(followUpOptions, "Back to all inquiries");

                if (followUpSelection == -1) {
                    break;
                } else if (followUpOptions[followUpSelection].equals("Redirect inquiry")) {
                    redirectInquiry(selectedInquiry);
                } else if (followUpOptions[followUpSelection].equals("Respond to inquiry")) {
                    respondToInquiry(selectedInquiry);
                    inquiryTitles = getInquiryTitles(sharedContext.inquiries); // required to remove responded inquiry from titles
                    break;
                }
            }
        }
    }

    /**
     * Redirects inquiry to requested email.
     * @param inquiry   the inquiry to redirect
     */
    private void redirectInquiry(Inquiry inquiry) {
        inquiry.setAssignedTo(view.getInput("Enter assignee email: "));
        email.sendEmail(
                SharedContext.ADMIN_STAFF_EMAIL,
                inquiry.getAssignedTo(),
                "New inquiry from " + inquiry.getInquirerEmail(),
                "Subject: " + inquiry.getSubject() + "\nPlease log into the Self Service Portal to review and respond to the inquiry."
        );
        view.displaySuccess("Inquiry has been reassigned");
    }


    /**
     * Displays and manages menu to add a new Course to the <code>CourseManager</code>
     */
    private void addCourse() {
        view.displayInfo("=== Add Course ===");

        // Get current user email for logging
        String email = ((AuthenticatedUser) sharedContext.currentUser).getEmail();

        // Get all required course information
        String courseCode = view.getInput("Enter course code: ");
        String name = view.getInput("Enter course name: ");
        String description = view.getInput("Enter course description: ");

        // Validate course info
        if (courseCode == null || courseCode.trim().isEmpty() ||
                name == null || name.trim().isEmpty() ||
                description == null || description.trim().isEmpty()) {
            view.displayError("Required course info not provided");
            return;
        }

        String requiresComputersInput;
        do {
            requiresComputersInput = view.getInput("Does this course require computers? (Y/N)");

            if (requiresComputersInput == null || requiresComputersInput.trim().isEmpty()) {
                view.displayError("Required course info not provided");
                return;
            }

            requiresComputersInput = requiresComputersInput.trim().toLowerCase();

            if (!requiresComputersInput.equals("y") && !requiresComputersInput.equals("n")) {
                view.displayError("Invalid input: Please enter 'Y' or 'N'");
            }
        } while (!requiresComputersInput.equals("y") && !requiresComputersInput.equals("n"));

        boolean requiresComputers = requiresComputersInput.equals("y");

        String courseOrganiserName = view.getInput("Enter course organiser name: ");
        String courseOrganiserEmail = view.getInput("Enter course organiser email: ");
        String courseSecretaryName = view.getInput("Enter course secretary name: ");
        String courseSecretaryEmail = view.getInput("Enter course secretary email: ");

        // Validate other required information
        if (courseOrganiserName == null || courseOrganiserName.trim().isEmpty() ||
                courseOrganiserEmail == null || courseOrganiserEmail.trim().isEmpty() ||
                courseSecretaryName == null || courseSecretaryName.trim().isEmpty() ||
                courseSecretaryEmail == null || courseSecretaryEmail.trim().isEmpty()) {
            view.displayError("Required course info not provided");
            return;
        }

        int requiredTutorials = 0;
        int requiredLabs = 0;
        try {
            requiredTutorials = Integer.parseInt(view.getInput("Enter number of required tutorials: "));
            requiredLabs = Integer.parseInt(view.getInput("Enter number of required labs: "));
        } catch (NumberFormatException e) {
            view.displayError("Required course info not provided");
            return;
        }

        // Combine all course info for logging
        String courseInfo = String.format(
                "Code: %s, Name: %s, Requires Computers: %b, CO: %s (%s), CS: %s (%s), Tutorials: %d, Labs: %d",
                courseCode, name, requiresComputers,
                courseOrganiserName, courseOrganiserEmail,
                courseSecretaryName, courseSecretaryEmail,
                requiredTutorials, requiredLabs
        );

        // Add the course using CourseManager (which will handle logging)
        boolean courseAdded = courseManager.addCourse(
                email,
                courseCode, name, description, requiresComputers,
                courseOrganiserName, courseOrganiserEmail,
                courseSecretaryName, courseSecretaryEmail,
                requiredTutorials, requiredLabs
        );

        if (!courseAdded) {
            return; // addCourse method in CourseManager will handle error display and logging
        }

        // Add activities to the course
        view.displayInfo("=== Add Course - Activities ===");

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // Loop to add activities
        boolean addMoreActivities = true;
        while (addMoreActivities) {
            // Activity type selection
            String[] activityTypes = {"Lecture", "Tutorial", "Lab"};
            int typeSelection = selectFromMenu(activityTypes, "Cancel");
            if (typeSelection == -1) {
                break;
            }

            String activityType = activityTypes[typeSelection];

            // Get common activity information
            String dayOfWeekStr = view.getInput("Enter day of week (e.g., MONDAY): ");
            String startDateStr = view.getInput("Enter start date (YYYY-MM-DD): ");
            String startTimeStr = view.getInput("Enter start time (HH:MM): ");
            String endDateStr = view.getInput("Enter end date (YYYY-MM-DD): ");
            String endTimeStr = view.getInput("Enter end time (HH:MM): ");
            String location = view.getInput("Enter location: ");

            // Parse dates and times
            try {
                LocalDate startDate = LocalDate.parse(startDateStr, dateFormatter);
                LocalTime startTime = LocalTime.parse(startTimeStr, timeFormatter);
                LocalDate endDate = LocalDate.parse(endDateStr, dateFormatter);
                LocalTime endTime = LocalTime.parse(endTimeStr, timeFormatter);
                DayOfWeek day = DayOfWeek.valueOf(dayOfWeekStr);

                // Get activity specific information
                if (activityType.equals("Lecture")) {
                    boolean recorded = view.getYesNoInput("Is this lecture recorded?");
                    // Add lecture activity
                    courseManager.addActivityToCourse(courseCode, startDate, startTime, endDate, endTime, location, day, "Lecture", recorded);
                } else if (activityType.equals("Tutorial") || activityType.equals("Lab")) {
                    int capacity = Integer.parseInt(view.getInput("Enter capacity: "));
                    // Add tutorial or lab activity
                    courseManager.addActivityToCourse(courseCode, startDate, startTime, endDate, endTime, location, day, activityType, capacity);
                }

            } catch (Exception e) {
                view.displayException(e);
                view.displayError("Failed to add activity. Please check your input format.");
                continue;
            }

            addMoreActivities = view.getYesNoInput("Add another activity?");
        }

        // Send email notification to course organiser
        this.email.sendEmail(
                email,
                courseOrganiserEmail,
                "Course Created - " + courseCode,
                "A course has been provided with the following details: " + courseInfo
        );

        view.displaySuccess("Course has been successfully created.");
    }

    /**
     * Displays and manages menu to remove course from the <code>CourseManager</code>
     */
    private void removeCourse() {
        view.displayInfo("=== Remove Course ===");

        // View available courses
        String coursesOverview = courseManager.viewCourses();
        if (coursesOverview.equals("No courses available.")) {
            view.displayInfo("No courses available to remove.");
            return;
        }

        // Display courses for selection
        view.displayInfo("Available Courses:");
        List<String> courseCodesList = new ArrayList<>();
        for (String line : coursesOverview.split("\n")) {
            if (line.startsWith("Course:")) {
                line = line.replace("Course:", "");
                String courseCode = line.split("-")[0].strip();
                courseCodesList.add(courseCode);
            }
        }

        // Create array for menu selection
        String[] courseCodesArray = courseCodesList.toArray(new String[0]);

        // Select course to remove
        int selection = selectFromMenu(courseCodesArray, "Back to main menu");
        if (selection == -1) {
            return;
        }

        String courseToRemove = courseCodesArray[selection];

        // Confirm removal
        boolean confirmRemoval = view.getYesNoInput("Are you sure you want to remove course " + courseToRemove + "? This will remove the course from all student timetables.");
        if (!confirmRemoval) {
            view.displayInfo("Course removal cancelled.");
            return;
        }

        // Get current user email for logging
        String email = ((AuthenticatedUser) sharedContext.currentUser).getEmail();

        // Remove the course using CourseManager
        String[] emailsToNotify = courseManager.removeCourse(courseToRemove);

        if (emailsToNotify != null) {
            // Log the course removal
            TinyLogLogger.log(
                    System.currentTimeMillis(),
                    email,
                    "removeCourse",
                    courseToRemove,
                    "SUCCESS (Course successfully removed)"
            );

            // Send email notifications
            String emailSubject = "Course Removed: " + courseToRemove;
            String emailBody = "The course " + courseToRemove + " has been removed from the system. " +
                    "All associated timetable entries have been cleared.";

            for (String recipientEmail : emailsToNotify) {
                if (recipientEmail != null) {
                    this.email.sendEmail(
                            SharedContext.ADMIN_STAFF_EMAIL,
                            recipientEmail,
                            emailSubject,
                            emailBody
                    );
                }
            }

            view.displaySuccess("Course " + courseToRemove + " has been successfully removed.");
        } else {
            // Log failure if course removal was unsuccessful
            TinyLogLogger.log(
                    System.currentTimeMillis(),
                    email,
                    "removeCourse",
                    courseToRemove,
                    "FAILURE (Course removal failed)"
            );
            view.displayError("Failed to remove course. Please try again.");
        }
    }

    /**
     * Displays and manages the menu to add and remove courses
     */
    public void manageCourse() {
        while (true) {
            view.displayInfo("=== Manage Courses ===");


            String[] menuOptions = {"Add a new course", "Remove a course"};
            int selection = selectFromMenu(menuOptions, "Back to main menu");

            if (selection == -1) {
                return;
            }

            switch (menuOptions[selection]) {
                case "Add a new course":
                    addCourse();
                    break;
                case "Remove a course":
                    removeCourse();
                    break;
            }
        }
    }


}
