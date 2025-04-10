package model;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents the FAQ
 */
public class FAQ {
    private final List<FAQSection> sections = new LinkedList<>();

    /**
     * adds a new section to the FAQ
     * @param section   the section to add
     */
    public void addSection(FAQSection section) {
        sections.add(section);
        section.setParent(null);
    }

    /**
     * @return a list of sections in the FAQ
     */
    public List<FAQSection> getSections() {
        return sections;
    }
}
