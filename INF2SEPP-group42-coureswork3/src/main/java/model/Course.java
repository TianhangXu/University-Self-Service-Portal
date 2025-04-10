package model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a university course, includes course information and scheduled activities that happen under the course
 */
public class Course {
    private final String courseCode;
    private final String name;
    private final String description;
    private final boolean requiresComputers;
    private final String courseOrganiserName;
    private final String courseOrganiserEmail;
    private final String courseSecretaryName;
    private final String courseSecretaryEmail;
    private final int requiredTutorials;
    private final int requiredLabs;
    private final List<Activity> activities;

    /**
     * @param code              the course code
     * @param name              the name of the course
     * @param description       the course description
     * @param requiresComputers whether the course requires a computer
     * @param COName            name of the course organiser
     * @param COEmail           email of the course organiser
     * @param CSName            name of the course secretary
     * @param CSEmail           email of the course secretary
     * @param reqTutorials      number of required tutorials
     * @param reqLabs           number of required labs
     */
    public Course(String code, String name, String description, boolean requiresComputers,
                  String COName, String COEmail, String CSName, String CSEmail,
                  int reqTutorials, int reqLabs) {
        this.courseCode = code;
        this.name = name;
        this.description = description;
        this.requiresComputers = requiresComputers;
        this.courseOrganiserName = COName;
        this.courseOrganiserEmail = COEmail;
        this.courseSecretaryName = CSName;
        this.courseSecretaryEmail = CSEmail;
        this.requiredTutorials = reqTutorials;
        this.requiredLabs = reqLabs;
        this.activities = new ArrayList<>();
    }

    /**
     * Add a new available activity to the course
     *
     * @param startDate         the date the activty starts
     * @param startTime         the time the activity starts
     * @param endDate           the date the activity ends
     * @param endTime           the time the activty ends
     * @param location          the location of the activity
     * @param day               the day of the week the activity is held
     * @param activityType      the type of the activity
     * @param additionalInfo    additional information specific for the course
     * @param id                the id for the activity
     */
    public void addActivity(LocalDate startDate, LocalTime startTime, LocalDate endDate,
                            LocalTime endTime, String location, DayOfWeek day,
                            String activityType, Object additionalInfo, int id) {
        Activity activity = null;

        switch (activityType) {
            case "Lecture":
                boolean recorded = (Boolean) additionalInfo;
                activity = new Lecture(id, startDate, startTime, endDate, endTime, location, day, recorded);
                break;
            case "Tutorial":
                int tutorialCapacity = (Integer) additionalInfo;
                activity = new Tutorial(id, startDate, startTime, endDate, endTime, location, day, tutorialCapacity);
                break;
            case "Lab":
                int labCapacity = (Integer) additionalInfo;
                activity = new Lab(id, startDate, startTime, endDate, endTime, location, day, labCapacity);
                break;
        }

        if (activity != null) {
            activities.add(activity);
        }
    }

    /**
     * remove all activities associated with the course.
     */
    public void removeActivities() {
        activities.clear();
    }

    /**
     * Checks if the course has a specific code.
     *
     * @param code the code to check against
     * @return {@code true} if the code matches the course code, otherwise {@code false}
     */
    public boolean hasCode(String code) {
        return courseCode.equals(code);
    }

    /**
     * Checks if any activity in the course has a specific id.
     * @param id    the id to check against
     * @return      {@code true} if any activity in the course activities has the id, otherwise {@code false}
     */
    public boolean hasActivityWithId(int id) {
        return activities.stream().anyMatch(activity -> activity.hasId(id));
    }

    /**
     * @return string representation of all activities in the course
     */
    public String getActivitiesAsString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < activities.size(); i++) {
            sb.append(i).append(": ").append(activities.get(i).toString()).append("\n");
        }
        return sb.toString();
    }

    /**
     * @param id    the id of the activity to get
     * @return activity with the matching id
     */
    public Activity getActivityById(int id) {
        for (Activity activity : activities) {
            if (activity.hasId(id)) {
                return activity;
            }
        }
        return null;
    }

    /**
     * checks if the given activity has an unrecorded lectures
     *
     * @param activityId    id of the activity to check
     * @return {@code true} if the activity is an unrecorded lecture, otherwise {@code false}
     */
    public boolean isUnrecordedLecture(int activityId) {
        return activities.stream()
                .filter(activity -> activity.hasId(activityId))
                .filter(activity -> activity instanceof Lecture)
                .map(activity -> (Lecture) activity)
                .anyMatch(lecture -> !lecture.isRecorded());
    }

    /**
     * @return a list of all activities in the course
     */
    public List<Activity> getActivities() {
        return new ArrayList<>(activities);
    }

    /**
     * @return the code of the course
     */
    public String getCourseCode() {
        return courseCode;
    }

    /**
     * @return the name of the course
     */
    public String getName() {
        return name;
    }

    /**
     * @return the description of the course
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return {@code true} if the course requires computers, otherwise {@code false}
     */
    public boolean isRequiresComputers() {
        return requiresComputers;
    }

    /**
     * @return the organiser name of the course
     */
    public String getCourseOrganiserName() {
        return courseOrganiserName;
    }

    /**
     * @return the organiser email of the course
     */
    public String getCourseOrganiserEmail() {
        return courseOrganiserEmail;
    }

    /**
     * @return the secretary name of the course
     */
    public String getCourseSecretaryName() {
        return courseSecretaryName;
    }

    /**
     * @return the secretary email of the course
     */
    public String getCourseSecretaryEmail() {
        return courseSecretaryEmail;
    }

    /**
     * @return the number of required tutorials in the course
     */
    public int getRequiredTutorials() {
        return requiredTutorials;
    }

    /**
     * @return the number of required labs for the course
     */
    public int getRequiredLabs() {
        return requiredLabs;
    }

    /**
     * @return a string representation of the course
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Course: ").append(courseCode).append(" - ").append(name).append("\n");
        sb.append("Description: ").append(description).append("\n");
        sb.append("Requires Computers: ").append(requiresComputers ? "Yes" : "No").append("\n");
        sb.append("Course Organiser: ").append(courseOrganiserName).append(" (").append(courseOrganiserEmail).append(")\n");
        sb.append("Course Secretary: ").append(courseSecretaryName).append(" (").append(courseSecretaryEmail).append(")\n");
        sb.append("Required Tutorials: ").append(requiredTutorials).append("\n");
        sb.append("Required Labs: ").append(requiredLabs).append("\n");
        sb.append("Activities:\n").append(getActivitiesAsString());
        return sb.toString();
    }


}