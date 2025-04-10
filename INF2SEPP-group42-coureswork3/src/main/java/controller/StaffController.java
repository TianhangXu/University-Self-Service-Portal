package controller;

import external.AuthenticationService;
import external.EmailService;
import model.AuthenticatedUser;
import model.Inquiry;
import model.SharedContext;
import view.View;
import java.util.List;

/**
 * Controller for staff. Provides functionality for receiving, viewing and responding to inquiries.
 */
public class StaffController extends Controller {
    /**
     * Instantiates a new staff controller.
     *
     * @param sharedContext the shared context
     * @param view          the user interface view
     * @param auth          the authentication service
     * @param email         the email of the user
     */
    public StaffController(SharedContext sharedContext, View view, AuthenticationService auth, EmailService email) {
        super(sharedContext, view, auth, email);
    }

    /**
     * Extracts and returns subject of each {@link Inquiry} in provided list.
     *
     * @param inquiries the list of inquiries
     * @return          an array of the inquiry titles
     */
    protected String[] getInquiryTitles(List<Inquiry> inquiries) {
        String[] inquiryTitles = new String[inquiries.size()];

        for (int i = 0; i < inquiryTitles.length; ++i) {
            inquiryTitles[i] = inquiries.get(i).getSubject().strip();
        }

        return inquiryTitles;
    }

    /**
     * Displays and manages menu to respond to a given inquiry.
     *
     * @param inquiry   the inquiry to respond to
     */
    protected void respondToInquiry(Inquiry inquiry) {
        String subject = view.getInput("Enter subject: ");
        String response = view.getInput("Enter response:\n");
        String currentEmail = ((AuthenticatedUser) sharedContext.currentUser).getEmail();
        email.sendEmail(currentEmail, inquiry.getInquirerEmail(), subject, response);
        sharedContext.inquiries.remove(inquiry);
        view.displaySuccess("Email response sent!");
    }
}
