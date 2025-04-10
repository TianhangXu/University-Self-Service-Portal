package model;

import java.util.LinkedList;
import java.util.List;

/**
 * Represent a section within the FAQ. A section can contain multiple {@link FAQItem} as well as other nested
 * subsections
 */
public class FAQSection {
    private final String topic;
    private final List<FAQItem> items = new LinkedList<>();
    private FAQSection parent;
    private final List<FAQSection> subsections = new LinkedList<>();

    /**
     * @param topic the topic or title of the section
     */
    public FAQSection(String topic) {
        this.topic = topic;
    }

    /**
     * Adds section as child of the current section.
     *
     * @param section   the section to add
     */
    public void addSubsection(FAQSection section) {
        subsections.add(section);
        section.setParent(this);
    }

    /**
     * @return a list of all child subsections
     */
    public List<FAQSection> getSubsections() {
        return subsections;
    }

    /**
     * @return the topic of the section
     */
    public String getTopic() {
        return topic;
    }

    /**
     * @return list of all FAQItems in the subsection
     */
    public List<FAQItem> getItems() {
        return items;
    }

    /**
     * @return the parent subsection, may return {@code null}
     */
    public FAQSection getParent() {
        return parent;
    }

    /**
     * Sets the given section as the parent of the section.
     *
     * @param parent    the section to set as the parent
     */
    public void setParent(FAQSection parent) {
        this.parent = parent;
    }

    /**
     * Adds a new {@link FAQItem} to the section
     *
     * @param question  the question of the faq item
     * @param answer    the answer of the faq item
     */
    public void addItem(String question, String answer) {
        int newId = items.size();
        items.add(new FAQItem(newId, question, answer));
    }

    /**
     * Adds a new {@link FAQItem} to the section with a given course tag
     *
     * @param question  the question of the faq item
     * @param answer    the answer of the faq item
     * @param courseTag the course tag of the section to add
     */
    public void addItem(String question, String answer, String courseTag) {
        int newId = items.size();
        items.add(new FAQItem(newId, question, answer, courseTag));
    }

    /**
     * Removes {@link FAQItem} with given item id from the section
     *
     * @param itemId    id of the item to remove
     * @return {@code true} if the given id is found and removed from the section's items, otherwise {@code false}
     */
    public boolean removeItem(int itemId) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() == itemId) {
                items.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Finds all {@link FAQItem} in the section with a specified course tag
     *
     * @param courseCode    the course code that you want to find items for
     * @return string representation of all {@link FAQItem} that has the given course code
     */
    public String getItemsByTag(String courseCode) {
        StringBuilder result = new StringBuilder();
        for (FAQItem item : items) {
            if (item.hasTag(courseCode)) {
                result.append("Q: ").append(item.getQuestion())
                        .append("\nA: ").append(item.getAnswer())
                        .append("\n\n");
            }
        }
        return result.toString();
    }

    /**
     * checks whether the topic of the section matches the given value
     * @param topic the topic to check
     * @return {@code true} if the topic matches, otherwise {@code false}
     */
    public boolean hasTopic(String topic) {
        return this.topic.equals(topic);
    }
}
