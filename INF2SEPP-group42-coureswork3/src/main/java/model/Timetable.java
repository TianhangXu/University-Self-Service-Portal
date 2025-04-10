package model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents students personal timetable. Handles adding of activities.
 */
public class Timetable {
    private final String studentEmail;
    final List<TimeSlot> timeSlots;

    /**
     * @param studentEmail  the email of the student
     */
    public Timetable(String studentEmail) {
        this.studentEmail = studentEmail;
        this.timeSlots = new ArrayList<>();
    }

    /**
     * Checks if timetable contains any chosen slots.
     *
     * @return {@code true} if there are chosen slots, otherwise {@code false}
     */
    public boolean hasChosenTimeSlots() {
        return timeSlots.stream()
                .anyMatch(TimeSlot::isChosen);
    }

    /**
     * Adds a new timeslot to the timetable, checks for conflicts.
     *
     * @param day           the day of the week of the activity
     * @param startDate     the start date of the activty
     * @param startTime     the start time of the activity
     * @param endDate       the end date of the activity
     * @param endTime       the end time of the activity
     * @param courseCode    the code of the course that activity is for
     * @param activityId    the activity id
     * @param status        the initial status of the slot, either CHOSEN or UNCHOSEN
     * @param activityType  the type of the activity
     */
    public void addTimeSlot(DayOfWeek day, LocalDate startDate, LocalTime startTime,
                            LocalDate endDate, LocalTime endTime,
                            String courseCode, int activityId, TimeSlotStatus status, String activityType) {
        // Only check for conflicts if the slot will be CHOSEN and it's not a recorded lecture
        boolean isRecordedLecture = activityType.equals("Recorded Lecture");

        if (status == TimeSlotStatus.CHOSEN && !isRecordedLecture) {
            // Check for conflicts with existing CHOSEN slots, considering the day
            String[] conflicts = checkConflicts(startDate, startTime, endDate, endTime, day);
            if (conflicts.length > 0) {
                throw new IllegalStateException("Time slot conflicts with existing slots: " + String.join(", ", conflicts));
            }
        }

        TimeSlot newSlot = new TimeSlot(day, startDate, startTime, endDate, endTime, courseCode, activityId, status, activityType);
        timeSlots.add(newSlot);
    }

    /**
     * Counts the number of activities for a specific course in the student's timetable.
     *
     * @param courseCode    the course code
     * @return the number of activities for the chosen course
     */
    public int numChosenActivities(String courseCode) {
        return (int) timeSlots.stream()
                .filter(slot -> slot.hasCourseCode(courseCode) && slot.isChosen())
                .count();
    }

    /**
     * Counts the number of activities of a specific activity type for a course.
     *
     * @param courseCode    the code of the course to look at
     * @param activityType  the activity type to count
     * @param courseManager the course manager
     * @return the number of activities that are the specific type and course.
     */
    public int countChosenActivitiesOfType(String courseCode, String activityType, CourseManager courseManager) {
        if (courseManager == null || !courseManager.hasCourse(courseCode)) {
            return 0; // No course manager or course doesn't exist
        }

        Course course = courseManager.getCourseByCode(courseCode);
        return (int) timeSlots.stream()
                .filter(slot -> slot.hasCourseCode(courseCode) && slot.isChosen()) // Filter chosen slots for this course
                .filter(slot -> {
                    Activity activity = course.getActivityById(slot.activityId); // Look up activity by ID
                    if (activity == null) return false;
                    // Match the activity type
                    return (activity instanceof Lecture && "Lecture".equals(activityType)) ||
                            (activity instanceof Tutorial && "Tutorial".equals(activityType)) ||
                            (activity instanceof Lab && "Lab".equals(activityType));
                })
                .count();
    }

    /**
     * Finds any conflicting TimeSlots with the given date range
     *
     * @param startDate The start date
     * @param startTime The start time
     * @param endDate   the end date
     * @param endTime   the end time
     * @return an array of the string representations of each conflicting activity
     */
    public String[] checkConflicts(LocalDate startDate, LocalTime startTime, LocalDate endDate, LocalTime endTime, DayOfWeek day) {
        // For same-day time comparisons, we can simply use the time component
        return timeSlots.stream()
                .filter(TimeSlot::isChosen)
                .filter(slot -> slot.getDay() == day) // Only check slots on the same day
                .filter(slot -> {
                    // Check if times overlap (start before other ends AND end after other starts)
                    return startTime.isBefore(slot.getEndTime()) && slot.getStartTime().isBefore(endTime);
                })
                .map(TimeSlot::toString)
                .toArray(String[]::new);
    }

