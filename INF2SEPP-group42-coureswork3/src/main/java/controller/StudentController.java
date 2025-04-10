package controller;

import external.AuthenticationService;
import external.EmailService;
import model.AuthenticatedUser;
import model.CourseManager;
import model.SharedContext;
import view.View;

/**
 * Controller for students. Provides functionality for managing and viewing personal timetable.
 */
public class StudentController extends Controller{
    /**
     * Instantiates a new student controller.
     *
     * @param sharedContext the shared context
     * @param view          the user interface view
     * @param auth          the authentication service
     * @param email         the email of the user
     */
    public StudentController(SharedContext sharedContext, View view, AuthenticationService auth, EmailService email) {
        super(sharedContext, view, auth, email);
    }

    /**
     * Displays and manages the menu for adding courses to personal timetable.
     */
    private void addCourseToTimetable() {
        // Display header for adding course to timetable
        view.displayInfo("=== Add Course to Timetable ===");

        // Check if user is authenticated
        if (!(sharedContext.currentUser instanceof AuthenticatedUser)) {
            view.displayError("You must be logged in to add a course to your timetable.");
            return;
        }

        // Get the current user's email
        String email = sharedContext.getCurrentUserEmail();

        // Get the course code from user input
        String courseCode = view.getInput("Enter the course code: ");

        // Get the CourseManager from SharedContext
        CourseManager courseManager = sharedContext.getCourseManager();

        // Attempt to add course to student's timetable
        boolean addedSuccessfully = courseManager.addCourseToStudentTimetable(email, courseCode);
        if (!addedSuccessfully) {

            view.displayError("Failed to add course to your timetable. Please check course code and conflicts.");
        }
    }

    /**
     * Displays and manages menu for adding activities for a course already in the student's timetable.
     * Only allows selecting tutorials and labs, not lectures.
     */
    private void chooseActivityForCourse() {
        // Check if user is authenticated
        if (!(sharedContext.currentUser instanceof AuthenticatedUser)) {
            view.displayError("You must be logged in to choose an activity.");
            return;
        }

        // Get the current user's email
        String email = sharedContext.getCurrentUserEmail();

        // Get the course code from user input
        String courseCode = view.getInput("Enter the course code: ");

        // Get the CourseManager from SharedContext
        CourseManager courseManager = sharedContext.getCourseManager();

        // Check if course exists
        if (!courseManager.hasCourse(courseCode)) {
            view.displayError("Course does not exist.");
            return;
        }

        // Get the activity ID from user input
        int activityId;
        try {
            activityId = Integer.parseInt(view.getInput("Enter the activity ID (for tutorials or labs only): "));
        } catch (NumberFormatException e) {
            view.displayError("Invalid activity ID. Please enter a numeric value.");
            return;
        }


        // Choose the activity for the course
        courseManager.chooseActivityForCourse(email, courseCode, activityId);
    }

    /**
     * Displays student's personal timetable
     */
    public void viewTimetable() {
        // Check if user is authenticated
        if (!(sharedContext.currentUser instanceof AuthenticatedUser)) {
            view.displayError("You must be logged in to view your timetable.");
            return;
        }

        // Get the current user's email
        String email = sharedContext.getCurrentUserEmail();

        // Get the CourseManager from SharedContext
        CourseManager courseManager = sharedContext.getCourseManager();
         courseManager.viewTimetable(email);
    }

    /**
     * Displays and manages menu for removing courses from personal timetable.
     */
    private void removeCourseFromTimetable() {
        // Display header for removing course from timetable
        view.displayInfo("=== Remove Course from Timetable ===");

        // Check if user is authenticated
        if (!(sharedContext.currentUser instanceof AuthenticatedUser)) {
            view.displayError("You must be logged in to remove a course from your timetable.");
            return;
        }

        // Get the current user's email
        String email = sharedContext.getCurrentUserEmail();

        // Get the course code from user input
        String courseCode = view.getInput("Enter the course code to remove: ");

        // Get the CourseManager from SharedContext
        CourseManager courseManager = sharedContext.getCourseManager();

        // Attempt to remove course from student's timetable
        courseManager.removeCourseFromStudentTimetable(email, courseCode);
    }

    /**
     * Displays and manages menu for managing timetable, allows student to view timetable,
     * add courses, select activities, and remove courses.
     */
    public void manageTimetable() {
        // Check if user is authenticated
        if (!(sharedContext.currentUser instanceof AuthenticatedUser)) {
            view.displayError("You must be logged in to manage your timetable.");
            return;
        }

        boolean exitMenu = false;
        while (!exitMenu) {
            view.displayInfo("=== Timetable Management ===");
            view.displayInfo("1. View Timetable");
            view.displayInfo("2. Add Course to Timetable");
            view.displayInfo("3. Choose Activity for Course");
            view.displayInfo("4. Remove Course from Timetable");
            view.displayInfo("5. Exit");

            String choice = view.getInput("Enter your choice (1-5): ");

            switch (choice) {
                case "1":
                    viewTimetable();
                    break;
                case "2":
                    addCourseToTimetable();
                    break;
                case "3":
                    chooseActivityForCourse();
                    break;
                case "4":
                    removeCourseFromTimetable();
                    break;
                case "5":
                    exitMenu = true;
                    break;
                default:
                    view.displayError("Invalid choice. Please try again.");
            }
        }
    }
}
