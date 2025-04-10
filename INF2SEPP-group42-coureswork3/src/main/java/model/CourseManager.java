package model;

import view.View;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles creation, deletion and updating of courses and student timetables.
 */
public class CourseManager {
    private final Map<String, Course> courses;
    private final Map<String, Timetable> timetables;
    private final View view;
    private int nextActivityId = 1;

    /**
     * @param view  the user interface view
     */
    public CourseManager(View view) {
        this.view = view;
        this.courses = new HashMap<>();
        this.timetables = new HashMap<>();
    }

    /**
     * Attempts to add a new course with given parameters.
     *
     * @param userEmail         the email of the {@link AuthenticatedUser}
     * @param code              the course code to add
     * @param name              the name of the course
     * @param description       the description of the course
     * @param requiresComputers whether the course requires a computer
     * @param COName            name of the course organiser
     * @param COEmail           email of the course organiser
     * @param CSName            name of the course secretary
     * @param CSEmail           email of the course secretary
     * @param reqTutorials      number of required tutorials
     * @param reqLabs           number of required labs
     * @return {@code true} if the course was added succesffuly, otherwise {@code false}
     */
    public boolean addCourse(String userEmail, String code, String name, String description,
                             boolean requiresComputers, String COName, String COEmail,
                             String CSName, String CSEmail, int reqTutorials, int reqLabs) {
        // Log the attempt to add a course
        String courseInfo = String.format(
                "Code: %s, Name: %s, Requires Computers: %b, CO: %s (%s), CS: %s (%s), Tutorials: %d, Labs: %d",
                code, name, requiresComputers,
                COName, COEmail,
                CSName, CSEmail,
                reqTutorials, reqLabs
        );

        // Validate required course info
        if (code == null || name == null || description == null || COName == null ||
                COEmail == null || CSName == null || CSEmail == null) {
            TinyLogLogger.log(
                    System.currentTimeMillis(),
                    "", // No email available at this point
                    "addCourse",
                    courseInfo,
                    "FAILURE (Error: Required course info not provided)"
            );
            view.displayError("Required course info not provided");
            return false;
        }

        // Validate course code
        if (!checkCourseCode(code)) {
            TinyLogLogger.log(
                    System.currentTimeMillis(),
                    "", // No email available at this point
                    "addCourse",
                    courseInfo,
                    "FAILURE (Error: Provided courseCode is invalid)"
            );
            view.displayError("Provided courseCode is invalid");
            return false;
        }

        // Check if course with this code already exists
        if (hasCourse(code)) {
            TinyLogLogger.log(
                    System.currentTimeMillis(),
                    "", // No email available at this point
                    "addCourse",
                    courseInfo,
                    "FAILURE (Error: Course with that code already exists)"
            );
            view.displayError("Course with that code already exists");
            return false;
        }

        // Create and add the course
        Course newCourse = new Course(code, name, description, requiresComputers,
                COName, COEmail, CSName, CSEmail,
                reqTutorials, reqLabs);
        courses.put(code, newCourse);

        // Log success
        TinyLogLogger.log(
                System.currentTimeMillis(),
                userEmail,
                "addCourse",
                courseInfo,
                "SUCCESS (New course added)"
        );

        return true;
    }

    /**
     * checks whether a course code follows a valid format.
     *
     * @param courseCode    the code to check
     * @return {@code true} if the given code follows the correct format, otherwise {@code false}
     */
    public boolean checkCourseCode(String courseCode) {
        if (courseCode == null || courseCode.isEmpty()) {
            return false;
        }
        // Format validation: e.g., "CS101" - alphanumeric format
        return courseCode.matches("[A-Z]{2,}\\d{3,}");
    }

    /**
     * checks whether a course with the given code already exists.
     *
     * @param courseCode    the code to check
     * @return {@code true} if the code already exists, otherwise {@code false}
     */
    public boolean hasCourse(String courseCode) {
        return courses.containsKey(courseCode);
    }