    /**
     * Checks if timetable belongs to the student with the give email
     *
     * @param email the student's email.
     * @return {@code true} if the email matches, otherwise {@code false}
     */
    public boolean hasStudentEmail(String email) {
        return studentEmail.equals(email);
    }

    /**
     * Chooses an activity for a specific timeslot, will cancel if the activity conflicts with other activities already
     * in the timetable
     *
     * @param courseCode    the code of the course the activity is from
     * @param activityId    the activity id
     * @return {@code true} if the activity was succesfully added, otherwise {@false}
     */
    public boolean chooseActivity(String courseCode, int activityId) {
        TimeSlot targetSlot = null;
        // Find the slot to be chosen
        for (TimeSlot slot : timeSlots) {
            if (slot.hasCourseCode(courseCode) && slot.hasActivityId(activityId)) {
                targetSlot = slot;
                break;
            }
        }

        if (targetSlot == null) {
            return false;
        }

        // Check for conflicts with ALL chosen slots, not just unrecorded lectures
        for (TimeSlot slot : timeSlots) {
            // Skip if it's not a chosen slot or not on the same day
            if (!slot.isChosen() || slot.getDay() != targetSlot.getDay()) {
                continue;
            }

            // Check time overlap
            if (targetSlot.getStartTime().isBefore(slot.getEndTime()) &&
                    slot.getStartTime().isBefore(targetSlot.getEndTime())) {
                // Allow overlap only if the existing slot is a recorded lecture
                if (!slot.getActivityType().equals("Recorded Lecture")) {
                    return false;  // Conflict found with a non-recorded lecture activity
                }
            }
        }

        // Set this slot as chosen
        targetSlot.setStatus(TimeSlotStatus.CHOSEN);
        return true;
    }

    /**
     * Checks if timetable has any timeslots for a given course
     *
     * @param courseCode    the course code
     * @return {@code true} if there is any slot associated with the course, otherwise {@false}
     */
    public boolean hasSlotsForCourse(String courseCode) {
        return timeSlots.stream().anyMatch(slot -> slot.hasCourseCode(courseCode));
    }

    /**
     * Removes all time slots for a given course
     *
     * @param courseCode    the course to remove slots from
     */
    public void removeSlotsForCourse(String courseCode) {
        timeSlots.removeIf(slot -> slot.hasCourseCode(courseCode));
    }

    /**
     * @return  the email of the student
     */
    public String getStudentEmail() {
        return studentEmail;
    }

    /**
     * Returns a detailed string representation of the timetable including both chosen and unchosen activities.
     *
     * @return A formatted string representing the timetable
     */
    public String toStringWithAllActivities() {
        StringBuilder sb = new StringBuilder();
        sb.append("Timetable for ").append(studentEmail).append("\n");

        // Get current date
        LocalDate today = LocalDate.now();

        // Find the next Monday if today is not already Monday
        LocalDate startOfWeek = today;
        if (today.getDayOfWeek() != DayOfWeek.MONDAY) {
            // Calculate days until next Monday
            int daysUntilMonday = DayOfWeek.MONDAY.getValue() - today.getDayOfWeek().getValue();
            if (daysUntilMonday <= 0) {
                daysUntilMonday += 7; // Wrap to next week if today is Monday or later in the week
            }
            startOfWeek = today.plusDays(daysUntilMonday);
        }

        // End of week is Friday
        LocalDate endOfWeek = startOfWeek.plusDays(4); // Monday + 4 days = Friday

        sb.append("Week from ").append(startOfWeek).append(" to ").append(endOfWeek).append("\n");

        // Group by day of week, but only include Monday through Friday
        for (DayOfWeek day : new DayOfWeek[]{
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY}) {

            LocalDate currentDayDate = startOfWeek.plusDays(day.getValue() - DayOfWeek.MONDAY.getValue());

            // Filter slots for this day, including both chosen and unchosen
            List<TimeSlot> slotsForDay = timeSlots.stream()
                    .filter(slot -> slot.getDay() == day)
                    .filter(slot -> {
                        // Only include activities that still take place (end date >= current day's date)
                        return !slot.getEndDate().isBefore(currentDayDate);
                    })
                    .collect(Collectors.toList());

            if (!slotsForDay.isEmpty()) {
                sb.append(day).append(" (").append(currentDayDate).append("):\n");

                // Sort by start time
                slotsForDay.sort((a, b) -> a.getStartTime().compareTo(b.getStartTime()));

                // Group by course code for better readability
                Map<String, List<TimeSlot>> courseSlots = slotsForDay.stream()
                        .collect(Collectors.groupingBy(slot -> slot.courseCode));

                for (String courseCode : courseSlots.keySet()) {
                    sb.append("  Course: ").append(courseCode).append("\n");

                    for (TimeSlot slot : courseSlots.get(courseCode)) {
                        sb.append("    ")
                                .append(slot.getActivityType())
                                .append(" (ID: ").append(slot.activityId).append(") - ")
                                .append(slot.getStartTime()).append("-").append(slot.getEndTime())
                                .append(" [").append(slot.status).append("]")
                                .append("\n");
                    }
                }
            } else {
                sb.append(day).append(" (").append(currentDayDate).append("): No scheduled activities\n");
            }
        }

        return sb.toString();
    }

