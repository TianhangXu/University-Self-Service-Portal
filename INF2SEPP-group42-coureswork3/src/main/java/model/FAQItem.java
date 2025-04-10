package model;

/**
 * Represents a single FAQ item contained with an {@link FAQSection}. Contains a question and answer, pair as well as
 * an optional course tag.
 */
public class FAQItem {
    private final int id;
    private final String question;
    private final String answer;
    private final String courseTag;

    /**
     * @param id        the id of the FAQ item
     * @param question  the question string
     * @param answer    the answer string
     */
    public FAQItem(int id, String question, String answer) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.courseTag = null;
    }

    /**
     * @param id        the id of the FAQ item
     * @param question  the question string
     * @param answer    the answer string
     * @param courseTag the tag of the course the FAQ is related to
     */
    public FAQItem(int id, String question, String answer, String courseTag) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.courseTag = courseTag;
    }

    /**
     * @return the id of the FAQ item.
     */
    public int getId() {
        return id;
    }

    /**
     * @return the question of the FAQ item.
     */
    public String getQuestion() {
        return question;
    }

    /**
     * @return the answer of the FAQ item.
     */
    public String getAnswer() {
        return answer;
    }

    /**
     * @return the course tag of the FAQ item.
     */
    public String getCourseTag() {
        return courseTag;
    }

    /**
     * Checks whether the FAQ has a specific course tag.
     *
     * @param courseTag the course tag to check
     * @return {@code true} if the course tag matches, otherwise {@code false}
     */
    public boolean hasTag(String courseTag) {
        if (this.courseTag == null) {
            return false;
        }
        return this.courseTag.equals(courseTag);
    }
}