    /**
     * removes a course with the given course code.
     *
     * @param courseCode    the code of the course to remove
     * @return array of emails of everyone involved in course, every student that has the course on the timetable aswell
     * as course organiser and secretary, if the course doesn't exist than returns {@code null}
     */
    public String[] removeCourse(String courseCode) {
        if (!hasCourse(courseCode)) {
            return null;
        }

        Course course = courses.get(courseCode);
        List<String> emailsToNotify = new ArrayList<>();

        course.removeActivities();

        // Find all students who have this course in their timetable
        for (Timetable timetable : timetables.values()) {
            if (timetable.hasSlotsForCourse(courseCode)) {
                emailsToNotify.add(timetable.getStudentEmail());
                timetable.removeSlotsForCourse(courseCode);
            }
        }

        // Add course staff emails to notify
        emailsToNotify.add(course.getCourseOrganiserEmail());
        emailsToNotify.add(course.getCourseSecretaryEmail());

        // Remove the course
        courses.remove(courseCode);

        return emailsToNotify.toArray(new String[0]);
    }

    /**
     * Adds the course with the given code to the timetable of the student with the given email.
     *
     * @param studentEmail  the email of the student to add the course to
     * @param courseCode    the course to add to the timetable
     * @return {@code true} if the course was successfully added to the timetable, otherwise {@code false}
     */
    public boolean addCourseToStudentTimetable(String studentEmail, String courseCode) {
        // Log the start of the action
        long timestamp = System.currentTimeMillis();

        // Validate course code
        if (!hasCourse(courseCode)) {
            view.displayError("Incorrect course code");
            TinyLogLogger.log(timestamp, studentEmail, "addCourseToStudentTimetable",
                    studentEmail + courseCode, "FAILURE (Error: Incorrect course code)");
            return false;
        }

        Course course = courses.get(courseCode);
        Timetable timetable = getTimetable(studentEmail);

        // Check if course already in timetable
        if (timetable.hasSlotsForCourse(courseCode)) {
            view.displayWarning("Course is already in your timetable");
            return false;
        }

        // Process activities and check for conflicts
        List<String> conflictingActivities = new ArrayList<>();
        boolean hasUnrecordedLectureConflict = false;

        // First check for unrecorded lecture conflicts with existing activities
        for (Activity activity : course.getActivities()) {
            if (activity instanceof Lecture && !((Lecture) activity).isRecorded()) {
                String[] conflicts = timetable.checkConflicts(
                        activity.getStartDate(),
                        activity.getStartTime(),
                        activity.getEndDate(),
                        activity.getEndTime(),
                        activity.getDay()
                );

                if (conflicts.length > 0) {
                    hasUnrecordedLectureConflict = true;
                    for (String conflict : conflicts) {
                        conflictingActivities.add("Unrecorded lecture conflict: " + activity.toString() + " with " + conflict);
                    }
                }
            }
        }

        // If there's an unrecorded lecture conflict, abort adding the course
        if (hasUnrecordedLectureConflict) {
            view.displayError("You have at least one clash with an unrecorded lecture. The course cannot be added to your timetable.");
            for (String conflict : conflictingActivities) {
                view.displayError(conflict);
            }
            TinyLogLogger.log(timestamp, studentEmail, "addCourseToStudentTimetable",
                    studentEmail + courseCode, "FAILURE (Error: Unrecorded lecture conflict)");
            return false;
        }

        // Reset conflict list
        conflictingActivities.clear();

        // Now check if any existing unrecorded lectures conflict with the new course's activities
        List<TimeSlot> existingUnrecordedLectures = timetable.getUnrecordedLectureSlots(this);

        for (Activity newActivity : course.getActivities()) {
            for (TimeSlot unrecordedSlot : existingUnrecordedLectures) {
                // Skip if not on the same day
                if (unrecordedSlot.getDay() != newActivity.getDay()) {
                    continue;
                }

                // Check for time conflicts
                if (newActivity.getStartTime().isBefore(unrecordedSlot.getEndTime()) &&
                        unrecordedSlot.getStartTime().isBefore(newActivity.getEndTime())) {

                    hasUnrecordedLectureConflict = true;
                    conflictingActivities.add(
                            "New activity conflicts with existing unrecorded lecture: " +
                                    newActivity.toString() + " with " + unrecordedSlot.toString()
                    );
                }
            }
        }

        // If there's a conflict with existing unrecorded lectures, abort
        if (hasUnrecordedLectureConflict) {
            view.displayError("You have at least one clash with an existing unrecorded lecture. The course cannot be added to your timetable.");
            for (String conflict : conflictingActivities) {
                view.displayError(conflict);
            }
            TinyLogLogger.log(timestamp, studentEmail, "addCourseToStudentTimetable",
                    studentEmail + courseCode, "FAILURE (Error: Conflict with existing unrecorded lecture)");
            return false;
        }

        // Reset conflict list again
        conflictingActivities.clear();

        // Now add all activities
        for (Activity activity : course.getActivities()) {
            // Set initial status - Lectures are automatically CHOSEN, other activities are UNCHOSEN
            TimeSlotStatus status = TimeSlotStatus.UNCHOSEN;
            String activityType;

            if (activity instanceof Lecture) {
                // Automatically set lectures to CHOSEN
                status = TimeSlotStatus.CHOSEN;
                activityType = "Lecture";

                // Add additional info if the lecture is recorded
                if (((Lecture) activity).isRecorded()) {
                    activityType = "Recorded Lecture";
                } else {
                    activityType = "Unrecorded Lecture";
                }
            } else if (activity instanceof Tutorial) {
                activityType = "Tutorial";
            } else if (activity instanceof Lab) {
                activityType = "Lab";
            } else {
                activityType = "Unknown";
            }

            try {
                timetable.addTimeSlot(
                        activity.getDay(),
                        activity.getStartDate(),
                        activity.getStartTime(),
                        activity.getEndDate(),
                        activity.getEndTime(),
                        courseCode,
                        activity.getId(),
                        status,
                        activityType
                );
            } catch (IllegalStateException e) {
                // This can only happen for non-lecture activities or unrecorded lectures
                // since recorded lectures are allowed to have conflicts
                conflictingActivities.add(e.getMessage());
            }
        }

        // Check for recorded lecture conflicts - just warnings
        List<String> recordedLectureConflicts = new ArrayList<>();
        for (Activity activity : course.getActivities()) {
            if (activity instanceof Lecture && ((Lecture) activity).isRecorded()) {
                String[] conflicts = timetable.checkConflicts(
                        activity.getStartDate(),
                        activity.getStartTime(),
                        activity.getEndDate(),
                        activity.getEndTime(),
                        activity.getDay()
                );

                if (conflicts.length > 0) {
                    for (String conflict : conflicts) {
                        recordedLectureConflicts.add("Time slot conflicts with existing slots: " + conflict);
                    }
                }
            }
        }

        // Display warnings for recorded lecture conflicts
        if (!recordedLectureConflicts.isEmpty()) {
            for (String conflict : recordedLectureConflicts) {
                view.displayWarning("Warning: Recorded lecture has potential conflict: " + conflict);
            }
            TinyLogLogger.log(timestamp, studentEmail, "addCourseToStudentTimetable",
                    studentEmail + courseCode, "WARNING (Recorded lecture conflicts)");
        }

        // Handle other activity conflicts - just warnings
        if (!conflictingActivities.isEmpty()) {
            view.displayWarning("You have at least one clash with another activity");
            for (String conflict : conflictingActivities) {
                view.displayWarning(conflict);
            }
            TinyLogLogger.log(timestamp, studentEmail, "addCourseToStudentTimetable",
                    studentEmail + courseCode, "WARNING (Activity conflicts)");
            // We still add the course with warnings
        }

        // Check required tutorials
        int requiredTutorials = course.getRequiredTutorials();
        if (requiredTutorials > 0) {
            view.displayWarning("You have to choose " + requiredTutorials + " tutorials for this course");
            TinyLogLogger.log(timestamp, studentEmail, "addCourseToStudentTimetable",
                    studentEmail + courseCode, "WARNING (Required tutorials not chosen)");
        }

        // Check required labs
        int requiredLabs = course.getRequiredLabs();
        if (requiredLabs > 0) {
            view.displayWarning("You have to choose " + requiredLabs + " labs for this course");
            TinyLogLogger.log(timestamp, studentEmail, "addCourseToStudentTimetable",
                    studentEmail + courseCode, "WARNING (Required labs not chosen)");
        }

        // Success scenario
        view.displaySuccess("The course was successfully added to your timetable. Lectures are automatically selected. Please select your required tutorials and labs.");
        TinyLogLogger.log(timestamp, studentEmail, "addCourseToStudentTimetable",
                studentEmail + courseCode, "SUCCESS");
        return true;
    }