    /**
     * Returns a string representation of the timetable showing only the working week ahead
     * (Monday to Friday) with activities that still take place.
     *
     * @return A formatted string representing the timetable for the week ahead
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Timetable for ").append(studentEmail).append("\n");

        // Get current date
        LocalDate today = LocalDate.now();

        // Find the next Monday if today is not already Monday
        LocalDate startOfWeek = today;
        if (today.getDayOfWeek() != DayOfWeek.MONDAY) {
            // Calculate days until next Monday
            int daysUntilMonday = DayOfWeek.MONDAY.getValue() - today.getDayOfWeek().getValue();
            if (daysUntilMonday <= 0) {
                daysUntilMonday += 7; // Wrap to next week if today is Monday or later in the week
            }
            startOfWeek = today.plusDays(daysUntilMonday);
        }

        // End of week is Friday
        LocalDate endOfWeek = startOfWeek.plusDays(4); // Monday + 4 days = Friday

        sb.append("Week from ").append(startOfWeek).append(" to ").append(endOfWeek).append("\n");

        // Group by day of week, but only include Monday through Friday
        for (DayOfWeek day : new DayOfWeek[]{
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY}) {

            LocalDate currentDayDate = startOfWeek.plusDays(day.getValue() - DayOfWeek.MONDAY.getValue());

            // Filter slots for this day that are chosen and take place during the week ahead
            List<TimeSlot> slotsForDay = timeSlots.stream()
                    .filter(slot -> slot.getDay() == day && slot.isChosen())
                    .filter(slot -> {
                        // Only include activities that still take place (end date >= current day's date)
                        return !slot.getEndDate().isBefore(currentDayDate);
                    })
                    .collect(Collectors.toList());

            if (!slotsForDay.isEmpty()) {
                sb.append(day).append(" (").append(currentDayDate).append("):\n");

                // Sort by start time
                slotsForDay.sort((a, b) -> a.getStartTime().compareTo(b.getStartTime()));

                for (TimeSlot slot : slotsForDay) {
                    sb.append("  ").append(slot.toString()).append("\n");
                }
            } else {
                sb.append(day).append(" (").append(currentDayDate).append("): No scheduled activities\n");
            }
        }

        return sb.toString();
    }

    /**
     * Check if the timetable has errors or warnings based on requirements.
     *
     * @param courseManager The course manager to get course information
     * @return A list of error and warning messages
     */
    public List<String> checkTimetableIssues(CourseManager courseManager) {
        List<String> issues = new ArrayList<>();

        // Group slots by course code
        Map<String, List<TimeSlot>> courseSlots = timeSlots.stream()
                .filter(TimeSlot::isChosen)
                .collect(Collectors.groupingBy(slot -> slot.courseCode));

        for (Map.Entry<String, List<TimeSlot>> entry : courseSlots.entrySet()) {
            String courseCode = entry.getKey();
            Course course = courseManager.getCourseByCode(courseCode);

            if (course == null) continue;

            // Check required tutorials
            int requiredTutorials = course.getRequiredTutorials();
            int chosenTutorials = countChosenActivitiesOfType(courseCode, "Tutorial", courseManager);
            if (chosenTutorials < requiredTutorials) {
                issues.add("WARNING: Course " + courseCode + " requires " + requiredTutorials +
                        " tutorials, but only " + chosenTutorials + " chosen.");
            }

            // Check required labs
            int requiredLabs = course.getRequiredLabs();
            int chosenLabs = countChosenActivitiesOfType(courseCode, "Lab", courseManager);
            if (chosenLabs < requiredLabs) {
                issues.add("WARNING: Course " + courseCode + " requires " + requiredLabs +
                        " labs, but only " + chosenLabs + " chosen.");
            }
        }

        return issues;
    }

