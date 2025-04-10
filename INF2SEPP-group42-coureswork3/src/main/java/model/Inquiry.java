package model;

import java.time.LocalDateTime;

/**
 * Represents inquiries that get submitted by users
 */
public class Inquiry {
    private final LocalDateTime createdAt;
    private final String inquirerEmail;
    private final String subject;
    private final String content;
    private final String courseCode; // Added courseCode field
    private String assignedTo;

    /**
     * @param inquirerEmail email of the user who submitted inquiry
     * @param subject       subject of the inquiry
     * @param content       content of the inquiry
     */
    public Inquiry(String inquirerEmail, String subject, String content) {
        this.createdAt = LocalDateTime.now();
        this.inquirerEmail = inquirerEmail;
        this.subject = subject;
        this.content = content;
        this.courseCode = null; // No course code for general inquiries
    }

    /**
     * @param inquirerEmail email of the user who submitted inquiry
     * @param subject       subject of the inquiry
     * @param content       content of the inquiry
     * @param courseCode    code of the course the inquiry relates to
     */
    public Inquiry(String inquirerEmail, String subject, String content, String courseCode) {
        this.createdAt = LocalDateTime.now();
        this.inquirerEmail = inquirerEmail;
        this.subject = subject;
        this.content = content;
        this.courseCode = courseCode;
    }

    /**
     * @return  the dateTime the inquiry was created at
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * @return the email of the user who submitted the inquiry
     */
    public String getInquirerEmail() {
        return inquirerEmail;
    }

    /**
     * @return the subject of the inquiry
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @return the content of the inquiry
     */
    public String getContent() {
        return content;
    }

    /**
     * @return the course code of the inquiry
     */
    public String getCourseCode() {
        return courseCode;
    }

    /**
     * checks whether the inquiry has a course code or not
     *
     * @return {@code true} if the inquiry has a course code, otherwise {@code false}
     */
    public boolean hasCourseCode() {
        return courseCode != null && !courseCode.trim().isEmpty();
    }

    /**
     * @return the email of the user who was assigned the inquiry
     */
    public String getAssignedTo() {
        return assignedTo;
    }

    /**
     * assigns the inquiry to a user.
     *
     * @param assignedTo    email of the user who is being assigned the inquiry
     */
    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }
}