    /**
     * Allows student to choose an activity to add to the timetable
     *
     * @param studentEmail  the student's email
     * @param courseCode    the course code
     * @param activityId    the id of the activity to choose
     */
    public void chooseActivityForCourse(String studentEmail, String courseCode, int activityId) {
        long timestamp = System.currentTimeMillis();

        // Check if course exists
        if (!hasCourse(courseCode)) {
            view.displayError("Course does not exist: " + courseCode);
            TinyLogLogger.log(timestamp, studentEmail, "chooseActivityForCourse",
                    courseCode + ":" + activityId, "FAILURE (Course does not exist)");
            return;
        }

        Course course = courses.get(courseCode);

        // Check if activity exists in course
        if (!course.hasActivityWithId(activityId)) {
            view.displayError("Invalid activity ID for this course");
            TinyLogLogger.log(timestamp, studentEmail, "chooseActivityForCourse",
                    courseCode + ":" + activityId, "FAILURE (Invalid activity ID)");
            return;
        }

        Timetable timetable = getTimetable(studentEmail);

        // Check if course is in student's timetable
        if (!timetable.hasSlotsForCourse(courseCode)) {
            view.displayError("Course is not in your timetable. Add it first before choosing activities.");
            TinyLogLogger.log(timestamp, studentEmail, "chooseActivityForCourse",
                    courseCode + ":" + activityId, "FAILURE (Course not in timetable)");
            return;
        }

        // Get the activity and check if it's a lecture
        Activity activity = course.getActivityById(activityId);
        if (activity == null) {
            view.displayError("Activity not found.");
            TinyLogLogger.log(timestamp, studentEmail, "chooseActivityForCourse",
                    courseCode + ":" + activityId, "FAILURE (Activity not found)");
            return;
        }

        // Prevent choosing lectures - they should already be chosen automatically
        if (activity instanceof Lecture) {
            view.displayError("Lectures are automatically selected. You can only choose tutorials and labs.");
            TinyLogLogger.log(timestamp, studentEmail, "chooseActivityForCourse",
                    courseCode + ":" + activityId, "FAILURE (Cannot manually choose lectures)");
            return;
        }

        // Check for conflicts with unrecorded lectures specifically
        List<TimeSlot> unrecordedLectures = timetable.getUnrecordedLectureSlots(this);
        boolean hasUnrecordedLectureConflict = false;
        List<String> conflictingUnrecordedLectures = new ArrayList<>();

        for (TimeSlot unrecordedSlot : unrecordedLectures) {
            if (unrecordedSlot.getDay() == activity.getDay() &&
                    activity.getStartTime().isBefore(unrecordedSlot.getEndTime()) &&
                    unrecordedSlot.getStartTime().isBefore(activity.getEndTime())) {

                hasUnrecordedLectureConflict = true;
                conflictingUnrecordedLectures.add(unrecordedSlot.toString());
            }
        }

        // If there's a conflict with unrecorded lectures, prevent choosing the activity
        if (hasUnrecordedLectureConflict) {
            view.displayError("This activity conflicts with unrecorded lectures that require attendance:");
            for (String conflict : conflictingUnrecordedLectures) {
                view.displayError("- " + conflict);
            }
            view.displayError("You cannot choose activities that conflict with unrecorded lectures.");
            TinyLogLogger.log(timestamp, studentEmail, "chooseActivityForCourse",
                    courseCode + ":" + activityId, "FAILURE (Conflicts with unrecorded lectures)");
            return;
        }

        // Check for other conflicts - these are just warnings
        String[] conflicts = timetable.checkConflicts(
                activity.getStartDate(),
                activity.getStartTime(),
                activity.getEndDate(),
                activity.getEndTime(),
                activity.getDay()
        );

        if (conflicts.length > 0) {
            view.displayWarning("This activity conflicts with existing activities in your timetable:");
            for (String conflict : conflicts) {
                view.displayWarning("- " + conflict);
            }
            TinyLogLogger.log(timestamp, studentEmail, "chooseActivityForCourse",
                    courseCode + ":" + activityId, "WARNING (Activity conflicts)");
            // Still proceed with choosing the activity since these conflicts are acceptable
        }

        // Choose the activity
        boolean success = timetable.chooseActivity(courseCode, activityId);
        if (success) {
            view.displaySuccess("Activity successfully chosen for course: " + courseCode);

            // Check requirements for tutorials and labs
            if (!checkChosenTutorials(courseCode, timetable)) {
                view.displayWarning("You still need to choose " + course.getRequiredTutorials() +
                        " tutorials for " + courseCode);
            }

            if (!checkChosenLabs(courseCode, timetable)) {
                view.displayWarning("You still need to choose " + course.getRequiredLabs() +
                        " labs for " + courseCode);
            }

            TinyLogLogger.log(timestamp, studentEmail, "chooseActivityForCourse",
                    courseCode + ":" + activityId, "SUCCESS");
        } else {
            view.displayError("Could not choose activity. Please try again.");
            TinyLogLogger.log(timestamp, studentEmail, "chooseActivityForCourse",
                    courseCode + ":" + activityId, "FAILURE (Could not choose activity)");
        }
    }

