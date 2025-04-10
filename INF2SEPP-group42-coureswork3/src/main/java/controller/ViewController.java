package controller;

import external.AuthenticationService;
import external.EmailService;
import model.Course;
import model.SharedContext;
import model.CourseManager;
import view.View;

public class ViewController extends Controller{
    private final CourseManager courseManager;
    public ViewController(SharedContext sharedContext, View view, AuthenticationService auth, EmailService email) {
        super(sharedContext, view, auth, email);
        this.courseManager = sharedContext.getCourseManager();
    }
    public void viewCourses() {
        // Display all available courses
        String coursesInfo = courseManager.viewCourses();
        view.displayInfo(coursesInfo);
    }

    public void viewSpecificCourse(String courseCode) {
        // If no course code is provided, prompt the user to enter one
        if (courseCode == null || courseCode.trim().isEmpty()) {
            courseCode = view.getInput("Please enter the course code: ");
        }

        // Trim the course code to remove any leading/trailing whitespace
        courseCode = courseCode.trim();

        // Check if the course exists
        if (!courseManager.hasCourse(courseCode)) {
            view.displayError("Course not found. Please check the course code and try again.");
            return;
        }

        // Get the course
        Course course = courseManager.getCourseByCode(courseCode);

        // Use the view to display the course details
        view.displayCourse(course);

        // Optional: Show additional information like activities
        String courseDetails = courseManager.viewCourse(courseCode);
        view.displayInfo(courseDetails);
    }
}
