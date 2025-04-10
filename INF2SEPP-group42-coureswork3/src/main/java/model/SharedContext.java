package model;

import java.util.*;
import view.View;

/**
 * A shared context that hold global states. Includes references to current user, FAQs, inquiries and course
 * management.
 */
public class SharedContext {
    public static final String ADMIN_STAFF_EMAIL = "inquiries@hindeburg.ac.nz";
    public User currentUser;

    public final List<Inquiry> inquiries;
    public final FAQ faq;

    private final CourseManager courseManager;

    /**
     * @param view  the user interface view
     */
    public SharedContext(View view) {
        this.currentUser = new Guest();
        this.inquiries = new ArrayList<>();
        faq = new FAQ();
        courseManager = new CourseManager(view);
    }

    /**
     * @return the FAQ
     */
    public FAQ getFAQ() {
        return faq;
    }

    /**
     * Gets the email of the teaching staff responsible for a specific course.
     *
     * @param courseCode The course code to look up
     * @return The email of the responsible teaching staff, or admin email if not found
     */
    public String getTeachingStaffEmailForCourse(String courseCode) {
        if (courseCode == null || courseCode.trim().isEmpty()) {
            return ADMIN_STAFF_EMAIL;
        }

        Course course = courseManager.getCourseByCode(courseCode);
        if (course != null) {
            // First try the course organiser
            if (course.getCourseOrganiserEmail() != null && !course.getCourseOrganiserEmail().isEmpty()) {
                return course.getCourseOrganiserEmail();
            }
            // Then try the course secretary
            if (course.getCourseSecretaryEmail() != null && !course.getCourseSecretaryEmail().isEmpty()) {
                return course.getCourseSecretaryEmail();
            }
        }

        // Default to admin staff if course not found or no staff email available
        return ADMIN_STAFF_EMAIL;
    }

    /**
     * @return  if authenticated the role of the current user, otherwise {@code "Guest"}
     */
    public String getCurrentUserRole() {
        if (currentUser instanceof AuthenticatedUser) {
            return ((AuthenticatedUser) currentUser).getRole();
        }
        return "Guest";
    }

    /**
     * @return  the email of the currently logged in user
     */
    public String getCurrentUserEmail() {
        if (currentUser instanceof AuthenticatedUser) {
            return ((AuthenticatedUser) currentUser).getEmail();
        }
        return null;
    }

    /**
     * @return the CourseManager
     */
    public CourseManager getCourseManager() {
        return courseManager;
    }
}