    /**
     * checks whether the number of chosen tutorials is sufficient for the course
     *
     * @param courseCode    the code of the course to check
     * @param timetable     the timetable of the student
     * @return {@code true} if the number of chosen tutorials is more than or equal to number of required tutorials,
     * otherwise {@code false}
     */
    private boolean checkChosenTutorials(String courseCode, Timetable timetable) {
        if (!hasCourse(courseCode)) {
            return false;
        }
        Course course = courses.get(courseCode);
        int requiredTutorials = course.getRequiredTutorials();
        int chosenTutorials = timetable.countChosenActivitiesOfType(courseCode, "Tutorial", this); // Pass 'this'
        return chosenTutorials >= requiredTutorials;
    }

    /**
     * checks whether the number of chosen labs is sufficient for the course
     *
     * @param courseCode    the coe of the course to check
     * @param timetable     the timetable of the student
     * @return {@code true} if the number of chosen labs is more than or equal to number of required labs, otherwise
     * {@code false}
     */
    private boolean checkChosenLabs(String courseCode, Timetable timetable) {
        if (!hasCourse(courseCode)) {
            return false;
        }
        Course course = courses.get(courseCode);
        int requiredLabs = course.getRequiredLabs();
        int chosenLabs = timetable.countChosenActivitiesOfType(courseCode, "Lab", this); // Pass 'this'
        return chosenLabs >= requiredLabs;
    }