    /**
     * Get all time slots for unrecorded lectures in the timetable
     * @param courseManager The course manager to lookup activity details
     * @return List of time slots for unrecorded lectures
     */
    public List<TimeSlot> getUnrecordedLectureSlots(CourseManager courseManager) {
        List<TimeSlot> unrecordedLectures = new ArrayList<>();

        for (TimeSlot slot : timeSlots) {
            if (slot.isChosen() &&
                    (slot.getActivityType().equals("Lecture") || slot.getActivityType().equals("Unrecorded Lecture"))) {

                // Get the course and activity to check if it's an unrecorded lecture
                Course course = courseManager.getCourseByCode(slot.courseCode);
                if (course != null) {
                    Activity activity = course.getActivityById(slot.activityId);
                    if (activity instanceof Lecture && !((Lecture) activity).isRecorded()) {
                        unrecordedLectures.add(slot);
                    }
                }
            }
        }

        return unrecordedLectures;
    }
}

/**
 * Enum representing the possible statuses of a time slot.
 */
enum TimeSlotStatus {
    UNCHOSEN,
    CHOSEN
}

/**
 * Represents the time slot of a single activity within the timetable.
 */
class TimeSlot {
    private final DayOfWeek day;
    private final LocalDate startDate;
    private final LocalTime startTime;
    private final LocalDate endDate;
    private final LocalTime endTime;
    public final String courseCode;
    public final int activityId;
    public TimeSlotStatus status;
    private final String activityType;

    /**
     * @param day           the day of the week of the activity
     * @param startDate     the start date of the activty
     * @param startTime     the start time of the activity
     * @param endDate       the end date of the activity
     * @param endTime       the end time of the activity
     * @param courseCode    the code of the course that activity is for
     * @param activityId    the activity id
     * @param status        the initial status of the slot, either CHOSEN or UNCHOSEN
     * @param activityType  the type of the activity
     */
    public TimeSlot(DayOfWeek day, LocalDate startDate, LocalTime startTime,
                    LocalDate endDate, LocalTime endTime,
                    String courseCode, int activityId, TimeSlotStatus status, String activityType) {
        this.day = day;
        this.startDate = startDate;
        this.startTime = startTime;
        this.endDate = endDate;
        this.endTime = endTime;
        this.courseCode = courseCode;
        this.activityId = activityId;
        this.status = status;
        this.activityType = activityType;
    }

    /**
     * Checks whether the course code of the activity matches given course code
     *
     * @param courseCode    the course code to check against
     * @return {@code true} if the course codes match, otherwise {@code false}
     */
    public boolean hasCourseCode(String courseCode) {
        return this.courseCode.equals(courseCode);
    }

    /**
     * Checks if the timeslot is for the activity with given id
     *
     * @param id    the id to check against
     * @return {@code true} if the id matches, otherwise {@code false}
     */
    public boolean hasActivityId(int id) {
        return this.activityId == id;
    }

    /**
     * @return whether the timeslot is currently CHOSEN
     */
    public boolean isChosen() {
        return status == TimeSlotStatus.CHOSEN;
    }

    /**
     * Sets the status of the time slot to a given status
     * @param status    the status to set the time slot to
     */
    public void setStatus(TimeSlotStatus status) {
        this.status = status;
    }

    /**
     * @return the day of the week the time slot is for
     */
    public DayOfWeek getDay() {
        return day;
    }

    /**
     * @return the start date of the time slot
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * @return the start time of the time slot
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * @return the end date of the time slot
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * @return the end time of the time slot
     */
    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * @return the activity type of the time slot
     */
    public String getActivityType() {
        return activityType;
    }

    /**
     * @return a string representation of the timeslot
     */
    @Override
    public String toString() {
        return courseCode + " - " + activityType + " - " + startTime + "-" + endTime + " (Activity ID: " + activityId + ")";
    }
}