package model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * abstract class representing a course activity with time frame and location.
 */
public abstract class Activity {
    private final int id;
    private final LocalDate startDate;
    private final LocalTime startTime;
    private final LocalDate endDate;
    private final LocalTime endTime;
    private final String location;
    private final DayOfWeek day;

    /**
     * Instantiates a new activity.
     *
     * @param id        unique identifier of activity
     * @param startDate the start date of the activity
     * @param startTime the start time of the activity
     * @param endDate   the end date of the activity
     * @param endTime   the end time of the activity
     * @param location  the location where the activity takes place
     * @param day       the day of the week the activity takes place
     */
    public Activity(int id, LocalDate startDate, LocalTime startTime, LocalDate endDate, LocalTime endTime,
                    String location, DayOfWeek day) {
        this.id = id;
        this.startDate = startDate;
        this.startTime = startTime;
        this.endDate = endDate;
        this.endTime = endTime;
        this.location = location;
        this.day = day;
    }

    /**
     * Checks if the activity has a specific id.
     *
     * @param   id the id to check against
     * @return {@code true} if the id matches the activities id, otherwise {@code false}
     */
    public boolean hasId(int id) {
        return this.id == id;
    }

    /**
     * @return the id of the activity
     */
    public int getId() {
        return id;
    }

    /**
     * @return the start date of the activity
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * @return the start time of the activity
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * @return the end date of the activity
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * @return the end time of the activity
     */
    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * @return the location of the activity
     */
    public String getLocation() {
        return location;
    }

    /**
     * @return the day of the week the activity is held
     */
    public DayOfWeek getDay() {
        return day;
    }

    /**
     * @return a string representation of the activity
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " +
                day + " " + startTime + "-" + endTime +
                " (" + startDate + " to " + endDate + ")" +
                " at " + location + " (ID: " + id + ")";
    }
}

/**
 * Represents a lecture activity.
 */
class Lecture extends Activity {
    private final boolean recorded;

    /**
     * @param id        unique identifier of activity
     * @param startDate the start date of the activity
     * @param startTime the start time of the activity
     * @param endDate   the end date of the activity
     * @param endTime   the end time of the activity
     * @param location  the location where the activity takes place
     * @param day       the day of the week the activity takes place
     * @param recorded  whether the lecture gets recorded or not
     */
    public Lecture(int id, LocalDate startDate, LocalTime startTime, LocalDate endDate, LocalTime endTime,
                   String location, DayOfWeek day, boolean recorded) {
        super(id, startDate, startTime, endDate, endTime, location, day);
        this.recorded = recorded;
    }

    /**
     * @return {@code true} if the lecture is recorded, otherwise {@code false}
     */
    public boolean isRecorded() {
        return recorded;
    }

    /**
     * @return a string representation of the lecture
     */
    @Override
    public String toString() {
        return super.toString() + " - " + (recorded ? "Recorded" : "Not Recorded");
    }
}

/**
 * represents a tutorial activity.
 */
class Tutorial extends Activity {
    private final int capacity;

    /**
     * @param id        unique identifier of activity
     * @param startDate the start date of the activity
     * @param startTime the start time of the activity
     * @param endDate   the end date of the activity
     * @param endTime   the end time of the activity
     * @param location  the location where the activity takes place
     * @param day       the day of the week the activity takes place
     * @param capacity  the number of students that can attend the tutorial
     */
    public Tutorial(int id, LocalDate startDate, LocalTime startTime, LocalDate endDate, LocalTime endTime,
                    String location, DayOfWeek day, int capacity) {
        super(id, startDate, startTime, endDate, endTime, location, day);
        this.capacity = capacity;
    }

    /**
     * @return a string representation of the tutorial
     */
    @Override
    public String toString() {
        return super.toString() + " - Capacity: " + capacity;
    }
}

/**
 * represents a lab activity.
 */
class Lab extends Activity {
    private final int capacity;
    /**
     * @param id        unique identifier of activity
     * @param startDate the start date of the activity
     * @param startTime the start time of the activity
     * @param endDate   the end date of the activity
     * @param endTime   the end time of the activity
     * @param location  the location where the activity takes place
     * @param day       the day of the week the activity takes place
     * @param capacity  the number of students that can attend the lab
     */
    public Lab(int id, LocalDate startDate, LocalTime startTime, LocalDate endDate, LocalTime endTime,
               String location, DayOfWeek day, int capacity) {
        super(id, startDate, startTime, endDate, endTime, location, day);
        this.capacity = capacity;
    }

    /**
     * @return a string representation of the lab
     */
    @Override
    public String toString() {
        return super.toString() + " - Capacity: " + capacity;
    }
}