    /**
     * removes a specified course the timetable of the student
     *
     * @param studentEmail  the email of the student
     * @param courseCode    the code of the course
     */
    public void removeCourseFromStudentTimetable(String studentEmail, String courseCode) {
        long timestamp = System.currentTimeMillis();

        // Check if course exists
        if (!hasCourse(courseCode)) {
            view.displayError("Course does not exist: " + courseCode);
            TinyLogLogger.log(timestamp, studentEmail, "removeCourseFromTimetable",
                    courseCode, "FAILURE (Course does not exist)");
            return;
        }

        Timetable timetable = getTimetable(studentEmail);

        // Check if course is in student's timetable
        if (!timetable.hasSlotsForCourse(courseCode)) {
            view.displayError("Course is not in your timetable.");
            TinyLogLogger.log(timestamp, studentEmail, "removeCourseFromTimetable",
                    courseCode, "FAILURE (Course not in timetable)");
            return;
        }

        // Remove the course from timetable
        timetable.removeSlotsForCourse(courseCode);
        view.displaySuccess("Course " + courseCode + " has been removed from your timetable.");
        TinyLogLogger.log(timestamp, studentEmail, "removeCourseFromTimetable",
                courseCode, "SUCCESS");
    }

    /**
     * gets the timetable of the student, if no timetable currently exists creates a new timetable, assigngs it the
     * student and returns that
     *
     * @param studentEmail  the email of the student
     * @return  the timetable of the student
     */
    public Timetable getTimetable(String studentEmail) {
        for (Timetable timetable : timetables.values()) {
            if (timetable.hasStudentEmail(studentEmail)) {
                return timetable;
            }
        }

        Timetable newTimetable = new Timetable(studentEmail);
        timetables.put(studentEmail, newTimetable);
        return newTimetable;
    }

    /**
     * @return a string representation all courses
     */
    public String viewCourses() {
        StringBuilder result = new StringBuilder();
        if (courses.isEmpty()) {
            result.append("No courses available.");
        } else {
            for (Course course : courses.values()) {
                String courseString = course.toString();
                result.append(courseString).append("\n");
                for (Activity activity : course.getActivities()) {
                    String activityDetailsAsString = activity.toString();
                    result.append(activityDetailsAsString).append("\n");
                }
            }
        }
        return result.toString();
    }

    /**
     * @param courseCode    code of the course
     * @return a detailed string representation of a single course
     */
    public String viewCourse(String courseCode) {
        // Check if the course exists first
        if (!hasCourse(courseCode)) {
            return "Course not found.";
        }

        // Retrieve the course (now we know it exists)
        Course course = getCourseByCode(courseCode);

        // Create a detailed course description
        StringBuilder result = new StringBuilder();
        result.append("Course Details:\n");
        result.append("-------------------------\n");
        result.append("Code: ").append(course.getCourseCode()).append("\n");
        result.append("Name: ").append(course.getName()).append("\n");
        result.append("Description: ").append(course.getDescription()).append("\n");
        result.append("Requires Computers: ").append(course.isRequiresComputers() ? "Yes" : "No").append("\n");
        result.append("Course Organiser: ").append(course.getCourseOrganiserName())
                .append(" (").append(course.getCourseOrganiserEmail()).append(")\n");
        result.append("Course Secretary: ").append(course.getCourseSecretaryName())
                .append(" (").append(course.getCourseSecretaryEmail()).append(")\n");
        result.append("Required Tutorials: ").append(course.getRequiredTutorials()).append("\n");
        result.append("Required Labs: ").append(course.getRequiredLabs()).append("\n");

        // Add activities
        result.append("\nActivities:\n");
        for (Activity activity : course.getActivities()) {
            result.append(activity.toString()).append("\n");
        }

        return result.toString();
    }

    /**
     * creates a new activity id
     * @return activity of the next id
     */
    public int getNextActivityId() {
        return nextActivityId++;
    }

    /**
     * Adds new activity to the specified course.
     *
     * @param courseCode        the code of the course
     * @param startDate         the start date of the activity
     * @param startTime         the start time of the activity
     * @param endDate           the end date of the activity
     * @param endTime           the end time of the activity
     * @param location          the location of the activity
     * @param day               the day of the week the activity is held
     * @param activityType      the type of the activity
     * @param additionalInfo    additional information about the activity specific information
     */
    public void addActivityToCourse(String courseCode, LocalDate startDate, LocalTime startTime,
                                    LocalDate endDate, LocalTime endTime, String location,
                                    DayOfWeek day, String activityType, Object additionalInfo) {
        if (!hasCourse(courseCode)) {
            return;
        }

        Course course = courses.get(courseCode);
        course.addActivity(startDate, startTime, endDate, endTime, location, day,
                activityType, additionalInfo, getNextActivityId());
    }

    public void viewTimetable(String studentEmail) {
        Timetable timetable = getTimetable(studentEmail);

        view.displayInfo("=== Viewing Complete Timetable ===");
        if (timetable.timeSlots.isEmpty()) {
            view.displayInfo("No courses in your timetable. Please add courses first.");
            return;
        }

        String timetableString = timetable.toStringWithAllActivities();
        view.displayInfo(timetableString);

        view.displayInfo("\n=== Available Activities to Choose ===");
        for (TimeSlot slot : timetable.timeSlots) {
            if (slot.status == TimeSlotStatus.UNCHOSEN &&
                    (slot.getActivityType().equals("Tutorial") || slot.getActivityType().equals("Lab"))) {
                view.displayInfo(slot.courseCode + " - " + slot.getActivityType() +
                        " (ID: " + slot.activityId + ") - " +
                        slot.getStartTime() + "-" + slot.getEndTime());
            }
        }

        view.displayInfo("\n=== Timetable Legend ===");
        view.displayInfo("UNCHOSEN - Activities available but not selected yet");
        view.displayInfo("CHOSEN - Activities you have selected for your timetable");

        view.displayInfo("\nTo choose an activity, select 'Choose Activity for Course' from the menu");
        view.displayInfo("and use the Activity ID shown above (only tutorials and labs can be chosen)");

        List<String> issues = timetable.checkTimetableIssues(this);
        if (!issues.isEmpty()) {
            view.displayInfo("\n=== Timetable Issues ===");
            for (String issue : issues) {
                view.displayWarning(issue);
            }
        }
    }

    /**
     * retrieves course by course code.
     *
     * @param courseCode    the code of the course
     * @return the course of the corresponding code
     */
    public Course getCourseByCode(String courseCode) {
        return courses.get(courseCode);
    }
}


